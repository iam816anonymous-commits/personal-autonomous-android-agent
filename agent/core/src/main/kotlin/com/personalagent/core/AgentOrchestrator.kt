package com.personalagent.core

import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException

fun interface GoalPlanner {
    suspend fun nextAction(goal: String, snapshot: DeviceSnapshot, completedActions: List<ActionId>): AgentAction?
}

interface DeviceObserver {
    suspend fun observe(): DeviceSnapshot
}

interface AgentActionDispatcher {
    suspend fun dispatch(action: AgentAction): ActionExecutionResult
}

interface ActionPostconditionVerifier {
    suspend fun verify(action: AgentAction, before: DeviceSnapshot, after: DeviceSnapshot): VerificationResult
}

interface TaskStepJournal {
    suspend fun append(record: TaskStepRecord)
}

fun interface ApprovalProvider {
    suspend fun request(action: AgentAction, snapshot: DeviceSnapshot): ApprovalToken?
}

data class OrchestratorLimits(
    val maxActions: Int = 8,
    val maxElapsedMillis: Long = 60_000,
) {
    init {
        require(maxActions in 1..8)
        require(maxElapsedMillis in 1_000L..180_000L)
    }
}

data class TaskOutcome(
    val taskId: TaskId,
    val state: TaskState,
    val verifiedActions: Int,
    val reason: String,
)

class AgentOrchestrator(
    private val planner: GoalPlanner,
    private val observer: DeviceObserver,
    private val dispatcher: AgentActionDispatcher,
    private val verifier: ActionPostconditionVerifier,
    private val registry: ToolRegistry,
    private val policy: ApprovalPolicy,
    private val approvalProvider: ApprovalProvider,
    private val journal: TaskStepJournal,
    private val durableJournal: DurableExecutionJournal,
    private val taskSessions: TaskSessionJournal,
    private val epochClock: EpochClock,
    private val clock: MonotonicClock,
    private val limits: OrchestratorLimits = OrchestratorLimits(),
) {
    suspend fun run(
        taskId: TaskId,
        goal: String,
        capabilityStatuses: Map<CapabilityId, CapabilityStatus>,
    ): TaskOutcome {
        require(goal.isNotBlank() && goal.length <= 2_000)
        val started = clock.nowMillis()
        val completed = mutableListOf<ActionId>()
        var verifiedCount = 0
        val createdAt = epochClock.nowEpochMillis()
        val created = taskSessions.create(TaskSessionRecord(
            taskId = taskId,
            goal = goal,
            inputModality = TaskInputModality.TEXT,
            state = TaskSessionState.CREATED,
            createdAtEpochMillis = createdAt,
            updatedAtEpochMillis = createdAt,
        ))
        if (!created) return TaskOutcome(taskId, TaskState.FAILED, 0, "Task session could not be created; execution was blocked")
        if (taskSessions.transition(taskId, TaskSessionState.RUNNING, epochClock.nowEpochMillis()) !is TaskSessionDecision.Accept) {
            return TaskOutcome(taskId, TaskState.FAILED, 0, "Task session could not enter RUNNING; execution was blocked")
        }

        val outcome = try {
            withTimeout(limits.maxElapsedMillis) {
                var snapshot = observer.observe().redacted()
                repeat(limits.maxActions) {
                    val action = planner.nextAction(goal, snapshot, completed)
                        ?: return@withTimeout TaskOutcome(taskId, TaskState.COMPLETED, verifiedCount, "Planner reports no remaining action")

                    journal.append(record(taskId, action, TaskState.VALIDATING, "Validating typed action", clock.nowMillis()))
                    val descriptor = runCatching { registry.requireAvailable(action.capability, capabilityStatuses) }
                        .getOrElse { return@withTimeout TaskOutcome(taskId, TaskState.FAILED, verifiedCount, it.message ?: "Capability unavailable") }
                    if (action.kindId() !in descriptor.supportedActions) {
                        return@withTimeout TaskOutcome(taskId, TaskState.FAILED, verifiedCount, "Action is not registered for this capability")
                    }
                    if (action.risk.severity > descriptor.riskCeiling.severity) {
                        return@withTimeout TaskOutcome(taskId, TaskState.FAILED, verifiedCount, "Action exceeds the registered tool risk ceiling")
                    }

                    val initialDecision = policy.evaluate(action, PolicyContext(capabilityStatuses, snapshot = snapshot))
                    val approval = when (initialDecision) {
                        PolicyDecision.Allowed -> null
                        is PolicyDecision.Denied -> return@withTimeout TaskOutcome(taskId, TaskState.FAILED, verifiedCount, initialDecision.reason)
                        is PolicyDecision.RequiresApproval -> {
                            val waiting = taskSessions.transition(taskId, TaskSessionState.AWAITING_APPROVAL, epochClock.nowEpochMillis())
                            if (waiting !is TaskSessionDecision.Accept) {
                                return@withTimeout TaskOutcome(taskId, TaskState.FAILED, verifiedCount, "Could not persist approval boundary")
                            }
                            val token = approvalProvider.request(action, snapshot)
                                ?: return@withTimeout TaskOutcome(taskId, TaskState.CANCELLED, verifiedCount, "User declined or did not provide approval")
                            val resumed = taskSessions.transition(taskId, TaskSessionState.RUNNING, epochClock.nowEpochMillis())
                            if (resumed !is TaskSessionDecision.Accept) {
                                return@withTimeout TaskOutcome(taskId, TaskState.FAILED, verifiedCount, "Could not persist approval result")
                            }
                            token
                        }
                    }
                    val finalDecision = policy.evaluate(action, PolicyContext(capabilityStatuses, approval, snapshot))
                    if (finalDecision !is PolicyDecision.Allowed) {
                        val reason = when (finalDecision) {
                            is PolicyDecision.Denied -> finalDecision.reason
                            is PolicyDecision.RequiresApproval -> finalDecision.reason
                            PolicyDecision.Allowed -> ""
                        }
                        return@withTimeout TaskOutcome(taskId, TaskState.FAILED, verifiedCount, reason)
                    }

                    // Write-ahead intent must be durable before the Android adapter is called.
                    val intent = DurableActionRecord(
                        taskId = taskId,
                        actionId = action.id,
                        actionKind = action.kindId().value,
                        targetPackage = action.targetPackage,
                        snapshotId = when (action) {
                            is ClickAccessibleNode -> action.snapshotId
                            is SetAccessibleText -> action.snapshotId
                            else -> snapshot.id
                        },
                        idempotency = action.idempotency,
                        dispatchState = DispatchState.PREPARED,
                        attempt = 1,
                        createdAtElapsedMillis = clock.nowMillis(),
                        updatedAtElapsedMillis = clock.nowMillis(),
                    )
                    if (!durableJournal.insertPrepared(intent)) {
                        return@withTimeout TaskOutcome(taskId, TaskState.FAILED, verifiedCount, "Could not persist action intent; dispatch was blocked")
                    }
                    val dispatching = durableJournal.transition(taskId, action.id, DispatchState.DISPATCHING, clock.nowMillis())
                    if (dispatching !is JournalDecision.Accept) {
                        return@withTimeout TaskOutcome(taskId, TaskState.FAILED, verifiedCount, "Could not persist dispatch boundary; dispatch was blocked")
                    }

                    journal.append(record(taskId, action, TaskState.EXECUTING, "Dispatch started after durable intent commit", clock.nowMillis()))
                    val dispatch = try {
                        withTimeout(action.limits.timeoutMillis) { dispatcher.dispatch(action) }
                    } catch (timeout: TimeoutCancellationException) {
                        durableJournal.transition(taskId, action.id, DispatchState.OUTCOME_UNKNOWN, clock.nowMillis(),
                            "Dispatch timed out; side effect may have occurred")
                        observer.observe().redacted()
                        journal.append(record(taskId, action, TaskState.RECOVERY_REQUIRED, "Dispatch outcome uncertain after timeout", clock.nowMillis()))
                        return@withTimeout TaskOutcome(taskId, TaskState.RECOVERY_REQUIRED, verifiedCount, "Dispatch timed out; device state must be reconciled")
                    }

                    when (dispatch) {
                        is ActionExecutionResult.Rejected -> {
                            durableJournal.transition(taskId, action.id, DispatchState.REJECTED, clock.nowMillis(), dispatch.reason)
                            return@withTimeout TaskOutcome(taskId, TaskState.FAILED, verifiedCount, dispatch.reason)
                        }
                        is ActionExecutionResult.Failed -> {
                            val next = if (dispatch.outcomeMayHaveOccurred) DispatchState.OUTCOME_UNKNOWN else DispatchState.FAILED
                            durableJournal.transition(taskId, action.id, next, clock.nowMillis(), dispatch.reason)
                            if (dispatch.outcomeMayHaveOccurred) {
                                observer.observe().redacted()
                                journal.append(record(taskId, action, TaskState.RECOVERY_REQUIRED, dispatch.reason, clock.nowMillis()))
                                return@withTimeout TaskOutcome(taskId, TaskState.RECOVERY_REQUIRED, verifiedCount, dispatch.reason)
                            }
                            return@withTimeout TaskOutcome(taskId, TaskState.FAILED, verifiedCount, dispatch.reason)
                        }
                        is ActionExecutionResult.Dispatched -> {
                            val receipt = dispatch.evidenceReference
                            val persisted = durableJournal.transition(
                                taskId, action.id, DispatchState.DISPATCHED, clock.nowMillis(),
                                receipt ?: if (action.idempotency == Idempotency.NON_IDEMPOTENT) null else "dispatch-returned",
                            )
                            if (persisted !is JournalDecision.Accept) {
                                durableJournal.transition(taskId, action.id, DispatchState.OUTCOME_UNKNOWN, clock.nowMillis(),
                                    "Dispatch returned but durable receipt could not be committed")
                                observer.observe().redacted()
                                return@withTimeout TaskOutcome(taskId, TaskState.RECOVERY_REQUIRED, verifiedCount,
                                    "Dispatch occurred but journal receipt is uncertain")
                            }
                        }
                    }

                    journal.append(record(taskId, action, TaskState.VERIFYING, "Dispatch returned; independent verification required", clock.nowMillis()))
                    val after = observer.observe().redacted()
                    when (val result = verifier.verify(action, snapshot, after)) {
                        is VerificationResult.Verified -> {
                            val verified = durableJournal.transition(taskId, action.id, DispatchState.VERIFIED, clock.nowMillis(), result.evidence)
                            if (verified !is JournalDecision.Accept) {
                                return@withTimeout TaskOutcome(taskId, TaskState.RECOVERY_REQUIRED, verifiedCount,
                                    "Postcondition verified but durable verification commit failed")
                            }
                            verifiedCount++
                            completed += action.id
                            journal.append(record(taskId, action, TaskState.COMPLETED, result.evidence.take(1_000), clock.nowMillis()))
                            snapshot = after
                        }
                        is VerificationResult.Failed -> {
                            durableJournal.transition(taskId, action.id, DispatchState.FAILED, clock.nowMillis(), result.reason)
                            return@withTimeout TaskOutcome(taskId, TaskState.FAILED, verifiedCount, result.reason)
                        }
                        is VerificationResult.Inconclusive -> {
                            durableJournal.transition(taskId, action.id, DispatchState.OUTCOME_UNKNOWN, clock.nowMillis(), result.reason)
                            return@withTimeout TaskOutcome(taskId, TaskState.RECOVERY_REQUIRED, verifiedCount, result.reason)
                        }
                    }
                }
                TaskOutcome(taskId, TaskState.FAILED, verifiedCount, "Maximum action count reached")
            }
        } catch (timeout: TimeoutCancellationException) {
            TaskOutcome(taskId, TaskState.RECOVERY_REQUIRED, verifiedCount, "Execution time budget expired; observe device state before retrying")
        } catch (cancelled: CancellationException) {
            val unresolved = runCatching {
                durableJournal.loadRecoverable().any { it.taskId == taskId }
            }.getOrDefault(true)
            val interruptedState = if (unresolved) TaskSessionState.RECOVERY_REQUIRED else TaskSessionState.CANCELLED
            taskSessions.transition(
                taskId, interruptedState, epochClock.nowEpochMillis(),
                if (unresolved) "Execution cancelled with an unresolved action; reconcile device state before retrying"
                else "Execution coroutine cancelled before any unresolved action remained",
            )
            throw cancelled
        } catch (failure: Exception) {
            TaskOutcome(taskId, TaskState.FAILED, verifiedCount, failure.message ?: "Execution failed")
        }

        val finalSessionState = when (outcome.state) {
            TaskState.COMPLETED -> TaskSessionState.COMPLETED
            TaskState.RECOVERY_REQUIRED -> TaskSessionState.RECOVERY_REQUIRED
            TaskState.CANCELLED -> TaskSessionState.CANCELLED
            else -> TaskSessionState.FAILED
        }
        val persistedOutcome = taskSessions.transition(taskId, finalSessionState, epochClock.nowEpochMillis(), outcome.reason)
        if (persistedOutcome !is TaskSessionDecision.Accept) {
            return TaskOutcome(taskId, TaskState.RECOVERY_REQUIRED, verifiedCount,
                "Execution ended as ${outcome.state}, but the task result could not be durably recorded")
        }
        check(clock.nowMillis() - started <= limits.maxElapsedMillis + 1_000) {
            "Orchestrator exceeded its monotonic time budget"
        }
        return outcome
    }

    private fun record(
        taskId: TaskId,
        action: AgentAction,
        state: TaskState,
        note: String,
        now: Long,
    ) = TaskStepRecord(taskId, action.id, state, now, null, note)
}

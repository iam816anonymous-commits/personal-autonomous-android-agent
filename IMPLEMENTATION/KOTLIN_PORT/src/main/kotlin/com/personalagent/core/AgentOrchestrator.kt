package com.personalagent.core

import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.CancellationException

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

        return try {
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

                    val initialDecision = policy.evaluate(action, PolicyContext(capabilityStatuses, snapshot = snapshot))
                    val approval = when (initialDecision) {
                        PolicyDecision.Allowed -> null
                        is PolicyDecision.Denied -> return@withTimeout TaskOutcome(taskId, TaskState.FAILED, verifiedCount, initialDecision.reason)
                        is PolicyDecision.RequiresApproval -> approvalProvider.request(action, snapshot)
                            ?: return@withTimeout TaskOutcome(taskId, TaskState.CANCELLED, verifiedCount, "User declined or did not provide approval")
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

                    journal.append(record(taskId, action, TaskState.EXECUTING, "Dispatch started", clock.nowMillis()))
                    val dispatch = withTimeout(action.limits.timeoutMillis) { dispatcher.dispatch(action) }
                    if (dispatch !is ActionExecutionResult.Dispatched) {
                        val reason = when (dispatch) {
                            is ActionExecutionResult.Rejected -> dispatch.reason
                            is ActionExecutionResult.Failed -> dispatch.reason
                            is ActionExecutionResult.Dispatched -> ""
                        }
                        // A timeout/failure may have happened after the side effect. Never replay blindly.
                        snapshot = observer.observe().redacted()
                        journal.append(record(taskId, action, TaskState.RECOVERY_REQUIRED, reason, clock.nowMillis()))
                        return@withTimeout TaskOutcome(taskId, TaskState.RECOVERY_REQUIRED, verifiedCount, reason)
                    }

                    journal.append(record(taskId, action, TaskState.VERIFYING, "Dispatch returned; independent verification required", clock.nowMillis()))
                    val after = observer.observe().redacted()
                    when (val result = verifier.verify(action, snapshot, after)) {
                        is VerificationResult.Verified -> {
                            verifiedCount++
                            completed += action.id
                            journal.append(record(taskId, action, TaskState.COMPLETED, result.evidence.take(1_000), clock.nowMillis()))
                            snapshot = after
                        }
                        is VerificationResult.Failed ->
                            return@withTimeout TaskOutcome(taskId, TaskState.FAILED, verifiedCount, result.reason)
                        is VerificationResult.Inconclusive ->
                            return@withTimeout TaskOutcome(taskId, TaskState.RECOVERY_REQUIRED, verifiedCount, result.reason)
                    }
                }
                TaskOutcome(taskId, TaskState.FAILED, verifiedCount, "Maximum action count reached")
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            TaskOutcome(taskId, TaskState.FAILED, verifiedCount, failure.message ?: "Execution failed")
        }.also {
            // Prevent a broken clock/provider from silently extending the task's lifetime.
            check(clock.nowMillis() - started <= limits.maxElapsedMillis + 1_000) {
                "Orchestrator exceeded its monotonic time budget"
            }
        }
    }

    private fun record(
        taskId: TaskId,
        action: AgentAction,
        state: TaskState,
        note: String,
        now: Long,
    ) = TaskStepRecord(taskId, action.id, state, now, null, note)
}

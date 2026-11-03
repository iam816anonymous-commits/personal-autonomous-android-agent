package com.personalagent.core

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AgentCoreTest {
    private class FakeDurableJournal : DurableExecutionJournal {
        private val records = linkedMapOf<Pair<TaskId, ActionId>, DurableActionRecord>()
        override suspend fun insertPrepared(record: DurableActionRecord): Boolean {
            val key = record.taskId to record.actionId
            if (key in records || record.dispatchState != DispatchState.PREPARED) return false
            records[key] = record
            return true
        }
        override suspend fun transition(
            taskId: TaskId, actionId: ActionId, next: DispatchState,
            nowElapsedMillis: Long, note: String?,
        ): JournalDecision {
            val key = taskId to actionId
            val current = records[key] ?: return JournalDecision.Reject("missing")
            val result = DurableActionProtocol.transition(current, next, nowElapsedMillis, note)
            if (result is JournalDecision.Accept) records[key] = result.record
            return result
        }
        override suspend fun loadRecoverable() = records.values.filter {
            it.dispatchState in setOf(DispatchState.PREPARED, DispatchState.DISPATCHING, DispatchState.DISPATCHED, DispatchState.OUTCOME_UNKNOWN)
        }
        override suspend fun reconcileAfterProcessDeath() = records.values.map {
            (DurableActionProtocol.recoverAfterProcessDeath(it) as JournalDecision.Accept).record
        }
    }

    private fun usable(id: CapabilityId) = CapabilityStatus(
        id = id,
        hardwarePresent = true,
        apiAvailable = true,
        permissionGranted = true,
        runtimeEnabled = true,
        verifiedAtElapsedMillis = 100,
    )

    private fun snapshot(id: String = "snap-1", at: Long = 100) = DeviceSnapshot(
        id = SnapshotId(id),
        capturedAtElapsedMillis = at,
        foregroundPackage = "com.example.notes",
        nodes = listOf(
            UiNodeSnapshot("node-1", "Title", null, null, "com.example.notes", true, false, false, null),
            UiNodeSnapshot("password", "secret", "secret", null, "com.example.notes", false, true, true, null),
        ),
        complete = true,
        source = EvidenceSource.ACCESSIBILITY_TREE,
    )

    @Test
    fun passwordContentIsRedactedBeforeExposure() {
        val redacted = snapshot().redacted()
        assertEquals("Title", redacted.nodes[0].text)
        assertEquals(null, redacted.nodes[1].text)
        assertEquals(null, redacted.nodes[1].contentDescription)
    }

    @Test
    fun registryRejectsDuplicateAndFreezesAfterBuild() {
        val id = CapabilityId("app.launch")
        val descriptor = ToolDescriptor(id, "Launch apps", RiskLevel.LOW, setOf(KClassName("launch_application")))
        val builder = ToolRegistry.Builder().register(descriptor)
        val registry = builder.build()
        assertEquals(1, registry.availableTools(mapOf(id to usable(id))).size)
        assertFalse(runCatching { builder.register(descriptor) }.isSuccess)
        assertFalse(runCatching { ToolRegistry.Builder().register(descriptor).register(descriptor) }.isSuccess)
    }

    @Test
    fun approvalIsBoundToExactActionAndFreshSnapshot() {
        var now = 100L
        val clock = MonotonicClock { now }
        val policy = ApprovalPolicy(clock)
        val id = CapabilityId("accessibility.set_text")
        val current = snapshot()
        val action = SetAccessibleText(
            id = ActionId("a-1"),
            snapshotId = current.id,
            nodeId = "node-1",
            targetPackageName = "com.example.notes",
            text = "hello",
            expected = Postcondition.VisibleTextContains("hello", "com.example.notes"),
        )
        val statuses = mapOf(id to usable(id))
        assertIs<PolicyDecision.RequiresApproval>(policy.evaluate(action, PolicyContext(statuses, snapshot = current)))
        val token = policy.issueUserApproval(action, current)
        assertEquals(PolicyDecision.Allowed, policy.evaluate(action, PolicyContext(statuses, token, current)))
        val changed = action.copy(text = "changed")
        assertIs<PolicyDecision.Denied>(policy.evaluate(changed, PolicyContext(statuses, token, current)))
        now = 20_000
        assertIs<PolicyDecision.Denied>(policy.evaluate(action, PolicyContext(statuses, token, current)))
    }

    @Test
    fun staleSnapshotCannotAuthorizeAccessibilityMutation() {
        val clock = MonotonicClock { 20_000 }
        val policy = ApprovalPolicy(clock)
        val current = snapshot(at = 100)
        val id = CapabilityId("accessibility.click")
        val action = ClickAccessibleNode(
            id = ActionId("a-2"),
            snapshotId = current.id,
            nodeId = "node-1",
            targetPackageName = "com.example.notes",
            expected = Postcondition.AccessibleNodeExists("node-2", "com.example.notes"),
        )
        assertIs<PolicyDecision.Denied>(
            policy.evaluate(action, PolicyContext(mapOf(id to usable(id)), snapshot = current)),
        )
    }

    @Test
    fun skillMustBeApprovedAndUseDeclaredCapabilities() {
        val launch = CapabilityId("app.launch")
        val observe = CapabilityId("device.observe")
        val skill = SkillManifest(
            id = "open-notes",
            version = 1,
            description = "Open notes",
            requiredCapabilities = setOf(launch),
            steps = listOf(
                SkillStep(
                    SkillStepKind.OBSERVE,
                    observe,
                    emptyMap(),
                    Postcondition.ObservationAvailable,
                ),
            ),
            approvedByUser = false,
        )
        val errors = SkillValidator().validate(skill, setOf(launch, observe))
        assertTrue(errors.any { it.contains("not been approved") })
        assertTrue(errors.any { it.contains("undeclared capability") })
    }

    @Test
    fun orchestratorOnlyCompletesAfterIndependentVerification() = runBlocking {
        val clock = MonotonicClock { 100 }
        val capability = CapabilityId("app.launch")
        val action = LaunchApplication(ActionId("launch-1"), "com.example.notes")
        var planned = false
        val records = mutableListOf<TaskStepRecord>()
        val registry = ToolRegistry.Builder().register(
            ToolDescriptor(
                capability,
                "Launch an app",
                RiskLevel.LOW,
                setOf(action.kindId()),
            ),
        ).build()
        val snapshot = snapshot()

        val orchestrator = AgentOrchestrator(
            planner = GoalPlanner { _, _, _ ->
                if (planned) null else action.also { planned = true }
            },
            observer = object : DeviceObserver {
                override suspend fun observe() = snapshot
            },
            dispatcher = object : AgentActionDispatcher {
                override suspend fun dispatch(action: AgentAction) = ActionExecutionResult.Dispatched()
            },
            verifier = object : ActionPostconditionVerifier {
                override suspend fun verify(
                    action: AgentAction,
                    before: DeviceSnapshot,
                    after: DeviceSnapshot,
                ) = VerificationResult.Verified("foreground package matched", EvidenceSource.ACCESSIBILITY_TREE)
            },
            registry = registry,
            policy = ApprovalPolicy(clock),
            approvalProvider = ApprovalProvider { _, _ -> null },
            journal = object : TaskStepJournal {
                override suspend fun append(record: TaskStepRecord) { records += record }
            },
            durableJournal = FakeDurableJournal(),
            clock = clock,
        )

        val outcome = orchestrator.run(TaskId("task-1"), "Open notes", mapOf(capability to usable(capability)))
        assertEquals(TaskState.COMPLETED, outcome.state)
        assertEquals(1, outcome.verifiedActions)
        assertTrue(records.any { it.state == TaskState.VERIFYING })
    }

    @Test
    fun dispatchWithoutVerificationCannotBeReportedAsSuccess() = runBlocking {
        val clock = MonotonicClock { 100 }
        val capability = CapabilityId("app.launch")
        val action = LaunchApplication(ActionId("launch-2"), "com.example.notes")
        val registry = ToolRegistry.Builder().register(
            ToolDescriptor(capability, "Launch", RiskLevel.LOW, setOf(action.kindId())),
        ).build()
        val outcome = AgentOrchestrator(
            planner = GoalPlanner { _, _, _ -> action },
            observer = object : DeviceObserver {
                override suspend fun observe() = snapshot()
            },
            dispatcher = object : AgentActionDispatcher {
                override suspend fun dispatch(action: AgentAction) = ActionExecutionResult.Dispatched()
            },
            verifier = object : ActionPostconditionVerifier {
                override suspend fun verify(action: AgentAction, before: DeviceSnapshot, after: DeviceSnapshot) =
                    VerificationResult.Inconclusive("No independent evidence")
            },
            registry = registry,
            policy = ApprovalPolicy(clock),
            approvalProvider = ApprovalProvider { _, _ -> null },
            journal = object : TaskStepJournal {
                override suspend fun append(record: TaskStepRecord) = Unit
            },
            durableJournal = FakeDurableJournal(),
            clock = clock,
        ).run(TaskId("task-2"), "Open notes", mapOf(capability to usable(capability)))
        assertEquals(TaskState.RECOVERY_REQUIRED, outcome.state)
        assertEquals(0, outcome.verifiedActions)
    }
}

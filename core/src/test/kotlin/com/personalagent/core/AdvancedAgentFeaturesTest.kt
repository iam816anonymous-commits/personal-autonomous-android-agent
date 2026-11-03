package com.personalagent.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AdvancedAgentFeaturesTest {
    @Test
    fun uncertainNonIdempotentDispatchIsNeverAutomaticallyReplayed() {
        val record = DurableActionRecord(
            taskId = TaskId("task-rea"),
            actionId = ActionId("payment-submit"),
            actionKind = "submit_form",
            targetPackage = "com.example.bank",
            snapshotId = SnapshotId("screen-1"),
            idempotency = Idempotency.NON_IDEMPOTENT,
            dispatchState = DispatchState.DISPATCHING,
            attempt = 1,
            createdAtElapsedMillis = 10,
            updatedAtElapsedMillis = 20,
        )
        val recovered = assertIs<JournalDecision.Accept>(DurableActionProtocol.recoverAfterProcessDeath(record)).record
        assertEquals(DispatchState.OUTCOME_UNKNOWN, recovered.dispatchState)
        assertFalse(DurableActionProtocol.mayAutomaticallyRetry(recovered, verifiedNotApplied = true))
    }

    @Test
    fun dispatchIntentMustPrecedeDispatchAndTransitionsAreBounded() {
        val prepared = DurableActionRecord(
            taskId = TaskId("task-1"),
            actionId = ActionId("action-1"),
            actionKind = "launch_application",
            targetPackage = "com.example.notes",
            snapshotId = null,
            idempotency = Idempotency.IDEMPOTENT,
            dispatchState = DispatchState.PREPARED,
            attempt = 1,
            createdAtElapsedMillis = 1,
            updatedAtElapsedMillis = 1,
        )
        val dispatching = assertIs<JournalDecision.Accept>(
            DurableActionProtocol.transition(prepared, DispatchState.DISPATCHING, 2),
        ).record
        assertEquals(DispatchState.DISPATCHING, dispatching.dispatchState)
        assertIs<JournalDecision.Reject>(
            DurableActionProtocol.transition(dispatching, DispatchState.PREPARED, 3),
        )
    }

    @Test
    fun toolFactoryRejectsUnknownActionAndUnapprovedCapability() {
        val cap = CapabilityId("accessibility.click")
        val tool = GeneratedToolDefinition(
            id = "click-save",
            version = 1,
            description = "Click a grounded save control",
            parameters = listOf(ToolParameter("nodeId", ToolParameterType.NODE_ID)),
            steps = listOf(
                ToolStepTemplate(
                    actionKind = KClassName("unregistered_click"),
                    capability = cap,
                    parameterBindings = mapOf("nodeId" to "$"+"nodeId"),
                    risk = RiskLevel.MODERATE,
                ),
            ),
            requiredCapabilities = setOf(cap),
            maximumRisk = RiskLevel.MODERATE,
            createdFromSkillId = null,
        )
        val result = SelfToolFactory().create(
            tool,
            ToolValidationContext(
                approvedCapabilities = emptySet(),
                registeredActionKinds = emptySet(),
                registeredActionSchemas = emptyMap(),
                maximumAllowedRisk = RiskLevel.MODERATE,
                userApproved = true,
            ),
        )
        val rejected = assertIs<ToolFactoryResult.Rejected>(result)
        assertTrue(rejected.reasons.any { it.contains("not approved") })
        assertTrue(rejected.reasons.any { it.contains("unregistered action") })
    }

    @Test
    fun toolFactoryCreatesOnlyBoundedDeclarativeToolAfterApproval() {
        val cap = CapabilityId("app.launch")
        val tool = GeneratedToolDefinition(
            id = "launch-notes",
            version = 1,
            description = "Launch notes app",
            parameters = listOf(ToolParameter("packageName", ToolParameterType.PACKAGE_NAME)),
            steps = listOf(
                ToolStepTemplate(
                    actionKind = KClassName("launch_application"),
                    capability = cap,
                    parameterBindings = mapOf("packageName" to "$"+"packageName"),
                    risk = RiskLevel.LOW,
                ),
            ),
            requiredCapabilities = setOf(cap),
            maximumRisk = RiskLevel.LOW,
            createdFromSkillId = null,
        )
        val result = assertIs<ToolFactoryResult.Created>(
            SelfToolFactory().create(
                tool,
                ToolValidationContext(
                    approvedCapabilities = setOf(cap),
                    registeredActionKinds = setOf(KClassName("launch_application")),
                    registeredActionSchemas = mapOf(KClassName("launch_application") to mapOf("packageName" to ToolParameterType.PACKAGE_NAME)),
                    maximumAllowedRisk = RiskLevel.LOW,
                    userApproved = true,
                ),
            ),
        )
        assertEquals(64, result.schemaDigest.length)
    }

    @Test
    fun reaCannotCertifySkillWithoutVerifiedProbeEvidence() {
        val cap = CapabilityId("app.launch")
        val manifest = SkillManifest(
            id = "open-notes",
            version = 1,
            description = "Open notes",
            requiredCapabilities = setOf(cap),
            steps = listOf(
                SkillStep(
                    SkillStepKind.LAUNCH_APP,
                    cap,
                    mapOf("packageName" to "com.example.notes"),
                    Postcondition.ForegroundPackageIs("com.example.notes"),
                ),
            ),
            approvedByUser = true,
        )
        val proposal = SkillProposal(manifest, setOf("launch"), "com.example.notes", "1.0", 0.95)
        val behavior = AppBehaviorModel(
            app = AppIdentity("com.example.notes", "1.0", 1, 27),
            observedScreens = emptyList(),
            transitions = emptyList(),
            probes = listOf(
                InteractionProbe(
                    "launch",
                    LaunchApplication(ActionId("launch-1"), "com.example.notes"),
                    Postcondition.ForegroundPackageIs("com.example.notes"),
                    SnapshotId("before"),
                    null,
                    null,
                ),
            ),
            limitations = emptyList(),
        )
        val result = assertIs<SkillCertification.Rejected>(
            AppSkillCertifier(SkillValidator()).certify(proposal, behavior, setOf(cap)),
        )
        assertTrue(result.reasons.any { it.contains("Unverified probe") })
    }
}

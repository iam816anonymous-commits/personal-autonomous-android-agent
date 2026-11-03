package com.personalagent.android

import android.content.Context
import android.os.SystemClock
import com.personalagent.android.accessibility.AndroidAccessibilityObserver
import com.personalagent.android.accessibility.AndroidActionDispatcher
import com.personalagent.android.accessibility.AndroidPostconditionVerifier
import com.personalagent.android.capability.AndroidCapabilityRegistry
import com.personalagent.android.data.RoomTaskStepJournal
import com.personalagent.android.data.RoomDurableExecutionJournal
import com.personalagent.core.*

/** Composition root: the model planner is injected, never given Android framework handles. */
class AndroidAgentRuntime(
    context: Context,
    private val journal: RoomTaskStepJournal,
    private val durableJournal: RoomDurableExecutionJournal,
) {
    private val appContext = context.applicationContext
    private val capabilities = AndroidCapabilityRegistry(appContext)
    private val clock = MonotonicClock { SystemClock.elapsedRealtime() }
    /** UI must call issueUserApproval only after a real, explicit confirmation gesture. */
    val approvalPolicy: ApprovalPolicy = ApprovalPolicy(clock)

    fun discoverCapabilities(): Map<CapabilityId, CapabilityStatus> = capabilities.discover()

    fun createOrchestrator(
        planner: GoalPlanner,
        approvalProvider: ApprovalProvider = ApprovalProvider { _, _ -> null },
    ): AgentOrchestrator {
        val registry = ToolRegistry.Builder()
            .register(ToolDescriptor(CapabilityId("app.launch"), "Launch an installed app by package", RiskLevel.LOW,
                setOf(KClassName("launch_application"))))
            .register(ToolDescriptor(CapabilityId("device.observe"), "Read the active semantic UI tree", RiskLevel.READ_ONLY,
                setOf(KClassName("read_foreground_state"))))
            .register(ToolDescriptor(CapabilityId("accessibility.click"), "Click a grounded accessible node", RiskLevel.MODERATE,
                setOf(KClassName("click_accessible_node"))))
            .register(ToolDescriptor(CapabilityId("accessibility.set_text"), "Set text in a grounded editable node", RiskLevel.MODERATE,
                setOf(KClassName("set_accessible_text"))))
            .build()
        val observer = AndroidAccessibilityObserver()
        return AgentOrchestrator(
            planner = planner,
            observer = observer,
            dispatcher = AndroidActionDispatcher(observer),
            verifier = AndroidPostconditionVerifier(),
            registry = registry,
            policy = approvalPolicy,
            approvalProvider = approvalProvider,
            journal = journal,
            durableJournal = durableJournal,
            taskSessions = (appContext as AgentApplication).taskSessionJournal,
            epochClock = EpochClock { System.currentTimeMillis() },
            clock = clock,
        )
    }
}

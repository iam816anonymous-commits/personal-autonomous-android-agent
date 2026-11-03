package com.personalagent.core

/**
 * Platform-neutral domain contracts. Android framework objects must never cross this boundary.
 * Values originating from a model must be decoded into these closed types before use.
 */

@JvmInline
value class TaskId(val value: String) {
    init { require(value.isNotBlank()) }
}

@JvmInline
value class ActionId(val value: String) {
    init { require(value.isNotBlank()) }
}

@JvmInline
value class CapabilityId(val value: String) {
    init { require(value.matches(Regex("[a-z][a-z0-9_.-]{1,63}"))) }
}

@JvmInline
value class SnapshotId(val value: String) {
    init { require(value.isNotBlank()) }
}

enum class RiskLevel(val severity: Int) { READ_ONLY(0), LOW(1), MODERATE(2), HIGH(3), CRITICAL(4) }
enum class Idempotency { IDEMPOTENT, CONDITIONALLY_IDEMPOTENT, NON_IDEMPOTENT }
enum class EvidenceSource { ACCESSIBILITY_TREE, ANDROID_API, APP_STATE, USER_CONFIRMATION }

data class ActionLimits(
    val timeoutMillis: Long,
    val maxAttempts: Int = 1,
) {
    init {
        require(timeoutMillis in 1L..120_000L) { "Action timeout must be between 1 ms and 120 s" }
        require(maxAttempts in 1..2) { "Retries are deliberately bounded to at most one retry" }
    }
}

sealed interface AgentAction {
    val id: ActionId
    val capability: CapabilityId
    val risk: RiskLevel
    val idempotency: Idempotency
    val limits: ActionLimits
    val targetPackage: String?
    val expected: Postcondition
}

data class LaunchApplication(
    override val id: ActionId,
    val packageName: String,
    override val capability: CapabilityId = CapabilityId("app.launch"),
    override val risk: RiskLevel = RiskLevel.LOW,
    override val idempotency: Idempotency = Idempotency.IDEMPOTENT,
    override val limits: ActionLimits = ActionLimits(10_000),
    override val expected: Postcondition = Postcondition.ForegroundPackageIs(packageName),
) : AgentAction {
    init { require(packageName.matches(Regex("[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)+"))) }
    override val targetPackage: String = packageName
}

data class ClickAccessibleNode(
    override val id: ActionId,
    val snapshotId: SnapshotId,
    val nodeId: String,
    val targetPackageName: String,
    override val capability: CapabilityId = CapabilityId("accessibility.click"),
    override val risk: RiskLevel = RiskLevel.MODERATE,
    override val idempotency: Idempotency = Idempotency.CONDITIONALLY_IDEMPOTENT,
    override val limits: ActionLimits = ActionLimits(8_000),
    override val expected: Postcondition,
) : AgentAction {
    init {
        require(nodeId.isNotBlank())
        require(targetPackageName.isNotBlank())
    }
    override val targetPackage: String = targetPackageName
}

data class SetAccessibleText(
    override val id: ActionId,
    val snapshotId: SnapshotId,
    val nodeId: String,
    val targetPackageName: String,
    val text: String,
    override val capability: CapabilityId = CapabilityId("accessibility.set_text"),
    override val risk: RiskLevel = RiskLevel.MODERATE,
    override val idempotency: Idempotency = Idempotency.CONDITIONALLY_IDEMPOTENT,
    override val limits: ActionLimits = ActionLimits(8_000),
    override val expected: Postcondition,
) : AgentAction {
    init {
        require(nodeId.isNotBlank())
        require(targetPackageName.isNotBlank())
        require(text.length <= 4_000)
    }
    override val targetPackage: String = targetPackageName
}

data class ReadForegroundState(
    override val id: ActionId,
    override val capability: CapabilityId = CapabilityId("device.observe"),
    override val risk: RiskLevel = RiskLevel.READ_ONLY,
    override val idempotency: Idempotency = Idempotency.IDEMPOTENT,
    override val limits: ActionLimits = ActionLimits(5_000),
    override val targetPackage: String? = null,
    override val expected: Postcondition = Postcondition.ObservationAvailable,
) : AgentAction

sealed interface Postcondition {
    data class ForegroundPackageIs(val packageName: String) : Postcondition
    data class VisibleTextContains(val text: String, val packageName: String) : Postcondition
    data class AccessibleNodeExists(val nodeId: String, val packageName: String) : Postcondition
    data object ObservationAvailable : Postcondition
}

data class UiNodeSnapshot(
    val stableId: String,
    val text: String?,
    val contentDescription: String?,
    val viewId: String?,
    val packageName: String,
    val clickable: Boolean,
    val editable: Boolean,
    val password: Boolean,
    val bounds: ScreenBounds?,
) {
    init { require(stableId.isNotBlank() && packageName.isNotBlank()) }
}

data class ScreenBounds(val left: Int, val top: Int, val right: Int, val bottom: Int) {
    init { require(right >= left && bottom >= top) }
}

data class DeviceSnapshot(
    val id: SnapshotId,
    val capturedAtElapsedMillis: Long,
    val foregroundPackage: String?,
    val nodes: List<UiNodeSnapshot>,
    val complete: Boolean,
    val source: EvidenceSource,
) {
    init {
        require(capturedAtElapsedMillis >= 0)
        require(nodes.size <= 250) { "Snapshot node count exceeds bounded traversal limit" }
    }

    /** Redacts password fields before persistence, logging or model exposure. */
    fun redacted(): DeviceSnapshot = copy(
        nodes = nodes.map { node ->
            if (node.password) node.copy(text = null, contentDescription = null)
            else node.copy(text = node.text?.take(500), contentDescription = node.contentDescription?.take(500))
        },
    )
}

sealed interface VerificationResult {
    data class Verified(val evidence: String, val source: EvidenceSource) : VerificationResult
    data class Failed(val reason: String) : VerificationResult
    data class Inconclusive(val reason: String) : VerificationResult
}

sealed interface ActionExecutionResult {
    data class Dispatched(val evidenceReference: String? = null) : ActionExecutionResult
    data class Rejected(val reason: String) : ActionExecutionResult
    data class Failed(val reason: String, val outcomeMayHaveOccurred: Boolean) : ActionExecutionResult
}

enum class TaskState {
    CREATED, OBSERVING, PLANNING, VALIDATING, AWAITING_APPROVAL,
    EXECUTING, VERIFYING, RECOVERY_REQUIRED, COMPLETED, FAILED, CANCELLED
}

data class TaskStepRecord(
    val taskId: TaskId,
    val actionId: ActionId,
    val state: TaskState,
    val startedAtElapsedMillis: Long,
    val finishedAtElapsedMillis: Long?,
    val note: String,
) {
    init {
        require(startedAtElapsedMillis >= 0)
        require(finishedAtElapsedMillis == null || finishedAtElapsedMillis >= startedAtElapsedMillis)
        require(note.length <= 1_000)
    }
}

/** Stable wire-level action identifiers; never derive schemas from JVM class names. */
fun AgentAction.kindId(): KClassName = KClassName(
    when (this) {
        is LaunchApplication -> "launch_application"
        is ClickAccessibleNode -> "click_accessible_node"
        is SetAccessibleText -> "set_accessible_text"
        is ReadForegroundState -> "read_foreground_state"
    },
)

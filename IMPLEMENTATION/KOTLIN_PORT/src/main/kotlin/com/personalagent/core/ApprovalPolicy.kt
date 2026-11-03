package com.personalagent.core

import java.security.MessageDigest

fun interface MonotonicClock {
    fun nowMillis(): Long
}

data class ApprovalToken(
    val actionDigest: String,
    val targetPackage: String?,
    val snapshotId: SnapshotId?,
    val expiresAtElapsedMillis: Long,
    val approvedByUser: Boolean,
)

sealed interface PolicyDecision {
    data object Allowed : PolicyDecision
    data class RequiresApproval(val reason: String) : PolicyDecision
    data class Denied(val reason: String) : PolicyDecision
}

data class PolicyContext(
    val capabilityStatuses: Map<CapabilityId, CapabilityStatus>,
    val approval: ApprovalToken? = null,
    val snapshot: DeviceSnapshot? = null,
)

class ApprovalPolicy(
    private val clock: MonotonicClock,
    private val approvalLifetimeMillis: Long = 60_000,
) {
    init { require(approvalLifetimeMillis in 1_000..300_000) }

    fun evaluate(action: AgentAction, context: PolicyContext): PolicyDecision {
        val capability = context.capabilityStatuses[action.capability]
            ?: return PolicyDecision.Denied("Capability was not discovered")
        if (!capability.usable) {
            return PolicyDecision.Denied(capability.unavailableReason ?: "Capability is unavailable")
        }

        if (action.risk == RiskLevel.CRITICAL) {
            return PolicyDecision.Denied("Critical-risk actions are not supported by this agent")
        }

        val snapshotBound = when (action) {
            is ClickAccessibleNode -> action.snapshotId
            is SetAccessibleText -> action.snapshotId
            else -> null
        }
        if (snapshotBound != null) {
            val current = context.snapshot
                ?: return PolicyDecision.Denied("A fresh UI snapshot is required")
            if (current.id != snapshotBound) {
                return PolicyDecision.Denied("Action target was grounded in a stale snapshot")
            }
            if (!current.complete) {
                return PolicyDecision.Denied("Incomplete UI snapshots cannot authorize mutation")
            }
            if (current.foregroundPackage != action.targetPackage) {
                return PolicyDecision.Denied("Foreground package changed after target selection")
            }
        }

        val approvalRequired = action.risk.severity >= RiskLevel.HIGH.severity ||
            action is SetAccessibleText || action is ClickAccessibleNode
        if (!approvalRequired) return PolicyDecision.Allowed

        val token = context.approval ?: return PolicyDecision.RequiresApproval("User approval is required")
        if (!token.approvedByUser) return PolicyDecision.Denied("Approval was not issued by the user")
        if (token.expiresAtElapsedMillis <= clock.nowMillis()) {
            return PolicyDecision.Denied("Approval expired")
        }
        if (token.actionDigest != digest(action)) {
            return PolicyDecision.Denied("Approval does not match this exact action")
        }
        if (token.targetPackage != action.targetPackage || token.snapshotId != snapshotBound) {
            return PolicyDecision.Denied("Approval target or snapshot binding changed")
        }
        return PolicyDecision.Allowed
    }

    fun issueUserApproval(action: AgentAction, snapshot: DeviceSnapshot?): ApprovalToken =
        ApprovalToken(
            actionDigest = digest(action),
            targetPackage = action.targetPackage,
            snapshotId = snapshot?.id,
            expiresAtElapsedMillis = Math.addExact(clock.nowMillis(), approvalLifetimeMillis),
            approvedByUser = true,
        )

    private fun digest(action: AgentAction): String {
        // Explicit canonical representation; do not use data-class toString() as a security format.
        val canonical = listOf(
            action::class.simpleName.orEmpty(),
            action.id.value,
            action.capability.value,
            action.risk.name,
            action.idempotency.name,
            action.targetPackage.orEmpty(),
            action.expected.toString(),
            when (action) {
                is LaunchApplication -> action.packageName
                is ClickAccessibleNode -> "${action.snapshotId.value}:${action.nodeId}"
                is SetAccessibleText -> "${action.snapshotId.value}:${action.nodeId}:${action.text}"
                is ReadForegroundState -> "read"
            },
        ).joinToString("|")
        return MessageDigest.getInstance("SHA-256")
            .digest(canonical.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }
}

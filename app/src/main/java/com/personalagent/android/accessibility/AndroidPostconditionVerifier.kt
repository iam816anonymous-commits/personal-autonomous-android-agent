package com.personalagent.android.accessibility
import com.personalagent.core.*
class AndroidPostconditionVerifier : ActionPostconditionVerifier {
    override suspend fun verify(action: AgentAction, before: DeviceSnapshot, after: DeviceSnapshot): VerificationResult {
        if (after.id == before.id || after.capturedAtElapsedMillis <= before.capturedAtElapsedMillis)
            return VerificationResult.Inconclusive("A distinct newer snapshot is required")
        val matched = when (val expected = action.expected) {
            is Postcondition.ForegroundPackageIs -> after.foregroundPackage == expected.packageName
            is Postcondition.VisibleTextContains -> after.nodes.any {
                it.packageName == expected.packageName && !it.password &&
                    it.text?.contains(expected.text, ignoreCase = true) == true
            }
            is Postcondition.AccessibleNodeExists -> after.nodes.any {
                it.packageName == expected.packageName && it.stableId == expected.nodeId
            }
            Postcondition.ObservationAvailable -> after.nodes.isNotEmpty()
        }
        return if (matched) VerificationResult.Verified(
            "Postcondition matched against fresh accessibility snapshot " + after.id.value,
            EvidenceSource.ACCESSIBILITY_TREE)
        else VerificationResult.Failed("Expected postcondition not present in fresh snapshot")
    }
}

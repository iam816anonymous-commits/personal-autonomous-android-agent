package com.personalagent.android.accessibility

import com.personalagent.core.*
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.coroutines.runBlocking

class AndroidPostconditionVerifierTest {
    private fun snapshot(id: String, elapsed: Long, foreground: String) = DeviceSnapshot(
        id = SnapshotId(id),
        capturedAtElapsedMillis = elapsed,
        foregroundPackage = foreground,
        nodes = listOf(UiNodeSnapshot(
            stableId = "node-1", text = "Ready", contentDescription = null, viewId = "ready",
            packageName = foreground, clickable = false, editable = false, password = false, bounds = null)),
        complete = true,
        source = EvidenceSource.ACCESSIBILITY_TREE,
    )

    @Test fun verifiesOnlyAgainstANewerSnapshot() = runBlocking {
        val before = snapshot("before", 100, "com.example.app")
        val after = snapshot("after", 200, "com.example.app")
        val action = LaunchApplication(ActionId("launch"), "com.example.app")
        val result = AndroidPostconditionVerifier().verify(action, before, after)
        assertTrue(result is VerificationResult.Verified)
    }

    @Test fun refusesTheSameSnapshotAsEvidence() = runBlocking {
        val before = snapshot("same", 100, "com.example.app")
        val action = LaunchApplication(ActionId("launch"), "com.example.app")
        val result = AndroidPostconditionVerifier().verify(action, before, before)
        assertTrue(result is VerificationResult.Inconclusive)
    }

    @Test fun failsWhenExpectedForegroundPackageIsAbsent() = runBlocking {
        val before = snapshot("before", 100, "com.other.app")
        val after = snapshot("after", 200, "com.other.app")
        val action = LaunchApplication(ActionId("launch"), "com.example.app")
        val result = AndroidPostconditionVerifier().verify(action, before, after)
        assertTrue(result is VerificationResult.Failed)
    }
}

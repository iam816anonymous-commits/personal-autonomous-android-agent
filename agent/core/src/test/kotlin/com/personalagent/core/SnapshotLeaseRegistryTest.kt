package com.personalagent.core

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SnapshotLeaseRegistryTest {
    private fun snapshot(
        id: String,
        at: Long,
        label: String = "Save",
        packageName: String = "com.example.app",
        complete: Boolean = true,
    ) = DeviceSnapshot(
        id = SnapshotId(id),
        capturedAtElapsedMillis = at,
        foregroundPackage = packageName,
        nodes = listOf(
            UiNodeSnapshot("node-save", label, null, "id/save", packageName,
                clickable = true, editable = false, password = false, bounds = ScreenBounds(0, 0, 10, 10)),
        ),
        complete = complete,
        source = EvidenceSource.ACCESSIBILITY_TREE,
    )

    @Test fun acceptsFreshEquivalentScreenEvenWithNewSnapshotId() {
        val leases = SnapshotLeaseRegistry()
        leases.remember(snapshot("before", 100))
        assertTrue(leases.matches(SnapshotId("before"), snapshot("after", 200), 250))
    }

    @Test fun rejectsChangedScreenAndForegroundPackage() {
        val leases = SnapshotLeaseRegistry()
        leases.remember(snapshot("before", 100))
        assertFalse(leases.matches(SnapshotId("before"), snapshot("after", 200, label = "Delete"), 250))
        assertFalse(leases.matches(SnapshotId("before"), snapshot("after", 200, packageName = "com.other.app"), 250))
    }

    @Test fun rejectsExpiredMissingAndIncompleteLeases() {
        val leases = SnapshotLeaseRegistry(maximumAgeMillis = 1_000)
        leases.remember(snapshot("before", 100))
        assertFalse(leases.matches(SnapshotId("before"), snapshot("after", 200), 1_101))
        assertFalse(leases.matches(SnapshotId("missing"), snapshot("after", 200), 250))
        assertFalse(leases.matches(SnapshotId("before"), snapshot("partial", 200, complete = false), 250))
    }

    @Test fun evictsOldestLeaseWhenCapacityIsReached() {
        val leases = SnapshotLeaseRegistry(maximumEntries = 1)
        leases.remember(snapshot("first", 100))
        leases.remember(snapshot("second", 110))
        assertFalse(leases.matches(SnapshotId("first"), snapshot("current", 120), 130))
        assertTrue(leases.matches(SnapshotId("second"), snapshot("current", 120), 130))
    }
}

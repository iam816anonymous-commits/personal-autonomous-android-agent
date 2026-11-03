package com.personalagent.core

import java.security.MessageDigest

/**
 * Short-lived lease tying a mutation target to the exact semantic screen that grounded it.
 * A new snapshot ID is expected on re-observation; the semantic fingerprint must remain equal.
 */
class SnapshotLeaseRegistry(
    private val maximumEntries: Int = 16,
    private val maximumAgeMillis: Long = 10_000,
) {
    private data class Lease(
        val packageName: String?,
        val capturedAtElapsedMillis: Long,
        val fingerprint: String,
    )

    private val leases = linkedMapOf<SnapshotId, Lease>()

    init {
        require(maximumEntries in 1..128)
        require(maximumAgeMillis in 100L..60_000L)
    }

    @Synchronized
    fun remember(snapshot: DeviceSnapshot) {
        if (!snapshot.complete || snapshot.nodes.isEmpty()) return
        leases[snapshot.id] = Lease(
            packageName = snapshot.foregroundPackage,
            capturedAtElapsedMillis = snapshot.capturedAtElapsedMillis,
            fingerprint = fingerprint(snapshot),
        )
        while (leases.size > maximumEntries) {
            leases.remove(leases.keys.first())
        }
    }

    @Synchronized
    fun matches(
        expectedSnapshotId: SnapshotId,
        current: DeviceSnapshot,
        nowElapsedMillis: Long,
    ): Boolean {
        val lease = leases[expectedSnapshotId] ?: return false
        if (!current.complete || current.nodes.isEmpty()) return false
        val leaseAge = nowElapsedMillis - lease.capturedAtElapsedMillis
        val currentAge = nowElapsedMillis - current.capturedAtElapsedMillis
        if (leaseAge !in 0L..maximumAgeMillis || currentAge !in 0L..maximumAgeMillis) return false
        if (current.capturedAtElapsedMillis < lease.capturedAtElapsedMillis) return false
        return lease.packageName == current.foregroundPackage &&
            lease.fingerprint == fingerprint(current)
    }

    private fun fingerprint(snapshot: DeviceSnapshot): String {
        val canonical = buildString {
            append(snapshot.foregroundPackage.orEmpty())
            snapshot.nodes.forEach { node ->
                append('|').append(node.stableId)
                append('|').append(node.packageName)
                append('|').append(node.viewId.orEmpty())
                append('|').append(node.text.orEmpty())
                append('|').append(node.contentDescription.orEmpty())
                append('|').append(node.clickable)
                append('|').append(node.editable)
                append('|').append(node.password)
                append('|').append(node.bounds?.let {
                    "${it.left},${it.top},${it.right},${it.bottom}"
                }.orEmpty())
            }
        }
        return MessageDigest.getInstance("SHA-256")
            .digest(canonical.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }
}

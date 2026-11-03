package com.personalagent.android.accessibility
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import com.personalagent.core.*
import java.security.MessageDigest
import java.util.UUID

class AndroidAccessibilityObserver(
    private val serviceProvider: () -> AgentAccessibilityService? = AgentAccessibilityService::current,
) : DeviceObserver {
    private val snapshotLeases = SnapshotLeaseRegistry()

    override suspend fun observe(): DeviceSnapshot = observeNow()

    /** Re-observes the foreground tree and verifies that the leased semantic screen is unchanged. */
    fun isSnapshotCurrent(expectedSnapshotId: SnapshotId): Boolean {
        val current = runCatching { observeNow() }.getOrNull() ?: return false
        return snapshotLeases.matches(expectedSnapshotId, current, SystemClock.elapsedRealtime())
    }

    /** Bounded synchronous snapshot for the AccessibilityService event worker. */
    fun observeNow(): DeviceSnapshot {
        val service = serviceProvider() ?: error("Accessibility service is not enabled")
        val activeRoot = service.rootInActiveWindow ?: error("No accessible active window")
        val root = AccessibilityNodeInfo.obtain(activeRoot)
        activeRoot.recycle()
        val nodes = ArrayList<UiNodeSnapshot>(MAX_NODES)
        traverse(root, nodes)
        if (nodes.isEmpty()) error("Active window exposed no semantic nodes; refusing opaque-window actions")
        val snapshot = DeviceSnapshot(
            id = SnapshotId(UUID.randomUUID().toString()),
            capturedAtElapsedMillis = SystemClock.elapsedRealtime(),
            foregroundPackage = nodes.first().packageName,
            nodes = nodes, complete = nodes.size < MAX_NODES,
            source = EvidenceSource.ACCESSIBILITY_TREE,
        ).redacted()
        snapshotLeases.remember(snapshot)
        return snapshot
    }
    private fun traverse(root: AccessibilityNodeInfo, out: MutableList<UiNodeSnapshot>) {
        val stack = ArrayDeque<AccessibilityNodeInfo>()
        stack.add(root)
        try {
            while (stack.isNotEmpty() && out.size < MAX_NODES) {
                val node = stack.removeLast()
                try {
                    val rect = android.graphics.Rect().also { node.getBoundsInScreen(it) }
                    val pkg = node.packageName?.toString() ?: continue
                    val text = node.text?.toString()
                    val description = node.contentDescription?.toString()
                    val password = node.isPassword
                    val viewId = node.viewIdResourceName
                    // Never incorporate password contents or descriptions into a target identifier.
                    val stable = digest(
                        pkg + "|" + viewId + "|" + node.className + "|" +
                            (if (password) "" else text.orEmpty()) + "|" +
                            (if (password) "" else description.orEmpty()) + "|" +
                            node.isClickable + "|" + node.isEditable + "|" + password + "|" + rect,
                    )
                    out += UiNodeSnapshot(stable, text?.take(500),
                        description?.take(500), viewId, pkg,
                        node.isClickable, node.isEditable, password,
                        ScreenBounds(rect.left, rect.top, rect.right, rect.bottom))
                    for (i in node.childCount - 1 downTo 0) node.getChild(i)?.let(stack::add)
                } finally { node.recycle() }
            }
        } finally {
            stack.forEach { it.recycle() }; stack.clear()
        }
    }
    companion object {
        private const val MAX_NODES = 250
        private fun digest(value: String): String =
            MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
                .take(12).joinToString("") { "%02x".format(it) }
    }
}

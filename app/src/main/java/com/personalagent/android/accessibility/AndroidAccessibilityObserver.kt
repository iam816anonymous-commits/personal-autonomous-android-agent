package com.personalagent.android.accessibility
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import com.personalagent.core.*
import java.security.MessageDigest
import java.util.UUID

class AndroidAccessibilityObserver(
    private val serviceProvider: () -> AgentAccessibilityService? = AgentAccessibilityService::current,
) : DeviceObserver {
    override suspend fun observe(): DeviceSnapshot = observeNow()

    /** Bounded synchronous snapshot for the AccessibilityService event worker. */
    fun observeNow(): DeviceSnapshot {
        val service = serviceProvider() ?: error("Accessibility service is not enabled")
        val activeRoot = service.rootInActiveWindow ?: error("No accessible active window")
        val root = AccessibilityNodeInfo.obtain(activeRoot)
        activeRoot.recycle()
        val nodes = ArrayList<UiNodeSnapshot>(MAX_NODES)
        traverse(root, nodes)
        if (nodes.isEmpty()) error("Active window exposed no semantic nodes; refusing opaque-window actions")
        return DeviceSnapshot(
            id = SnapshotId(UUID.randomUUID().toString()),
            capturedAtElapsedMillis = SystemClock.elapsedRealtime(),
            foregroundPackage = nodes.first().packageName,
            nodes = nodes, complete = nodes.size < MAX_NODES,
            source = EvidenceSource.ACCESSIBILITY_TREE,
        ).redacted()
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
                    val viewId = node.viewIdResourceName
                    val stable = digest(pkg + "|" + viewId + "|" + node.className + "|" + text + "|" + rect)
                    out += UiNodeSnapshot(stable, text?.take(500),
                        node.contentDescription?.toString()?.take(500), viewId, pkg,
                        node.isClickable, node.isEditable, node.isPassword,
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

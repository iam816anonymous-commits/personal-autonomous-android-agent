package com.personalagent.android.accessibility
import android.content.Intent
import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo
import com.personalagent.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class AndroidActionDispatcher(
    private val serviceProvider: () -> AgentAccessibilityService? = AgentAccessibilityService::current,
) : AgentActionDispatcher {
    override suspend fun dispatch(action: AgentAction): ActionExecutionResult = withContext(Dispatchers.Main) {
        when (action) {
            is LaunchApplication -> launch(action)
            is ClickAccessibleNode -> click(action)
            is SetAccessibleText -> setText(action)
            is ReadForegroundState -> ActionExecutionResult.Dispatched("Fresh observation required by verifier")
        }
    }
    private fun launch(action: LaunchApplication): ActionExecutionResult {
        val service = serviceProvider() ?: return ActionExecutionResult.Rejected("Accessibility service disabled")
        val intent = service.packageManager.getLaunchIntentForPackage(action.packageName)
            ?: return ActionExecutionResult.Rejected("No launch intent for package")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        service.startActivity(intent)
        return ActionExecutionResult.Dispatched("Launch intent submitted; foreground state must be verified")
    }
    private fun click(action: ClickAccessibleNode): ActionExecutionResult {
        val service = serviceProvider() ?: return ActionExecutionResult.Rejected("Accessibility service disabled")
        val active = service.rootInActiveWindow ?: return ActionExecutionResult.Rejected("No active root")
        val root = AccessibilityNodeInfo.obtain(active); active.recycle()
        return try {
            val node = findByStableId(root, action.nodeId)
                ?: return ActionExecutionResult.Rejected("Target node is stale; refresh snapshot")
            try {
                if (!node.isClickable || node.isPassword) return ActionExecutionResult.Rejected("Target is not an allowed clickable node")
                if (node.packageName?.toString() != action.targetPackageName) return ActionExecutionResult.Rejected("Target package changed")
                if (node.performAction(AccessibilityNodeInfo.ACTION_CLICK))
                    ActionExecutionResult.Dispatched("Click accepted; independent verification required")
                else ActionExecutionResult.Failed("Accessibility click rejected", false)
            } finally { node.recycle() }
        } finally { root.recycle() }
    }
    private fun setText(action: SetAccessibleText): ActionExecutionResult {
        val service = serviceProvider() ?: return ActionExecutionResult.Rejected("Accessibility service disabled")
        val active = service.rootInActiveWindow ?: return ActionExecutionResult.Rejected("No active root")
        val root = AccessibilityNodeInfo.obtain(active); active.recycle()
        return try {
            val node = findByStableId(root, action.nodeId)
                ?: return ActionExecutionResult.Rejected("Target node is stale; refresh snapshot")
            try {
                if (!node.isEditable || node.isPassword) return ActionExecutionResult.Rejected("Target is not an allowed editable node")
                if (node.packageName?.toString() != action.targetPackageName) return ActionExecutionResult.Rejected("Target package changed")
                val args = Bundle().apply { putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, action.text) }
                if (node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args))
                    ActionExecutionResult.Dispatched("Text action accepted; independent verification required")
                else ActionExecutionResult.Failed("Accessibility text action rejected", false)
            } finally { node.recycle() }
        } finally { root.recycle() }
    }
    private fun findByStableId(root: AccessibilityNodeInfo, expected: String): AccessibilityNodeInfo? {
        val stack = ArrayDeque<AccessibilityNodeInfo>()
        stack.add(AccessibilityNodeInfo.obtain(root))
        try {
            while (stack.isNotEmpty()) {
                val node = stack.removeLast()
                val rect = android.graphics.Rect().also { node.getBoundsInScreen(it) }
                val pkg = node.packageName?.toString().orEmpty()
                val value = digest(pkg + "|" + node.viewIdResourceName + "|" + node.className + "|" + node.text + "|" + rect)
                if (value == expected) {
                    stack.forEach { it.recycle() }; stack.clear(); return node
                }
                for (i in 0 until node.childCount) node.getChild(i)?.let(stack::add)
                node.recycle()
            }
            return null
        } finally { stack.forEach { it.recycle() }; stack.clear() }
    }
    private fun digest(value: String): String =
        MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
            .take(12).joinToString("") { "%02x".format(it) }
}

package com.personalagent.android.accessibility
import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import java.lang.ref.WeakReference
class AgentAccessibilityService : AccessibilityService() {
    override fun onServiceConnected() { super.onServiceConnected(); active = WeakReference(this) }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
    override fun onInterrupt() = Unit
    override fun onDestroy() { if (active?.get() === this) active = null; super.onDestroy() }
    companion object {
        @Volatile private var active: WeakReference<AgentAccessibilityService>? = null
        fun current(): AgentAccessibilityService? = active?.get()
    }
}

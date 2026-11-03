package com.personalagent.android.capability
import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import android.text.TextUtils
import com.personalagent.android.accessibility.AgentAccessibilityService
import com.personalagent.core.*

class AndroidCapabilityRegistry(private val context: Context) {
    fun discover(): Map<CapabilityId, CapabilityStatus> {
        val enabled = isAccessibilityEnabled()
        val running = AgentAccessibilityService.current() != null
        val now = SystemClock.elapsedRealtime()
        val click = CapabilityId("accessibility.click")
        val observe = CapabilityId("device.observe")
        val launch = CapabilityId("app.launch")
        fun accessibility(id: CapabilityId) = CapabilityStatus(
            id, null, Build.VERSION.SDK_INT >= 21, enabled, running, now,
            if (enabled && running) null else "Enable the agent Accessibility service in Settings")
        return mapOf(click to accessibility(click), observe to accessibility(observe),
            launch to CapabilityStatus(launch, null, true, true, true, now))
    }
    private fun isAccessibilityEnabled(): Boolean {
        val expected = context.packageName + "/" + AgentAccessibilityService::class.java.name
        val setting = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            ?: return false
        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(setting)
        return splitter.any { it.equals(expected, ignoreCase = true) }
    }
}

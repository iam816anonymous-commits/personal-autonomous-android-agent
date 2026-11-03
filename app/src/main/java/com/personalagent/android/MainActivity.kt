package com.personalagent.android

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.accessibilityservice.AccessibilityServiceInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val info = TextView(this).apply {
            text = "Personal Autonomous Agent\n\nEnable the Accessibility service only when you want the agent to observe and interact with visible interfaces. Protected actions require policy approval."
            textSize = 18f
            setPadding(32, 48, 32, 24)
        }
        val button = Button(this).apply {
            text = "Open Accessibility Settings"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        }
        val status = TextView(this).apply { text = serviceStatus(); setPadding(32, 24, 32, 24) }
        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(info); addView(button); addView(status)
        })
    }
    private fun serviceStatus(): String {
        val manager = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabled = manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            .any { it.resolveInfo.serviceInfo.packageName == packageName &&
                it.resolveInfo.serviceInfo.name == "com.personalagent.android.accessibility.AgentAccessibilityService" }
        return if (enabled) "Accessibility service: enabled" else "Accessibility service: disabled"
    }
}

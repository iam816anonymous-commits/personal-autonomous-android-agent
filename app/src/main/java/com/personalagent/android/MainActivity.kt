package com.personalagent.android

import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.personalagent.android.rea.AndroidReverseEngineeringController
import com.personalagent.core.AppBehaviorModel

class MainActivity : AppCompatActivity() {
    private lateinit var status: TextView
    private lateinit var appPicker: Spinner
    private lateinit var reaController: AndroidReverseEngineeringController
    private var selectedPackages: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reaController = (application as AgentApplication).reaController

        val heading = TextView(this).apply {
            text = "Personal Autonomous Agent"
            textSize = 22f
            setPadding(24, 32, 24, 12)
        }
        val explanation = TextView(this).apply {
            text = "App inspection (REA) observes visible accessibility structure only. Start an inspection, open the selected app, explore it manually, then return here to finish. Password fields and sensitive values are redacted."
            textSize = 14f
            setPadding(24, 8, 24, 20)
        }
        val settingsButton = Button(this).apply {
            text = "Open Accessibility Settings"
            setOnClickListener { startActivity(Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        }
        appPicker = Spinner(this)
        val refreshButton = Button(this).apply {
            text = "Refresh installed apps"
            setOnClickListener { loadLaunchableApps() }
        }
        val startButton = Button(this).apply {
            text = "Start inspection and open app"
            setOnClickListener { startInspection() }
        }
        val finishButton = Button(this).apply {
            text = "Finish inspection"
            setOnClickListener { finishInspection() }
        }
        val cancelButton = Button(this).apply {
            text = "Cancel inspection"
            setOnClickListener {
                reaController.cancel()
                status.text = "Inspection cancelled. No skill was created."
            }
        }
        status = TextView(this).apply {
            textSize = 14f
            setPadding(24, 20, 24, 32)
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 8, 16, 16)
            addView(heading)
            addView(explanation)
            addView(settingsButton)
            addView(appPicker)
            addView(refreshButton)
            addView(startButton)
            addView(finishButton)
            addView(cancelButton)
            addView(status)
        }
        setContentView(ScrollView(this).apply { addView(content) })
        loadLaunchableApps()
        updateStatus()
    }

    override fun onResume() {
        super.onResume()
        if (::status.isInitialized) updateStatus()
    }

    private fun loadLaunchableApps() {
        val query = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps: List<ResolveInfo> = packageManager.queryIntentActivities(query, 0)
            .filter { it.activityInfo.packageName != packageName }
            .sortedBy { it.loadLabel(packageManager).toString().lowercase() }
        selectedPackages = apps.map { it.activityInfo.packageName }
        val labels = apps.map { "${it.loadLabel(packageManager)}  ·  ${it.activityInfo.packageName}" }
        appPicker.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, labels)
            .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        status.text = "Found ${apps.size} launchable apps. Accessibility service: ${accessibilityEnabled()}."
    }

    private fun startInspection() {
        val index = appPicker.selectedItemPosition
        val target = selectedPackages.getOrNull(index)
        if (target == null) {
            Toast.makeText(this, "No launchable app selected", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val identity = reaController.begin(target)
            val launch = packageManager.getLaunchIntentForPackage(target)
                ?: error("Selected app has no launch intent")
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(launch)
            status.text = "Inspecting ${identity.packageName} (${identity.versionName ?: "unknown version"}). Explore the app normally. Return here and finish the inspection."
        } catch (failure: Exception) {
            reaController.cancel()
            status.text = "Could not start inspection: ${failure.message ?: failure.javaClass.simpleName}"
        }
    }

    private fun finishInspection() {
        if (!reaController.isActive()) {
            status.text = "No inspection is active."
            return
        }
        try {
            val model = reaController.finish()
            status.text = formatSummary(model)
        } catch (failure: Exception) {
            status.text = "Inspection could not be finalized: ${failure.message ?: failure.javaClass.simpleName}"
        }
    }

    private fun formatSummary(model: AppBehaviorModel): String = buildString {
        appendLine("Inspection captured")
        appendLine("App: ${model.app.packageName}")
        appendLine("Version: ${model.app.versionName ?: "unknown"}")
        appendLine("Screens discovered: ${model.observedScreens.size}")
        appendLine("Screen transitions: ${model.transitions.size}")
        appendLine("Verified interaction probes: ${model.probes.size}")
        appendLine("Limitations: ${model.limitations.size}")
        model.limitations.take(8).forEach { appendLine("• $it") }
        if (model.observedScreens.isEmpty()) {
            appendLine("No screens were captured. Confirm Accessibility is enabled and the target app exposes an accessibility tree.")
        }
        appendLine("No reusable skill was generated automatically. Certification requires verified probe evidence and explicit user approval.")
    }

    private fun accessibilityEnabled(): Boolean {
        val manager = getSystemService(ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager
        return manager.getEnabledAccessibilityServiceList(
            android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK,
        ).any { it.resolveInfo.serviceInfo.packageName == packageName }
    }

    private fun updateStatus() {
        if (!reaController.isActive()) {
            status.text = "Ready. Accessibility service: ${if (accessibilityEnabled()) "enabled" else "disabled"}. Select an app to inspect."
        }
    }
}

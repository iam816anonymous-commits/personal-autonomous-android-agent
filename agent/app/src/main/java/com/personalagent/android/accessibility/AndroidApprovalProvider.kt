package com.personalagent.android.accessibility

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.personalagent.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/** Human confirmation gate. Must be invoked from a visible Activity lifecycle scope. */
class AndroidApprovalProvider(
    private val activity: AppCompatActivity,
    private val policy: ApprovalPolicy,
) : ApprovalProvider {
    override suspend fun request(action: AgentAction, snapshot: DeviceSnapshot): ApprovalToken? =
        withContext(Dispatchers.Main.immediate) {
            if (activity.isFinishing || activity.isDestroyed) return@withContext null
            suspendCancellableCoroutine { continuation ->
                val dialog = AlertDialog.Builder(activity)
                    .setTitle("Approve device action?")
                    .setMessage(description(action))
                    .setNegativeButton("Deny") { _, _ ->
                        if (continuation.isActive) continuation.resume(null)
                    }
                    .setPositiveButton("Approve") { _, _ ->
                        if (continuation.isActive) {
                            continuation.resume(policy.issueUserApproval(action, snapshot))
                        }
                    }
                    .setOnCancelListener {
                        if (continuation.isActive) continuation.resume(null)
                    }
                    .create()
                continuation.invokeOnCancellation { dialog.dismiss() }
                dialog.show()
            }
        }

    private fun description(action: AgentAction): String = when (action) {
        is LaunchApplication -> "Open application: " + action.packageName
        is ClickAccessibleNode -> "Click a visible control in " + action.targetPackageName
        is SetAccessibleText -> "Enter this text in " + action.targetPackageName + ":\n\n" + action.text
        is ReadForegroundState -> "Read the current visible interface"
    }
}

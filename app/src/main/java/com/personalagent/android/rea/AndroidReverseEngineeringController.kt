package com.personalagent.android.rea

import android.content.Context
import android.os.Build
import com.personalagent.android.accessibility.AndroidAccessibilityObserver
import com.personalagent.core.*
import java.util.concurrent.atomic.AtomicReference

/**
 * App-scoped REA bridge. Start/stop must be initiated by the app UI after the user chooses the
 * package to inspect. This controller only captures permitted semantic UI observations.
 */
class AndroidReverseEngineeringController(
    context: Context,
    private val observer: AndroidAccessibilityObserver = AndroidAccessibilityObserver(),
) {
    private val appContext = context.applicationContext
    private val active = AtomicReference<ReverseEngineeringSession?>(null)

    fun begin(packageName: String): AppIdentity {
        check(active.get() == null) { "An REA session is already active" }
        val packageInfo = appContext.packageManager.getPackageInfo(packageName, 0)
        val identity = AppIdentity(
            packageName = packageName,
            versionName = packageInfo.versionName,
            versionCode = packageInfo.versionCode.toLong(),
            sdkInt = Build.VERSION.SDK_INT,
        )
        check(active.compareAndSet(null, ReverseEngineeringSession(identity))) {
            "Another REA session started concurrently"
        }
        return identity
    }

    suspend fun captureCurrentScreen(): Int {
        val session = active.get() ?: error("Start an REA session first")
        val snapshot = observer.observe().redacted()
        return session.observe(snapshot) ?: error(
            "Current app is not observable in the foreground or its accessibility snapshot is incomplete",
        )
    }

    fun recordVerifiedProbe(probe: InteractionProbe) {
        val session = active.get() ?: error("Start an REA session first")
        require(probe.verification is VerificationResult.Verified) {
            "Only independently verified probes can be recorded as successful REA evidence"
        }
        session.recordProbe(probe)
    }

    fun addLimitation(reason: String) {
        active.get()?.addLimitation(reason) ?: error("Start an REA session first")
    }

    fun finish(): AppBehaviorModel {
        val session = active.getAndSet(null) ?: error("No REA session is active")
        return session.finish()
    }

    fun cancel() {
        active.set(null)
    }

    fun isActive(): Boolean = active.get() != null
}

package com.personalagent.android.rea

import android.content.Context
import android.os.Build
import android.os.SystemClock
import com.personalagent.android.accessibility.AgentAccessibilityService
import com.personalagent.android.accessibility.AndroidAccessibilityObserver
import com.personalagent.core.*
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

/**
 * App-scoped REA bridge. The selected app is observed passively through user-enabled
 * AccessibilityService events while it is foregrounded. REA never dispatches probes itself.
 */
class AndroidReverseEngineeringController(
    context: Context,
    private val observer: AndroidAccessibilityObserver = AndroidAccessibilityObserver(),
) {
    private val appContext = context.applicationContext
    private val active = AtomicReference<ReverseEngineeringSession?>(null)
    private val captureExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "rea-observer").apply { isDaemon = true }
    }
    private val lastCaptureAt = AtomicLong(0L)
    private val captureLock = Any()

    fun begin(packageName: String): AppIdentity {
        check(AgentAccessibilityService.current() != null) {
            "Enable the Personal Agent Accessibility service before inspecting an app"
        }
        check(active.get() == null) { "An REA session is already active" }
        val packageInfo = appContext.packageManager.getPackageInfo(packageName, 0)
        val identity = AppIdentity(
            packageName = packageName,
            versionName = packageInfo.versionName,
            versionCode = packageInfo.versionCode.toLong(),
            sdkInt = Build.VERSION.SDK_INT,
        )
        val session = ReverseEngineeringSession(identity)
        check(active.compareAndSet(null, session)) { "Another REA session started concurrently" }
        lastCaptureAt.set(0L)
        AgentAccessibilityService.setEventListener { foregroundPackage ->
            if (foregroundPackage != identity.packageName) return@setEventListener
            val now = SystemClock.elapsedRealtime()
            val previous = lastCaptureAt.get()
            if (now - previous < MIN_CAPTURE_INTERVAL_MS || !lastCaptureAt.compareAndSet(previous, now)) {
                return@setEventListener
            }
            captureExecutor.execute {
                synchronized(captureLock) {
                    if (active.get() !== session) return@synchronized
                    runCatching { session.observe(observer.observeNow()) }
                        .onFailure { failure ->
                            runCatching {
                                session.addLimitation(
                                    "Accessibility capture failed: ${failure.message ?: failure.javaClass.simpleName}",
                                )
                            }
                        }
                }
            }
        }
        return identity
    }

    /** Manual capture is useful when the selected app is already foregrounded. */
    suspend fun captureCurrentScreen(): Int {
        val session = active.get() ?: error("Start an REA session first")
        val snapshot = observer.observe().redacted()
        return session.observe(snapshot) ?: error(
            "Selected app is not foregrounded or its accessibility snapshot is incomplete",
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

    fun finish(): AppBehaviorModel = synchronized(captureLock) {
        val session = active.getAndSet(null) ?: error("No REA session is active")
        AgentAccessibilityService.setEventListener(null)
        session.finish()
    }

    fun cancel() = synchronized(captureLock) {
        active.set(null)
        AgentAccessibilityService.setEventListener(null)
    }

    fun isActive(): Boolean = active.get() != null

    companion object {
        private const val MIN_CAPTURE_INTERVAL_MS = 300L
    }
}

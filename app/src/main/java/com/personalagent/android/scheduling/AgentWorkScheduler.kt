package com.personalagent.android.scheduling
import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class AgentWorkScheduler(context: Context) {
    private val manager = WorkManager.getInstance(context.applicationContext)
    fun enqueue(jobId: String, delayMillis: Long) {
        require(jobId.matches(Regex("[A-Za-z0-9_.-]{1,80}")) && delayMillis >= 0)
        val request = OneTimeWorkRequestBuilder<AgentScheduledWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS).addTag("agent-job:" + jobId).build()
        manager.enqueueUniqueWork("agent-job:" + jobId, ExistingWorkPolicy.KEEP, request)
    }
    fun cancel(jobId: String) {
        require(jobId.matches(Regex("[A-Za-z0-9_.-]{1,80}")))
        manager.cancelUniqueWork("agent-job:" + jobId)
    }
}
/** Worker only emits a due signal; it never executes model-generated actions. */
class AgentScheduledWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val jobId = tags.firstOrNull { it.startsWith("agent-job:") }?.removePrefix("agent-job:")
            ?: return Result.failure()
        applicationContext.sendBroadcast(Intent(ACTION_JOB_DUE).setPackage(applicationContext.packageName)
            .putExtra(EXTRA_JOB_ID, jobId))
        return Result.success()
    }
    companion object {
        const val ACTION_JOB_DUE = "com.personalagent.android.action.JOB_DUE"
        const val EXTRA_JOB_ID = "job_id"
    }
}

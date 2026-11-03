package com.personalagent.core

enum class ScheduledJobState { ENQUEUED, RUNNING, SUCCEEDED, FAILED, CANCELLED, EXPIRED }

data class ScheduledAgentJob(
    val id: String,
    val taskGoal: String,
    val runAtEpochMillis: Long,
    val expiresAtEpochMillis: Long,
    val state: ScheduledJobState,
    val uniqueWorkName: String,
) {
    init {
        require(id.isNotBlank() && uniqueWorkName.isNotBlank())
        require(taskGoal.isNotBlank() && taskGoal.length <= 2_000)
        require(runAtEpochMillis >= 0 && expiresAtEpochMillis > runAtEpochMillis)
    }
}

/**
 * Android implementation: WorkManager for deferrable durable work and AlarmManager only where
 * exact timing is justified and the OS permits it. The core never starts a background thread.
 */
interface ScheduledJobStore {
    suspend fun insert(job: ScheduledAgentJob): Boolean
    suspend fun updateState(id: String, state: ScheduledJobState): Boolean
    suspend fun find(id: String): ScheduledAgentJob?
    suspend fun due(nowEpochMillis: Long, limit: Int = 10): List<ScheduledAgentJob>
}

interface ScheduledJobEnqueuer {
    suspend fun enqueueUnique(job: ScheduledAgentJob)
    suspend fun cancelUnique(uniqueWorkName: String)
}

class ScheduledJobCoordinator(
    private val store: ScheduledJobStore,
    private val enqueuer: ScheduledJobEnqueuer,
) {
    suspend fun schedule(job: ScheduledAgentJob): Boolean {
        require(job.state == ScheduledJobState.ENQUEUED)
        if (!store.insert(job)) return false
        enqueuer.enqueueUnique(job)
        return true
    }

    suspend fun cancel(id: String): Boolean {
        val job = store.find(id) ?: return false
        if (job.state == ScheduledJobState.RUNNING ||
            job.state == ScheduledJobState.SUCCEEDED ||
            job.state == ScheduledJobState.CANCELLED
        ) return false
        enqueuer.cancelUnique(job.uniqueWorkName)
        return store.updateState(id, ScheduledJobState.CANCELLED)
    }

    suspend fun markExpired(nowEpochMillis: Long) {
        store.due(nowEpochMillis, 100).filter { it.expiresAtEpochMillis <= nowEpochMillis }
            .forEach { store.updateState(it.id, ScheduledJobState.EXPIRED) }
    }
}

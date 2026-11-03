package com.personalagent.core

/** Durable lifecycle for a user goal, independent from individual action attempts. */
enum class TaskSessionState {
    CREATED, RUNNING, AWAITING_APPROVAL, RECOVERY_REQUIRED, COMPLETED, FAILED, CANCELLED
}

enum class TaskInputModality { TEXT, USER_STARTED_VOICE }

data class TaskSessionRecord(
    val taskId: TaskId,
    val goal: String,
    val inputModality: TaskInputModality,
    val state: TaskSessionState,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val terminalReason: String? = null,
) {
    init {
        require(goal.isNotBlank() && goal.length <= 2_000)
        require(createdAtEpochMillis >= 0)
        require(updatedAtEpochMillis >= createdAtEpochMillis)
        require(terminalReason == null || terminalReason.length <= 1_000)
    }
}

sealed interface TaskSessionDecision {
    data class Accept(val record: TaskSessionRecord) : TaskSessionDecision
    data class Reject(val reason: String) : TaskSessionDecision
}

/** Enforces legal task lifecycle transitions and monotonic wall-clock timestamps. */
object TaskSessionProtocol {
    private val allowed = mapOf(
        TaskSessionState.CREATED to setOf(TaskSessionState.RUNNING, TaskSessionState.RECOVERY_REQUIRED, TaskSessionState.CANCELLED, TaskSessionState.FAILED),
        TaskSessionState.RUNNING to setOf(TaskSessionState.AWAITING_APPROVAL, TaskSessionState.RECOVERY_REQUIRED,
            TaskSessionState.COMPLETED, TaskSessionState.FAILED, TaskSessionState.CANCELLED),
        TaskSessionState.AWAITING_APPROVAL to setOf(TaskSessionState.RUNNING, TaskSessionState.CANCELLED, TaskSessionState.FAILED),
        TaskSessionState.RECOVERY_REQUIRED to setOf(TaskSessionState.RUNNING, TaskSessionState.COMPLETED,
            TaskSessionState.FAILED, TaskSessionState.CANCELLED),
        TaskSessionState.COMPLETED to emptySet(),
        TaskSessionState.FAILED to emptySet(),
        TaskSessionState.CANCELLED to emptySet(),
    )

    fun transition(
        current: TaskSessionRecord,
        next: TaskSessionState,
        nowEpochMillis: Long,
        reason: String? = null,
    ): TaskSessionDecision {
        if (nowEpochMillis < current.updatedAtEpochMillis) {
            return TaskSessionDecision.Reject("Wall clock moved backwards")
        }
        if (next !in allowed.getValue(current.state)) {
            return TaskSessionDecision.Reject("Illegal task transition ${current.state} -> $next")
        }
        if (next in TERMINAL && reason.isNullOrBlank()) {
            return TaskSessionDecision.Reject("Terminal task state requires a reason")
        }
        return TaskSessionDecision.Accept(current.copy(
            state = next,
            updatedAtEpochMillis = nowEpochMillis,
            terminalReason = reason?.take(1_000),
        ))
    }

    fun interrupted(record: TaskSessionRecord, nowEpochMillis: Long): TaskSessionDecision =
        when (record.state) {
            TaskSessionState.CREATED, TaskSessionState.RUNNING, TaskSessionState.AWAITING_APPROVAL ->
                transition(record, TaskSessionState.RECOVERY_REQUIRED, maxOf(nowEpochMillis, record.updatedAtEpochMillis),
                    "App process restarted; task requires state reconciliation")
            else -> TaskSessionDecision.Accept(record)
        }

    private val TERMINAL = setOf(TaskSessionState.COMPLETED, TaskSessionState.FAILED, TaskSessionState.CANCELLED)
}

interface TaskSessionJournal {
    suspend fun create(record: TaskSessionRecord): Boolean
    suspend fun transition(taskId: TaskId, next: TaskSessionState, nowEpochMillis: Long, reason: String? = null): TaskSessionDecision
    suspend fun get(taskId: TaskId): TaskSessionRecord?
    suspend fun loadActive(): List<TaskSessionRecord>
    suspend fun reconcileAfterProcessRestart(nowEpochMillis: Long): List<TaskSessionRecord>
}

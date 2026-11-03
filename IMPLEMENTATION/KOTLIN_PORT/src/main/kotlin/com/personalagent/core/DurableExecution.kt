package com.personalagent.core

/**
 * Platform-neutral durable execution protocol.
 *
 * Android persistence must implement this protocol with Room transactions. The protocol is
 * deliberately split around dispatch: an intent is committed before calling Android APIs.
 */
enum class DispatchState {
    PREPARED,
    DISPATCHING,
    DISPATCHED,
    REJECTED,
    OUTCOME_UNKNOWN,
    VERIFIED,
    FAILED,
}

data class DurableActionRecord(
    val taskId: TaskId,
    val actionId: ActionId,
    val actionKind: String,
    val targetPackage: String?,
    val snapshotId: SnapshotId?,
    val idempotency: Idempotency,
    val dispatchState: DispatchState,
    val attempt: Int,
    val createdAtElapsedMillis: Long,
    val updatedAtElapsedMillis: Long,
    val resultNote: String? = null,
) {
    init {
        require(actionKind.matches(Regex("[a-z][a-z0-9_]{1,63}")))
        require(attempt in 1..2)
        require(createdAtElapsedMillis >= 0)
        require(updatedAtElapsedMillis >= createdAtElapsedMillis)
        require(resultNote == null || resultNote.length <= 1_000)
    }
}

sealed interface JournalDecision {
    data class Accept(val record: DurableActionRecord) : JournalDecision
    data class Reject(val reason: String) : JournalDecision
}

object DurableActionProtocol {
    private val allowed = mapOf(
        DispatchState.PREPARED to setOf(DispatchState.DISPATCHING, DispatchState.REJECTED, DispatchState.FAILED),
        DispatchState.DISPATCHING to setOf(DispatchState.DISPATCHED, DispatchState.REJECTED, DispatchState.OUTCOME_UNKNOWN, DispatchState.FAILED),
        DispatchState.DISPATCHED to setOf(DispatchState.VERIFIED, DispatchState.OUTCOME_UNKNOWN, DispatchState.FAILED),
        DispatchState.OUTCOME_UNKNOWN to setOf(DispatchState.VERIFIED, DispatchState.FAILED),
        DispatchState.REJECTED to emptySet(),
        DispatchState.VERIFIED to emptySet(),
        DispatchState.FAILED to emptySet(),
    )

    fun transition(
        current: DurableActionRecord,
        next: DispatchState,
        nowElapsedMillis: Long,
        note: String? = null,
    ): JournalDecision {
        if (nowElapsedMillis < current.updatedAtElapsedMillis) {
            return JournalDecision.Reject("Monotonic time moved backwards")
        }
        if (next !in allowed.getValue(current.dispatchState)) {
            return JournalDecision.Reject("Illegal dispatch transition ${current.dispatchState} -> $next")
        }
        if (current.dispatchState == DispatchState.DISPATCHING &&
            next == DispatchState.DISPATCHED &&
            current.idempotency == Idempotency.NON_IDEMPOTENT &&
            note.isNullOrBlank()
        ) {
            return JournalDecision.Reject("Non-idempotent dispatch requires a durable dispatch receipt")
        }
        return JournalDecision.Accept(
            current.copy(
                dispatchState = next,
                updatedAtElapsedMillis = nowElapsedMillis,
                resultNote = note?.take(1_000),
            ),
        )
    }

    /**
     * A process death while DISPATCHING is ambiguous. It is never safe to replay automatically.
     * Reconciliation must observe the device and verify the original postcondition.
     */
    fun recoverAfterProcessDeath(record: DurableActionRecord): JournalDecision =
        when (record.dispatchState) {
            DispatchState.PREPARED -> JournalDecision.Accept(record.copy(dispatchState = DispatchState.FAILED,
                resultNote = "Process died before dispatch began; safe to create a new attempt after revalidation"))
            DispatchState.DISPATCHING, DispatchState.DISPATCHED ->
                JournalDecision.Accept(record.copy(dispatchState = DispatchState.OUTCOME_UNKNOWN,
                    resultNote = "Dispatch outcome is uncertain; observe and verify before any retry"))
            DispatchState.OUTCOME_UNKNOWN -> JournalDecision.Accept(record)
            DispatchState.REJECTED, DispatchState.VERIFIED, DispatchState.FAILED -> JournalDecision.Accept(record)
        }

    fun mayAutomaticallyRetry(record: DurableActionRecord, verifiedNotApplied: Boolean): Boolean =
        when (record.dispatchState) {
            DispatchState.REJECTED -> true
            DispatchState.FAILED -> verifiedNotApplied &&
                record.idempotency != Idempotency.NON_IDEMPOTENT && record.attempt < 2
            else -> false
        }
}

/**
 * Implementations must persist each accepted transition atomically and enforce unique task/action
 * identity. Room implementations should use a database transaction, not append-only chat logs.
 */
interface DurableExecutionJournal {
    suspend fun insertPrepared(record: DurableActionRecord): Boolean
    suspend fun transition(taskId: TaskId, actionId: ActionId, next: DispatchState,
                           nowElapsedMillis: Long, note: String? = null): JournalDecision
    suspend fun loadRecoverable(): List<DurableActionRecord>
}

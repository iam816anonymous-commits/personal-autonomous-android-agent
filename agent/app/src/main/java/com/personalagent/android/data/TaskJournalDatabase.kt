package com.personalagent.android.data

import androidx.room.*
import androidx.room.withTransaction
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.personalagent.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Entity(tableName = "task_sessions", indices = [Index(value = ["state", "updatedAtEpochMillis"])])
data class TaskSessionEntity(
    @PrimaryKey val taskId: String,
    val goalCiphertext: ByteArray,
    val inputModality: String,
    val state: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val terminalReasonCiphertext: ByteArray?,
)

@Entity(tableName = "task_steps", indices = [Index(value = ["taskId", "startedAtElapsedMillis"])])
data class TaskStepEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val taskId: String,
    val actionId: String,
    val state: String,
    val startedAtElapsedMillis: Long,
    val finishedAtElapsedMillis: Long?,
    val noteCiphertext: ByteArray,
)

@Entity(
    tableName = "action_attempts",
    indices = [Index(value = ["taskId", "actionId"], unique = true),
        Index(value = ["dispatchState", "updatedAtElapsedMillis"])],
)
data class DurableActionEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val taskId: String,
    val actionId: String,
    val actionKind: String,
    val targetPackage: String?,
    val snapshotId: String?,
    val idempotency: String,
    val dispatchState: String,
    val attempt: Int,
    val createdAtElapsedMillis: Long,
    val updatedAtElapsedMillis: Long,
    val resultNoteCiphertext: ByteArray?,
)

@Dao
interface TaskSessionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(record: TaskSessionEntity): Long

    @Query("SELECT * FROM task_sessions WHERE taskId = :taskId LIMIT 1")
    suspend fun find(taskId: String): TaskSessionEntity?

    @Update
    suspend fun update(record: TaskSessionEntity): Int

    @Query("SELECT * FROM task_sessions WHERE state IN ('CREATED', 'RUNNING', 'AWAITING_APPROVAL', 'RECOVERY_REQUIRED') ORDER BY createdAtEpochMillis ASC")
    suspend fun active(): List<TaskSessionEntity>
}

@Dao
interface TaskStepDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(step: TaskStepEntity): Long

    @Query("SELECT * FROM task_steps WHERE taskId = :taskId ORDER BY rowId ASC")
    suspend fun byTask(taskId: String): List<TaskStepEntity>

    @Query("SELECT * FROM task_steps ORDER BY rowId DESC LIMIT :limit")
    suspend fun latest(limit: Int): List<TaskStepEntity>
}

@Dao
interface DurableActionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(record: DurableActionEntity): Long

    @Query("SELECT * FROM action_attempts WHERE taskId = :taskId AND actionId = :actionId LIMIT 1")
    suspend fun find(taskId: String, actionId: String): DurableActionEntity?

    @Update
    suspend fun update(record: DurableActionEntity): Int

    @Query("""
        SELECT * FROM action_attempts
        WHERE dispatchState IN ('PREPARED', 'DISPATCHING', 'DISPATCHED', 'OUTCOME_UNKNOWN')
        ORDER BY createdAtElapsedMillis ASC
    """)
    suspend fun recoverable(): List<DurableActionEntity>
}

@Database(entities = [TaskSessionEntity::class, TaskStepEntity::class, DurableActionEntity::class], version = 3, exportSchema = true)
abstract class TaskJournalDatabase : RoomDatabase() {
    abstract fun taskSessionDao(): TaskSessionDao
    abstract fun taskStepDao(): TaskStepDao
    abstract fun durableActionDao(): DurableActionDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS action_attempts (
                        rowId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        taskId TEXT NOT NULL,
                        actionId TEXT NOT NULL,
                        actionKind TEXT NOT NULL,
                        targetPackage TEXT,
                        snapshotId TEXT,
                        idempotency TEXT NOT NULL,
                        dispatchState TEXT NOT NULL,
                        attempt INTEGER NOT NULL,
                        createdAtElapsedMillis INTEGER NOT NULL,
                        updatedAtElapsedMillis INTEGER NOT NULL,
                        resultNoteCiphertext BLOB
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_action_attempts_taskId_actionId ON action_attempts(taskId, actionId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_action_attempts_dispatchState_updatedAtElapsedMillis ON action_attempts(dispatchState, updatedAtElapsedMillis)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS task_sessions (
                        taskId TEXT NOT NULL PRIMARY KEY,
                        goalCiphertext BLOB NOT NULL,
                        inputModality TEXT NOT NULL,
                        state TEXT NOT NULL,
                        createdAtEpochMillis INTEGER NOT NULL,
                        updatedAtEpochMillis INTEGER NOT NULL,
                        terminalReasonCiphertext BLOB
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_task_sessions_state_updatedAtEpochMillis ON task_sessions(state, updatedAtEpochMillis)")
            }
        }
    }
}

class RoomTaskSessionJournal(
    private val database: TaskJournalDatabase,
    private val crypto: JournalCrypto,
) : TaskSessionJournal {
    override suspend fun create(record: TaskSessionRecord): Boolean = withContext(Dispatchers.IO) {
        require(record.state == TaskSessionState.CREATED) { "New task sessions must start in CREATED" }
        database.taskSessionDao().insert(record.toEntity()) != -1L
    }

    override suspend fun transition(
        taskId: TaskId,
        next: TaskSessionState,
        nowEpochMillis: Long,
        reason: String?,
    ): TaskSessionDecision = withContext(Dispatchers.IO) {
        database.withTransaction {
            val dao = database.taskSessionDao()
            val current = dao.find(taskId.value)?.toRecord()
                ?: return@withTransaction TaskSessionDecision.Reject("Task session does not exist")
            val decision = TaskSessionProtocol.transition(current, next, nowEpochMillis, reason)
            if (decision is TaskSessionDecision.Accept) {
                val updated = dao.update(decision.record.toEntity())
                if (updated != 1) return@withTransaction TaskSessionDecision.Reject("Concurrent task update; reload before proceeding")
            }
            decision
        }
    }

    override suspend fun get(taskId: TaskId): TaskSessionRecord? = withContext(Dispatchers.IO) {
        database.taskSessionDao().find(taskId.value)?.toRecord()
    }

    override suspend fun loadActive(): List<TaskSessionRecord> = withContext(Dispatchers.IO) {
        database.taskSessionDao().active().map { it.toRecord() }
    }

    override suspend fun reconcileAfterProcessRestart(nowEpochMillis: Long): List<TaskSessionRecord> =
        withContext(Dispatchers.IO) {
            database.withTransaction {
                val dao = database.taskSessionDao()
                dao.active().map { entity ->
                    val current = entity.toRecord()
                    val decision = TaskSessionProtocol.interrupted(current, nowEpochMillis)
                    if (decision is TaskSessionDecision.Accept && decision.record != current) {
                        dao.update(decision.record.toEntity())
                        decision.record
                    } else current
                }
            }
        }

    private fun TaskSessionRecord.toEntity() = TaskSessionEntity(
        taskId = taskId.value,
        goalCiphertext = crypto.encrypt(goal.toByteArray(Charsets.UTF_8)),
        inputModality = inputModality.name,
        state = state.name,
        createdAtEpochMillis = createdAtEpochMillis,
        updatedAtEpochMillis = updatedAtEpochMillis,
        terminalReasonCiphertext = terminalReason?.let { crypto.encrypt(it.toByteArray(Charsets.UTF_8)) },
    )

    private fun TaskSessionEntity.toRecord() = TaskSessionRecord(
        taskId = TaskId(taskId),
        goal = crypto.decrypt(goalCiphertext).toString(Charsets.UTF_8),
        inputModality = TaskInputModality.valueOf(inputModality),
        state = TaskSessionState.valueOf(state),
        createdAtEpochMillis = createdAtEpochMillis,
        updatedAtEpochMillis = updatedAtEpochMillis,
        terminalReason = terminalReasonCiphertext?.let { crypto.decrypt(it).toString(Charsets.UTF_8) },
    )
}

class RoomTaskStepJournal(private val dao: TaskStepDao, private val crypto: JournalCrypto) : TaskStepJournal {
    override suspend fun append(record: TaskStepRecord) = withContext(Dispatchers.IO) {
        dao.insert(TaskStepEntity(
            taskId = record.taskId.value,
            actionId = record.actionId.value,
            state = record.state.name,
            startedAtElapsedMillis = record.startedAtElapsedMillis,
            finishedAtElapsedMillis = record.finishedAtElapsedMillis,
            noteCiphertext = crypto.encrypt(record.note.toByteArray(Charsets.UTF_8)),
        ))
        Unit
    }
}

/**
 * Durable write-ahead action journal. insertPrepared commits before Android dispatch;
 * transition() uses a single SQLite transaction and rejects illegal state changes.
 */
class RoomDurableExecutionJournal(
    private val database: TaskJournalDatabase,
    private val crypto: JournalCrypto,
) : DurableExecutionJournal {
    override suspend fun insertPrepared(record: DurableActionRecord): Boolean = withContext(Dispatchers.IO) {
        require(record.dispatchState == DispatchState.PREPARED) { "First persisted state must be PREPARED" }
        database.withTransaction {
            val rowId = database.durableActionDao().insert(record.toEntity(crypto))
            rowId != -1L
        }
    }

    override suspend fun transition(
        taskId: TaskId,
        actionId: ActionId,
        next: DispatchState,
        nowElapsedMillis: Long,
        note: String?,
    ): JournalDecision = withContext(Dispatchers.IO) {
        database.withTransaction {
            val dao = database.durableActionDao()
            val current = dao.find(taskId.value, actionId.value)
                ?: return@withTransaction JournalDecision.Reject("Action intent was not persisted before dispatch")
            val decision = DurableActionProtocol.transition(current.toRecord(), next, nowElapsedMillis, note)
            if (decision is JournalDecision.Accept) {
                val updated = dao.update(decision.record.toEntity(crypto, rowId = current.rowId))
                if (updated != 1) return@withTransaction JournalDecision.Reject("Concurrent journal update; reload before proceeding")
            }
            decision
        }
    }

    override suspend fun loadRecoverable(): List<DurableActionRecord> = withContext(Dispatchers.IO) {
        database.durableActionDao().recoverable().map { it.toRecord() }
    }

    override suspend fun reconcileAfterProcessDeath(): List<DurableActionRecord> = withContext(Dispatchers.IO) {
        database.withTransaction {
            val dao = database.durableActionDao()
            dao.recoverable().map { entity ->
                val recovered = DurableActionProtocol.recoverAfterProcessDeath(entity.toRecord())
                if (recovered is JournalDecision.Accept && recovered.record != entity.toRecord()) {
                    dao.update(recovered.record.toEntity(crypto, rowId = entity.rowId))
                    recovered.record
                } else {
                    entity.toRecord()
                }
            }
        }
    }

    private fun DurableActionRecord.toEntity(crypto: JournalCrypto, rowId: Long = 0) = DurableActionEntity(
        rowId = rowId,
        taskId = taskId.value,
        actionId = actionId.value,
        actionKind = actionKind,
        targetPackage = targetPackage,
        snapshotId = snapshotId?.value,
        idempotency = idempotency.name,
        dispatchState = dispatchState.name,
        attempt = attempt,
        createdAtElapsedMillis = createdAtElapsedMillis,
        updatedAtElapsedMillis = updatedAtElapsedMillis,
        resultNoteCiphertext = resultNote?.let { crypto.encrypt(it.toByteArray(Charsets.UTF_8)) },
    )

    private fun DurableActionEntity.toRecord() = DurableActionRecord(
        taskId = TaskId(taskId),
        actionId = ActionId(actionId),
        actionKind = actionKind,
        targetPackage = targetPackage,
        snapshotId = snapshotId?.let(::SnapshotId),
        idempotency = Idempotency.valueOf(idempotency),
        dispatchState = DispatchState.valueOf(dispatchState),
        attempt = attempt,
        createdAtElapsedMillis = createdAtElapsedMillis,
        updatedAtElapsedMillis = updatedAtElapsedMillis,
        resultNote = null, // Notes remain encrypted at rest and are not needed for recovery decisions.
    )
}

interface JournalCrypto {
    fun encrypt(plaintext: ByteArray): ByteArray
    fun decrypt(ciphertext: ByteArray): ByteArray
}

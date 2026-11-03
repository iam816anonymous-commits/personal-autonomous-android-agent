package com.personalagent.android.data
import androidx.room.*
import com.personalagent.core.TaskStepJournal
import com.personalagent.core.TaskStepRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
@Dao
interface TaskStepDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(step: TaskStepEntity): Long
    @Query("SELECT * FROM task_steps WHERE taskId = :taskId ORDER BY rowId ASC")
    suspend fun byTask(taskId: String): List<TaskStepEntity>
    @Query("SELECT * FROM task_steps ORDER BY rowId DESC LIMIT :limit")
    suspend fun latest(limit: Int): List<TaskStepEntity>
}
@Database(entities = [TaskStepEntity::class], version = 1, exportSchema = true)
abstract class TaskJournalDatabase : RoomDatabase() {
    abstract fun taskStepDao(): TaskStepDao
}
class RoomTaskStepJournal(private val dao: TaskStepDao, private val crypto: JournalCrypto) : TaskStepJournal {
    override suspend fun append(record: TaskStepRecord) = withContext(Dispatchers.IO) {
        dao.insert(TaskStepEntity(
            taskId = record.taskId.value, actionId = record.actionId.value, state = record.state.name,
            startedAtElapsedMillis = record.startedAtElapsedMillis,
            finishedAtElapsedMillis = record.finishedAtElapsedMillis,
            noteCiphertext = crypto.encrypt(record.note.toByteArray(Charsets.UTF_8))))
        Unit
    }
}
interface JournalCrypto { fun encrypt(plaintext: ByteArray): ByteArray }

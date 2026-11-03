package com.personalagent.android

import android.app.Application
import androidx.room.Room
import com.personalagent.android.data.AndroidKeystoreJournalCrypto
import com.personalagent.android.data.RoomDurableExecutionJournal
import com.personalagent.android.data.RoomTaskStepJournal
import com.personalagent.android.data.RoomTaskSessionJournal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.personalagent.android.data.TaskJournalDatabase
import com.personalagent.android.rea.AndroidReverseEngineeringController

class AgentApplication : Application() {
    lateinit var journal: RoomTaskStepJournal
        private set
    lateinit var durableJournal: RoomDurableExecutionJournal
        private set
    lateinit var taskSessionJournal: RoomTaskSessionJournal
        private set
    lateinit var reaController: AndroidReverseEngineeringController
        private set

    override fun onCreate() {
        super.onCreate()
        val database = Room.databaseBuilder(this, TaskJournalDatabase::class.java, "agent-journal.db")
            .setJournalMode(androidx.room.RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
            .addMigrations(TaskJournalDatabase.MIGRATION_1_2, TaskJournalDatabase.MIGRATION_2_3)
            .build()
        val crypto = AndroidKeystoreJournalCrypto()
        journal = RoomTaskStepJournal(database.taskStepDao(), crypto)
        durableJournal = RoomDurableExecutionJournal(database, crypto)
        taskSessionJournal = RoomTaskSessionJournal(database, crypto)
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            // Any task that was active when the process died must be reconciled, never replayed.
            taskSessionJournal.reconcileAfterProcessRestart(System.currentTimeMillis())
            durableJournal.reconcileAfterProcessDeath()
        }
        reaController = AndroidReverseEngineeringController(this)
    }
}

package com.personalagent.android

import android.app.Application
import androidx.room.Room
import com.personalagent.android.data.AndroidKeystoreJournalCrypto
import com.personalagent.android.data.RoomDurableExecutionJournal
import com.personalagent.android.data.RoomTaskStepJournal
import com.personalagent.android.data.TaskJournalDatabase

class AgentApplication : Application() {
    lateinit var journal: RoomTaskStepJournal
        private set
    lateinit var durableJournal: RoomDurableExecutionJournal
        private set

    override fun onCreate() {
        super.onCreate()
        val database = Room.databaseBuilder(this, TaskJournalDatabase::class.java, "agent-journal.db")
            .setJournalMode(androidx.room.RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
            .addMigrations(TaskJournalDatabase.MIGRATION_1_2)
            .build()
        val crypto = AndroidKeystoreJournalCrypto()
        journal = RoomTaskStepJournal(database.taskStepDao(), crypto)
        durableJournal = RoomDurableExecutionJournal(database, crypto)
    }
}

package com.personalagent.android
import android.app.Application
import androidx.room.Room
import com.personalagent.android.data.AndroidKeystoreJournalCrypto
import com.personalagent.android.data.RoomTaskStepJournal
import com.personalagent.android.data.TaskJournalDatabase

class AgentApplication : Application() {
    lateinit var journal: RoomTaskStepJournal
        private set
    override fun onCreate() {
        super.onCreate()
        val database = Room.databaseBuilder(this, TaskJournalDatabase::class.java, "agent-journal.db")
            .setJournalMode(androidx.room.RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
            .build()
        journal = RoomTaskStepJournal(database.taskStepDao(), AndroidKeystoreJournalCrypto())
    }
}

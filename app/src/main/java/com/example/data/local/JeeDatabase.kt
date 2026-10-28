package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.DailyGoalEntity
import com.example.data.model.StudySessionEntity

@Database(
    entities = [StudySessionEntity::class, DailyGoalEntity::class],
    version = 1,
    exportSchema = false
)
abstract class JeeDatabase : RoomDatabase() {
    abstract fun jeeDao(): JeeDao

    companion object {
        @Volatile
        private var INSTANCE: JeeDatabase? = null

        fun getInstance(context: Context): JeeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JeeDatabase::class.java,
                    "jee_focus_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

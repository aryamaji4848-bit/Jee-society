package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subject: String,
    val durationMinutes: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val targetNotes: String = "",
    val completed: Boolean = true,
    val epochDay: Long = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
)

@Entity(tableName = "daily_goals")
data class DailyGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val subject: String,
    val targetCount: Int = 30, // e.g. 30 problems or 1 chapter
    val completedCount: Int = 0,
    val isCompleted: Boolean = false,
    val epochDay: Long = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
)

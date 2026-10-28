package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DailyGoalEntity
import com.example.data.model.StudySessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JeeDao {
    @Query("SELECT * FROM study_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE epochDay = :epochDay ORDER BY timestamp DESC")
    fun getSessionsForDay(epochDay: Long): Flow<List<StudySessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySessionEntity): Long

    @Query("SELECT SUM(durationMinutes) FROM study_sessions WHERE epochDay = :epochDay")
    fun getTodayStudyMinutes(epochDay: Long): Flow<Int?>

    @Query("SELECT SUM(durationMinutes) FROM study_sessions")
    fun getTotalStudyMinutes(): Flow<Int?>

    @Query("SELECT DISTINCT epochDay FROM study_sessions WHERE durationMinutes >= 20 ORDER BY epochDay DESC")
    fun getStudyEpochDays(): Flow<List<Long>>

    // Daily Goals
    @Query("SELECT * FROM daily_goals WHERE epochDay = :epochDay ORDER BY id ASC")
    fun getGoalsForDay(epochDay: Long): Flow<List<DailyGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: DailyGoalEntity): Long

    @Update
    suspend fun updateGoal(goal: DailyGoalEntity)

    @Query("DELETE FROM daily_goals WHERE id = :goalId")
    suspend fun deleteGoal(goalId: Long)
}

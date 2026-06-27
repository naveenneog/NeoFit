package com.neofit.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.neofit.data.local.entity.WorkoutSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSessionDao {
    @Query("SELECT * FROM workout_session WHERE epochDay = :day ORDER BY startEpochMillis DESC")
    fun observeForDay(day: Long): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT COALESCE(SUM(caloriesBurned), 0) FROM workout_session WHERE epochDay = :day AND completed = 1")
    fun observeCaloriesForDay(day: Long): Flow<Int>

    @Upsert
    suspend fun upsert(entity: WorkoutSessionEntity): Long
}

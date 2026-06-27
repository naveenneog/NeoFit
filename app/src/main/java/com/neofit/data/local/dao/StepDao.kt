package com.neofit.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.neofit.data.local.entity.StepSummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {
    @Query("SELECT * FROM step_summary WHERE dateEpochDay = :day LIMIT 1")
    fun observeForDay(day: Long): Flow<StepSummaryEntity?>

    @Query("SELECT * FROM step_summary WHERE dateEpochDay BETWEEN :startDay AND :endDay ORDER BY dateEpochDay ASC")
    fun observeBetween(startDay: Long, endDay: Long): Flow<List<StepSummaryEntity>>

    @Query("SELECT * FROM step_summary WHERE dateEpochDay = :day LIMIT 1")
    suspend fun get(day: Long): StepSummaryEntity?

    @Upsert
    suspend fun upsert(entity: StepSummaryEntity)
}

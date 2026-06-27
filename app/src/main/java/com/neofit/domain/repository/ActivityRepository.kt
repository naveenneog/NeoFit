package com.neofit.domain.repository

import com.neofit.core.common.DataResult
import com.neofit.domain.model.StepSummary
import com.neofit.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {
    fun observeSteps(epochDay: Long): Flow<StepSummary>
    fun observeWeeklySteps(): Flow<List<StepSummary>>
    fun observeSyncStatus(): Flow<SyncStatus>

    /** Pull from Health Connect if available, else compute an estimate. */
    suspend fun syncToday(): DataResult<StepSummary>

    fun observeWater(epochDay: Long): Flow<Int>
    suspend fun addWater(epochDay: Long, delta: Int)
}

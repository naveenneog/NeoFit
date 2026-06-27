package com.neofit.domain.repository

import com.neofit.domain.model.WeightEntry
import kotlinx.coroutines.flow.Flow

interface WeightRepository {
    fun observeHistory(): Flow<List<WeightEntry>>
    suspend fun add(entry: WeightEntry)
    suspend fun latest(): WeightEntry?
}

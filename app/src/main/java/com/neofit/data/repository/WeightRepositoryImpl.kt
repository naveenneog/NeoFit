package com.neofit.data.repository

import com.neofit.data.local.dao.WeightDao
import com.neofit.data.local.toDomain
import com.neofit.data.local.toEntity
import com.neofit.domain.model.WeightEntry
import com.neofit.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeightRepositoryImpl @Inject constructor(
    private val weightDao: WeightDao,
) : WeightRepository {

    override fun observeHistory(): Flow<List<WeightEntry>> =
        weightDao.observeAll().map { list -> list.map { it.toDomain() } }

    // One weight per day: replace any existing same-day entry so the trend
    // reflects day-over-day change instead of multiple same-day logs.
    override suspend fun add(entry: WeightEntry) {
        val entity = entry.toEntity()
        weightDao.deleteForDay(entity.dateEpochDay)
        weightDao.insert(entity)
    }

    override suspend fun latest(): WeightEntry? = weightDao.latest()?.toDomain()
}

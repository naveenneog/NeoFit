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

    override suspend fun add(entry: WeightEntry) = weightDao.insert(entry.toEntity())

    override suspend fun latest(): WeightEntry? = weightDao.latest()?.toDomain()
}

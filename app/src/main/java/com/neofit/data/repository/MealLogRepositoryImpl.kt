package com.neofit.data.repository

import com.neofit.data.local.dao.MealLogDao
import com.neofit.data.local.toDomain
import com.neofit.data.local.toEntity
import com.neofit.domain.model.MealLog
import com.neofit.domain.repository.MealLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MealLogRepositoryImpl @Inject constructor(
    private val mealLogDao: MealLogDao,
) : MealLogRepository {

    override fun observeMealsForDay(epochDay: Long): Flow<List<MealLog>> =
        mealLogDao.observeForDay(epochDay).map { list -> list.map { it.toDomain() } }

    override fun observeMealsBetween(startDay: Long, endDay: Long): Flow<List<MealLog>> =
        mealLogDao.observeBetween(startDay, endDay).map { list -> list.map { it.toDomain() } }

    override suspend fun recentMeals(limit: Int): List<MealLog> =
        mealLogDao.recent(limit).map { it.toDomain() }

    override suspend fun lastMeal(): MealLog? = mealLogDao.last()?.toDomain()

    override suspend fun getById(id: Long): MealLog? = mealLogDao.getById(id)?.toDomain()

    override suspend fun add(meal: MealLog): Long = mealLogDao.insert(meal.toEntity())

    override suspend fun update(meal: MealLog) = mealLogDao.update(meal.toEntity())

    override suspend fun delete(id: Long) = mealLogDao.deleteById(id)
}

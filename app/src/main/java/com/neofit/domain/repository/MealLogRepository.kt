package com.neofit.domain.repository

import com.neofit.domain.model.MealLog
import kotlinx.coroutines.flow.Flow

interface MealLogRepository {
    fun observeMealsForDay(epochDay: Long): Flow<List<MealLog>>
    fun observeMealsBetween(startDay: Long, endDay: Long): Flow<List<MealLog>>
    suspend fun recentMeals(limit: Int = 10): List<MealLog>
    suspend fun lastMeal(): MealLog?
    suspend fun getById(id: Long): MealLog?
    suspend fun add(meal: MealLog): Long
    suspend fun update(meal: MealLog)
    suspend fun delete(id: Long)
}

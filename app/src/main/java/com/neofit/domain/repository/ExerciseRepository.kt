package com.neofit.domain.repository

import com.neofit.domain.model.ExercisePlan
import com.neofit.domain.model.WorkoutSession
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    fun plans(): List<ExercisePlan>
    fun planById(id: String): ExercisePlan?
    suspend fun saveSession(session: WorkoutSession): Long
    fun observeSessionsForDay(epochDay: Long): Flow<List<WorkoutSession>>
    fun observeCaloriesBurnedForDay(epochDay: Long): Flow<Int>
}

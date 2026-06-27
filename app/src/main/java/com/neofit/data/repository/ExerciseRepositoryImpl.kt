package com.neofit.data.repository

import com.neofit.core.util.DateUtil
import com.neofit.data.local.dao.WorkoutSessionDao
import com.neofit.data.local.toDomain
import com.neofit.data.local.toEntity
import com.neofit.data.seed.ExerciseLibrary
import com.neofit.domain.model.ExercisePlan
import com.neofit.domain.model.WorkoutSession
import com.neofit.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseRepositoryImpl @Inject constructor(
    private val workoutSessionDao: WorkoutSessionDao,
) : ExerciseRepository {

    override fun plans(): List<ExercisePlan> = ExerciseLibrary.plans

    override fun planById(id: String): ExercisePlan? = ExerciseLibrary.byId[id]

    override suspend fun saveSession(session: WorkoutSession): Long {
        val day = DateUtil.epochDayOf(session.startEpochMillis)
        return workoutSessionDao.upsert(session.toEntity(day))
    }

    override fun observeSessionsForDay(epochDay: Long): Flow<List<WorkoutSession>> =
        workoutSessionDao.observeForDay(epochDay).map { list -> list.map { it.toDomain() } }

    override fun observeCaloriesBurnedForDay(epochDay: Long): Flow<Int> =
        workoutSessionDao.observeCaloriesForDay(epochDay)
}

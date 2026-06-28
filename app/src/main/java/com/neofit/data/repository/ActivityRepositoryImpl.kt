package com.neofit.data.repository

import com.neofit.core.common.DataResult
import com.neofit.core.util.DateUtil
import com.neofit.data.local.dao.StepDao
import com.neofit.data.local.dao.WaterDao
import com.neofit.data.local.entity.WaterEntity
import com.neofit.data.local.toDomain
import com.neofit.data.local.toEntity
import com.neofit.domain.model.StepSource
import com.neofit.domain.model.StepSummary
import com.neofit.domain.model.SyncState
import com.neofit.domain.model.SyncStatus
import com.neofit.domain.repository.ActivityRepository
import com.neofit.domain.repository.UserRepository
import com.neofit.engine.CalorieMath
import com.neofit.integration.health.HealthConnectManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityRepositoryImpl @Inject constructor(
    private val stepDao: StepDao,
    private val waterDao: WaterDao,
    private val healthConnectManager: HealthConnectManager,
    private val userRepository: UserRepository,
) : ActivityRepository {

    private val syncStatus = MutableStateFlow(SyncStatus(source = "health_connect"))

    override fun observeSteps(epochDay: Long): Flow<StepSummary> =
        stepDao.observeForDay(epochDay).map { it?.toDomain() ?: StepSummary(epochDay, 0) }

    override fun observeWeeklySteps(): Flow<List<StepSummary>> {
        val days = DateUtil.lastDays(7)
        return stepDao.observeBetween(days.first(), days.last()).map { rows ->
            val byDay = rows.associateBy { it.dateEpochDay }
            days.map { day -> byDay[day]?.toDomain() ?: StepSummary(day, 0) }
        }
    }

    override fun observeSyncStatus(): Flow<SyncStatus> = syncStatus.asStateFlow()

    override suspend fun syncToday(): DataResult<StepSummary> {
        val today = DateUtil.todayEpochDay()
        syncStatus.value = syncStatus.value.copy(state = SyncState.SYNCING)
        return DataResult.catching {
            val weightKg = userRepository.getProfile()?.currentWeightKg ?: 65f
            val heightCm = userRepository.getProfile()?.heightCm ?: 165f

            val snapshot = healthConnectManager.readToday()
            val summary = if (snapshot != null) {
                val active = if (snapshot.activeCaloriesKcal > 0) snapshot.activeCaloriesKcal
                else CalorieMath.caloriesFromSteps(snapshot.steps, weightKg)
                StepSummary(
                    dateEpochDay = today,
                    steps = snapshot.steps,
                    distanceMeters = if (snapshot.distanceMeters > 0f) snapshot.distanceMeters
                    else CalorieMath.distanceMetersFromSteps(snapshot.steps, heightCm),
                    activeCaloriesKcal = active,
                    source = StepSource.HEALTH_CONNECT,
                )
            } else {
                // Health Connect unavailable: surface only genuinely observed data.
                // Keep a real value already recorded for today (HC or manual entry);
                // otherwise report zero — never fabricate activity (QA #4/#6). This
                // also overwrites any stale value left by the old step simulator.
                val existing = stepDao.get(today)?.toDomain()
                val isReal = existing != null && existing.steps > 0 &&
                    (existing.source == StepSource.HEALTH_CONNECT || existing.source == StepSource.MANUAL)
                if (isReal) {
                    existing!!
                } else {
                    StepSummary(dateEpochDay = today, steps = 0, source = StepSource.NONE)
                }
            }
            stepDao.upsert(summary.toEntity())
            syncStatus.value = SyncStatus(
                source = "health_connect",
                lastSyncEpochMillis = DateUtil.nowMillis(),
                state = SyncState.SUCCESS,
                message = if (snapshot != null) "Synced from Health Connect" else "Health Connect not connected",
            )
            summary
        }.also { result ->
            if (result is DataResult.Failure) {
                syncStatus.value = SyncStatus(
                    source = "health_connect",
                    state = SyncState.ERROR,
                    message = result.message,
                )
            }
        }
    }

    override fun observeWater(epochDay: Long): Flow<Int> =
        waterDao.observe(epochDay).map { it?.glasses ?: 0 }

    override suspend fun addWater(epochDay: Long, delta: Int) {
        val current = waterDao.get(epochDay)?.glasses ?: 0
        waterDao.upsert(WaterEntity(epochDay, (current + delta).coerceAtLeast(0)))
    }
}

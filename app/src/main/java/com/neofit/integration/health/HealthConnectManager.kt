package com.neofit.integration.health

import android.content.Context
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.neofit.core.util.DateUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/** Result of a Health Connect read for a single day. */
data class HealthSnapshot(
    val steps: Int,
    val distanceMeters: Float,
    val activeCaloriesKcal: Int,
)

/**
 * Thin, defensive wrapper over Health Connect. Every method degrades gracefully:
 * if the SDK or provider is missing, or permissions are not granted, reads
 * return null and callers fall back to estimation.
 */
@Singleton
class HealthConnectManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val permissions: Set<String> = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
    )

    fun isAvailable(): Boolean =
        runCatching { HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE }
            .getOrDefault(false)

    private fun clientOrNull(): HealthConnectClient? =
        if (isAvailable()) runCatching { HealthConnectClient.getOrCreate(context) }.getOrNull() else null

    suspend fun hasAllPermissions(): Boolean {
        val client = clientOrNull() ?: return false
        return runCatching {
            client.permissionController.getGrantedPermissions().containsAll(permissions)
        }.getOrDefault(false)
    }

    /** Contract to launch the Health Connect permission request from an Activity. */
    fun permissionRequestContract(): ActivityResultContract<Set<String>, Set<String>> =
        androidx.health.connect.client.PermissionController.createRequestPermissionResultContract()

    /** Aggregates today's steps/distance/active calories, or null if unavailable. */
    suspend fun readToday(): HealthSnapshot? {
        val client = clientOrNull() ?: return null
        if (!hasAllPermissions()) return null
        val start: Instant = Instant.ofEpochMilli(DateUtil.startOfDayMillis(DateUtil.todayEpochDay()))
        val end: Instant = Instant.now()
        return runCatching {
            val result = client.aggregate(
                AggregateRequest(
                    metrics = setOf(
                        StepsRecord.COUNT_TOTAL,
                        DistanceRecord.DISTANCE_TOTAL,
                        ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL,
                    ),
                    timeRangeFilter = TimeRangeFilter.between(start, end),
                ),
            )
            HealthSnapshot(
                steps = (result[StepsRecord.COUNT_TOTAL] ?: 0L).toInt(),
                distanceMeters = (result[DistanceRecord.DISTANCE_TOTAL]?.inMeters ?: 0.0).toFloat(),
                activeCaloriesKcal = (result[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories ?: 0.0).toInt(),
            )
        }.getOrNull()
    }
}

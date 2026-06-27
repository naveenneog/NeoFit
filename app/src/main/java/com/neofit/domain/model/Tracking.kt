package com.neofit.domain.model

/** Daily movement, from Health Connect when available, else estimated. */
data class StepSummary(
    val dateEpochDay: Long,
    val steps: Int,
    val distanceMeters: Float = 0f,
    val activeCaloriesKcal: Int = 0,
    val source: StepSource = StepSource.ESTIMATED,
)

/** A weight measurement on a given day. */
data class WeightEntry(
    val id: Long = 0,
    val weightKg: Float,
    val dateEpochDay: Long,
    val note: String? = null,
)

/**
 * Transparent wellness score (0–100). Components are surfaced individually so
 * the user can see exactly how the number is built (see WellnessScoreEngine).
 */
data class WellnessSummary(
    val dateEpochDay: Long,
    val score: Int,
    val consistencyScore: Int,
    val activityScore: Int,
    val calorieAdherenceScore: Int,
    val workoutScore: Int,
    val explanation: String,
) {
    companion object {
        fun empty(day: Long) = WellnessSummary(
            dateEpochDay = day,
            score = 0,
            consistencyScore = 0,
            activityScore = 0,
            calorieAdherenceScore = 0,
            workoutScore = 0,
            explanation = "Log a meal, take some steps, or finish a workout to build your score.",
        )
    }
}

/** Cached image for a food or exercise pose. */
data class FoodImageAsset(
    val key: String,
    val localPath: String? = null,
    val remoteUrl: String? = null,
    val source: ImageSource = ImageSource.PLACEHOLDER,
    val prompt: String? = null,
    val createdAtMillis: Long = 0,
) {
    /** Best displayable reference: local file first, then remote URL. */
    fun bestRef(): String? = localPath ?: remoteUrl
}

/** A surfaced suggestion/nudge for the dashboard or insights screen. */
data class Recommendation(
    val id: String,
    val type: RecommendationType,
    val title: String,
    val message: String,
    val priority: Int = 0,
    val actionRoute: String? = null,
)

/** Status of an external sync (e.g., Health Connect). */
data class SyncStatus(
    val source: String,
    val lastSyncEpochMillis: Long = 0,
    val state: SyncState = SyncState.IDLE,
    val message: String? = null,
)

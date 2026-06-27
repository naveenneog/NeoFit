package com.neofit.domain.model

/**
 * Aggregated snapshot for the Home dashboard. Computed, never persisted.
 * `remaining = target - consumed + burned` (exercise calories can be eaten back).
 */
data class DashboardSummary(
    val dateEpochDay: Long,
    val userName: String,
    val caloriesConsumed: Int,
    val caloriesBurned: Int,
    val calorieTarget: Int,
    val proteinConsumedG: Float,
    val carbsConsumedG: Float,
    val fatConsumedG: Float,
    val proteinTargetG: Int,
    val steps: Int,
    val stepTarget: Int,
    val stepSource: StepSource,
    val currentWeightKg: Float,
    val weeklyWeightDeltaKg: Float,
    val streakDays: Int,
    val waterGlasses: Int,
    val waterTarget: Int,
    val wellness: WellnessSummary,
    val recommendations: List<Recommendation>,
    val recommendedMeals: List<FoodItem>,
) {
    val caloriesRemaining: Int get() = calorieTarget - caloriesConsumed + caloriesBurned
    val calorieProgress: Float
        get() = if (calorieTarget > 0) (caloriesConsumed.toFloat() / calorieTarget).coerceIn(0f, 1f) else 0f
    val stepProgress: Float
        get() = if (stepTarget > 0) (steps.toFloat() / stepTarget).coerceIn(0f, 1f) else 0f
}

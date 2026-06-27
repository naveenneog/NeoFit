package com.neofit.domain.model

/**
 * Single-user profile (id is fixed to [DEFAULT_ID]). Captured during onboarding
 * and editable later. Targets are derived but cached for quick dashboard reads.
 */
data class UserProfile(
    val id: Long = DEFAULT_ID,
    val name: String,
    val age: Int,
    val sex: Sex,
    val heightCm: Float,
    val currentWeightKg: Float,
    val targetWeightKg: Float,
    val activityLevel: ActivityLevel,
    val dietaryPreference: DietaryPreference,
    val goal: WellnessGoal,
    val preferredRegion: FoodRegion,
    val language: AppLanguage,
    val foodRestrictions: List<String> = emptyList(),
    val dailyCalorieTarget: Int = 0,
    val dailyProteinTargetG: Int = 0,
    val dailyStepTarget: Int = 8000,
    val dailyWaterGlassTarget: Int = 8,
    val onboardingComplete: Boolean = false,
    val createdAtEpochDay: Long = 0,
    val updatedAtEpochMillis: Long = 0,
) {
    val bmi: Float
        get() = if (heightCm > 0) currentWeightKg / ((heightCm / 100f) * (heightCm / 100f)) else 0f

    companion object {
        const val DEFAULT_ID = 1L
    }
}

/**
 * A concrete, derived plan toward the user's goal. Computed from [UserProfile]
 * by ComputeGoalUseCase but represented explicitly for transparency.
 */
data class Goal(
    val type: WellnessGoal,
    val startWeightKg: Float,
    val targetWeightKg: Float,
    val weeklyRateKg: Float,
    val dailyCalorieTarget: Int,
    val dailyProteinTargetG: Int,
    val dailyStepTarget: Int,
    val tdeeKcal: Int,
    val bmrKcal: Int,
    val rationale: String,
)

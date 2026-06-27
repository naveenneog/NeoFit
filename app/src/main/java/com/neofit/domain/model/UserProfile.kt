package com.neofit.domain.model

import java.time.DayOfWeek
import java.time.LocalDate

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
    /** Weekdays (java.time DayOfWeek value 1=Mon..7=Sun) the user keeps vegetarian. */
    val vegDays: Set<Int> = emptySet(),
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

/** True if [day] is one of the user's vegetarian days. */
fun UserProfile.isVegDay(day: DayOfWeek = LocalDate.now().dayOfWeek): Boolean =
    vegDays.contains(day.value)

/**
 * The diet to use for suggestions today. On a veg day, non-veg/eggetarian/flexitarian
 * users are treated as vegetarian (vegans stay vegan). Otherwise the base preference.
 */
fun UserProfile.effectiveDiet(day: DayOfWeek = LocalDate.now().dayOfWeek): DietaryPreference =
    if (isVegDay(day) && dietaryPreference != DietaryPreference.VEGAN) DietaryPreference.VEGETARIAN
    else dietaryPreference

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

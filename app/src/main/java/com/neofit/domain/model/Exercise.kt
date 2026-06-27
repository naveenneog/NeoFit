package com.neofit.domain.model

/** One movement within a plan. Step images are generated on demand. */
data class ExerciseItem(
    val id: String,
    val name: String,
    val localizedNames: Map<String, String> = emptyMap(),
    val reps: String? = null,
    val durationSec: Int? = null,
    val restSec: Int = 20,
    val instructions: List<String> = emptyList(),
    val targetMuscles: List<String> = emptyList(),
    val met: Float = 4.0f,
    val imagePrompt: String,
    val imageRef: String? = null,
    val videoUrl: String? = null,
    val voiceCue: String = "",
) {
    fun displayName(language: AppLanguage): String =
        localizedNames[language.code] ?: nameEn(language)

    private fun nameEn(language: AppLanguage): String =
        localizedNames[language.localeTag] ?: name
}

/** A target-based workout plan made of [ExerciseItem]s. */
data class ExercisePlan(
    val id: String,
    val title: String,
    val description: String,
    val difficulty: Difficulty,
    val planGoal: PlanGoal,
    val durationMin: Int,
    val schedule: String,
    val requiredEquipment: List<String>,
    val estimatedCalories: Int,
    val safetyNote: String,
    val items: List<ExerciseItem>,
)

/** A logged/active workout attempt for completion tracking. */
data class WorkoutSession(
    val id: Long = 0,
    val planId: String,
    val startEpochMillis: Long,
    val endEpochMillis: Long? = null,
    val completedItemIds: List<String> = emptyList(),
    val caloriesBurned: Int = 0,
    val completed: Boolean = false,
)

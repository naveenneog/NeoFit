package com.neofit.engine

import com.neofit.domain.model.WellnessSummary
import kotlin.math.abs
import kotlin.math.roundToInt

/** Inputs to the wellness score — all observable, nothing hidden. */
data class WellnessInputs(
    val dateEpochDay: Long,
    val loggedDaysInWeek: Int,
    val steps: Int,
    val stepTarget: Int,
    val caloriesConsumed: Int,
    val calorieTarget: Int,
    val workoutsToday: Int,
)

/**
 * Transparent 0–100 wellness score. Four weighted components:
 *  • Consistency 25  — days logged this week
 *  • Activity 25     — steps vs target
 *  • Calorie 30      — how close intake is to the daily target
 *  • Workout 20      — completed a workout today
 *
 * The exact contribution of each is returned so the UI can explain the number.
 */
class WellnessScoreEngine {

    fun compute(input: WellnessInputs): WellnessSummary {
        val consistency = ((input.loggedDaysInWeek.coerceIn(0, 7) / 7f) * CONSISTENCY_MAX).roundToInt()

        val activity = if (input.stepTarget > 0) {
            ((input.steps.toFloat() / input.stepTarget).coerceIn(0f, 1f) * ACTIVITY_MAX).roundToInt()
        } else 0

        val calorie = calorieAdherence(input.caloriesConsumed, input.calorieTarget)

        val workout = if (input.workoutsToday >= 1) WORKOUT_MAX else 0

        val total = (consistency + activity + calorie + workout).coerceIn(0, 100)

        return WellnessSummary(
            dateEpochDay = input.dateEpochDay,
            score = total,
            consistencyScore = consistency,
            activityScore = activity,
            calorieAdherenceScore = calorie,
            workoutScore = workout,
            explanation = "Consistency $consistency/$CONSISTENCY_MAX · " +
                "Activity $activity/$ACTIVITY_MAX · " +
                "Calories $calorie/$CALORIE_MAX · " +
                "Workout $workout/$WORKOUT_MAX",
        )
    }

    private fun calorieAdherence(consumed: Int, target: Int): Int {
        if (target <= 0 || consumed <= 0) return 0
        val ratio = consumed.toFloat() / target
        // Best around 95% of target; linearly falls off within ±50%.
        val deviation = abs(ratio - 0.95f)
        val factor = (1f - deviation / 0.5f).coerceIn(0f, 1f)
        return (factor * CALORIE_MAX).roundToInt()
    }

    companion object {
        const val CONSISTENCY_MAX = 25
        const val ACTIVITY_MAX = 25
        const val CALORIE_MAX = 30
        const val WORKOUT_MAX = 20
    }
}

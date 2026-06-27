package com.neofit.engine

import com.neofit.domain.model.ActivityLevel
import com.neofit.domain.model.Sex
import com.neofit.domain.model.WellnessGoal
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Pure energy-balance math. Uses the Mifflin–St Jeor equation for BMR — the
 * most accurate widely-used predictive equation for resting metabolic rate.
 *
 * All outputs are estimates and are surfaced to the user as such.
 */
object CalorieMath {

    /** Basal Metabolic Rate (kcal/day), Mifflin–St Jeor. */
    fun bmr(sex: Sex, weightKg: Float, heightCm: Float, age: Int): Int {
        val base = 10f * weightKg + 6.25f * heightCm - 5f * age
        val adjusted = when (sex) {
            Sex.MALE -> base + 5f
            Sex.FEMALE -> base - 161f
            Sex.OTHER -> base - 78f // midpoint of the two constants
        }
        return adjusted.roundToInt().coerceAtLeast(0)
    }

    /** Total Daily Energy Expenditure = BMR × activity factor. */
    fun tdee(bmr: Int, activity: ActivityLevel): Int =
        (bmr * activity.multiplier).roundToInt()

    /**
     * Daily calorie target for a goal. Deficits/surpluses are capped to safe,
     * sustainable ranges (~0.45 kg/week) and never below a hard floor.
     */
    fun calorieTarget(tdee: Int, goal: WellnessGoal, sex: Sex): Int {
        val raw = when (goal) {
            WellnessGoal.WEIGHT_LOSS -> tdee - 500
            WellnessGoal.WEIGHT_GAIN -> tdee + 400
            WellnessGoal.IMPROVE_STAMINA -> tdee + 100
            WellnessGoal.MAINTENANCE, WellnessGoal.GENERAL_WELLNESS -> tdee
        }
        val floor = if (sex == Sex.FEMALE) 1200 else 1500
        return max(raw, floor)
    }

    /** Goal-based protein target (g/day) using grams per kg of body weight. */
    fun proteinTargetG(goal: WellnessGoal, weightKg: Float): Int {
        val perKg = when (goal) {
            WellnessGoal.WEIGHT_LOSS -> 1.6f
            WellnessGoal.WEIGHT_GAIN -> 1.8f
            WellnessGoal.IMPROVE_STAMINA -> 1.4f
            WellnessGoal.MAINTENANCE, WellnessGoal.GENERAL_WELLNESS -> 1.2f
        }
        return (perKg * weightKg).roundToInt()
    }

    /** Suggested weekly weight change (kg) for the goal. */
    fun weeklyRateKg(goal: WellnessGoal): Float = when (goal) {
        WellnessGoal.WEIGHT_LOSS -> -0.45f
        WellnessGoal.WEIGHT_GAIN -> 0.35f
        else -> 0f
    }

    /** Approximate calories burned per step, scaled by body weight. */
    fun caloriesPerStep(weightKg: Float): Float = weightKg * 0.00057f

    fun caloriesFromSteps(steps: Int, weightKg: Float): Int =
        (steps * caloriesPerStep(weightKg)).roundToInt()

    /** Rough stride length (m) from height — used to estimate distance. */
    fun strideMeters(heightCm: Float): Float = heightCm / 100f * 0.415f

    fun distanceMetersFromSteps(steps: Int, heightCm: Float): Float =
        steps * strideMeters(heightCm)

    /** MET-based calorie burn: kcal = MET × kg × hours. */
    fun metCalories(met: Float, weightKg: Float, durationSec: Int): Int =
        (met * weightKg * (durationSec / 3600f)).roundToInt()
}

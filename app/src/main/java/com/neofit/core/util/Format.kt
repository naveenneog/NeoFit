package com.neofit.core.util

import com.neofit.domain.model.ConfidenceLevel
import com.neofit.domain.model.NutritionEstimate

/** Formatting helpers that keep calorie display honest about approximation. */
object Format {

    /** e.g. "~220 kcal" for approximate, "220 kcal" when confident/exact. */
    fun calories(estimate: NutritionEstimate): String {
        val prefix = if (estimate.isApproximate) "~" else ""
        return "$prefix${estimate.caloriesKcal} kcal"
    }

    fun calories(kcal: Int, approximate: Boolean = true): String =
        (if (approximate) "~" else "") + "$kcal kcal"

    fun grams(value: Float): String =
        if (value <= 0f) "0 g" else "${value.toInt()} g"

    fun weight(kg: Float): String = String.format("%.1f kg", kg)

    fun bmi(value: Float): String = String.format("%.1f", value)

    fun bmiCategory(bmi: Float): String = when {
        bmi <= 0f -> "—"
        bmi < 18.5f -> "Underweight"
        bmi < 25f -> "Healthy"
        bmi < 30f -> "Overweight"
        else -> "Obese"
    }

    fun confidenceEmoji(level: ConfidenceLevel): String = when (level) {
        ConfidenceLevel.HIGH -> "🟢"
        ConfidenceLevel.MEDIUM -> "🟡"
        ConfidenceLevel.ROUGH -> "🟠"
    }
}

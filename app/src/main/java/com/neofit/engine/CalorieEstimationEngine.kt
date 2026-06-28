package com.neofit.engine

import com.neofit.domain.model.ConfidenceLevel
import com.neofit.domain.model.CookingStyle
import com.neofit.domain.model.FoodItem
import com.neofit.domain.model.MealCategory
import com.neofit.domain.model.NutritionEstimate
import com.neofit.domain.model.PortionSize
import kotlin.math.roundToInt

/**
 * Estimates the nutrition of a serving from the food knowledge base.
 *
 * Pipeline: base lookup → portion multiplier → cooking-style adjustment →
 * uncertainty/confidence scoring → human-readable basis. Indian home/street
 * dishes vary widely, so most results are intentionally labelled approximate.
 */
class CalorieEstimationEngine {

    fun estimate(
        food: FoodItem,
        portion: PortionSize,
        cookingOverride: CookingStyle? = null,
        manuallyCorrected: Boolean = false,
    ): NutritionEstimate {
        val baseMult = food.baseServing.multiplier.takeIf { it > 0f } ?: 1f
        val relativePortion = portion.multiplier / baseMult

        val cookingFactor = if (cookingOverride != null && cookingOverride != food.cookingStyle) {
            cookingOverride.factor / food.cookingStyle.factor
        } else 1f

        val calories = (food.caloriesKcal * relativePortion * cookingFactor).roundToInt()
        val protein = food.proteinG * relativePortion
        val carbs = food.carbsG * relativePortion
        val fat = food.fatG * relativePortion * cookingFactor
        val fiber = food.fiberG * relativePortion

        val confidence = scoreConfidence(food, relativePortion, cookingOverride, manuallyCorrected)
        val basis = buildBasis(food, portion, cookingOverride, manuallyCorrected)

        return NutritionEstimate(
            caloriesKcal = calories,
            proteinG = protein,
            carbsG = carbs,
            fatG = fat,
            fiberG = fiber,
            confidence = confidence,
            basis = basis,
            isApproximate = confidence != ConfidenceLevel.HIGH,
        )
    }

    /** Fallback estimate for a custom dish that isn't in the knowledge base. */
    fun estimateUnknown(
        category: MealCategory,
        portion: PortionSize,
        caloriesOverride: Int? = null,
    ): NutritionEstimate {
        val baseCalories = caloriesOverride ?: when (category) {
            MealCategory.BREAKFAST -> 300
            MealCategory.LUNCH -> 550
            MealCategory.DINNER -> 500
            MealCategory.SNACK -> 200
        }
        val cal = (baseCalories * portion.multiplier).roundToInt()
        // Rough macro split: 50% carbs, 20% protein, 30% fat by calories.
        return NutritionEstimate(
            caloriesKcal = cal,
            proteinG = (cal * 0.20f / 4f),
            carbsG = (cal * 0.50f / 4f),
            fatG = (cal * 0.30f / 9f),
            fiberG = 0f,
            confidence = if (caloriesOverride != null) ConfidenceLevel.MEDIUM else ConfidenceLevel.ROUGH,
            basis = if (caloriesOverride != null) {
                "Using your entered calories for a ${portion.label}."
            } else {
                "Rough estimate for a typical ${category.label.lowercase()} (${portion.label})."
            },
            isApproximate = true,
        )
    }

    private fun scoreConfidence(
        food: FoodItem,
        relativePortion: Float,
        cookingOverride: CookingStyle?,
        manuallyCorrected: Boolean,
    ): ConfidenceLevel {
        if (manuallyCorrected) return ConfidenceLevel.HIGH
        var level = food.baseConfidence
        // Combos/thalis/street food are inherently harder to pin down.
        val variable = food.tags.any { it in VARIABLE_TAGS } || food.isStreetFood
        if (variable && level == ConfidenceLevel.HIGH) level = ConfidenceLevel.MEDIUM
        // Unusual portion sizes add uncertainty.
        if (relativePortion < 0.5f || relativePortion > 2.0f) level = downgrade(level)
        // A cooking-style change from the assumed default adds uncertainty.
        if (cookingOverride != null && cookingOverride != food.cookingStyle) level = downgrade(level)
        return level
    }

    private fun downgrade(level: ConfidenceLevel): ConfidenceLevel = when (level) {
        ConfidenceLevel.HIGH -> ConfidenceLevel.MEDIUM
        ConfidenceLevel.MEDIUM -> ConfidenceLevel.ROUGH
        ConfidenceLevel.ROUGH -> ConfidenceLevel.ROUGH
    }

    private fun buildBasis(
        food: FoodItem,
        portion: PortionSize,
        cookingOverride: CookingStyle?,
        manuallyCorrected: Boolean,
    ): String {
        if (manuallyCorrected) return "Values confirmed by you."
        val sb = StringBuilder("Based on ${portion.label} of ${food.nameEn}")
        val style = cookingOverride ?: food.cookingStyle
        sb.append(", ${style.name.lowercase()} style")
        if (food.isStreetFood) sb.append("; street-food portions vary")
        sb.append(". Estimated from a standard serving.")
        return sb.toString()
    }

    companion object {
        private val VARIABLE_TAGS = setOf("combo", "thali", "street", "festival", "sweet")
    }
}

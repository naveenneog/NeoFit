package com.neofit.domain.usecase

import com.neofit.domain.model.CookingStyle
import com.neofit.domain.model.FoodItem
import com.neofit.domain.model.MealCategory
import com.neofit.domain.model.NutritionEstimate
import com.neofit.domain.model.PortionSize
import com.neofit.engine.CalorieEstimationEngine
import javax.inject.Inject

/** Thin use-case wrapper over the calorie estimation engine. */
class EstimateMealUseCase @Inject constructor(
    private val engine: CalorieEstimationEngine,
) {
    fun forFood(
        food: FoodItem,
        portion: PortionSize,
        cookingOverride: CookingStyle? = null,
        manuallyCorrected: Boolean = false,
    ): NutritionEstimate = engine.estimate(food, portion, cookingOverride, manuallyCorrected)

    fun forUnknown(
        category: MealCategory,
        portion: PortionSize,
        caloriesOverride: Int? = null,
    ): NutritionEstimate = engine.estimateUnknown(category, portion, caloriesOverride)
}

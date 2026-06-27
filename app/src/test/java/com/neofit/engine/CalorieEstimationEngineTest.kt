package com.neofit.engine

import com.google.common.truth.Truth.assertThat
import com.neofit.domain.model.ConfidenceLevel
import com.neofit.domain.model.CookingStyle
import com.neofit.domain.model.FoodDiet
import com.neofit.domain.model.FoodItem
import com.neofit.domain.model.FoodRegion
import com.neofit.domain.model.MealCategory
import com.neofit.domain.model.PortionSize
import org.junit.Test

class CalorieEstimationEngineTest {

    private val engine = CalorieEstimationEngine()

    private fun food(
        style: CookingStyle = CookingStyle.ROASTED,
        confidence: ConfidenceLevel = ConfidenceLevel.HIGH,
        tags: List<String> = emptyList(),
        street: Boolean = false,
    ) = FoodItem(
        id = "test",
        nameEn = "Test Dish",
        region = FoodRegion.PAN_INDIA,
        diet = FoodDiet.VEG,
        typicalCategory = MealCategory.LUNCH,
        baseServing = PortionSize.STANDARD,
        caloriesKcal = 200,
        proteinG = 8f,
        carbsG = 30f,
        fatG = 6f,
        cookingStyle = style,
        tags = tags,
        isStreetFood = street,
        baseConfidence = confidence,
    )

    @Test
    fun baseServing_returnsBaseValues_andKeepsHighConfidence() {
        val e = engine.estimate(food(), PortionSize.STANDARD)
        assertThat(e.caloriesKcal).isEqualTo(200)
        assertThat(e.confidence).isEqualTo(ConfidenceLevel.HIGH)
        assertThat(e.isApproximate).isFalse()
    }

    @Test
    fun doublePortion_doublesCalories() {
        val e = engine.estimate(food(), PortionSize("2x", 2f))
        assertThat(e.caloriesKcal).isEqualTo(400)
    }

    @Test
    fun friedOverride_raisesCalories_andLowersConfidence() {
        val e = engine.estimate(food(style = CookingStyle.ROASTED), PortionSize.STANDARD, cookingOverride = CookingStyle.FRIED)
        assertThat(e.caloriesKcal).isGreaterThan(200)
        assertThat(e.confidence).isEqualTo(ConfidenceLevel.MEDIUM)
        assertThat(e.isApproximate).isTrue()
    }

    @Test
    fun streetFood_isNotHighConfidence() {
        val e = engine.estimate(food(confidence = ConfidenceLevel.HIGH, street = true), PortionSize.STANDARD)
        assertThat(e.confidence).isNotEqualTo(ConfidenceLevel.HIGH)
    }

    @Test
    fun unknownDish_withoutOverride_isRough() {
        val e = engine.estimateUnknown(MealCategory.LUNCH, PortionSize.STANDARD)
        assertThat(e.confidence).isEqualTo(ConfidenceLevel.ROUGH)
        assertThat(e.caloriesKcal).isEqualTo(550)
    }

    @Test
    fun unknownDish_withOverride_usesEnteredCalories() {
        val e = engine.estimateUnknown(MealCategory.LUNCH, PortionSize.STANDARD, caloriesOverride = 400)
        assertThat(e.caloriesKcal).isEqualTo(400)
        assertThat(e.confidence).isEqualTo(ConfidenceLevel.MEDIUM)
    }
}

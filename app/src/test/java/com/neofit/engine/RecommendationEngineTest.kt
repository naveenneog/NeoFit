package com.neofit.engine

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RecommendationEngineTest {

    private val engine = RecommendationEngine()

    private fun inputs(
        consumed: Int = 0,
        target: Int = 2000,
        protein: Float = 0f,
        proteinTarget: Int = 80,
        steps: Int = 0,
        meals: Int = 0,
        water: Int = 0,
        hour: Int = 11,
    ) = RecommendationInputs(
        caloriesConsumed = consumed,
        caloriesBurned = 0,
        calorieTarget = target,
        proteinConsumedG = protein,
        proteinTargetG = proteinTarget,
        steps = steps,
        stepTarget = 8000,
        waterGlasses = water,
        waterTarget = 8,
        mealsLoggedToday = meals,
        hourOfDay = hour,
    )

    @Test
    fun noMealsByLateMorning_suggestsLoggingFirstMeal() {
        val recs = engine.nudges(inputs(meals = 0, hour = 11))
        assertThat(recs.map { it.id }).contains("log-first-meal")
    }

    @Test
    fun overBudget_warnsUser() {
        val recs = engine.nudges(inputs(consumed = 2500, target = 2000, meals = 3, hour = 20))
        assertThat(recs.map { it.id }).contains("over-budget")
    }

    @Test
    fun nearlyAtStepGoal_encourages() {
        val recs = engine.nudges(inputs(steps = 7000, meals = 2, hour = 18))
        assertThat(recs.map { it.id }).contains("steps-close")
    }

    @Test
    fun recommendationsAreSortedByPriorityDescending() {
        val recs = engine.nudges(inputs(consumed = 2500, target = 2000, protein = 0f, meals = 0, hour = 20))
        val priorities = recs.map { it.priority }
        assertThat(priorities).isInOrder(Comparator<Int> { a, b -> b.compareTo(a) })
    }
}

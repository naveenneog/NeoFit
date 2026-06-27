package com.neofit.engine

import com.neofit.domain.model.Recommendation
import com.neofit.domain.model.RecommendationType
import kotlin.math.roundToInt

/** Snapshot the recommendation engine reasons over. */
data class RecommendationInputs(
    val caloriesConsumed: Int,
    val caloriesBurned: Int,
    val calorieTarget: Int,
    val proteinConsumedG: Float,
    val proteinTargetG: Int,
    val steps: Int,
    val stepTarget: Int,
    val waterGlasses: Int,
    val waterTarget: Int,
    val mealsLoggedToday: Int,
    val hourOfDay: Int,
)

/**
 * Generates short, practical nudges from the day's data. Meal *suggestions*
 * come from the food repository; this engine produces the coaching messages.
 */
class RecommendationEngine {

    fun nudges(i: RecommendationInputs): List<Recommendation> {
        val out = mutableListOf<Recommendation>()
        val remaining = i.calorieTarget - i.caloriesConsumed + i.caloriesBurned

        if (i.mealsLoggedToday == 0 && i.hourOfDay >= 10) {
            out += Recommendation(
                id = "log-first-meal",
                type = RecommendationType.MEAL,
                title = "Start your day's log",
                message = "Aaj ka pehla meal log karein to track your calories.",
                priority = 90,
                actionRoute = "food_add",
            )
        }

        val proteinGap = i.proteinTargetG - i.proteinConsumedG
        if (i.proteinTargetG > 0 && proteinGap > i.proteinTargetG * 0.25f && i.hourOfDay >= 13) {
            out += Recommendation(
                id = "protein-gap",
                type = RecommendationType.NUDGE,
                title = "You're short on protein",
                message = "About ${proteinGap.roundToInt()} g to go. Dal, paneer, eggs, or curd can help.",
                priority = 70,
                actionRoute = "food_search",
            )
        }

        if (i.stepTarget > 0) {
            val pct = (i.steps.toFloat() / i.stepTarget * 100).roundToInt()
            if (pct in 70..99) {
                out += Recommendation(
                    id = "steps-close",
                    type = RecommendationType.NUDGE,
                    title = "Almost there!",
                    message = "You've already walked $pct% of your daily target. A short walk will close it.",
                    priority = 60,
                    actionRoute = "exercise",
                )
            } else if (pct < 30 && i.hourOfDay >= 18) {
                out += Recommendation(
                    id = "steps-low",
                    type = RecommendationType.EXERCISE,
                    title = "Move a little",
                    message = "Only $pct% of your steps so far. A 10-minute walk helps your goal.",
                    priority = 55,
                    actionRoute = "exercise",
                )
            }
        }

        if (remaining < 0) {
            out += Recommendation(
                id = "over-budget",
                type = RecommendationType.NUDGE,
                title = "Over today's budget",
                message = "A light dinner — khichdi or a salad — may help you stay on track.",
                priority = 80,
                actionRoute = "food_search",
            )
        } else if (remaining > i.calorieTarget * 0.4f && i.hourOfDay >= 20) {
            out += Recommendation(
                id = "under-budget",
                type = RecommendationType.MEAL,
                title = "You have room left",
                message = "Around $remaining kcal remaining. Don't skip a balanced dinner.",
                priority = 50,
                actionRoute = "food_search",
            )
        }

        if (i.waterTarget > 0 && i.waterGlasses < i.waterTarget * 0.5f && i.hourOfDay >= 14) {
            out += Recommendation(
                id = "hydrate",
                type = RecommendationType.HYDRATION,
                title = "Paani peeyein",
                message = "You've had ${i.waterGlasses} of ${i.waterTarget} glasses. Stay hydrated.",
                priority = 65,
                actionRoute = "home",
            )
        }

        return out.sortedByDescending { it.priority }
    }
}

package com.neofit.domain.usecase

import com.neofit.core.util.DateUtil
import com.neofit.domain.model.DashboardSummary
import com.neofit.domain.model.DietaryPreference
import com.neofit.domain.model.FoodRegion
import com.neofit.domain.model.MealCategory
import com.neofit.domain.model.MealLog
import com.neofit.domain.model.StepSummary
import com.neofit.domain.model.UserProfile
import com.neofit.domain.model.WeightEntry
import com.neofit.domain.model.effectiveDiet
import com.neofit.domain.model.isVegDay
import com.neofit.domain.repository.ActivityRepository
import com.neofit.domain.repository.ExerciseRepository
import com.neofit.domain.repository.FoodRepository
import com.neofit.domain.repository.MealLogRepository
import com.neofit.domain.repository.UserRepository
import com.neofit.domain.repository.WeightRepository
import com.neofit.engine.RecommendationEngine
import com.neofit.engine.RecommendationInputs
import com.neofit.engine.WellnessInputs
import com.neofit.engine.WellnessScoreEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalTime
import javax.inject.Inject

/** Streams the fully-aggregated Home dashboard snapshot. */
class GetDashboardUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val mealLogRepository: MealLogRepository,
    private val activityRepository: ActivityRepository,
    private val exerciseRepository: ExerciseRepository,
    private val weightRepository: WeightRepository,
    private val foodRepository: FoodRepository,
    private val wellnessEngine: WellnessScoreEngine,
    private val recommendationEngine: RecommendationEngine,
) {
    private data class Core(
        val profile: UserProfile?,
        val meals: List<MealLog>,
        val steps: StepSummary,
        val workoutBurned: Int,
        val water: Int,
    )

    operator fun invoke(): Flow<DashboardSummary> {
        val today = DateUtil.todayEpochDay()
        val core = combine(
            userRepository.observeProfile(),
            mealLogRepository.observeMealsForDay(today),
            activityRepository.observeSteps(today),
            exerciseRepository.observeCaloriesBurnedForDay(today),
            activityRepository.observeWater(today),
        ) { profile, meals, steps, burned, water -> Core(profile, meals, steps, burned, water) }

        return combine(
            core,
            weightRepository.observeHistory(),
            mealLogRepository.observeMealsBetween(today - 30, today),
        ) { c, weights, monthMeals -> build(today, c, weights, monthMeals) }
    }

    private fun build(
        today: Long,
        c: Core,
        weights: List<WeightEntry>,
        monthMeals: List<MealLog>,
    ): DashboardSummary {
        val profile = c.profile
        val calorieTarget = profile?.dailyCalorieTarget?.takeIf { it > 0 } ?: 2000
        val proteinTarget = profile?.dailyProteinTargetG?.takeIf { it > 0 } ?: 50
        val stepTarget = profile?.dailyStepTarget?.takeIf { it > 0 } ?: 8000
        val waterTarget = profile?.dailyWaterGlassTarget?.takeIf { it > 0 } ?: 8
        val region = profile?.preferredRegion ?: FoodRegion.PAN_INDIA
        val diet = profile?.effectiveDiet() ?: DietaryPreference.VEGETARIAN
        val isVegDay = profile?.isVegDay() ?: false

        val consumed = c.meals.sumOf { it.estimate.caloriesKcal }
        val protein = c.meals.fold(0f) { a, m -> a + m.estimate.proteinG }
        val carbs = c.meals.fold(0f) { a, m -> a + m.estimate.carbsG }
        val fat = c.meals.fold(0f) { a, m -> a + m.estimate.fatG }
        val burned = c.steps.activeCaloriesKcal + c.workoutBurned

        val currentWeight = weights.lastOrNull()?.weightKg ?: profile?.currentWeightKg ?: 0f
        val weeklyDelta = weeklyWeightDelta(weights, today)

        val distinctDays = monthMeals.map { it.epochDay }.toSet()
        val loggedDaysInWeek = distinctDays.count { it > today - 7 }
        val streak = streak(distinctDays, today)
        val workoutsToday = if (c.workoutBurned > 0) 1 else 0
        val hour = LocalTime.now().hour

        val wellness = wellnessEngine.compute(
            WellnessInputs(
                dateEpochDay = today,
                loggedDaysInWeek = loggedDaysInWeek,
                steps = c.steps.steps,
                stepTarget = stepTarget,
                caloriesConsumed = consumed,
                calorieTarget = calorieTarget,
                workoutsToday = workoutsToday,
            ),
        )

        val remaining = calorieTarget - consumed + burned
        val recommendations = recommendationEngine.nudges(
            RecommendationInputs(
                caloriesConsumed = consumed,
                caloriesBurned = burned,
                calorieTarget = calorieTarget,
                proteinConsumedG = protein,
                proteinTargetG = proteinTarget,
                steps = c.steps.steps,
                stepTarget = stepTarget,
                waterGlasses = c.water,
                waterTarget = waterTarget,
                mealsLoggedToday = c.meals.size,
                hourOfDay = hour,
            ),
        )

        val recommendedMeals = foodRepository.recommended(
            remainingKcal = remaining,
            region = region,
            diet = diet,
            category = nextMealCategory(hour),
            limit = 6,
        )

        return DashboardSummary(
            dateEpochDay = today,
            userName = profile?.name.orEmpty(),
            caloriesConsumed = consumed,
            caloriesBurned = burned,
            calorieTarget = calorieTarget,
            proteinConsumedG = protein,
            carbsConsumedG = carbs,
            fatConsumedG = fat,
            proteinTargetG = proteinTarget,
            steps = c.steps.steps,
            stepTarget = stepTarget,
            stepSource = c.steps.source,
            currentWeightKg = currentWeight,
            weeklyWeightDeltaKg = weeklyDelta,
            streakDays = streak,
            waterGlasses = c.water,
            waterTarget = waterTarget,
            wellness = wellness,
            recommendations = recommendations,
            recommendedMeals = recommendedMeals,
            isVegDayToday = isVegDay,
        )
    }

    private fun nextMealCategory(hour: Int): MealCategory = when {
        hour < 11 -> MealCategory.BREAKFAST
        hour < 16 -> MealCategory.LUNCH
        hour < 19 -> MealCategory.SNACK
        else -> MealCategory.DINNER
    }

    private fun streak(days: Set<Long>, today: Long): Int {
        var start = when {
            days.contains(today) -> today
            days.contains(today - 1) -> today - 1
            else -> return 0
        }
        var count = 0
        while (days.contains(start)) {
            count++
            start--
        }
        return count
    }

    private fun weeklyWeightDelta(weights: List<WeightEntry>, today: Long): Float {
        if (weights.isEmpty()) return 0f
        val latest = weights.last().weightKg
        val reference = weights.lastOrNull { it.dateEpochDay <= today - 7 } ?: weights.first()
        return latest - reference.weightKg
    }
}

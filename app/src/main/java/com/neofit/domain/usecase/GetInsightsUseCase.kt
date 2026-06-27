package com.neofit.domain.usecase

import com.neofit.core.util.DateUtil
import com.neofit.domain.model.FoodRegion
import com.neofit.domain.model.MealLog
import com.neofit.domain.model.Recommendation
import com.neofit.domain.model.StepSummary
import com.neofit.domain.model.WellnessSummary
import com.neofit.domain.repository.ActivityRepository
import com.neofit.domain.repository.ExerciseRepository
import com.neofit.domain.repository.MealLogRepository
import com.neofit.domain.repository.UserRepository
import com.neofit.engine.RecommendationEngine
import com.neofit.engine.RecommendationInputs
import com.neofit.engine.RegionInsight
import com.neofit.engine.WellnessInputs
import com.neofit.engine.WellnessScoreEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalTime
import javax.inject.Inject

data class DayValue(val epochDay: Long, val value: Int)

data class InsightsData(
    val regionInsight: RegionInsight,
    val wellness: WellnessSummary,
    val recommendations: List<Recommendation>,
    val weeklySteps: List<StepSummary>,
    val weeklyCalories: List<DayValue>,
)

/** Streams the data behind the Insights/recommendations screen. */
class GetInsightsUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val mealLogRepository: MealLogRepository,
    private val activityRepository: ActivityRepository,
    private val exerciseRepository: ExerciseRepository,
    private val classifyRegion: ClassifyRegionUseCase,
    private val wellnessEngine: WellnessScoreEngine,
    private val recommendationEngine: RecommendationEngine,
) {
    operator fun invoke(): Flow<InsightsData> {
        val today = DateUtil.todayEpochDay()
        return combine(
            userRepository.observeProfile(),
            mealLogRepository.observeMealsBetween(today - 30, today),
            activityRepository.observeWeeklySteps(),
            exerciseRepository.observeCaloriesBurnedForDay(today),
            activityRepository.observeWater(today),
        ) { profile, monthMeals, weeklySteps, burned, water ->
            val todayMeals: List<MealLog> = monthMeals.filter { it.epochDay == today }
            val consumed = todayMeals.sumOf { it.estimate.caloriesKcal }
            val protein = todayMeals.fold(0f) { a, m -> a + m.estimate.proteinG }
            val todaySteps = weeklySteps.lastOrNull()?.steps ?: 0

            val calorieTarget = profile?.dailyCalorieTarget?.takeIf { it > 0 } ?: 2000
            val proteinTarget = profile?.dailyProteinTargetG?.takeIf { it > 0 } ?: 50
            val stepTarget = profile?.dailyStepTarget?.takeIf { it > 0 } ?: 8000
            val waterTarget = profile?.dailyWaterGlassTarget?.takeIf { it > 0 } ?: 8
            val region = profile?.preferredRegion ?: FoodRegion.PAN_INDIA
            val hour = LocalTime.now().hour

            val loggedDaysInWeek = monthMeals.map { it.epochDay }.toSet().count { it > today - 7 }

            val wellness = wellnessEngine.compute(
                WellnessInputs(today, loggedDaysInWeek, todaySteps, stepTarget, consumed, calorieTarget, if (burned > 0) 1 else 0),
            )

            val recommendations = recommendationEngine.nudges(
                RecommendationInputs(
                    caloriesConsumed = consumed,
                    caloriesBurned = burned + (weeklySteps.lastOrNull()?.activeCaloriesKcal ?: 0),
                    calorieTarget = calorieTarget,
                    proteinConsumedG = protein,
                    proteinTargetG = proteinTarget,
                    steps = todaySteps,
                    stepTarget = stepTarget,
                    waterGlasses = water,
                    waterTarget = waterTarget,
                    mealsLoggedToday = todayMeals.size,
                    hourOfDay = hour,
                ),
            )

            val regionInsight = classifyRegion(monthMeals, region)

            val weeklyCalories = DateUtil.lastDays(7).map { day ->
                DayValue(day, monthMeals.filter { it.epochDay == day }.sumOf { it.estimate.caloriesKcal })
            }

            // diet is read to keep insights diet-aware for future suggestions.

            InsightsData(
                regionInsight = regionInsight,
                wellness = wellness,
                recommendations = recommendations,
                weeklySteps = weeklySteps,
                weeklyCalories = weeklyCalories,
            )
        }
    }
}

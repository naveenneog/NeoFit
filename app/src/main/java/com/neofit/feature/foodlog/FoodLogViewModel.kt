package com.neofit.feature.foodlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neofit.core.util.DateUtil
import com.neofit.domain.model.FoodItem
import com.neofit.domain.model.LogSource
import com.neofit.domain.model.MealCategory
import com.neofit.domain.model.MealLog
import com.neofit.domain.repository.FoodRepository
import com.neofit.domain.repository.MealLogRepository
import com.neofit.domain.usecase.EstimateMealUseCase
import com.neofit.domain.usecase.LogMealParams
import com.neofit.domain.usecase.LogMealUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FoodLogState(
    val meals: List<MealLog> = emptyList(),
    val totalKcal: Int = 0,
    val byCategory: Map<MealCategory, List<MealLog>> = emptyMap(),
    val recents: List<MealLog> = emptyList(),
    val favourites: List<FoodItem> = emptyList(),
)

@HiltViewModel
class FoodLogViewModel @Inject constructor(
    private val mealLogRepository: MealLogRepository,
    private val foodRepository: FoodRepository,
    private val estimateMeal: EstimateMealUseCase,
    private val logMeal: LogMealUseCase,
) : ViewModel() {

    val state: StateFlow<FoodLogState> =
        combine(
            mealLogRepository.observeMealsForDay(DateUtil.todayEpochDay()),
            mealLogRepository.observeRecentDistinct(8),
            foodRepository.observeFavourites(),
        ) { meals, recents, favIds ->
            FoodLogState(
                meals = meals,
                totalKcal = meals.sumOf { it.estimate.caloriesKcal },
                byCategory = meals.groupBy { it.category },
                recents = recents,
                favourites = favIds.mapNotNull { foodRepository.getById(it) },
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FoodLogState())

    fun delete(id: Long) = viewModelScope.launch { mealLogRepository.delete(id) }

    fun repeatLast() = viewModelScope.launch {
        mealLogRepository.lastMeal()?.let { quickAddRecent(it) }
    }

    /** One-tap re-log of a previously logged meal (keeps its portion/estimate). */
    fun quickAddRecent(meal: MealLog) = viewModelScope.launch {
        mealLogRepository.add(meal.copy(id = 0, timestampEpochMillis = DateUtil.nowMillis()))
    }

    /** One-tap log of a favourite dish at its standard serving. */
    fun quickAddFavourite(food: FoodItem) = viewModelScope.launch {
        val estimate = estimateMeal.forFood(food, food.baseServing)
        logMeal(
            LogMealParams(
                foodId = food.id,
                name = food.nameEn,
                category = food.typicalCategory,
                region = food.region,
                portion = food.baseServing,
                estimate = estimate,
                source = LogSource.SEARCH,
            ),
        )
    }
}

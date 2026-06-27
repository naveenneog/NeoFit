package com.neofit.feature.foodlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neofit.core.util.DateUtil
import com.neofit.domain.model.MealCategory
import com.neofit.domain.model.MealLog
import com.neofit.domain.repository.MealLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FoodLogState(
    val meals: List<MealLog> = emptyList(),
    val totalKcal: Int = 0,
    val byCategory: Map<MealCategory, List<MealLog>> = emptyMap(),
)

@HiltViewModel
class FoodLogViewModel @Inject constructor(
    private val mealLogRepository: MealLogRepository,
) : ViewModel() {

    val state: StateFlow<FoodLogState> =
        mealLogRepository.observeMealsForDay(DateUtil.todayEpochDay())
            .map { meals ->
                FoodLogState(
                    meals = meals,
                    totalKcal = meals.sumOf { it.estimate.caloriesKcal },
                    byCategory = meals.groupBy { it.category },
                )
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FoodLogState())

    fun delete(id: Long) = viewModelScope.launch { mealLogRepository.delete(id) }

    fun repeatLast() = viewModelScope.launch {
        mealLogRepository.lastMeal()?.let { last ->
            mealLogRepository.add(last.copy(id = 0, timestampEpochMillis = DateUtil.nowMillis()))
        }
    }
}

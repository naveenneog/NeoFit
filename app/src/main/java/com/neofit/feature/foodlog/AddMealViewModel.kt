package com.neofit.feature.foodlog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neofit.domain.model.CookingStyle
import com.neofit.domain.model.FoodItem
import com.neofit.domain.model.FoodRegion
import com.neofit.domain.model.LogSource
import com.neofit.domain.model.MealCategory
import com.neofit.domain.model.NutritionEstimate
import com.neofit.domain.model.PortionSize
import com.neofit.domain.repository.FoodRepository
import com.neofit.domain.repository.MealLogRepository
import com.neofit.domain.repository.UserRepository
import com.neofit.domain.usecase.EstimateMealUseCase
import com.neofit.domain.usecase.LogMealParams
import com.neofit.domain.usecase.LogMealUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

data class AddMealState(
    val food: FoodItem? = null,
    val isCustom: Boolean = false,
    val isEditing: Boolean = false,
    val name: String = "",
    val category: MealCategory = MealCategory.SNACK,
    val portion: PortionSize = PortionSize.STANDARD,
    val cooking: CookingStyle? = null,
    val manualCalories: String = "",
    val region: FoodRegion = FoodRegion.PAN_INDIA,
    val estimate: NutritionEstimate? = null,
    val manuallyCorrected: Boolean = false,
    val saving: Boolean = false,
    val saved: Boolean = false,
    val isDirty: Boolean = false,
) {
    val portionOptions: List<PortionSize> = PortionSize.COMMON
    val cookingOptions: List<CookingStyle> = CookingStyle.entries
}

@HiltViewModel
class AddMealViewModel @Inject constructor(
    private val foodRepository: FoodRepository,
    private val estimateMeal: EstimateMealUseCase,
    private val logMeal: LogMealUseCase,
    private val mealLogRepository: MealLogRepository,
    private val userRepository: UserRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow(AddMealState(category = defaultCategory()))
    val state: StateFlow<AddMealState> = _state.asStateFlow()

    private var editingId: Long? = null

    private data class FormSnapshot(
        val name: String,
        val category: MealCategory,
        val portion: PortionSize,
        val cooking: CookingStyle?,
        val manualCalories: String,
    )

    private fun snapshotOf(s: AddMealState) =
        FormSnapshot(s.name.trim(), s.category, s.portion, s.cooking, s.manualCalories)

    private var baseline: FormSnapshot? = null

    init {
        val editId = savedStateHandle.get<Long>("mealId") ?: -1L
        val foodId = savedStateHandle.get<String>("foodId")
        viewModelScope.launch {
            val profileRegion = userRepository.getProfile()?.preferredRegion ?: FoodRegion.PAN_INDIA
            when {
                editId > 0 -> {
                    val meal = mealLogRepository.getById(editId)
                    if (meal != null) {
                        editingId = meal.id
                        val food = meal.foodId?.let { foodRepository.getById(it) }
                        _state.value = _state.value.copy(
                            food = food,
                            isCustom = meal.foodId == null,
                            isEditing = true,
                            name = meal.name,
                            category = meal.category,
                            portion = meal.portion,
                            region = meal.region,
                            manuallyCorrected = meal.manuallyCorrected,
                            manualCalories = if (meal.manuallyCorrected) meal.estimate.caloriesKcal.toString() else "",
                        )
                    }
                }
                else -> {
                    val food = foodId?.let { foodRepository.getById(it) }
                    _state.value = if (food != null) {
                        _state.value.copy(
                            food = food,
                            isCustom = false,
                            name = food.nameEn,
                            category = food.typicalCategory,
                            portion = food.baseServing,
                            region = food.region,
                        )
                    } else {
                        _state.value.copy(isCustom = true, region = profileRegion)
                    }
                }
            }
            recompute()
            baseline = snapshotOf(_state.value)
        }
    }

    fun setName(value: String) { _state.value = _state.value.copy(name = value); recompute() }

    fun setCategory(value: MealCategory) { _state.value = _state.value.copy(category = value); recompute() }

    fun setPortion(value: PortionSize) { _state.value = _state.value.copy(portion = value); recompute() }

    fun setCooking(value: CookingStyle?) { _state.value = _state.value.copy(cooking = value); recompute() }

    fun setManualCalories(value: String) {
        _state.value = _state.value.copy(manualCalories = value.filter { it.isDigit() }, manuallyCorrected = true)
        recompute()
    }

    fun setRegion(value: FoodRegion) { _state.value = _state.value.copy(region = value) }

    private fun recompute() {
        val s = _state.value
        val estimate = when {
            s.food != null -> estimateMeal.forFood(s.food, s.portion, s.cooking, s.manuallyCorrected && s.manualCalories.isNotEmpty())
                .let { base ->
                    val override = s.manualCalories.toIntOrNull()
                    if (override != null) base.copy(caloriesKcal = override, basis = "Calories edited by you.", isApproximate = false) else base
                }
            else -> estimateMeal.forUnknown(s.category, s.portion, s.manualCalories.toIntOrNull())
        }
        _state.value = _state.value.copy(estimate = estimate)
        val dirty = baseline?.let { snapshotOf(_state.value) != it } ?: false
        _state.value = _state.value.copy(isDirty = dirty)
    }

    fun save(onDone: () -> Unit) {
        val s = _state.value
        val estimate = s.estimate ?: return
        if (s.name.isBlank()) return
        _state.value = s.copy(saving = true)
        viewModelScope.launch {
            val editId = editingId
            if (editId != null) {
                mealLogRepository.getById(editId)?.let { existing ->
                    mealLogRepository.update(
                        existing.copy(
                            name = s.name.trim(),
                            category = s.category,
                            region = s.region,
                            portion = s.portion,
                            estimate = estimate,
                            manuallyCorrected = s.manuallyCorrected,
                        ),
                    )
                }
            } else {
                logMeal(
                    LogMealParams(
                        foodId = s.food?.id,
                        name = s.name.trim(),
                        category = s.category,
                        region = s.region,
                        portion = s.portion,
                        estimate = estimate,
                        source = if (s.isCustom) LogSource.MANUAL else LogSource.SEARCH,
                        manuallyCorrected = s.manuallyCorrected,
                    ),
                )
            }
            _state.value = _state.value.copy(saving = false, saved = true)
            onDone()
        }
    }

    private fun defaultCategory(): MealCategory {
        val hour = LocalTime.now().hour
        return when {
            hour < 11 -> MealCategory.BREAKFAST
            hour < 16 -> MealCategory.LUNCH
            hour < 19 -> MealCategory.SNACK
            else -> MealCategory.DINNER
        }
    }
}

package com.neofit.feature.foodlog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neofit.domain.model.MealLog
import com.neofit.domain.repository.ImageRepository
import com.neofit.domain.repository.MealLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MealDetailState(
    val meal: MealLog? = null,
    val imageRef: String? = null,
    val generating: Boolean = false,
    val imageMessage: String? = null,
)

@HiltViewModel
class MealDetailViewModel @Inject constructor(
    private val mealLogRepository: MealLogRepository,
    private val imageRepository: ImageRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val mealId: Long = savedStateHandle.get<Long>("mealId") ?: 0L
    private val _state = MutableStateFlow(MealDetailState())
    val state: StateFlow<MealDetailState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val meal = mealLogRepository.getById(mealId)
            val fallback = meal?.imageRef ?: meal?.foodId?.let { "file:///android_asset/food/$it.jpg" }
            _state.value = _state.value.copy(meal = meal, imageRef = fallback)
        }
    }

    fun generateImage() {
        val meal = _state.value.meal ?: return
        if (_state.value.generating) return
        _state.value = _state.value.copy(generating = true, imageMessage = null)
        viewModelScope.launch {
            val asset = imageRepository.getOrFetchFoodImageByName(meal.name)
            val ref = asset.bestRef()
            if (ref != null) {
                mealLogRepository.update(meal.copy(imageRef = ref))
                _state.value = _state.value.copy(generating = false, imageRef = ref, meal = meal.copy(imageRef = ref))
            } else {
                _state.value = _state.value.copy(
                    generating = false,
                    imageMessage = "Couldn't generate a photo. AI image generation runs on Azure via your Azure AD sign-in and isn't enabled in this build.",
                )
            }
        }
    }

    fun delete(onDone: () -> Unit) {
        viewModelScope.launch {
            mealLogRepository.delete(mealId)
            onDone()
        }
    }
}

package com.neofit.feature.foodlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neofit.integration.ai.FoodPrediction
import com.neofit.integration.ai.FoodRecognitionService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PhotoLogState(
    val imageUri: String? = null,
    val analyzing: Boolean = false,
    val predictions: List<FoodPrediction> = emptyList(),
)

@HiltViewModel
class PhotoFoodLogViewModel @Inject constructor(
    private val recognitionService: FoodRecognitionService,
) : ViewModel() {

    private val _state = MutableStateFlow(PhotoLogState())
    val state: StateFlow<PhotoLogState> = _state.asStateFlow()

    fun onImageCaptured(uri: String?) {
        _state.value = _state.value.copy(imageUri = uri, analyzing = true)
        viewModelScope.launch {
            // On-device ML Kit labeling maps the photo to candidate dishes.
            val predictions = recognitionService.recognize(imageUri = uri, hint = null)
            _state.value = _state.value.copy(analyzing = false, predictions = predictions)
        }
    }

    fun refineWithHint(hint: String) {
        _state.value = _state.value.copy(analyzing = true)
        viewModelScope.launch {
            val predictions = recognitionService.recognize(imageUri = _state.value.imageUri, hint = hint)
            _state.value = _state.value.copy(analyzing = false, predictions = predictions)
        }
    }
}

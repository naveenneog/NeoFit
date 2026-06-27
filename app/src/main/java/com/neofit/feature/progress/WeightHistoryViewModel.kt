package com.neofit.feature.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neofit.domain.model.WeightEntry
import com.neofit.domain.repository.WeightRepository
import com.neofit.domain.usecase.LogWeightUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeightHistoryViewModel @Inject constructor(
    weightRepository: WeightRepository,
    private val logWeight: LogWeightUseCase,
) : ViewModel() {

    val history: StateFlow<List<WeightEntry>> =
        weightRepository.observeHistory()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun log(weightKg: Float) = viewModelScope.launch { logWeight(weightKg) }
}

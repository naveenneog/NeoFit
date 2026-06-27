package com.neofit.feature.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neofit.domain.usecase.GetInsightsUseCase
import com.neofit.domain.usecase.InsightsData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class InsightsViewModel @Inject constructor(
    getInsights: GetInsightsUseCase,
) : ViewModel() {
    val state: StateFlow<InsightsData?> =
        getInsights().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}

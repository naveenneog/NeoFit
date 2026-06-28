package com.neofit.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neofit.core.common.UiState
import com.neofit.core.util.DateUtil
import com.neofit.domain.model.DashboardSummary
import com.neofit.domain.repository.ActivityRepository
import com.neofit.domain.usecase.GetDashboardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    getDashboard: GetDashboardUseCase,
    private val activityRepository: ActivityRepository,
) : ViewModel() {

    val state: StateFlow<UiState<DashboardSummary>> = getDashboard()
        .map<DashboardSummary, UiState<DashboardSummary>> { UiState.Success(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    init {
        // Pull today's activity (Health Connect when available) on entry.
        viewModelScope.launch { activityRepository.syncToday() }
    }

    fun addWater() = viewModelScope.launch {
        activityRepository.addWater(DateUtil.todayEpochDay(), 1)
    }

    fun syncSteps() = viewModelScope.launch { activityRepository.syncToday() }
}

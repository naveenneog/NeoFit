package com.neofit.feature.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neofit.domain.model.StepSummary
import com.neofit.domain.model.WeightEntry
import com.neofit.domain.repository.ActivityRepository
import com.neofit.domain.repository.UserRepository
import com.neofit.domain.repository.WeightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ProgressState(
    val weights: List<WeightEntry> = emptyList(),
    val startWeightKg: Float = 0f,
    val currentWeightKg: Float = 0f,
    val targetWeightKg: Float = 0f,
    val bmi: Float = 0f,
    val weeklySteps: List<StepSummary> = emptyList(),
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    weightRepository: WeightRepository,
    userRepository: UserRepository,
    activityRepository: ActivityRepository,
) : ViewModel() {

    val state: StateFlow<ProgressState> = combine(
        weightRepository.observeHistory(),
        userRepository.observeProfile(),
        activityRepository.observeWeeklySteps(),
    ) { weights, profile, steps ->
        ProgressState(
            weights = weights,
            startWeightKg = weights.firstOrNull()?.weightKg ?: profile?.currentWeightKg ?: 0f,
            currentWeightKg = weights.lastOrNull()?.weightKg ?: profile?.currentWeightKg ?: 0f,
            targetWeightKg = profile?.targetWeightKg ?: 0f,
            bmi = profile?.bmi ?: 0f,
            weeklySteps = steps,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProgressState())
}

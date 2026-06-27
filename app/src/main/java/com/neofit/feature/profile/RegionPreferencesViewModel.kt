package com.neofit.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neofit.core.util.DateUtil
import com.neofit.domain.model.FoodRegion
import com.neofit.domain.model.UserProfile
import com.neofit.domain.repository.MealLogRepository
import com.neofit.domain.repository.UserRepository
import com.neofit.domain.usecase.ClassifyRegionUseCase
import com.neofit.engine.RegionInsight
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegionPrefsState(
    val profile: UserProfile? = null,
    val insight: RegionInsight? = null,
)

@HiltViewModel
class RegionPreferencesViewModel @Inject constructor(
    private val userRepository: UserRepository,
    mealLogRepository: MealLogRepository,
    classifyRegion: ClassifyRegionUseCase,
) : ViewModel() {

    private val today = DateUtil.todayEpochDay()

    val state: StateFlow<RegionPrefsState> = combine(
        userRepository.observeProfile(),
        mealLogRepository.observeMealsBetween(today - 30, today),
    ) { profile, meals ->
        RegionPrefsState(
            profile = profile,
            insight = classifyRegion(meals, profile?.preferredRegion ?: FoodRegion.MIXED),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RegionPrefsState())

    fun setRegion(region: FoodRegion) = viewModelScope.launch {
        userRepository.getProfile()?.let { profile ->
            userRepository.upsertProfile(profile.copy(preferredRegion = region))
        }
    }
}

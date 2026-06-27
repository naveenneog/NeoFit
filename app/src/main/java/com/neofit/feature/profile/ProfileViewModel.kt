package com.neofit.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neofit.domain.model.AppLanguage
import com.neofit.domain.model.Goal
import com.neofit.domain.model.UserProfile
import com.neofit.domain.repository.PreferencesRepository
import com.neofit.domain.repository.UserRepository
import com.neofit.domain.usecase.ComputeGoalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val profile: UserProfile? = null,
    val goal: Goal? = null,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val darkTheme: Boolean? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    userRepository: UserRepository,
    private val preferencesRepository: PreferencesRepository,
    private val computeGoal: ComputeGoalUseCase,
) : ViewModel() {

    val state: StateFlow<ProfileState> = combine(
        userRepository.observeProfile(),
        preferencesRepository.observeLanguage(),
        preferencesRepository.observeDarkTheme(),
    ) { profile, language, dark ->
        ProfileState(
            profile = profile,
            goal = profile?.let { computeGoal(it) },
            language = language,
            darkTheme = dark,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileState())

    fun setLanguage(language: AppLanguage) = viewModelScope.launch {
        preferencesRepository.setLanguage(language)
    }

    fun setDarkTheme(value: Boolean?) = viewModelScope.launch {
        preferencesRepository.setDarkTheme(value)
    }
}

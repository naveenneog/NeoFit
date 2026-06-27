package com.neofit.feature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neofit.domain.model.AppLanguage
import com.neofit.domain.repository.PreferencesRepository
import com.neofit.feature.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppUiState(
    val loading: Boolean = true,
    val onboardingComplete: Boolean = false,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val darkTheme: Boolean? = null,
) {
    val startDestination: String
        get() = if (onboardingComplete) Routes.HOME else Routes.ONBOARDING
}

@HiltViewModel
class AppViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    val state: StateFlow<AppUiState> = combine(
        preferencesRepository.observeOnboardingComplete(),
        preferencesRepository.observeLanguage(),
        preferencesRepository.observeDarkTheme(),
    ) { onboarded, language, dark ->
        AppUiState(loading = false, onboardingComplete = onboarded, language = language, darkTheme = dark)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, AppUiState())

    fun setLanguage(language: AppLanguage) = viewModelScope.launch {
        preferencesRepository.setLanguage(language)
    }

    fun setDarkTheme(value: Boolean?) = viewModelScope.launch {
        preferencesRepository.setDarkTheme(value)
    }
}

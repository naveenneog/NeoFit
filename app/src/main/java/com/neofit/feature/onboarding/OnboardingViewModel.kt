package com.neofit.feature.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neofit.core.i18n.LocaleManager
import com.neofit.domain.model.ActivityLevel
import com.neofit.domain.model.AppLanguage
import com.neofit.domain.model.DietaryPreference
import com.neofit.domain.model.FoodRegion
import com.neofit.domain.model.Sex
import com.neofit.domain.model.UserProfile
import com.neofit.domain.model.WellnessGoal
import com.neofit.domain.usecase.CompleteOnboardingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingDraft(
    val step: Int = 0,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val name: String = "",
    val age: String = "25",
    val sex: Sex? = null,
    val heightCm: String = "165",
    val currentWeightKg: String = "65",
    val targetWeightKg: String = "60",
    val activityLevel: ActivityLevel = ActivityLevel.MODERATE,
    val diet: DietaryPreference = DietaryPreference.VEGETARIAN,
    val restrictions: String = "",
    val goal: WellnessGoal = WellnessGoal.GENERAL_WELLNESS,
    val region: FoodRegion = FoodRegion.MIXED,
    val saving: Boolean = false,
    val finished: Boolean = false,
) {
    val totalSteps: Int = 7

    val canProceed: Boolean
        get() = when (step) {
            1 -> name.isNotBlank() && age.toIntOrNull() != null && sex != null
            2 -> heightCm.toFloatOrNull() != null && currentWeightKg.toFloatOrNull() != null &&
                targetWeightKg.toFloatOrNull() != null
            else -> true
        }
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val completeOnboarding: CompleteOnboardingUseCase,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingDraft())
    val state: StateFlow<OnboardingDraft> = _state.asStateFlow()

    fun update(transform: (OnboardingDraft) -> OnboardingDraft) {
        _state.value = transform(_state.value)
    }

    fun next() {
        val s = _state.value
        if (s.step < s.totalSteps - 1) _state.value = s.copy(step = s.step + 1) else finish()
    }

    fun back() {
        val s = _state.value
        if (s.step > 0) _state.value = s.copy(step = s.step - 1)
    }

    private fun finish() {
        val s = _state.value
        if (s.saving) return
        _state.value = s.copy(saving = true)
        viewModelScope.launch {
            val draft = UserProfile(
                name = s.name.trim(),
                age = s.age.toIntOrNull() ?: 25,
                sex = s.sex ?: Sex.OTHER,
                heightCm = s.heightCm.toFloatOrNull() ?: 165f,
                currentWeightKg = s.currentWeightKg.toFloatOrNull() ?: 65f,
                targetWeightKg = s.targetWeightKg.toFloatOrNull() ?: 60f,
                activityLevel = s.activityLevel,
                dietaryPreference = s.diet,
                goal = s.goal,
                preferredRegion = s.region,
                language = s.language,
                foodRestrictions = s.restrictions.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            )
            completeOnboarding(draft)
            LocaleManager.persist(context, s.language.localeTag)
            _state.value = _state.value.copy(saving = false, finished = true)
        }
    }
}

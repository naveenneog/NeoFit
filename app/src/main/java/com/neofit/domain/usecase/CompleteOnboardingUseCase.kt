package com.neofit.domain.usecase

import com.neofit.core.util.DateUtil
import com.neofit.domain.model.UserProfile
import com.neofit.domain.model.WeightEntry
import com.neofit.domain.repository.PreferencesRepository
import com.neofit.domain.repository.UserRepository
import com.neofit.domain.repository.WeightRepository
import javax.inject.Inject

/**
 * Finalises onboarding: computes calorie/protein targets, persists the profile,
 * records the starting weight, and flips the onboarding-complete flag.
 */
class CompleteOnboardingUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val weightRepository: WeightRepository,
    private val preferencesRepository: PreferencesRepository,
    private val computeGoal: ComputeGoalUseCase,
) {
    suspend operator fun invoke(draft: UserProfile): UserProfile {
        val goal = computeGoal(draft)
        val today = DateUtil.todayEpochDay()
        val profile = draft.copy(
            dailyCalorieTarget = goal.dailyCalorieTarget,
            dailyProteinTargetG = goal.dailyProteinTargetG,
            onboardingComplete = true,
            createdAtEpochDay = today,
            updatedAtEpochMillis = DateUtil.nowMillis(),
        )
        userRepository.upsertProfile(profile)
        weightRepository.add(WeightEntry(weightKg = profile.currentWeightKg, dateEpochDay = today))
        preferencesRepository.setLanguage(profile.language)
        preferencesRepository.setOnboardingComplete(true)
        return profile
    }
}

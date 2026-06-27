package com.neofit.domain.usecase

import com.neofit.core.util.DateUtil
import com.neofit.domain.model.WeightEntry
import com.neofit.domain.repository.UserRepository
import com.neofit.domain.repository.WeightRepository
import javax.inject.Inject

/** Records a weight measurement and keeps the profile's current weight in sync. */
class LogWeightUseCase @Inject constructor(
    private val weightRepository: WeightRepository,
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(weightKg: Float, note: String? = null) {
        val today = DateUtil.todayEpochDay()
        weightRepository.add(WeightEntry(weightKg = weightKg, dateEpochDay = today, note = note))
        userRepository.getProfile()?.let { profile ->
            userRepository.upsertProfile(
                profile.copy(
                    currentWeightKg = weightKg,
                    updatedAtEpochMillis = DateUtil.nowMillis(),
                ),
            )
        }
    }
}

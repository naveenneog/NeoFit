package com.neofit.feature.exercise

import androidx.lifecycle.ViewModel
import com.neofit.domain.model.ExercisePlan
import com.neofit.domain.repository.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ExercisePlansViewModel @Inject constructor(
    exerciseRepository: ExerciseRepository,
) : ViewModel() {
    val plans: List<ExercisePlan> = exerciseRepository.plans()
}

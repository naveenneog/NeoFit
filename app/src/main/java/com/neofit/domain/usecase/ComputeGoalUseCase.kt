package com.neofit.domain.usecase

import com.neofit.domain.model.Goal
import com.neofit.domain.model.UserProfile
import com.neofit.engine.CalorieMath
import javax.inject.Inject

/** Derives an explicit, explainable [Goal] (targets + rationale) from a profile. */
class ComputeGoalUseCase @Inject constructor() {
    operator fun invoke(profile: UserProfile): Goal {
        val bmr = CalorieMath.bmr(profile.sex, profile.currentWeightKg, profile.heightCm, profile.age)
        val tdee = CalorieMath.tdee(bmr, profile.activityLevel)
        val target = CalorieMath.calorieTarget(tdee, profile.goal, profile.sex)
        val protein = CalorieMath.proteinTargetG(profile.goal, profile.currentWeightKg)
        val weekly = CalorieMath.weeklyRateKg(profile.goal)

        val rationale = buildString {
            append("BMR ≈ $bmr kcal, TDEE ≈ $tdee kcal at ${profile.activityLevel.label.lowercase()}. ")
            append("Target ${target} kcal/day for ${profile.goal.label.lowercase()}")
            if (weekly != 0f) append(" (≈ ${"%.2f".format(kotlin.math.abs(weekly))} kg/week)")
            append(". Protein ≈ $protein g/day. These are estimates you can adjust.")
        }

        return Goal(
            type = profile.goal,
            startWeightKg = profile.currentWeightKg,
            targetWeightKg = profile.targetWeightKg,
            weeklyRateKg = weekly,
            dailyCalorieTarget = target,
            dailyProteinTargetG = protein,
            dailyStepTarget = profile.dailyStepTarget,
            tdeeKcal = tdee,
            bmrKcal = bmr,
            rationale = rationale,
        )
    }
}

package com.neofit.engine

import com.google.common.truth.Truth.assertThat
import com.neofit.domain.model.ActivityLevel
import com.neofit.domain.model.Sex
import com.neofit.domain.model.WellnessGoal
import org.junit.Test

class CalorieMathTest {

    @Test
    fun bmr_male_matchesMifflinStJeor() {
        // 10*70 + 6.25*175 - 5*30 + 5 = 1648.75 -> 1649
        assertThat(CalorieMath.bmr(Sex.MALE, 70f, 175f, 30)).isEqualTo(1649)
    }

    @Test
    fun bmr_female_isLowerThanMale() {
        val male = CalorieMath.bmr(Sex.MALE, 70f, 175f, 30)
        val female = CalorieMath.bmr(Sex.FEMALE, 70f, 175f, 30)
        assertThat(female).isLessThan(male)
        assertThat(male - female).isEqualTo(166) // +5 vs -161
    }

    @Test
    fun tdee_appliesActivityMultiplier() {
        assertThat(CalorieMath.tdee(1600, ActivityLevel.MODERATE)).isEqualTo(2480)
    }

    @Test
    fun calorieTarget_weightLoss_appliesDeficit() {
        assertThat(CalorieMath.calorieTarget(2500, WellnessGoal.WEIGHT_LOSS, Sex.MALE)).isEqualTo(2000)
    }

    @Test
    fun calorieTarget_respectsFemaleFloor() {
        assertThat(CalorieMath.calorieTarget(1500, WellnessGoal.WEIGHT_LOSS, Sex.FEMALE)).isEqualTo(1200)
    }

    @Test
    fun proteinTarget_weightLoss_usesHigherPerKg() {
        assertThat(CalorieMath.proteinTargetG(WellnessGoal.WEIGHT_LOSS, 70f)).isEqualTo(112)
    }

    @Test
    fun caloriesFromSteps_scalesWithWeight() {
        val light = CalorieMath.caloriesFromSteps(10_000, 50f)
        val heavy = CalorieMath.caloriesFromSteps(10_000, 90f)
        assertThat(heavy).isGreaterThan(light)
    }
}

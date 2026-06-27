package com.neofit.engine

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WellnessScoreEngineTest {

    private val engine = WellnessScoreEngine()

    @Test
    fun perfectDay_scoresOneHundred() {
        val result = engine.compute(
            WellnessInputs(
                dateEpochDay = 0,
                loggedDaysInWeek = 7,
                steps = 10_000,
                stepTarget = 8_000,
                caloriesConsumed = 2_280, // 95% of target -> best
                calorieTarget = 2_400,
                workoutsToday = 1,
            ),
        )
        assertThat(result.score).isEqualTo(100)
        assertThat(result.consistencyScore).isEqualTo(WellnessScoreEngine.CONSISTENCY_MAX)
        assertThat(result.workoutScore).isEqualTo(WellnessScoreEngine.WORKOUT_MAX)
    }

    @Test
    fun emptyDay_scoresZero() {
        val result = engine.compute(
            WellnessInputs(0, loggedDaysInWeek = 0, steps = 0, stepTarget = 8_000, caloriesConsumed = 0, calorieTarget = 2_400, workoutsToday = 0),
        )
        assertThat(result.score).isEqualTo(0)
    }

    @Test
    fun componentsNeverExceedTheirMaxima() {
        val result = engine.compute(
            WellnessInputs(0, loggedDaysInWeek = 99, steps = 99_999, stepTarget = 8_000, caloriesConsumed = 2_400, calorieTarget = 2_400, workoutsToday = 5),
        )
        assertThat(result.activityScore).isAtMost(WellnessScoreEngine.ACTIVITY_MAX)
        assertThat(result.consistencyScore).isAtMost(WellnessScoreEngine.CONSISTENCY_MAX)
        assertThat(result.score).isAtMost(100)
    }
}

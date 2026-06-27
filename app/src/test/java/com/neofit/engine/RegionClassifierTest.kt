package com.neofit.engine

import com.google.common.truth.Truth.assertThat
import com.neofit.domain.model.ConfidenceLevel
import com.neofit.domain.model.FoodRegion
import org.junit.Test

class RegionClassifierTest {

    private val classifier = RegionClassifier()

    @Test
    fun insufficientData_fallsBackToPreferred() {
        val result = classifier.classify(listOf(FoodRegion.SOUTH), FoodRegion.NORTH)
        assertThat(result.inferred).isEqualTo(FoodRegion.NORTH)
        assertThat(result.confidence).isEqualTo(ConfidenceLevel.ROUGH)
    }

    @Test
    fun noDataAndNeutralPreference_returnsPanIndia() {
        val result = classifier.classify(emptyList(), FoodRegion.MIXED)
        assertThat(result.inferred).isEqualTo(FoodRegion.PAN_INDIA)
    }

    @Test
    fun clearMajority_isHighConfidence() {
        val regions = listOf(FoodRegion.SOUTH, FoodRegion.SOUTH, FoodRegion.SOUTH, FoodRegion.NORTH)
        val result = classifier.classify(regions, FoodRegion.MIXED)
        assertThat(result.inferred).isEqualTo(FoodRegion.SOUTH)
        assertThat(result.confidence).isEqualTo(ConfidenceLevel.HIGH)
    }

    @Test
    fun neutralRegions_areIgnoredAsSignal() {
        val regions = listOf(
            FoodRegion.PAN_INDIA, FoodRegion.PAN_INDIA, // neutral, ignored
            FoodRegion.WEST, FoodRegion.WEST, FoodRegion.WEST,
        )
        val result = classifier.classify(regions, FoodRegion.MIXED)
        assertThat(result.inferred).isEqualTo(FoodRegion.WEST)
        assertThat(result.distribution).doesNotContainKey(FoodRegion.PAN_INDIA)
    }
}

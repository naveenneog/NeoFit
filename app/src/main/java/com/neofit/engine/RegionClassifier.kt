package com.neofit.engine

import com.neofit.domain.model.ConfidenceLevel
import com.neofit.domain.model.FoodRegion

/** Result of region inference. Advisory only and always explainable. */
data class RegionInsight(
    val inferred: FoodRegion,
    val confidence: ConfidenceLevel,
    val explanation: String,
    val distribution: Map<FoodRegion, Int>,
)

/**
 * Infers a broad regional food profile from the user's logged dishes.
 *
 * Principles: advisory not restrictive, always overridable, and it explains
 * *why* a region was inferred. PAN_INDIA/MIXED carry no regional signal.
 */
class RegionClassifier {

    fun classify(
        loggedRegions: List<FoodRegion>,
        preferredRegion: FoodRegion,
    ): RegionInsight {
        val signal = loggedRegions.filter { it !in NEUTRAL }
        val distribution = signal.groupingBy { it }.eachCount()

        if (signal.size < MIN_SIGNAL) {
            val fallback = if (preferredRegion in NEUTRAL) FoodRegion.PAN_INDIA else preferredRegion
            return RegionInsight(
                inferred = fallback,
                confidence = ConfidenceLevel.ROUGH,
                explanation = if (preferredRegion in NEUTRAL) {
                    "Not enough logged meals yet to detect a region. Showing a pan-Indian mix. You can set a preference anytime."
                } else {
                    "Based on your selected preference (${preferredRegion.label}). Log more meals to refine this. You can override it anytime."
                },
                distribution = distribution,
            )
        }

        val sorted = distribution.entries.sortedByDescending { it.value }
        val top = sorted.first()
        val topShare = top.value.toFloat() / signal.size
        val runnerUp = sorted.getOrNull(1)

        val confidence = when {
            topShare >= 0.6f -> ConfidenceLevel.HIGH
            topShare >= 0.4f -> ConfidenceLevel.MEDIUM
            else -> ConfidenceLevel.ROUGH
        }

        val explanation = buildString {
            append("Detected mostly ${top.key.label} dishes ")
            append("(${top.value} of ${signal.size} regional meals)")
            if (runnerUp != null) append(", with some ${runnerUp.key.label}")
            append(". This is advisory — you can override your region in preferences.")
        }

        return RegionInsight(
            inferred = top.key,
            confidence = confidence,
            explanation = explanation,
            distribution = distribution,
        )
    }

    companion object {
        private const val MIN_SIGNAL = 3
        private val NEUTRAL = setOf(FoodRegion.PAN_INDIA, FoodRegion.MIXED)
    }
}

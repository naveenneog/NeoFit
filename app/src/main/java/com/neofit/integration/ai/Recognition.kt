package com.neofit.integration.ai

import com.neofit.data.seed.FoodKnowledgeBase
import javax.inject.Inject

/** A candidate dish from food recognition. Always user-confirmable. */
data class FoodPrediction(
    val foodId: String?,
    val name: String,
    val confidence: Float,
)

/**
 * Recognises a dish from a photo (and/or a text hint). The result is always
 * presented to the user for confirmation/editing — we never auto-log a guess.
 */
interface FoodRecognitionService {
    suspend fun recognize(hint: String?): List<FoodPrediction>
}

/**
 * Heuristic stand-in for an ML image classifier. It matches a text hint against
 * the knowledge base, or returns popular dishes when there's nothing to go on.
 *
 * TODO(prod): replace with a real recogniser — an on-device TFLite/ML Kit model
 * or a cloud vision endpoint. Keep the "confirm before logging" UX regardless.
 */
class MockFoodRecognitionService @Inject constructor() : FoodRecognitionService {

    override suspend fun recognize(hint: String?): List<FoodPrediction> {
        val matches = if (!hint.isNullOrBlank()) {
            FoodKnowledgeBase.foods.filter { it.matches(hint) }.take(5)
        } else emptyList()

        val source = matches.ifEmpty {
            DEFAULT_GUESS_IDS.mapNotNull { FoodKnowledgeBase.byId[it] }
        }

        val topConfidence = if (matches.isNotEmpty()) 0.62f else 0.3f
        return source.mapIndexed { index, food ->
            FoodPrediction(
                foodId = food.id,
                name = food.nameEn,
                confidence = (topConfidence - index * 0.07f).coerceAtLeast(0.15f),
            )
        }
    }

    companion object {
        private val DEFAULT_GUESS_IDS = listOf("idli", "poha", "dal_chawal", "roti_sabzi", "chicken_curry")
    }
}

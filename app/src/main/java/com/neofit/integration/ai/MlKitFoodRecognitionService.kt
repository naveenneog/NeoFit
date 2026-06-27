package com.neofit.integration.ai

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.neofit.data.seed.FoodKnowledgeBase
import com.neofit.domain.model.FoodItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * On-device food recognition using Google ML Kit's bundled image-labeling model
 * (fully offline). ML Kit returns broad labels (Food, Rice, Curry, Bread, Dessert…)
 * which we map onto the Indian knowledge base to propose specific candidate
 * dishes. Results are always shown for user confirmation.
 *
 * TODO(prod): for dish-level precision, train a custom Indian-food classifier
 * (e.g. MobileNet/EfficientNet) on the bundled gpt-image-2 reference photos and
 * load it via ML Kit custom models or LiteRT — this service is the drop-in seam.
 */
class MlKitFoodRecognitionService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fallback: MockFoodRecognitionService,
) : FoodRecognitionService {

    private val labeler by lazy {
        ImageLabeling.getClient(ImageLabelerOptions.Builder().setConfidenceThreshold(0.4f).build())
    }

    override suspend fun recognize(imageUri: String?, hint: String?): List<FoodPrediction> {
        val labels = imageUri?.let { runCatching { labelImage(it) }.getOrNull() }.orEmpty()
        if (labels.isEmpty()) return fallback.recognize(imageUri, hint)

        val scores = HashMap<FoodItem, Float>()
        for ((text, conf) in labels) {
            val keywords = LABEL_KEYWORDS[text.lowercase()] ?: continue
            if (keywords.isEmpty()) continue
            for (food in FoodKnowledgeBase.foods) {
                if (relates(food, keywords)) scores[food] = (scores[food] ?: 0f) + conf
            }
        }
        if (scores.isEmpty()) return fallback.recognize(imageUri, hint)

        return scores.entries.sortedByDescending { it.value }
            .take(6)
            .mapIndexed { i, e ->
                FoodPrediction(e.key.id, e.key.nameEn, (0.6f - i * 0.06f).coerceAtLeast(0.2f))
            }
    }

    private suspend fun labelImage(imageUri: String): List<Pair<String, Float>> =
        suspendCancellableCoroutine { cont ->
            val image = InputImage.fromFilePath(context, Uri.parse(imageUri))
            labeler.process(image)
                .addOnSuccessListener { result -> cont.resume(result.map { it.text to it.confidence }) }
                .addOnFailureListener { cont.resume(emptyList()) }
        }

    private fun relates(food: FoodItem, keywords: List<String>): Boolean {
        val hay = buildString {
            append(food.id); append(' '); append(food.nameEn.lowercase()); append(' ')
            append(food.tags.joinToString(" ")); append(' ')
            append(food.ingredients.joinToString(" ")); append(' ')
            append(food.diet.name.lowercase()); append(' '); append(food.typicalCategory.name.lowercase())
        }
        return keywords.any { hay.contains(it) }
    }

    private companion object {
        val LABEL_KEYWORDS: Map<String, List<String>> = mapOf(
            "rice" to listOf("rice", "biryani", "pulao", "pongal", "khichdi", "curd", "poha"),
            "fried rice" to listOf("rice", "biryani", "pulao"),
            "bread" to listOf("roti", "naan", "paratha", "thepla", "bhature", "jowar", "litti", "pav", "bati"),
            "flatbread" to listOf("roti", "naan", "paratha", "thepla", "jowar", "dosa"),
            "pancake" to listOf("dosa", "cheela", "chilla", "uttapam"),
            "curry" to listOf("curry", "sambar", "dal", "masala", "gravy", "chicken", "fish", "paneer", "rajma", "chole", "posto"),
            "soup" to listOf("sambar", "dal", "thukpa", "rasam"),
            "dumpling" to listOf("momo", "modak"),
            "dessert" to listOf("sweet", "jamun", "jalebi", "ladoo", "rasgulla", "mishti", "shrikhand", "chikki"),
            "cake" to listOf("sweet"),
            "doughnut" to listOf("vada", "jalebi", "medu"),
            "egg" to listOf("egg", "omelette", "bhurji"),
            "meat" to listOf("chicken", "pork", "meat"),
            "chicken" to listOf("chicken"),
            "fish" to listOf("fish", "macher"),
            "fruit" to listOf("banana", "fruit"),
            "vegetable" to listOf("sabzi", "vegetable", "bhaji", "aloo", "palak"),
            "snack" to listOf("samosa", "pakoda", "vada", "sev", "murukku", "snack", "dhokla"),
            "junk food" to listOf("samosa", "pakoda", "vada", "fried"),
            "beverage" to listOf("chai", "tea", "coffee", "lassi", "buttermilk", "milk"),
            "drink" to listOf("chai", "tea", "coffee", "lassi", "buttermilk", "milk"),
            "coffee" to listOf("coffee"),
            "tea" to listOf("chai", "tea"),
            "breakfast" to listOf("idli", "dosa", "poha", "upma", "pongal", "paratha"),
            "fast food" to listOf("vada_pav", "pav_bhaji", "samosa"),
        )
    }
}

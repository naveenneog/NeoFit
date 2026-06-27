package com.neofit.domain.model

/**
 * A serving descriptor. `multiplier` scales the food's base nutrition.
 * `grams` is an optional rough weight for display/education.
 */
data class PortionSize(
    val label: String,
    val multiplier: Float,
    val grams: Int? = null,
) {
    companion object {
        // Common Indian household portions used across the app.
        val KATORI = PortionSize("1 katori", 1.0f, 150)
        val HALF_KATORI = PortionSize("½ katori", 0.5f, 75)
        val PLATE = PortionSize("1 plate", 1.5f, 300)
        val PIECE = PortionSize("1 piece", 1.0f)
        val TWO_PIECES = PortionSize("2 pieces", 2.0f)
        val GLASS = PortionSize("1 glass", 1.0f, 200)
        val BOWL = PortionSize("1 bowl", 1.5f, 250)
        val TABLESPOON = PortionSize("1 tbsp", 0.2f, 15)
        val STANDARD = PortionSize("1 serving", 1.0f)

        val COMMON = listOf(HALF_KATORI, KATORI, BOWL, PLATE, PIECE, TWO_PIECES, GLASS, STANDARD)
    }
}

/**
 * Estimated nutrition for a serving. Always treated as approximate for Indian
 * home/street food; [confidence] and [basis] communicate that to the user.
 */
data class NutritionEstimate(
    val caloriesKcal: Int,
    val proteinG: Float,
    val carbsG: Float,
    val fatG: Float,
    val fiberG: Float = 0f,
    val confidence: ConfidenceLevel,
    val basis: String,
    val isApproximate: Boolean = true,
)

/**
 * A food in the knowledge base. Base nutrition is per [baseServing].
 * `localizedNames` maps an [AppLanguage.code] to the native dish name.
 */
data class FoodItem(
    val id: String,
    val nameEn: String,
    val localizedNames: Map<String, String> = emptyMap(),
    val region: FoodRegion,
    val diet: FoodDiet,
    val typicalCategory: MealCategory,
    val baseServing: PortionSize,
    val caloriesKcal: Int,
    val proteinG: Float,
    val carbsG: Float,
    val fatG: Float,
    val fiberG: Float = 0f,
    val cookingStyle: CookingStyle,
    val ingredients: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val aliases: List<String> = emptyList(),
    val isStreetFood: Boolean = false,
    val isSweet: Boolean = false,
    val defaultImageUrl: String? = null,
    val baseConfidence: ConfidenceLevel = ConfidenceLevel.MEDIUM,
) {
    /** Native name for a language, falling back to English. */
    fun displayName(language: AppLanguage): String =
        localizedNames[language.code]
            ?: localizedNames[language.localeTag]
            ?: nameEn

    fun matches(query: String): Boolean {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return true
        return nameEn.lowercase().contains(q) ||
            id.contains(q) ||
            aliases.any { it.lowercase().contains(q) } ||
            localizedNames.values.any { it.lowercase().contains(q) }
    }
}

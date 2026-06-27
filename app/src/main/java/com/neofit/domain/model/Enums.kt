package com.neofit.domain.model

/** Biological sex used for BMR/TDEE estimation (Mifflin–St Jeor). */
enum class Sex { MALE, FEMALE, OTHER }

/** Physical activity level with the standard TDEE activity multiplier. */
enum class ActivityLevel(val multiplier: Float, val label: String) {
    SEDENTARY(1.2f, "Sedentary (little/no exercise)"),
    LIGHT(1.375f, "Light (1–3 days/week)"),
    MODERATE(1.55f, "Moderate (3–5 days/week)"),
    ACTIVE(1.725f, "Active (6–7 days/week)"),
    VERY_ACTIVE(1.9f, "Very active (physical job)"),
}

/** What the user eats. Drives meal suggestions and food filtering. */
enum class DietaryPreference(val label: String) {
    VEGETARIAN("Vegetarian"),
    EGGETARIAN("Eggetarian"),
    NON_VEGETARIAN("Non-vegetarian"),
    VEGAN("Vegan"),
}

/** What a food itself contains, for diet compatibility checks. */
enum class FoodDiet { VEG, EGG, NONVEG, VEGAN }

/** The user's primary wellness goal. */
enum class WellnessGoal(val label: String) {
    WEIGHT_LOSS("Weight loss"),
    WEIGHT_GAIN("Weight gain"),
    MAINTENANCE("Maintenance"),
    IMPROVE_STAMINA("Improve stamina"),
    GENERAL_WELLNESS("General wellness"),
}

/**
 * Broad Indian food region styles. Advisory only — never restrictive.
 * PAN_INDIA = dishes common everywhere; MIXED = user has no preference.
 */
enum class FoodRegion(val label: String) {
    NORTH("North India"),
    SOUTH("South India"),
    NORTH_EAST("North East India"),
    WEST("West India"),
    CENTRAL("Central India"),
    EAST("East India"),
    PAN_INDIA("Pan-Indian"),
    MIXED("Mixed / no preference"),
}

enum class MealCategory(val label: String) {
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner"),
    SNACK("Snack"),
}

/** Transparency about how reliable a calorie estimate is. */
enum class ConfidenceLevel(val label: String) {
    HIGH("High confidence"),
    MEDIUM("Medium confidence"),
    ROUGH("Rough estimate"),
}

/**
 * Cooking method, used to adjust calories (oil/fat absorption).
 * factor multiplies the base calories of the dish.
 */
enum class CookingStyle(val factor: Float) {
    RAW(1.0f),
    STEAMED(1.0f),
    BOILED(1.0f),
    ROASTED(1.05f),
    GRILLED(1.05f),
    BAKED(1.1f),
    CURRY(1.2f),
    FRIED(1.4f),
}

enum class Difficulty(val label: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced"),
}

/** Category a workout plan targets. */
enum class PlanGoal(val label: String) {
    WEIGHT_LOSS("Weight loss"),
    STAMINA("Stamina"),
    STRENGTH("Strength"),
    MOBILITY("Mobility & flexibility"),
    HOME_NO_EQUIPMENT("No-equipment home"),
    OFFICE("Office-friendly"),
}

enum class LogSource { SEARCH, QUICK_ADD, PHOTO, MANUAL, REPEAT }

enum class StepSource { HEALTH_CONNECT, ESTIMATED, MANUAL }

enum class ImageSource { WEB, AZURE_GENERATED, PLACEHOLDER, BUNDLED }

enum class RecommendationType { MEAL, EXERCISE, NUDGE, HYDRATION }

enum class SyncState { IDLE, SYNCING, SUCCESS, ERROR, UNAVAILABLE }

/**
 * Supported display languages. `localeTag` selects Android resources;
 * `romanized` blends (Hinglish/Kanglish/Tanglish) reuse English resources
 * but show native dish names in Latin script.
 */
enum class AppLanguage(
    val code: String,
    val localeTag: String,
    val nativeLabel: String,
    val romanized: Boolean,
) {
    ENGLISH("en", "en", "English", false),
    HINGLISH("hi-en", "en", "Hinglish", true),
    HINDI("hi", "hi", "हिन्दी", false),
    KANNADA("kn", "kn", "ಕನ್ನಡ", false),
    KANGLISH("kn-en", "en", "Kanglish", true),
    TAMIL("ta", "ta", "தமிழ்", false),
    TANGLISH("ta-en", "en", "Tanglish", true),
    TELUGU("te", "te", "తెలుగు", false),
    MARATHI("mr", "mr", "मराठी", false),
    BENGALI("bn", "bn", "বাংলা", false);

    companion object {
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code == code } ?: ENGLISH
    }
}

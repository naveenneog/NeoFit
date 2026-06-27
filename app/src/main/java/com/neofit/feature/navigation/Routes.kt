package com.neofit.feature.navigation

/** Central registry of navigation routes. */
object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"

    const val HOME = "home"
    const val FOOD_LOG = "food_log"
    const val FOOD_ADD = "food_add"
    const val FOOD_SEARCH = "food_search"
    const val FOOD_PHOTO = "food_photo"
    const val MEAL_DETAIL = "meal_detail"
    const val EXERCISE_PLANS = "exercise_plans"
    const val EXERCISE_DETAIL = "exercise_detail"
    const val PROGRESS = "progress"
    const val WEIGHT_HISTORY = "weight_history"
    const val PROFILE = "profile"
    const val REGION_PREFS = "region_prefs"
    const val INSIGHTS = "insights"

    fun foodAdd(foodId: String? = null): String =
        if (foodId == null) FOOD_ADD else "$FOOD_ADD?foodId=$foodId"

    fun mealDetail(mealId: Long): String = "$MEAL_DETAIL/$mealId"
    fun exerciseDetail(planId: String): String = "$EXERCISE_DETAIL/$planId"
}

/** Bottom navigation tabs. */
enum class BottomTab(val route: String) {
    HOME(Routes.HOME),
    FOOD(Routes.FOOD_LOG),
    EXERCISE(Routes.EXERCISE_PLANS),
    PROGRESS(Routes.PROGRESS),
    PROFILE(Routes.PROFILE),
}

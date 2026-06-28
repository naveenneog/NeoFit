package com.neofit.domain.model

/** Who authored a coach chat line. */
enum class CoachRole { USER, COACH }

/** A single line in the Coach conversation. */
data class CoachMessage(
    val role: CoachRole,
    val text: String,
)

/**
 * Everything the Coach needs to ground a reply in the user's real, observed data.
 * Nothing is fabricated — the same numbers the dashboard shows.
 */
data class CoachContext(
    val profile: UserProfile?,
    val dashboard: DashboardSummary,
    val recentMeals: List<MealLog>,
)

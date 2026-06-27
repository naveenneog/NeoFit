package com.neofit.domain.model

/** A logged meal. The unit the dashboard sums for "calories eaten". */
data class MealLog(
    val id: Long = 0,
    val foodId: String?,
    val name: String,
    val category: MealCategory,
    val region: FoodRegion,
    val portion: PortionSize,
    val estimate: NutritionEstimate,
    val timestampEpochMillis: Long,
    val imageRef: String? = null,
    val manuallyCorrected: Boolean = false,
    val source: LogSource = LogSource.SEARCH,
    val note: String? = null,
) {
    val epochDay: Long get() = timestampEpochMillis / 86_400_000L
}

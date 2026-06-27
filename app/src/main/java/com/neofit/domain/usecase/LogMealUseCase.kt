package com.neofit.domain.usecase

import com.neofit.core.util.DateUtil
import com.neofit.domain.model.FoodRegion
import com.neofit.domain.model.LogSource
import com.neofit.domain.model.MealCategory
import com.neofit.domain.model.MealLog
import com.neofit.domain.model.NutritionEstimate
import com.neofit.domain.model.PortionSize
import com.neofit.domain.repository.MealLogRepository
import javax.inject.Inject

data class LogMealParams(
    val foodId: String?,
    val name: String,
    val category: MealCategory,
    val region: FoodRegion,
    val portion: PortionSize,
    val estimate: NutritionEstimate,
    val source: LogSource,
    val imageRef: String? = null,
    val note: String? = null,
    val manuallyCorrected: Boolean = false,
    val timestampEpochMillis: Long = DateUtil.nowMillis(),
)

/** Builds and persists a [MealLog] from logging parameters. */
class LogMealUseCase @Inject constructor(
    private val mealLogRepository: MealLogRepository,
) {
    suspend operator fun invoke(params: LogMealParams): Long {
        val meal = MealLog(
            foodId = params.foodId,
            name = params.name,
            category = params.category,
            region = params.region,
            portion = params.portion,
            estimate = params.estimate,
            timestampEpochMillis = params.timestampEpochMillis,
            imageRef = params.imageRef,
            manuallyCorrected = params.manuallyCorrected,
            source = params.source,
            note = params.note,
        )
        return mealLogRepository.add(meal)
    }
}

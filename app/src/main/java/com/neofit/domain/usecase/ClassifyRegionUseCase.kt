package com.neofit.domain.usecase

import com.neofit.domain.model.FoodRegion
import com.neofit.domain.model.MealLog
import com.neofit.engine.RegionClassifier
import com.neofit.engine.RegionInsight
import javax.inject.Inject

/** Infers an advisory regional food profile from logged meals. */
class ClassifyRegionUseCase @Inject constructor(
    private val classifier: RegionClassifier,
) {
    operator fun invoke(meals: List<MealLog>, preferredRegion: FoodRegion): RegionInsight =
        classifier.classify(meals.map { it.region }, preferredRegion)
}

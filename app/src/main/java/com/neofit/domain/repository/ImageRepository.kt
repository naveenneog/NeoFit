package com.neofit.domain.repository

import com.neofit.domain.model.ExerciseItem
import com.neofit.domain.model.FoodImageAsset
import com.neofit.domain.model.FoodItem

/**
 * Resolves a displayable image for a dish or exercise pose. Implementations
 * try web sources first, then Azure image generation, then a local placeholder,
 * caching the result locally.
 */
interface ImageRepository {
    suspend fun getOrFetchFoodImage(food: FoodItem): FoodImageAsset
    suspend fun getOrFetchFoodImageByName(name: String, prompt: String? = null): FoodImageAsset
    suspend fun getOrFetchExerciseImage(item: ExerciseItem): FoodImageAsset
}

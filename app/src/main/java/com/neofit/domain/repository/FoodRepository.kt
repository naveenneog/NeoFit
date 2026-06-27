package com.neofit.domain.repository

import com.neofit.domain.model.DietaryPreference
import com.neofit.domain.model.FoodItem
import com.neofit.domain.model.FoodRegion
import com.neofit.domain.model.MealCategory
import kotlinx.coroutines.flow.Flow

interface FoodRepository {
    fun all(): List<FoodItem>
    fun getById(id: String): FoodItem?
    fun search(
        query: String,
        diet: DietaryPreference? = null,
        region: FoodRegion? = null,
    ): List<FoodItem>

    /** Region/diet/budget-aware suggestions for the dashboard & recommendations. */
    fun recommended(
        remainingKcal: Int,
        region: FoodRegion,
        diet: DietaryPreference,
        category: MealCategory?,
        limit: Int = 6,
    ): List<FoodItem>

    fun observeFavourites(): Flow<List<String>>
    suspend fun toggleFavourite(foodId: String)
}

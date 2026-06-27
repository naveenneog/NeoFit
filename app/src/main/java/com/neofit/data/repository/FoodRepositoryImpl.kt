package com.neofit.data.repository

import com.neofit.data.local.dao.FavouriteDao
import com.neofit.data.local.entity.FavouriteFoodEntity
import com.neofit.data.seed.FoodKnowledgeBase
import com.neofit.domain.model.DietaryPreference
import com.neofit.domain.model.FoodDiet
import com.neofit.domain.model.FoodItem
import com.neofit.domain.model.FoodRegion
import com.neofit.domain.model.MealCategory
import com.neofit.domain.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodRepositoryImpl @Inject constructor(
    private val favouriteDao: FavouriteDao,
) : FoodRepository {

    private val library = FoodKnowledgeBase.foods

    override fun all(): List<FoodItem> = library

    override fun getById(id: String): FoodItem? = FoodKnowledgeBase.byId[id]

    override fun search(query: String, diet: DietaryPreference?, region: FoodRegion?): List<FoodItem> =
        library.asSequence()
            .filter { it.matches(query) }
            .filter { diet == null || dietAllows(diet, it.diet) }
            .filter { region == null || region == FoodRegion.MIXED || it.region == region || it.region == FoodRegion.PAN_INDIA }
            .sortedByDescending { relevance(it, query) }
            .take(40)
            .toList()

    override fun recommended(
        remainingKcal: Int,
        region: FoodRegion,
        diet: DietaryPreference,
        category: MealCategory?,
        limit: Int,
    ): List<FoodItem> =
        library.asSequence()
            .filter { dietAllows(diet, it.diet) }
            .filter { category == null || it.typicalCategory == category }
            .sortedByDescending { score(it, remainingKcal, region) }
            .take(limit)
            .toList()

    override fun observeFavourites(): Flow<List<String>> =
        favouriteDao.observeAll().map { list -> list.map { it.foodId } }

    override suspend fun toggleFavourite(foodId: String) {
        if (favouriteDao.exists(foodId)) favouriteDao.deleteById(foodId)
        else favouriteDao.insert(FavouriteFoodEntity(foodId))
    }

    private fun dietAllows(pref: DietaryPreference, food: FoodDiet): Boolean = when (pref) {
        DietaryPreference.VEGAN -> food == FoodDiet.VEGAN
        DietaryPreference.VEGETARIAN -> food == FoodDiet.VEG || food == FoodDiet.VEGAN
        DietaryPreference.EGGETARIAN -> food != FoodDiet.NONVEG
        DietaryPreference.NON_VEGETARIAN -> true
    }

    private fun relevance(food: FoodItem, query: String): Int {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return 0
        return when {
            food.nameEn.lowercase() == q -> 100
            food.nameEn.lowercase().startsWith(q) -> 60
            food.id.startsWith(q) -> 50
            else -> 10
        }
    }

    private fun score(food: FoodItem, remainingKcal: Int, region: FoodRegion): Int {
        var s = 0
        if (food.region == region) s += 30
        else if (food.region == FoodRegion.PAN_INDIA) s += 12
        // Prefer dishes that fit comfortably within the remaining budget.
        if (remainingKcal > 0) {
            if (food.caloriesKcal in 1..remainingKcal) s += 20
            else if (food.caloriesKcal <= remainingKcal + 150) s += 8
        }
        if (!food.isSweet) s += 4
        return s
    }
}

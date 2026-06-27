package com.neofit.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.neofit.data.local.entity.FavouriteFoodEntity
import com.neofit.data.local.entity.FoodImageAssetEntity
import com.neofit.data.local.entity.WaterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodImageDao {
    @Query("SELECT * FROM food_image WHERE `key` = :key LIMIT 1")
    suspend fun get(key: String): FoodImageAssetEntity?

    @Upsert
    suspend fun upsert(entity: FoodImageAssetEntity)
}

@Dao
interface WaterDao {
    @Query("SELECT * FROM water_log WHERE dateEpochDay = :day LIMIT 1")
    fun observe(day: Long): Flow<WaterEntity?>

    @Query("SELECT * FROM water_log WHERE dateEpochDay = :day LIMIT 1")
    suspend fun get(day: Long): WaterEntity?

    @Upsert
    suspend fun upsert(entity: WaterEntity)
}

@Dao
interface FavouriteDao {
    @Query("SELECT * FROM favourite_food")
    fun observeAll(): Flow<List<FavouriteFoodEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favourite_food WHERE foodId = :foodId)")
    suspend fun exists(foodId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: FavouriteFoodEntity)

    @Query("DELETE FROM favourite_food WHERE foodId = :foodId")
    suspend fun deleteById(foodId: String)
}

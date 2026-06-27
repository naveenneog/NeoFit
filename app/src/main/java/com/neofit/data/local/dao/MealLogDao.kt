package com.neofit.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.neofit.data.local.entity.MealLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealLogDao {
    @Query("SELECT * FROM meal_log WHERE epochDay = :day ORDER BY timestampEpochMillis DESC")
    fun observeForDay(day: Long): Flow<List<MealLogEntity>>

    @Query("SELECT * FROM meal_log WHERE epochDay BETWEEN :startDay AND :endDay ORDER BY timestampEpochMillis DESC")
    fun observeBetween(startDay: Long, endDay: Long): Flow<List<MealLogEntity>>

    @Query("SELECT * FROM meal_log ORDER BY timestampEpochMillis DESC LIMIT :limit")
    suspend fun recent(limit: Int): List<MealLogEntity>

    // Most recent log per distinct dish (library dishes keyed by foodId, custom by name),
    // for one-tap re-logging of routine meals.
    @Query(
        "SELECT * FROM meal_log WHERE id IN " +
            "(SELECT MAX(id) FROM meal_log GROUP BY COALESCE(foodId, name)) " +
            "ORDER BY timestampEpochMillis DESC LIMIT :limit",
    )
    fun observeRecentDistinct(limit: Int): Flow<List<MealLogEntity>>

    @Query("SELECT * FROM meal_log ORDER BY timestampEpochMillis DESC LIMIT 1")
    suspend fun last(): MealLogEntity?

    @Query("SELECT COUNT(DISTINCT epochDay) FROM meal_log WHERE epochDay BETWEEN :startDay AND :endDay")
    fun observeLoggedDayCount(startDay: Long, endDay: Long): Flow<Int>

    @Query("SELECT * FROM meal_log WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): MealLogEntity?

    @Insert
    suspend fun insert(entity: MealLogEntity): Long

    @Update
    suspend fun update(entity: MealLogEntity)

    @Query("DELETE FROM meal_log WHERE id = :id")
    suspend fun deleteById(id: Long)
}

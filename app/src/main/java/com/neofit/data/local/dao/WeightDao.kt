package com.neofit.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.neofit.data.local.entity.WeightEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {
    @Query("SELECT * FROM weight_entry ORDER BY dateEpochDay ASC, id ASC")
    fun observeAll(): Flow<List<WeightEntity>>

    @Query("SELECT * FROM weight_entry ORDER BY dateEpochDay DESC, id DESC LIMIT 1")
    suspend fun latest(): WeightEntity?

    @Insert
    suspend fun insert(entity: WeightEntity)

    @Query("DELETE FROM weight_entry WHERE dateEpochDay = :day")
    suspend fun deleteForDay(day: Long)
}

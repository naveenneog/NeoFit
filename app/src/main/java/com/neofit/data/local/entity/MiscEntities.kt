package com.neofit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_log")
data class WaterEntity(
    @PrimaryKey val dateEpochDay: Long,
    val glasses: Int,
)

@Entity(tableName = "favourite_food")
data class FavouriteFoodEntity(
    @PrimaryKey val foodId: String,
)

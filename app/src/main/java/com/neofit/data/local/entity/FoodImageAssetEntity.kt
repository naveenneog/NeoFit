package com.neofit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Local cache of resolved food/exercise images, keyed by a stable slug. */
@Entity(tableName = "food_image")
data class FoodImageAssetEntity(
    @PrimaryKey val key: String,
    val localPath: String?,
    val remoteUrl: String?,
    val source: String,
    val prompt: String?,
    val createdAtMillis: Long,
)

package com.neofit.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "meal_log",
    indices = [Index("epochDay")],
)
data class MealLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val foodId: String?,
    val name: String,
    val category: String,
    val region: String,
    val portionLabel: String,
    val portionMultiplier: Float,
    val portionGrams: Int?,
    val caloriesKcal: Int,
    val proteinG: Float,
    val carbsG: Float,
    val fatG: Float,
    val fiberG: Float,
    val confidence: String,
    val basis: String,
    val isApproximate: Boolean,
    val timestampEpochMillis: Long,
    val epochDay: Long,
    val imageRef: String?,
    val manuallyCorrected: Boolean,
    val source: String,
    val note: String?,
)

package com.neofit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Cached daily step summary (from Health Connect or estimated). */
@Entity(tableName = "step_summary")
data class StepSummaryEntity(
    @PrimaryKey val dateEpochDay: Long,
    val steps: Int,
    val distanceMeters: Float,
    val activeCaloriesKcal: Int,
    val source: String,
)

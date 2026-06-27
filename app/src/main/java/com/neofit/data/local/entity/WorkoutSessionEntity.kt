package com.neofit.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "workout_session", indices = [Index("epochDay")])
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planId: String,
    val startEpochMillis: Long,
    val endEpochMillis: Long?,
    val epochDay: Long,
    val completedItemIds: List<String>,
    val caloriesBurned: Int,
    val completed: Boolean,
)

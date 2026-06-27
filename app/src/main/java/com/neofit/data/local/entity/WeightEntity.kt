package com.neofit.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "weight_entry", indices = [Index("dateEpochDay")])
data class WeightEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weightKg: Float,
    val dateEpochDay: Long,
    val note: String?,
)

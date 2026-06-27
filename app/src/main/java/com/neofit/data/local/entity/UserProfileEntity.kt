package com.neofit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Long = 1L,
    val name: String,
    val age: Int,
    val sex: String,
    val heightCm: Float,
    val currentWeightKg: Float,
    val targetWeightKg: Float,
    val activityLevel: String,
    val dietaryPreference: String,
    val goal: String,
    val preferredRegion: String,
    val language: String,
    val foodRestrictions: List<String>,
    val dailyCalorieTarget: Int,
    val dailyProteinTargetG: Int,
    val dailyStepTarget: Int,
    val dailyWaterGlassTarget: Int,
    val onboardingComplete: Boolean,
    val createdAtEpochDay: Long,
    val updatedAtEpochMillis: Long,
)

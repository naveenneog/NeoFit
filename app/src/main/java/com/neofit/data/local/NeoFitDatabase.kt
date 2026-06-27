package com.neofit.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.neofit.data.local.converter.Converters
import com.neofit.data.local.dao.FavouriteDao
import com.neofit.data.local.dao.FoodImageDao
import com.neofit.data.local.dao.MealLogDao
import com.neofit.data.local.dao.StepDao
import com.neofit.data.local.dao.UserDao
import com.neofit.data.local.dao.WaterDao
import com.neofit.data.local.dao.WeightDao
import com.neofit.data.local.dao.WorkoutSessionDao
import com.neofit.data.local.entity.FavouriteFoodEntity
import com.neofit.data.local.entity.FoodImageAssetEntity
import com.neofit.data.local.entity.MealLogEntity
import com.neofit.data.local.entity.StepSummaryEntity
import com.neofit.data.local.entity.UserProfileEntity
import com.neofit.data.local.entity.WaterEntity
import com.neofit.data.local.entity.WeightEntity
import com.neofit.data.local.entity.WorkoutSessionEntity

@Database(
    entities = [
        UserProfileEntity::class,
        MealLogEntity::class,
        WeightEntity::class,
        WorkoutSessionEntity::class,
        StepSummaryEntity::class,
        FoodImageAssetEntity::class,
        WaterEntity::class,
        FavouriteFoodEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class NeoFitDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun mealLogDao(): MealLogDao
    abstract fun weightDao(): WeightDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun stepDao(): StepDao
    abstract fun foodImageDao(): FoodImageDao
    abstract fun waterDao(): WaterDao
    abstract fun favouriteDao(): FavouriteDao

    companion object {
        const val NAME = "neofit.db"
    }
}

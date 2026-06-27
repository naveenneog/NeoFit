package com.neofit.di

import android.content.Context
import androidx.room.Room
import com.neofit.data.local.NeoFitDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NeoFitDatabase =
        Room.databaseBuilder(context, NeoFitDatabase::class.java, NeoFitDatabase.NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideUserDao(db: NeoFitDatabase) = db.userDao()
    @Provides fun provideMealLogDao(db: NeoFitDatabase) = db.mealLogDao()
    @Provides fun provideWeightDao(db: NeoFitDatabase) = db.weightDao()
    @Provides fun provideWorkoutSessionDao(db: NeoFitDatabase) = db.workoutSessionDao()
    @Provides fun provideStepDao(db: NeoFitDatabase) = db.stepDao()
    @Provides fun provideFoodImageDao(db: NeoFitDatabase) = db.foodImageDao()
    @Provides fun provideWaterDao(db: NeoFitDatabase) = db.waterDao()
    @Provides fun provideFavouriteDao(db: NeoFitDatabase) = db.favouriteDao()
}

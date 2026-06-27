package com.neofit.di

import com.neofit.engine.CalorieEstimationEngine
import com.neofit.engine.RecommendationEngine
import com.neofit.engine.RegionClassifier
import com.neofit.engine.WellnessScoreEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Provides the pure, framework-agnostic engines (kept free of Hilt annotations). */
@Module
@InstallIn(SingletonComponent::class)
object EngineModule {
    @Provides @Singleton fun provideCalorieEngine() = CalorieEstimationEngine()
    @Provides @Singleton fun provideRegionClassifier() = RegionClassifier()
    @Provides @Singleton fun provideWellnessEngine() = WellnessScoreEngine()
    @Provides @Singleton fun provideRecommendationEngine() = RecommendationEngine()
}

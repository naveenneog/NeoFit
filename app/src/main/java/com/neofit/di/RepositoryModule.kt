package com.neofit.di

import com.neofit.data.repository.ActivityRepositoryImpl
import com.neofit.data.repository.ExerciseRepositoryImpl
import com.neofit.data.repository.FoodRepositoryImpl
import com.neofit.data.repository.ImageRepositoryImpl
import com.neofit.data.repository.MealLogRepositoryImpl
import com.neofit.data.repository.PreferencesRepositoryImpl
import com.neofit.data.repository.UserRepositoryImpl
import com.neofit.data.repository.WeightRepositoryImpl
import com.neofit.domain.coach.CoachService
import com.neofit.domain.repository.ActivityRepository
import com.neofit.domain.repository.ExerciseRepository
import com.neofit.domain.repository.FoodRepository
import com.neofit.domain.repository.ImageRepository
import com.neofit.domain.repository.MealLogRepository
import com.neofit.domain.repository.PreferencesRepository
import com.neofit.domain.repository.UserRepository
import com.neofit.domain.repository.WeightRepository
import com.neofit.integration.ai.AzureImageGenerationService
import com.neofit.integration.ai.FoodRecognitionService
import com.neofit.integration.ai.ImageGenerationService
import com.neofit.integration.ai.MlKitFoodRecognitionService
import com.neofit.integration.ai.RuleBasedCoachService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds @Singleton
    abstract fun bindMealLogRepository(impl: MealLogRepositoryImpl): MealLogRepository

    @Binds @Singleton
    abstract fun bindWeightRepository(impl: WeightRepositoryImpl): WeightRepository

    @Binds @Singleton
    abstract fun bindFoodRepository(impl: FoodRepositoryImpl): FoodRepository

    @Binds @Singleton
    abstract fun bindExerciseRepository(impl: ExerciseRepositoryImpl): ExerciseRepository

    @Binds @Singleton
    abstract fun bindActivityRepository(impl: ActivityRepositoryImpl): ActivityRepository

    @Binds @Singleton
    abstract fun bindImageRepository(impl: ImageRepositoryImpl): ImageRepository

    @Binds @Singleton
    abstract fun bindPreferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository

    // ---- AI services ----
    @Binds @Singleton
    abstract fun bindImageGenerationService(impl: AzureImageGenerationService): ImageGenerationService

    @Binds @Singleton
    abstract fun bindFoodRecognitionService(impl: MlKitFoodRecognitionService): FoodRecognitionService

    @Binds @Singleton
    abstract fun bindCoachService(impl: RuleBasedCoachService): CoachService
}

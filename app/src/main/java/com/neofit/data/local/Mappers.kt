package com.neofit.data.local

import com.neofit.data.local.entity.FoodImageAssetEntity
import com.neofit.data.local.entity.MealLogEntity
import com.neofit.data.local.entity.StepSummaryEntity
import com.neofit.data.local.entity.UserProfileEntity
import com.neofit.data.local.entity.WeightEntity
import com.neofit.data.local.entity.WorkoutSessionEntity
import com.neofit.domain.model.ActivityLevel
import com.neofit.domain.model.AppLanguage
import com.neofit.domain.model.ConfidenceLevel
import com.neofit.domain.model.DietaryPreference
import com.neofit.domain.model.FoodImageAsset
import com.neofit.domain.model.FoodRegion
import com.neofit.domain.model.ImageSource
import com.neofit.domain.model.LogSource
import com.neofit.domain.model.MealCategory
import com.neofit.domain.model.MealLog
import com.neofit.domain.model.NutritionEstimate
import com.neofit.domain.model.PortionSize
import com.neofit.domain.model.Sex
import com.neofit.domain.model.StepSource
import com.neofit.domain.model.StepSummary
import com.neofit.domain.model.UserProfile
import com.neofit.domain.model.WeightEntry
import com.neofit.domain.model.WellnessGoal
import com.neofit.domain.model.WorkoutSession

private inline fun <reified T : Enum<T>> String.toEnum(default: T): T =
    enumValues<T>().firstOrNull { it.name == this } ?: default

// ---- UserProfile ----
fun UserProfileEntity.toDomain() = UserProfile(
    id = id,
    name = name,
    age = age,
    sex = sex.toEnum(Sex.OTHER),
    heightCm = heightCm,
    currentWeightKg = currentWeightKg,
    targetWeightKg = targetWeightKg,
    activityLevel = activityLevel.toEnum(ActivityLevel.MODERATE),
    dietaryPreference = dietaryPreference.toEnum(DietaryPreference.VEGETARIAN),
    goal = goal.toEnum(WellnessGoal.GENERAL_WELLNESS),
    preferredRegion = preferredRegion.toEnum(FoodRegion.PAN_INDIA),
    language = AppLanguage.fromCode(language),
    foodRestrictions = foodRestrictions,
    dailyCalorieTarget = dailyCalorieTarget,
    dailyProteinTargetG = dailyProteinTargetG,
    dailyStepTarget = dailyStepTarget,
    dailyWaterGlassTarget = dailyWaterGlassTarget,
    onboardingComplete = onboardingComplete,
    createdAtEpochDay = createdAtEpochDay,
    updatedAtEpochMillis = updatedAtEpochMillis,
)

fun UserProfile.toEntity() = UserProfileEntity(
    id = id,
    name = name,
    age = age,
    sex = sex.name,
    heightCm = heightCm,
    currentWeightKg = currentWeightKg,
    targetWeightKg = targetWeightKg,
    activityLevel = activityLevel.name,
    dietaryPreference = dietaryPreference.name,
    goal = goal.name,
    preferredRegion = preferredRegion.name,
    language = language.code,
    foodRestrictions = foodRestrictions,
    dailyCalorieTarget = dailyCalorieTarget,
    dailyProteinTargetG = dailyProteinTargetG,
    dailyStepTarget = dailyStepTarget,
    dailyWaterGlassTarget = dailyWaterGlassTarget,
    onboardingComplete = onboardingComplete,
    createdAtEpochDay = createdAtEpochDay,
    updatedAtEpochMillis = updatedAtEpochMillis,
)

// ---- MealLog ----
fun MealLogEntity.toDomain() = MealLog(
    id = id,
    foodId = foodId,
    name = name,
    category = category.toEnum(MealCategory.SNACK),
    region = region.toEnum(FoodRegion.PAN_INDIA),
    portion = PortionSize(portionLabel, portionMultiplier, portionGrams),
    estimate = NutritionEstimate(
        caloriesKcal = caloriesKcal,
        proteinG = proteinG,
        carbsG = carbsG,
        fatG = fatG,
        fiberG = fiberG,
        confidence = confidence.toEnum(ConfidenceLevel.ROUGH),
        basis = basis,
        isApproximate = isApproximate,
    ),
    timestampEpochMillis = timestampEpochMillis,
    imageRef = imageRef,
    manuallyCorrected = manuallyCorrected,
    source = source.toEnum(LogSource.MANUAL),
    note = note,
)

fun MealLog.toEntity() = MealLogEntity(
    id = id,
    foodId = foodId,
    name = name,
    category = category.name,
    region = region.name,
    portionLabel = portion.label,
    portionMultiplier = portion.multiplier,
    portionGrams = portion.grams,
    caloriesKcal = estimate.caloriesKcal,
    proteinG = estimate.proteinG,
    carbsG = estimate.carbsG,
    fatG = estimate.fatG,
    fiberG = estimate.fiberG,
    confidence = estimate.confidence.name,
    basis = estimate.basis,
    isApproximate = estimate.isApproximate,
    timestampEpochMillis = timestampEpochMillis,
    epochDay = epochDay,
    imageRef = imageRef,
    manuallyCorrected = manuallyCorrected,
    source = source.name,
    note = note,
)

// ---- Weight ----
fun WeightEntity.toDomain() = WeightEntry(id, weightKg, dateEpochDay, note)
fun WeightEntry.toEntity() = WeightEntity(id, weightKg, dateEpochDay, note)

// ---- Workout ----
fun WorkoutSessionEntity.toDomain() = WorkoutSession(
    id = id,
    planId = planId,
    startEpochMillis = startEpochMillis,
    endEpochMillis = endEpochMillis,
    completedItemIds = completedItemIds,
    caloriesBurned = caloriesBurned,
    completed = completed,
)

fun WorkoutSession.toEntity(epochDay: Long) = WorkoutSessionEntity(
    id = id,
    planId = planId,
    startEpochMillis = startEpochMillis,
    endEpochMillis = endEpochMillis,
    epochDay = epochDay,
    completedItemIds = completedItemIds,
    caloriesBurned = caloriesBurned,
    completed = completed,
)

// ---- Steps ----
fun StepSummaryEntity.toDomain() = StepSummary(
    dateEpochDay = dateEpochDay,
    steps = steps,
    distanceMeters = distanceMeters,
    activeCaloriesKcal = activeCaloriesKcal,
    source = source.toEnum(StepSource.ESTIMATED),
)

fun StepSummary.toEntity() = StepSummaryEntity(
    dateEpochDay = dateEpochDay,
    steps = steps,
    distanceMeters = distanceMeters,
    activeCaloriesKcal = activeCaloriesKcal,
    source = source.name,
)

// ---- Image ----
fun FoodImageAssetEntity.toDomain() = FoodImageAsset(
    key = key,
    localPath = localPath,
    remoteUrl = remoteUrl,
    source = source.toEnum(ImageSource.PLACEHOLDER),
    prompt = prompt,
    createdAtMillis = createdAtMillis,
)

fun FoodImageAsset.toEntity() = FoodImageAssetEntity(
    key = key,
    localPath = localPath,
    remoteUrl = remoteUrl,
    source = source.name,
    prompt = prompt,
    createdAtMillis = createdAtMillis,
)

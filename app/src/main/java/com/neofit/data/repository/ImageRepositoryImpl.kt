package com.neofit.data.repository

import android.content.Context
import com.neofit.core.util.DateUtil
import com.neofit.data.local.dao.FoodImageDao
import com.neofit.data.local.toDomain
import com.neofit.data.local.toEntity
import com.neofit.domain.model.ExerciseItem
import com.neofit.domain.model.FoodImageAsset
import com.neofit.domain.model.FoodItem
import com.neofit.domain.model.ImageSource
import com.neofit.domain.repository.ImageRepository
import com.neofit.integration.ai.AzureGeneratedImageProvider
import com.neofit.integration.ai.FoodImageProvider
import com.neofit.integration.ai.ImagePromptRequest
import com.neofit.integration.ai.WebImageProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves images via the provider chain (web → Azure generation) and caches the
 * result locally. If every provider declines (e.g., no Azure key configured),
 * a non-cached PLACEHOLDER asset is returned so the UI can draw a fallback and
 * retry later.
 */
@Singleton
class ImageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val foodImageDao: FoodImageDao,
    webImageProvider: WebImageProvider,
    azureImageProvider: AzureGeneratedImageProvider,
) : ImageRepository {

    // Order matters: prefer a real web image, then generate one.
    private val providers: List<FoodImageProvider> = listOf(webImageProvider, azureImageProvider)

    override suspend fun getOrFetchFoodImage(food: FoodItem): FoodImageAsset =
        resolve(
            ImagePromptRequest(
                key = "food_${food.id}",
                dishName = food.nameEn,
                region = food.region,
                knownUrl = food.defaultImageUrl,
            ),
        )

    override suspend fun getOrFetchFoodImageByName(name: String, prompt: String?): FoodImageAsset =
        resolve(
            ImagePromptRequest(
                key = "food_${slug(name)}",
                dishName = name,
                customPrompt = prompt,
            ),
        )

    override suspend fun getOrFetchExerciseImage(item: ExerciseItem): FoodImageAsset =
        resolve(
            ImagePromptRequest(
                key = "ex_${item.id}",
                dishName = item.name,
                customPrompt = item.imagePrompt,
            ),
        )

    private suspend fun resolve(request: ImagePromptRequest): FoodImageAsset {
        cached(request.key)?.let { return it }
        for (provider in providers) {
            val provided = runCatching { provider.provide(request) }.getOrNull() ?: continue
            val asset = when {
                provided.bytes != null -> FoodImageAsset(
                    key = request.key,
                    localPath = saveBytes(request.key, provided.bytes),
                    source = provided.source,
                    prompt = request.customPrompt,
                    createdAtMillis = DateUtil.nowMillis(),
                )
                provided.remoteUrl != null -> FoodImageAsset(
                    key = request.key,
                    remoteUrl = provided.remoteUrl,
                    source = provided.source,
                    createdAtMillis = DateUtil.nowMillis(),
                )
                else -> continue
            }
            foodImageDao.upsert(asset.toEntity())
            return asset
        }
        return FoodImageAsset(key = request.key, source = ImageSource.PLACEHOLDER)
    }

    private suspend fun cached(key: String): FoodImageAsset? {
        val asset = foodImageDao.get(key)?.toDomain() ?: return null
        // Validate that a cached local file still exists.
        if (asset.localPath != null && !File(asset.localPath).exists()) return null
        return asset.takeIf { it.bestRef() != null }
    }

    private suspend fun saveBytes(key: String, bytes: ByteArray): String =
        withContext(Dispatchers.IO) {
            val dir = File(context.filesDir, "food_images").apply { mkdirs() }
            val file = File(dir, "$key.png")
            file.writeBytes(bytes)
            file.absolutePath
        }

    private fun slug(name: String): String =
        name.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')
}

package com.neofit.integration.ai

import com.neofit.domain.model.FoodRegion
import com.neofit.domain.model.ImageSource
import javax.inject.Inject

/** What an image is needed for. `knownUrl` lets the web provider short-circuit. */
data class ImagePromptRequest(
    val key: String,
    val dishName: String,
    val region: FoodRegion? = null,
    val customPrompt: String? = null,
    val knownUrl: String? = null,
)

/** A provider's output: either raw bytes (to cache) or a remote URL. */
data class ProvidedImage(
    val bytes: ByteArray? = null,
    val remoteUrl: String? = null,
    val source: ImageSource,
)

/**
 * Pluggable strategy for resolving a dish/exercise image. Implementations are
 * tried in order by [com.neofit.data.repository.ImageRepositoryImpl].
 */
interface FoodImageProvider {
    val source: ImageSource
    suspend fun provide(request: ImagePromptRequest): ProvidedImage?
}

/**
 * Returns an existing web image when a URL is already known for the dish.
 *
 * TODO(prod): plug a real image-search/CDN here (e.g., a licensed food image
 * API). For now it only resolves images whose URL ships with the food data,
 * so the chain falls through to Azure generation for everything else.
 */
class WebImageProvider @Inject constructor() : FoodImageProvider {
    override val source: ImageSource = ImageSource.WEB
    override suspend fun provide(request: ImagePromptRequest): ProvidedImage? =
        request.knownUrl?.takeIf { it.isNotBlank() }
            ?.let { ProvidedImage(remoteUrl = it, source = ImageSource.WEB) }
}

/** Generates an image with the configured Azure model when no web image exists. */
class AzureGeneratedImageProvider @Inject constructor(
    private val imageGenerationService: ImageGenerationService,
) : FoodImageProvider {
    override val source: ImageSource = ImageSource.AZURE_GENERATED

    override suspend fun provide(request: ImagePromptRequest): ProvidedImage? {
        if (!imageGenerationService.isConfigured()) return null
        val prompt = request.customPrompt ?: PromptBuilder.foodPhoto(request.dishName, request.region)
        val bytes = imageGenerationService.generatePng(prompt) ?: return null
        return ProvidedImage(bytes = bytes, source = ImageSource.AZURE_GENERATED)
    }
}

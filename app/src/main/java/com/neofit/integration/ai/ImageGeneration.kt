package com.neofit.integration.ai

import android.util.Base64
import com.neofit.BuildConfig
import com.neofit.domain.model.FoodRegion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

/** Builds consistent, realistic prompts for food and exercise imagery. */
object PromptBuilder {
    fun foodPhoto(dishName: String, region: FoodRegion? = null): String {
        val regionPart = region?.takeIf { it != FoodRegion.PAN_INDIA && it != FoodRegion.MIXED }
            ?.let { " in ${it.label} style" } ?: ""
        return "Generate a realistic high-quality food photo of $dishName, served in authentic Indian style$regionPart, " +
            "top-down or slight angled lighting, natural plating on traditional tableware, appetising but realistic, " +
            "no text, no branding."
    }
}

@Serializable
private data class AzureImageRequest(val prompt: String, val n: Int = 1, val size: String = "1024x1024")

@Serializable
private data class AzureImageResponse(val data: List<AzureImageData> = emptyList())

@Serializable
private data class AzureImageData(
    @SerialName("b64_json") val b64Json: String? = null,
    val url: String? = null,
)

/** Abstraction over an image-generation backend. Returns PNG bytes or null. */
interface ImageGenerationService {
    /** True when credentials are configured and generation can be attempted. */
    fun isConfigured(): Boolean
    suspend fun generatePng(prompt: String, size: String = "1024x1024"): ByteArray?
}

/**
 * Azure-hosted image generation (gpt-image-2) via the deployment-path REST API:
 *   POST {endpoint}/openai/deployments/{deployment}/images/generations?api-version=...
 *
 * Auth uses the `api-key` header. Credentials come from BuildConfig (populated
 * from local.properties). If no key is set, [isConfigured] is false and the
 * provider chain falls back to a local placeholder — the app stays fully usable.
 *
 * TODO(prod): support Azure AD (DefaultAzureCredential / Managed Identity) auth
 * in addition to api-key for backend-proxied deployments.
 */
@Singleton
class AzureImageGenerationService @Inject constructor(
    private val client: OkHttpClient,
) : ImageGenerationService {

    private val json = Json { ignoreUnknownKeys = true }

    override fun isConfigured(): Boolean =
        BuildConfig.AZURE_OPENAI_API_KEY.isNotBlank() && BuildConfig.AZURE_OPENAI_ENDPOINT.isNotBlank()

    override suspend fun generatePng(prompt: String, size: String): ByteArray? {
        if (!isConfigured()) return null
        return withContext(Dispatchers.IO) {
            runCatching {
                val url = "${BuildConfig.AZURE_OPENAI_ENDPOINT.trimEnd('/')}" +
                    "/openai/deployments/${BuildConfig.AZURE_IMAGE_DEPLOYMENT_NAME}" +
                    "/images/generations?api-version=${BuildConfig.AZURE_API_VERSION}"
                val payload = json.encodeToString(AzureImageRequest(prompt = prompt, size = size))
                val request = Request.Builder()
                    .url(url)
                    .addHeader("api-key", BuildConfig.AZURE_OPENAI_API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(payload.toRequestBody("application/json".toMediaType()))
                    .build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@use null
                    val body = response.body?.string() ?: return@use null
                    val parsed = json.decodeFromString<AzureImageResponse>(body)
                    val b64 = parsed.data.firstOrNull()?.b64Json ?: return@use null
                    Base64.decode(b64, Base64.DEFAULT)
                }
            }.getOrNull()
        }
    }
}

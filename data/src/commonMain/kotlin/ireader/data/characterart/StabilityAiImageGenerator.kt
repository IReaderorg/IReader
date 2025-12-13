package ireader.data.characterart

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Stability AI image generation using their REST API.
 * 
 * Get API key from: https://platform.stability.ai/account/keys
 * Pricing: Pay-per-use, ~$0.002-0.02 per image depending on model
 */
class StabilityAiImageGenerator(
    private val httpClient: HttpClient,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : ImageGeneratorProvider {
    
    override val name = "Stability AI"
    override val requiresApiKey = true
    
    companion object {
        private const val API_URL = "https://api.stability.ai/v2beta/stable-image/generate"
        
        val AVAILABLE_MODELS = listOf(
            ImageModel("sd3-large", "Stable Diffusion 3 Large", "Highest quality, most detailed"),
            ImageModel("sd3-large-turbo", "SD3 Large Turbo", "Fast, high quality"),
            ImageModel("sd3-medium", "Stable Diffusion 3 Medium", "Balanced quality and speed"),
            ImageModel("core", "Stable Image Core", "Fast, good quality"),
            ImageModel("ultra", "Stable Image Ultra", "Premium quality")
        )
    }
    
    override suspend fun generateImage(
        apiKey: String,
        prompt: String,
        characterName: String,
        bookTitle: String,
        style: String
    ): Result<GeneratedImage> {
        return generateWithModel(apiKey, prompt, characterName, bookTitle, style, "core")
    }
    
    suspend fun generateWithModel(
        apiKey: String,
        prompt: String,
        characterName: String,
        bookTitle: String,
        style: String,
        modelId: String
    ): Result<GeneratedImage> {
        return try {
            val enhancedPrompt = buildPrompt(prompt, characterName, bookTitle, style)
            
            val endpoint = when (modelId) {
                "sd3-large", "sd3-large-turbo", "sd3-medium" -> "$API_URL/sd3"
                "core" -> "$API_URL/core"
                "ultra" -> "$API_URL/ultra"
                else -> "$API_URL/core"
            }
            
            val response = httpClient.submitFormWithBinaryData(
                url = endpoint,
                formData = formData {
                    append("prompt", enhancedPrompt)
                    append("output_format", "png")
                    append("aspect_ratio", "1:1")
                    if (modelId.startsWith("sd3")) {
                        append("model", modelId)
                    }
                }
            ) {
                header("Authorization", "Bearer $apiKey")
                header("Accept", "image/*")
            }
            
            when {
                response.status.isSuccess() -> {
                    val imageBytes = response.readRawBytes()
                    Result.success(
                        GeneratedImage(
                            bytes = imageBytes,
                            mimeType = response.contentType()?.toString() ?: "image/png",
                            prompt = enhancedPrompt
                        )
                    )
                }
                response.status.value == 401 -> {
                    Result.failure(Exception("Invalid API key. Get one at platform.stability.ai"))
                }
                response.status.value == 402 -> {
                    Result.failure(Exception("Insufficient credits. Add credits at platform.stability.ai"))
                }
                response.status.value == 429 -> {
                    Result.failure(Exception("Rate limit exceeded. Please wait and try again."))
                }
                else -> {
                    val errorBody = response.bodyAsText()
                    Result.failure(Exception("Generation failed: ${response.status} - $errorBody"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getAvailableModels(apiKey: String): Result<List<ImageModel>> {
        return Result.success(AVAILABLE_MODELS)
    }
    
    private fun buildPrompt(
        userPrompt: String,
        characterName: String,
        bookTitle: String,
        style: String
    ): String {
        return buildString {
            append("$style portrait of $characterName from the book \"$bookTitle\". ")
            append(userPrompt)
            append(", highly detailed, professional illustration, masterpiece, best quality")
        }
    }
}

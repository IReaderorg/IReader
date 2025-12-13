package ireader.data.characterart

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Hugging Face Inference API for image generation.
 * 
 * Free tier: ~1000 requests/day with rate limiting
 * Get API key from: https://huggingface.co/settings/tokens
 * 
 * Supports various Stable Diffusion models.
 */
class HuggingFaceImageGenerator(
    private val httpClient: HttpClient,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : ImageGeneratorProvider {
    
    override val name = "Hugging Face"
    override val requiresApiKey = true
    
    companion object {
        private const val API_URL = "https://api-inference.huggingface.co/models"
        
        // Popular free image generation models on Hugging Face
        val AVAILABLE_MODELS = listOf(
            ImageModel("stabilityai/stable-diffusion-xl-base-1.0", "SDXL 1.0", "High quality, detailed images"),
            ImageModel("runwayml/stable-diffusion-v1-5", "SD 1.5", "Classic Stable Diffusion"),
            ImageModel("stabilityai/stable-diffusion-2-1", "SD 2.1", "Improved quality over 1.5"),
            ImageModel("prompthero/openjourney-v4", "Openjourney v4", "Midjourney-style images"),
            ImageModel("dreamlike-art/dreamlike-diffusion-1.0", "Dreamlike Diffusion", "Artistic, dreamlike style"),
            ImageModel("SG161222/Realistic_Vision_V5.1_noVAE", "Realistic Vision", "Photorealistic images"),
            ImageModel("Lykon/dreamshaper-8", "DreamShaper 8", "Versatile artistic model")
        )
    }
    
    override suspend fun generateImage(
        apiKey: String,
        prompt: String,
        characterName: String,
        bookTitle: String,
        style: String
    ): Result<GeneratedImage> {
        return generateWithModel(apiKey, prompt, characterName, bookTitle, style, AVAILABLE_MODELS.first().id)
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
            
            val requestBody = HFImageRequest(inputs = enhancedPrompt)
            
            val response = httpClient.post("$API_URL/$modelId") {
                header("Authorization", "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(HFImageRequest.serializer(), requestBody))
            }
            
            when {
                response.status.isSuccess() -> {
                    // HuggingFace returns raw image bytes directly
                    val imageBytes = response.readBytes()
                    Result.success(
                        GeneratedImage(
                            bytes = imageBytes,
                            mimeType = response.contentType()?.toString() ?: "image/png",
                            prompt = enhancedPrompt
                        )
                    )
                }
                response.status.value == 503 -> {
                    // Model is loading
                    val errorBody = response.bodyAsText()
                    val estimatedTime = try {
                        json.decodeFromString<HFLoadingResponse>(errorBody).estimatedTime
                    } catch (e: Exception) { 20.0 }
                    Result.failure(Exception("Model is loading. Please wait ${estimatedTime.toInt()} seconds and try again."))
                }
                response.status.value == 429 -> {
                    Result.failure(Exception("Rate limit exceeded. Please wait a moment and try again."))
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
        // Return static list - HF doesn't have a simple API to list image models
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

@Serializable
data class HFImageRequest(
    val inputs: String,
    val parameters: HFParameters = HFParameters()
)

@Serializable
data class HFParameters(
    val negative_prompt: String = "blurry, bad quality, worst quality, low quality, normal quality, lowres, watermark, text",
    val num_inference_steps: Int = 30,
    val guidance_scale: Double = 7.5
)

@Serializable
data class HFLoadingResponse(
    val error: String = "",
    val estimatedTime: Double = 20.0
)

package ireader.data.characterart

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Gemini AI Image Generator using Google's Imagen 3 model.
 * 
 * Requires a Gemini API key from: https://aistudio.google.com/apikey
 * 
 * Free tier limits:
 * - Imagen 3: Limited requests per day
 * - Output: Up to 1024x1024 images
 */
class GeminiImageGenerator(
    private val httpClient: HttpClient,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    
    companion object {
        private const val GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models"
        private const val IMAGE_MODEL = "imagen-4.0-generate-001"
        
        // Default available image generation models (only image generation capable)
        val DEFAULT_IMAGE_MODELS = listOf(
            ImageModel("imagen-4.0-generate-001", "Imagen 4", "Latest high quality image generation"),
            ImageModel("imagen-3.0-generate-002", "Imagen 3", "High quality image generation"),
            ImageModel("imagen-3.0-fast-generate-001", "Imagen 3 Fast", "Faster generation, slightly lower quality"),
            ImageModel("gemini-2.0-flash-preview-image-generation", "Gemini 2.0 Flash", "Multimodal image generation")
        )
        
        // Known image generation model prefixes for filtering
        private val IMAGE_MODEL_PREFIXES = listOf(
            "imagen-",
            "gemini-2.0-flash-preview-image",
            "gemini-2.5-flash-preview-image"
        )
    }
    
    /**
     * Fetch available image generation models from Gemini API
     * 
     * @param apiKey User's Gemini API key
     * @return List of available models that support image generation (only image models, not text models)
     */
    suspend fun fetchAvailableModels(apiKey: String): Result<List<ImageModel>> {
        return try {
            val response = httpClient.get(GEMINI_API_URL) {
                parameter("key", apiKey)
            }
            
            if (response.status.isSuccess()) {
                val responseBody = response.bodyAsText()
                val modelsResponse = json.decodeFromString<ModelsListResponse>(responseBody)
                
                // Filter to ONLY image generation models (not text/chat models)
                val imageModels = modelsResponse.models
                    .filter { model ->
                        val modelId = model.name.removePrefix("models/")
                        // Only include models that are specifically for image generation
                        IMAGE_MODEL_PREFIXES.any { prefix -> 
                            modelId.startsWith(prefix, ignoreCase = true) 
                        }
                    }
                    .map { model ->
                        ImageModel(
                            id = model.name.removePrefix("models/"),
                            displayName = model.displayName.ifBlank { 
                                formatModelName(model.name.removePrefix("models/"))
                            },
                            description = model.description.ifBlank {
                                getDefaultDescription(model.name.removePrefix("models/"))
                            }
                        )
                    }
                    .sortedBy { model ->
                        // Sort: Imagen 4 first, then Imagen 3, then Gemini
                        when {
                            model.id.contains("imagen-4") -> 0
                            model.id.contains("imagen-3") && !model.id.contains("fast") -> 1
                            model.id.contains("imagen-3") && model.id.contains("fast") -> 2
                            else -> 3
                        }
                    }
                
                if (imageModels.isEmpty()) {
                    // Return default models if none found
                    Result.success(DEFAULT_IMAGE_MODELS)
                } else {
                    Result.success(imageModels)
                }
            } else {
                val errorBody = response.bodyAsText()
                val errorMessage = try {
                    val error = json.decodeFromString<GeminiErrorResponse>(errorBody)
                    error.error.message
                } catch (e: Exception) {
                    "Failed to fetch models: ${response.status}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            // Return default models on error
            Result.success(DEFAULT_IMAGE_MODELS)
        }
    }
    
    private fun formatModelName(modelId: String): String {
        return when {
            modelId.contains("imagen-4") -> "Imagen 4"
            modelId.contains("imagen-3") && modelId.contains("fast") -> "Imagen 3 Fast"
            modelId.contains("imagen-3") -> "Imagen 3"
            modelId.contains("gemini-2.0-flash") -> "Gemini 2.0 Flash"
            modelId.contains("gemini-2.5-flash") -> "Gemini 2.5 Flash"
            else -> modelId
        }
    }
    
    private fun getDefaultDescription(modelId: String): String {
        return when {
            modelId.contains("imagen-4") -> "Latest high quality image generation"
            modelId.contains("imagen-3") && modelId.contains("fast") -> "Faster generation, slightly lower quality"
            modelId.contains("imagen-3") -> "High quality image generation"
            modelId.contains("gemini") -> "Multimodal image generation"
            else -> "Image generation model"
        }
    }
    
    /**
     * Generate an image using Gemini's Imagen model
     * 
     * @param apiKey User's Gemini API key
     * @param prompt Description of the image to generate
     * @param characterName Name of the character (added to prompt)
     * @param bookTitle Book the character is from (added to prompt)
     * @param style Art style preference
     * @param modelId Optional model ID to use (defaults to IMAGE_MODEL)
     * @return Result containing image bytes or error
     */
    suspend fun generateImage(
        apiKey: String,
        prompt: String,
        characterName: String,
        bookTitle: String,
        style: String = "digital art",
        modelId: String = IMAGE_MODEL
    ): Result<GeneratedImage> {
        return try {
            // Build enhanced prompt
            val enhancedPrompt = buildPrompt(prompt, characterName, bookTitle, style)
            
            val requestBody = ImageGenerationRequest(
                instances = listOf(
                    ImageInstance(prompt = enhancedPrompt)
                ),
                parameters = ImageParameters(
                    sampleCount = 1,
                    aspectRatio = "1:1",
                    safetyFilterLevel = "block_medium_and_above",
                    personGeneration = "allow_adult"
                )
            )
            
            val response = httpClient.post("$GEMINI_API_URL/$modelId:predict") {
                header("x-goog-api-key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(ImageGenerationRequest.serializer(), requestBody))
            }
            
            if (response.status.isSuccess()) {
                val responseBody = response.bodyAsText()
                val result = json.decodeFromString<ImageGenerationResponse>(responseBody)
                
                val imageData = result.predictions.firstOrNull()
                    ?: return Result.failure(Exception("No image generated"))
                
                @OptIn(ExperimentalEncodingApi::class)
                val imageBytes = Base64.decode(imageData.bytesBase64Encoded)
                
                Result.success(
                    GeneratedImage(
                        bytes = imageBytes,
                        mimeType = imageData.mimeType,
                        prompt = enhancedPrompt
                    )
                )
            } else {
                val errorBody = response.bodyAsText()
                val errorMessage = parseErrorMessage(response.status.value, errorBody)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Parse error message from API response with user-friendly messages
     */
    private fun parseErrorMessage(statusCode: Int, errorBody: String): String {
        return when (statusCode) {
            429 -> "Rate limit exceeded. Please wait a moment and try again. Free tier has limited requests per minute."
            402 -> "Quota exceeded. Please check your Gemini API billing or wait for quota reset."
            403 -> "Access forbidden. Please check your API key permissions."
            404 -> "Model not found. The selected model may not be available in your region."
            else -> {
                try {
                    val error = json.decodeFromString<GeminiErrorResponse>(errorBody)
                    error.error.message
                } catch (e: Exception) {
                    "Image generation failed (HTTP $statusCode)"
                }
            }
        }
    }
    
    /**
     * Generate image using Gemini Flash model (2.0 or 2.5)
     * Uses the generateContent endpoint with image response modality
     */
    suspend fun generateWithGemini2Flash(
        apiKey: String,
        prompt: String,
        characterName: String,
        bookTitle: String,
        modelId: String = "gemini-2.0-flash-preview-image-generation"
    ): Result<GeneratedImage> {
        return try {
            val enhancedPrompt = buildPrompt(prompt, characterName, bookTitle, "detailed illustration")
            
            val requestBody = Gemini2Request(
                contents = listOf(
                    Gemini2Content(
                        parts = listOf(
                            Gemini2Part(text = enhancedPrompt)
                        )
                    )
                ),
                generationConfig = Gemini2GenerationConfig(
                    responseModalities = listOf("TEXT", "IMAGE")
                )
            )
            
            val response = httpClient.post("$GEMINI_API_URL/$modelId:generateContent") {
                header("x-goog-api-key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(Gemini2Request.serializer(), requestBody))
            }
            
            if (response.status.isSuccess()) {
                val responseBody = response.bodyAsText()
                val result = json.decodeFromString<Gemini2Response>(responseBody)
                
                // Find image part in response
                val imagePart = result.candidates.firstOrNull()?.content?.parts
                    ?.find { it.inlineData != null }?.inlineData
                    ?: return Result.failure(Exception("No image in response. The model may not support image generation."))
                
                @OptIn(ExperimentalEncodingApi::class)
                val imageBytes = Base64.decode(imagePart.data)
                
                Result.success(
                    GeneratedImage(
                        bytes = imageBytes,
                        mimeType = imagePart.mimeType,
                        prompt = enhancedPrompt
                    )
                )
            } else {
                val errorBody = response.bodyAsText()
                val errorMessage = parseErrorMessage(response.status.value, errorBody)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun buildPrompt(
        userPrompt: String,
        characterName: String,
        bookTitle: String,
        style: String
    ): String {
        return buildString {
            append("Create a $style portrait of $characterName from the book \"$bookTitle\". ")
            append(userPrompt)
            append(" High quality, detailed, character portrait, book illustration style.")
        }
    }
}

/**
 * Result of image generation
 */
data class GeneratedImage(
    val bytes: ByteArray,
    val mimeType: String,
    val prompt: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as GeneratedImage
        return bytes.contentEquals(other.bytes) && prompt == other.prompt
    }
    
    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + prompt.hashCode()
        return result
    }
}

// Request/Response DTOs for Imagen 3

@Serializable
data class ImageGenerationRequest(
    val instances: List<ImageInstance>,
    val parameters: ImageParameters
)

@Serializable
data class ImageInstance(
    val prompt: String
)

@Serializable
data class ImageParameters(
    val sampleCount: Int = 1,
    val aspectRatio: String = "1:1",
    val safetyFilterLevel: String = "block_medium_and_above",
    val personGeneration: String = "allow_adult"
)

@Serializable
data class ImageGenerationResponse(
    val predictions: List<ImagePrediction> = emptyList()
)

@Serializable
data class ImagePrediction(
    val bytesBase64Encoded: String,
    val mimeType: String = "image/png"
)

@Serializable
data class GeminiErrorResponse(
    val error: GeminiError
)

@Serializable
data class GeminiError(
    val code: Int = 0,
    val message: String = "Unknown error",
    val status: String = ""
)

// Request/Response DTOs for Gemini 2.0 Flash

@Serializable
data class Gemini2Request(
    val contents: List<Gemini2Content>,
    val generationConfig: Gemini2GenerationConfig? = null
)

@Serializable
data class Gemini2Content(
    val parts: List<Gemini2Part>
)

@Serializable
data class Gemini2Part(
    val text: String? = null,
    val inlineData: Gemini2InlineData? = null
)

@Serializable
data class Gemini2InlineData(
    val mimeType: String,
    val data: String
)

@Serializable
data class Gemini2GenerationConfig(
    val responseModalities: List<String> = listOf("TEXT", "IMAGE")
)

@Serializable
data class Gemini2Response(
    val candidates: List<Gemini2Candidate> = emptyList()
)

@Serializable
data class Gemini2Candidate(
    val content: Gemini2Content? = null
)

// Models list response DTOs

@Serializable
data class ModelsListResponse(
    val models: List<GeminiModelInfo> = emptyList()
)

@Serializable
data class GeminiModelInfo(
    val name: String,
    val displayName: String = "",
    val description: String = "",
    val supportedGenerationMethods: List<String> = emptyList(),
    val inputTokenLimit: Int = 0,
    val outputTokenLimit: Int = 0
)

/**
 * Simplified image model info for UI
 */
data class ImageModel(
    val id: String,
    val displayName: String,
    val description: String = ""
)

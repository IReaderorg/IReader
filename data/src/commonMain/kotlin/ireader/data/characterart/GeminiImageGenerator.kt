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
        private const val DEFAULT_IMAGE_MODEL = "gemini-2.5-flash-image"
        
        // Default available image generation models
        val DEFAULT_IMAGE_MODELS = listOf(
            // Nano Banana models (newest, use generateContent API)
            ImageModel("nano-banana-pro-preview", "Nano Banana Pro", "Gemini 3 Pro Image Preview"),
            ImageModel("gemini-3-pro-image-preview", "Gemini 3 Pro Image", "Gemini 3 Pro Image Preview"),
            ImageModel("gemini-2.5-flash-image", "Nano Banana", "Gemini 2.5 Flash Image"),
            ImageModel("gemini-2.5-flash-image-preview", "Nano Banana Preview", "Gemini 2.5 Flash Preview Image"),
            // Imagen models (use predict API)
            ImageModel("imagen-4.0-ultra-generate-001", "Imagen 4 Ultra", "Highest quality image generation"),
            ImageModel("imagen-4.0-generate-001", "Imagen 4", "High quality image generation"),
            ImageModel("imagen-4.0-fast-generate-001", "Imagen 4 Fast", "Fast image generation")
        )
        
        // Models that use the predict API (Imagen models)
        private val PREDICT_API_MODELS = setOf(
            "imagen-4.0-generate-001",
            "imagen-4.0-ultra-generate-001",
            "imagen-4.0-fast-generate-001",
            "imagen-4.0-generate-preview-06-06",
            "imagen-4.0-ultra-generate-preview-06-06"
        )
    }
    
    /**
     * Fetch available models from Gemini API
     * Shows all models but prioritizes image-related ones at the top
     * 
     * @param apiKey User's Gemini API key
     * @return List of available models, sorted with image models first
     */
    suspend fun fetchAvailableModels(apiKey: String): Result<List<ImageModel>> {
        return try {
            val response = httpClient.get(GEMINI_API_URL) {
                parameter("key", apiKey)
            }
            
            if (response.status.isSuccess()) {
                val responseBody = response.bodyAsText()
                val modelsResponse = json.decodeFromString<ModelsListResponse>(responseBody)
                
                // Show ALL models, but sort with image-related ones first
                val allModels = modelsResponse.models
                    .map { model ->
                        val modelId = model.name.removePrefix("models/")
                        val description = model.description.ifBlank { 
                            getDefaultDescription(modelId) 
                        }
                        ImageModel(
                            id = modelId,
                            displayName = model.displayName.ifBlank { 
                                formatModelName(modelId)
                            },
                            description = description
                        )
                    }
                    .sortedBy { model ->
                        // Priority sorting: banana/nano/image models first
                        val modelLower = model.id.lowercase()
                        val descLower = model.description.lowercase()
                        val displayLower = model.displayName.lowercase()
                        when {
                            // Top priority: nano banana models
                            modelLower.contains("nano-banana") || displayLower.contains("nano banana") -> -3
                            modelLower.contains("banana") || displayLower.contains("banana") -> -2
                            // High priority: models with "image" in ID
                            modelLower.contains("-image") -> -1
                            // Imagen 4 models
                            modelLower.contains("imagen-4") && modelLower.contains("ultra") -> 0
                            modelLower.contains("imagen-4") && !modelLower.contains("fast") -> 1
                            modelLower.contains("imagen-4") && modelLower.contains("fast") -> 2
                            modelLower.contains("imagen") -> 3
                            // Models with "image" in description
                            descLower.contains("image") -> 4
                            // Gemini 3 models
                            modelLower.contains("gemini-3") -> 5
                            // Gemini 2.5 flash models
                            modelLower.contains("gemini-2.5") && modelLower.contains("flash") -> 6
                            modelLower.contains("gemini-2") && modelLower.contains("flash") -> 7
                            modelLower.contains("gemini-2") -> 8
                            // Other gemini models
                            modelLower.contains("gemini") -> 9
                            // Lowest: everything else
                            else -> 10
                        }
                    }
                
                if (allModels.isEmpty()) {
                    // Return default models if none found
                    Result.success(DEFAULT_IMAGE_MODELS)
                } else {
                    Result.success(allModels)
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
     * Generate an image using Gemini API
     * Automatically selects the correct API endpoint based on model type
     * 
     * @param apiKey User's Gemini API key
     * @param prompt Description of the image to generate
     * @param characterName Name of the character (added to prompt)
     * @param bookTitle Book the character is from (added to prompt)
     * @param style Art style preference
     * @param modelId Model ID to use
     * @return Result containing image bytes or error
     */
    suspend fun generateImage(
        apiKey: String,
        prompt: String,
        characterName: String,
        bookTitle: String,
        style: String = "digital art",
        modelId: String = DEFAULT_IMAGE_MODEL
    ): Result<GeneratedImage> {
        // Use predict API for Imagen models, generateContent for Gemini models
        return if (PREDICT_API_MODELS.contains(modelId) || modelId.startsWith("imagen")) {
            generateWithPredictApi(apiKey, prompt, characterName, bookTitle, style, modelId)
        } else {
            generateWithGenerateContentApi(apiKey, prompt, characterName, bookTitle, style, modelId)
        }
    }
    
    /**
     * Generate image using the newer generateContent API (for Gemini models)
     * This is the simpler API format used by gemini-2.5-flash-image and similar models
     */
    private suspend fun generateWithGenerateContentApi(
        apiKey: String,
        prompt: String,
        characterName: String,
        bookTitle: String,
        style: String,
        modelId: String
    ): Result<GeneratedImage> {
        return try {
            val enhancedPrompt = buildPrompt(prompt, characterName, bookTitle, style)
            
            // Simple request format matching the curl command
            val requestBody = SimpleGenerateContentRequest(
                contents = listOf(
                    SimpleContent(
                        parts = listOf(
                            SimplePart(text = enhancedPrompt)
                        )
                    )
                )
            )
            
            val response = httpClient.post("$GEMINI_API_URL/$modelId:generateContent") {
                header("x-goog-api-key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(SimpleGenerateContentRequest.serializer(), requestBody))
            }
            
            if (response.status.isSuccess()) {
                val responseBody = response.bodyAsText()
                val result = json.decodeFromString<Gemini2Response>(responseBody)
                
                // Find image part in response (look for inlineData with image)
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
    
    /**
     * Generate image using the predict API (for Imagen models)
     */
    private suspend fun generateWithPredictApi(
        apiKey: String,
        prompt: String,
        characterName: String,
        bookTitle: String,
        style: String,
        modelId: String
    ): Result<GeneratedImage> {
        return try {
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
     * Uses the generateContent endpoint - delegates to the main generateImage method
     */
    suspend fun generateWithGemini2Flash(
        apiKey: String,
        prompt: String,
        characterName: String,
        bookTitle: String,
        modelId: String = DEFAULT_IMAGE_MODEL
    ): Result<GeneratedImage> {
        return generateWithGenerateContentApi(apiKey, prompt, characterName, bookTitle, "detailed illustration", modelId)
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

// GeneratedImage is defined in ImageGeneratorProvider.kt

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

// Simple request format for newer Gemini image models (like gemini-2.5-flash-image)

@Serializable
data class SimpleGenerateContentRequest(
    val contents: List<SimpleContent>
)

@Serializable
data class SimpleContent(
    val parts: List<SimplePart>
)

@Serializable
data class SimplePart(
    val text: String
)

// Request/Response DTOs for Gemini 2.0 Flash (with generationConfig)

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

// ImageModel is defined in ImageGeneratorProvider.kt

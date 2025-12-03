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
        private const val IMAGE_MODEL = "imagen-3.0-generate-002"
    }
    
    /**
     * Generate an image using Gemini's Imagen 3 model
     * 
     * @param apiKey User's Gemini API key
     * @param prompt Description of the image to generate
     * @param characterName Name of the character (added to prompt)
     * @param bookTitle Book the character is from (added to prompt)
     * @param style Art style preference
     * @return Result containing image bytes or error
     */
    suspend fun generateImage(
        apiKey: String,
        prompt: String,
        characterName: String,
        bookTitle: String,
        style: String = "digital art"
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
            
            val response = httpClient.post("$GEMINI_API_URL/$IMAGE_MODEL:predict") {
                parameter("key", apiKey)
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
                val errorMessage = try {
                    val error = json.decodeFromString<GeminiErrorResponse>(errorBody)
                    error.error.message
                } catch (e: Exception) {
                    "Image generation failed: ${response.status}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Alternative: Use Gemini 2.0 Flash for image generation (experimental)
     */
    suspend fun generateWithGemini2Flash(
        apiKey: String,
        prompt: String,
        characterName: String,
        bookTitle: String
    ): Result<GeneratedImage> {
        return try {
            val enhancedPrompt = buildPrompt(prompt, characterName, bookTitle, "detailed illustration")
            
            val requestBody = Gemini2Request(
                contents = listOf(
                    Gemini2Content(
                        parts = listOf(
                            Gemini2Part(text = "Generate an image: $enhancedPrompt")
                        )
                    )
                ),
                generationConfig = Gemini2GenerationConfig(
                    responseModalities = listOf("TEXT", "IMAGE")
                )
            )
            
            val response = httpClient.post("$GEMINI_API_URL/gemini-2.0-flash-exp:generateContent") {
                parameter("key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(Gemini2Request.serializer(), requestBody))
            }
            
            if (response.status.isSuccess()) {
                val responseBody = response.bodyAsText()
                val result = json.decodeFromString<Gemini2Response>(responseBody)
                
                // Find image part in response
                val imagePart = result.candidates.firstOrNull()?.content?.parts
                    ?.find { it.inlineData != null }?.inlineData
                    ?: return Result.failure(Exception("No image in response"))
                
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
                Result.failure(Exception("Generation failed: ${response.status}"))
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

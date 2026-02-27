package ireader.data.characterart

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.encodeURLPathPart
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Pollinations.ai - Image generation API
 * 
 * Updated to use the new unified API: gen.pollinations.ai (Feb 2026)
 * 
 * **IMPORTANT: API Key Required (Free Tier Available)**
 * - Sign up at https://enter.pollinations.ai to get a FREE API key
 * - Free tier: 1 pollen per IP per hour (publishable key: pk_...)
 * - Publishable keys (pk_) can be used client-side
 * - Secret keys (sk_) should only be used server-side
 * 
 * Uses various open-source models including Flux, GPT Image, and Seedream.
 * 
 * Website: https://pollinations.ai
 * API Docs: https://github.com/pollinations/pollinations
 */
class PollinationsImageGenerator(
    private val httpClient: HttpClient
) : ImageGeneratorProvider {
    
    override val name = "Pollinations.ai"
    override val requiresApiKey = true // Changed: Now requires API key
    
    companion object {
        // New unified API endpoint
        private const val API_URL = "https://gen.pollinations.ai/image"
        
        val AVAILABLE_MODELS = listOf(
            ImageModel("flux", "Flux", "High quality, fast generation (default)"),
            ImageModel("flux-realism", "Flux Realism", "Photorealistic images"),
            ImageModel("flux-anime", "Flux Anime", "Anime/manga style"),
            ImageModel("flux-3d", "Flux 3D", "3D rendered style"),
            ImageModel("turbo", "Turbo", "Fastest generation"),
            ImageModel("gptimage", "GPT Image", "OpenAI-style generation"),
            ImageModel("seedream", "Seedream", "Artistic style")
        )
    }
    
    override suspend fun generateImage(
        apiKey: String,
        prompt: String,
        characterName: String,
        bookTitle: String,
        style: String
    ): Result<GeneratedImage> {
        if (apiKey.isBlank()) {
            return Result.failure(Exception(
                "API key required. Get a FREE key at https://enter.pollinations.ai\n" +
                "Free tier: 1 pollen per IP per hour"
            ))
        }
        return generateWithModel(apiKey, prompt, characterName, bookTitle, style, "flux")
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
            
            // URL encode the prompt using Ktor's built-in function
            val encodedPrompt = enhancedPrompt.encodeURLPathPart()
            
            // Build URL with new API format
            // Note: seed must be a reasonable integer (not timestamp in milliseconds)
            val seed = (ireader.core.util.currentTimeMillis() % 2147483647).toInt() // Keep within Int32 range
            val url = buildString {
                append("$API_URL/$encodedPrompt")
                append("?model=$modelId")
                append("&width=1024")
                append("&height=1024")
                append("&seed=$seed") // Random seed for variety
                append("&nologo=true")
                append("&enhance=true") // Auto-enhance prompts
                append("&key=$apiKey") // API key required
            }
            
            val response = httpClient.get(url)
            
            if (response.status.isSuccess()) {
                val imageBytes = response.readBytes()
                Result.success(
                    GeneratedImage(
                        bytes = imageBytes,
                        mimeType = response.contentType()?.toString() ?: "image/png",
                        prompt = enhancedPrompt
                    )
                )
            } else {
                Result.failure(Exception("Generation failed: ${response.status} - ${response.bodyAsText()}"))
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
            append("$style portrait of $characterName from the book $bookTitle. ")
            append(userPrompt)
            append(", highly detailed, professional illustration")
        }
    }
}

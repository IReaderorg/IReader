package ireader.data.characterart

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Pollinations.ai - FREE image generation, NO API key required!
 * 
 * This is a great fallback option when other APIs have rate limits.
 * Uses various open-source models including Flux.
 * 
 * Website: https://pollinations.ai
 */
class PollinationsImageGenerator(
    private val httpClient: HttpClient
) : ImageGeneratorProvider {
    
    override val name = "Pollinations.ai"
    override val requiresApiKey = false
    
    companion object {
        private const val API_URL = "https://image.pollinations.ai/prompt"
        
        val AVAILABLE_MODELS = listOf(
            ImageModel("flux", "Flux", "High quality, fast generation"),
            ImageModel("flux-realism", "Flux Realism", "Photorealistic images"),
            ImageModel("flux-anime", "Flux Anime", "Anime/manga style"),
            ImageModel("flux-3d", "Flux 3D", "3D rendered style"),
            ImageModel("turbo", "Turbo", "Fastest generation")
        )
    }
    
    override suspend fun generateImage(
        apiKey: String, // Not used, but kept for interface compatibility
        prompt: String,
        characterName: String,
        bookTitle: String,
        style: String
    ): Result<GeneratedImage> {
        return generateWithModel(prompt, characterName, bookTitle, style, "flux")
    }
    
    suspend fun generateWithModel(
        prompt: String,
        characterName: String,
        bookTitle: String,
        style: String,
        modelId: String
    ): Result<GeneratedImage> {
        return try {
            val enhancedPrompt = buildPrompt(prompt, characterName, bookTitle, style)
            
            // URL encode the prompt
            val encodedPrompt = enhancedPrompt.encodeURLParameter()
            
            // Build URL with parameters
            val url = buildString {
                append("$API_URL/$encodedPrompt")
                append("?model=$modelId")
                append("&width=1024")
                append("&height=1024")
                append("&seed=${currentTimeToLong()}") // Random seed for variety
                append("&nologo=true")
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
                Result.failure(Exception("Generation failed: ${response.status}"))
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
    
    // Extension to encode URL parameters
    private fun String.encodeURLParameter(): String {
        return this.replace(" ", "%20")
            .replace("\"", "%22")
            .replace("#", "%23")
            .replace("&", "%26")
            .replace("'", "%27")
            .replace("(", "%28")
            .replace(")", "%29")
            .replace("+", "%2B")
            .replace(",", "%2C")
    }
}

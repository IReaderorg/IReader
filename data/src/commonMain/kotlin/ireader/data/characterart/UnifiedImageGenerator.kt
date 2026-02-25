package ireader.data.characterart

import io.ktor.client.*
import kotlinx.serialization.json.Json

/**
 * Unified image generator that supports multiple providers.
 * 
 * Providers:
 * - Gemini (Google) - Requires API key, high quality
 * - Pollinations.ai - Requires API key (free tier available)
 */
class UnifiedImageGenerator(
    private val httpClient: HttpClient
) {
    private val geminiGenerator by lazy { GeminiImageGenerator(httpClient) }
    private val pollinationsGenerator by lazy { PollinationsImageGenerator(httpClient) }
    
    /**
     * Get all available providers
     */
    fun getProviders(): List<ImageProvider> = ImageProvider.entries
    
    /**
     * Get available models for a provider
     */
    suspend fun getModelsForProvider(provider: ImageProvider, apiKey: String = ""): Result<List<ImageModel>> {
        return when (provider) {
            ImageProvider.GEMINI -> geminiGenerator.fetchAvailableModels(apiKey)
            ImageProvider.POLLINATIONS -> pollinationsGenerator.getAvailableModels(apiKey)
        }
    }
    
    /**
     * Generate image using specified provider
     */
    suspend fun generateImage(
        provider: ImageProvider,
        apiKey: String,
        prompt: String,
        characterName: String,
        bookTitle: String,
        style: String = "digital art",
        modelId: String? = null
    ): Result<GeneratedImage> {
        return when (provider) {
            ImageProvider.GEMINI -> {
                val model = modelId ?: "gemini-2.5-flash-image"
                geminiGenerator.generateImage(apiKey, prompt, characterName, bookTitle, style, model)
            }
            ImageProvider.POLLINATIONS -> {
                val model = modelId ?: "flux"
                pollinationsGenerator.generateWithModel(apiKey, prompt, characterName, bookTitle, style, model)
            }
        }
    }
    
    /**
     * Try to generate with fallback providers if primary fails with rate limit
     */
    suspend fun generateWithFallback(
        primaryProvider: ImageProvider,
        apiKey: String,
        prompt: String,
        characterName: String,
        bookTitle: String,
        style: String = "digital art",
        modelId: String? = null
    ): Result<GeneratedImage> {
        // Try primary provider first
        val primaryResult = generateImage(primaryProvider, apiKey, prompt, characterName, bookTitle, style, modelId)
        
        if (primaryResult.isSuccess) {
            return primaryResult
        }
        
        // Check if it's a rate limit error
        val error = primaryResult.exceptionOrNull()
        val isRateLimit = error?.message?.contains("429", ignoreCase = true) == true ||
                          error?.message?.contains("rate limit", ignoreCase = true) == true
        
        if (isRateLimit) {
            // Fallback to Pollinations if not already using it
            if (primaryProvider != ImageProvider.POLLINATIONS) {
                println("Primary provider rate limited, falling back to Pollinations.ai")
                return generateImage(ImageProvider.POLLINATIONS, "", prompt, characterName, bookTitle, style, "flux")
            }
        }
        
        return primaryResult
    }
}

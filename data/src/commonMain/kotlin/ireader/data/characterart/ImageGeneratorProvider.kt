package ireader.data.characterart

/**
 * Unified interface for image generation providers.
 * Supports multiple AI image generation services.
 */
interface ImageGeneratorProvider {
    val name: String
    val requiresApiKey: Boolean
    
    /**
     * Generate an image from a text prompt
     */
    suspend fun generateImage(
        apiKey: String,
        prompt: String,
        characterName: String,
        bookTitle: String,
        style: String = "digital art"
    ): Result<GeneratedImage>
    
    /**
     * Get available models for this provider
     */
    suspend fun getAvailableModels(apiKey: String): Result<List<ImageModel>>
}

/**
 * Enum of supported image generation providers
 */
enum class ImageProvider(
    val displayName: String,
    val emoji: String,
    val requiresApiKey: Boolean,
    val description: String
) {
    GEMINI(
        displayName = "Google Gemini",
        emoji = "✨",
        requiresApiKey = true,
        description = "High quality, requires API key"
    ),
    POLLINATIONS(
        displayName = "Pollinations.ai",
        emoji = "🌸",
        requiresApiKey = true,
        description = "Paid - API key required (free tier available)"
    )
}

/**
 * Data class for provider configuration
 */
data class ProviderConfig(
    val provider: ImageProvider,
    val apiKey: String = "",
    val selectedModel: String = ""
)

/**
 * Represents an AI model for image generation
 */
data class ImageModel(
    val id: String,
    val displayName: String,
    val description: String = ""
)

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
        return bytes.contentEquals(other.bytes) && mimeType == other.mimeType && prompt == other.prompt
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + prompt.hashCode()
        return result
    }
}

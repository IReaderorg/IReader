package ireader.domain.plugins

import kotlinx.serialization.Serializable

/**
 * Plugin interface for translation services
 * Requirements: 4.1, 4.2, 4.3, 4.4, 4.5
 */
interface TranslationPlugin : Plugin {
    /**
     * Translate a single text string
     * @param text Text to translate
     * @param from Source language code (ISO 639-1)
     * @param to Target language code (ISO 639-1)
     * @return Result containing translated text or error
     */
    suspend fun translate(text: String, from: String, to: String): Result<String>
    
    /**
     * Translate multiple text strings in batch
     * @param texts List of texts to translate
     * @param from Source language code (ISO 639-1)
     * @param to Target language code (ISO 639-1)
     * @return Result containing list of translated texts or error
     */
    suspend fun translateBatch(texts: List<String>, from: String, to: String): Result<List<String>>
    
    /**
     * Get supported language pairs for translation
     * @return List of supported language pairs
     */
    fun getSupportedLanguages(): List<LanguagePair>
    
    /**
     * Check if this translation service requires an API key
     * @return true if API key is required
     */
    fun requiresApiKey(): Boolean
    
    /**
     * Configure the API key for this translation service
     * @param key API key to use
     */
    fun configureApiKey(key: String)
}

/**
 * Language pair for translation
 */
@Serializable
data class LanguagePair(
    val from: String,
    val to: String
)

package ireader.plugin.api

import kotlinx.serialization.Serializable

/**
 * Plugin interface for translation services.
 * Translation plugins provide text translation capabilities.
 * 
 * Example:
 * ```kotlin
 * class DeepLPlugin : TranslationPlugin {
 *     override val manifest = PluginManifest(
 *         id = "com.example.deepl",
 *         name = "DeepL Translation",
 *         type = PluginType.TRANSLATION,
 *         permissions = listOf(PluginPermission.NETWORK),
 *         // ... other manifest fields
 *     )
 *     
 *     override suspend fun translate(text: String, from: String, to: String): Result<String> {
 *         // Call DeepL API and return translated text
 *     }
 *     
 *     // ... other implementations
 * }
 * ```
 */
interface TranslationPlugin : Plugin {
    /**
     * Translate a single text string.
     * 
     * @param text Text to translate
     * @param from Source language code (ISO 639-1, e.g., "en")
     * @param to Target language code (ISO 639-1, e.g., "ja")
     * @return Result containing translated text or error
     */
    suspend fun translate(text: String, from: String, to: String): Result<String>
    
    /**
     * Translate multiple text strings in batch.
     * More efficient than calling translate() multiple times.
     * 
     * @param texts List of texts to translate
     * @param from Source language code (ISO 639-1)
     * @param to Target language code (ISO 639-1)
     * @return Result containing list of translated texts or error
     */
    suspend fun translateBatch(texts: List<String>, from: String, to: String): Result<List<String>>
    
    /**
     * Get supported language pairs for translation.
     * 
     * @return List of supported language pairs
     */
    fun getSupportedLanguages(): List<LanguagePair>
    
    /**
     * Check if this translation service requires an API key.
     * 
     * @return true if API key is required
     */
    fun requiresApiKey(): Boolean
    
    /**
     * Configure the API key for this translation service.
     * Called when user provides API key in settings.
     * 
     * @param key API key to use
     */
    fun configureApiKey(key: String)
}

/**
 * Language pair for translation.
 * Represents a supported source-to-target language combination.
 */
@Serializable
data class LanguagePair(
    /** Source language code (ISO 639-1) */
    val from: String,
    /** Target language code (ISO 639-1) */
    val to: String
)

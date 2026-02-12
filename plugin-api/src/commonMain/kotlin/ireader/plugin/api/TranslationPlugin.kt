package ireader.plugin.api

import kotlinx.serialization.Serializable

/**
 * Plugin interface for translation services.
 * Translation plugins provide text translation capabilities.
 * 
 * ## Configuration via Manifest Metadata
 * 
 * Translation plugins can be configured via manifest metadata for simpler implementations:
 * 
 * ```kotlin
 * metadata = mapOf(
 *     "translation.apiUrl" to "https://api.example.com/translate",
 *     "translation.apiType" to "REST_JSON",  // REST_JSON, REST_FORM, GRAPHQL
 *     "translation.requiresApiKey" to "true",
 *     "translation.apiKeyHeader" to "Authorization",
 *     "translation.apiKeyPrefix" to "Bearer ",
 *     "translation.maxCharsPerRequest" to "5000",
 *     "translation.rateLimitDelayMs" to "1000",
 *     "translation.supportsAI" to "true",
 *     "translation.supportsContextAware" to "true",
 *     "translation.supportedLanguages" to "en,zh,ja,ko,es,fr,de,ru,pt,it"
 * )
 * ```
 * 
 * ## Full Implementation Example
 * 
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
    
    // ==================== Plugin Configuration ====================
    
    /**
     * Get the configuration fields for this plugin.
     * The app will automatically generate UI based on these fields.
     * 
     * @return List of configuration fields
     */
    fun getConfigFields(): List<PluginConfig<*>> = emptyList()
    
    /**
     * Called when a configuration value changes.
     * Override to handle config changes (e.g., reconnect to server).
     * 
     * @param key The config field key that changed
     * @param value The new value
     */
    fun onConfigChanged(key: String, value: Any?) {}
    
    /**
     * Get the current value of a configuration field.
     * Override to return the actual saved/current value for a config key.
     * 
     * @param key The config field key
     * @return The current value, or null if not set
     */
    fun getConfigValue(key: String): Any? = null
    
    /**
     * Get dynamic options for a Select configuration field.
     * Override to provide dynamically updated options (e.g., models from server).
     * 
     * @param key The config field key
     * @return List of options for the select field, or null to use static options
     */
    fun getConfigOptions(key: String): List<String>? = null
    
    // ==================== Core Translation Methods ====================
    
    /**
     * Translate a single text string.
     * 
     * @param text Text to translate
     * @param from Source language code (ISO 639-1, e.g., "en", or "auto" for auto-detect)
     * @param to Target language code (ISO 639-1, e.g., "ja")
     * @return Result containing translated text or error
     */
    suspend fun translate(text: String, from: String, to: String): Result<String>
    
    /**
     * Translate multiple text strings in batch.
     * More efficient than calling translate() multiple times.
     * Default implementation calls translate() for each text.
     * 
     * @param texts List of texts to translate
     * @param from Source language code (ISO 639-1, or "auto")
     * @param to Target language code (ISO 639-1)
     * @return Result containing list of translated texts or error
     */
    suspend fun translateBatch(texts: List<String>, from: String, to: String): Result<List<String>> {
        return try {
            val results = texts.map { text ->
                translate(text, from, to).getOrThrow()
            }
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Translate with context-aware settings for AI-powered engines.
     * Allows specifying content type, tone, and style preservation.
     * Default implementation falls back to regular translate().
     * 
     * @param text Text to translate
     * @param from Source language code
     * @param to Target language code
     * @param context Translation context with content type, tone, etc.
     * @return Result containing translated text or error
     */
    suspend fun translateWithContext(
        text: String,
        from: String,
        to: String,
        context: TranslationContext
    ): Result<String> {
        return translate(text, from, to)
    }
    
    /**
     * Translate batch with context-aware settings.
     * Default implementation calls translateWithContext() for each text.
     * 
     * @param texts List of texts to translate
     * @param from Source language code
     * @param to Target language code
     * @param context Translation context
     * @return Result containing list of translated texts or error
     */
    suspend fun translateBatchWithContext(
        texts: List<String>,
        from: String,
        to: String,
        context: TranslationContext
    ): Result<List<String>> {
        return try {
            val results = texts.map { text ->
                translateWithContext(text, from, to, context).getOrThrow()
            }
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ==================== Language Support ====================
    
    /**
     * Get supported language pairs for translation.
     * 
     * @return List of supported language pairs
     */
    fun getSupportedLanguages(): List<LanguagePair>
    
    /**
     * Get list of all supported language codes with display names.
     * Default implementation extracts unique languages from getSupportedLanguages().
     * 
     * @return List of (code, displayName) pairs
     */
    fun getAvailableLanguages(): List<Pair<String, String>> {
        val languages = mutableSetOf<String>()
        getSupportedLanguages().forEach {
            if (it.from != "*") languages.add(it.from)
            if (it.to != "*") languages.add(it.to)
        }
        return languages.map { it to it }
    }
    
    /**
     * Check if a specific language pair is supported.
     * 
     * @param from Source language code
     * @param to Target language code
     * @return true if the pair is supported
     */
    fun supportsLanguagePair(from: String, to: String): Boolean {
        return getSupportedLanguages().any { pair ->
            (pair.from == from || pair.from == "*") && 
            (pair.to == to || pair.to == "*")
        }
    }
    
    // ==================== API Key Configuration ====================
    
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
    
    /**
     * Get the current API key (if set).
     * Default implementation returns null.
     * 
     * @return Current API key or null
     */
    fun getApiKey(): String? = null
    
    // ==================== Engine Capabilities ====================
    
    /**
     * Whether this engine supports AI-powered translation.
     * AI engines typically provide better context understanding.
     */
    val supportsAI: Boolean get() = false
    
    /**
     * Whether this engine supports context-aware translation.
     * Context-aware engines can adjust translation based on content type and tone.
     */
    val supportsContextAwareTranslation: Boolean get() = false
    
    /**
     * Whether this engine supports style preservation.
     * Style-preserving engines maintain literary style, metaphors, etc.
     */
    val supportsStylePreservation: Boolean get() = false
    
    /**
     * Whether this engine runs offline/locally.
     * Offline engines don't need network access after initialization.
     */
    val isOffline: Boolean get() = false
    
    /**
     * Whether this engine requires initialization before use.
     * Some engines need to download language models first.
     */
    val requiresInitialization: Boolean get() = false
    
    // ==================== Rate Limiting ====================
    
    /**
     * Maximum characters per request for this engine.
     * Used to chunk large texts before sending to the API.
     * Default: 4000 characters
     */
    val maxCharsPerRequest: Int get() = 4000
    
    /**
     * Minimum delay between requests in milliseconds.
     * Used to prevent rate limiting from online APIs.
     * Default: 1000ms for online engines, 0 for offline
     */
    val rateLimitDelayMs: Long get() = if (isOffline) 0L else 1000L
    
    // ==================== Initialization ====================
    
    /**
     * Initialize the translation engine.
     * Called before first use if requiresInitialization is true.
     * Can be used to download language models, etc.
     * 
     * @param sourceLanguage Source language code
     * @param targetLanguage Target language code
     * @param onProgress Progress callback (0-100)
     * @return Result indicating success or failure
     */
    suspend fun initializeEngine(
        sourceLanguage: String,
        targetLanguage: String,
        onProgress: (Int) -> Unit = {}
    ): Result<Unit> {
        return Result.success(Unit)
    }
    
    /**
     * Check if the engine is initialized for the given language pair.
     * 
     * @param sourceLanguage Source language code
     * @param targetLanguage Target language code
     * @return true if initialized
     */
    fun isInitialized(sourceLanguage: String, targetLanguage: String): Boolean = true
}

/**
 * Language pair for translation.
 * Represents a supported source-to-target language combination.
 * Use "*" as wildcard to indicate any language is supported.
 */
@Serializable
data class LanguagePair(
    /** Source language code (ISO 639-1), or "*" for any */
    val from: String,
    /** Target language code (ISO 639-1), or "*" for any */
    val to: String
)

/**
 * Context information for enhanced AI translation.
 * Allows fine-tuning translation behavior for different content types.
 */
@Serializable
data class TranslationContext(
    /** Type of content being translated */
    val contentType: TranslationContentType = TranslationContentType.GENERAL,
    /** Desired tone of the translation */
    val toneType: TranslationToneType = TranslationToneType.NEUTRAL,
    /** Whether to preserve the original writing style */
    val preserveStyle: Boolean = true,
    /** Whether to preserve formatting (paragraphs, etc.) */
    val preserveFormatting: Boolean = true
)

/**
 * Content type for context-aware translation.
 */
@Serializable
enum class TranslationContentType {
    /** General purpose text */
    GENERAL,
    /** Literary/fiction content - preserve style, metaphors, flow */
    LITERARY,
    /** Technical documentation - precise terminology */
    TECHNICAL,
    /** Conversational/dialogue - natural spoken language */
    CONVERSATION,
    /** Poetry - preserve rhythm, rhyme, poetic devices */
    POETRY,
    /** Academic writing - formal language, precise terms */
    ACADEMIC,
    /** Business/professional content */
    BUSINESS,
    /** Creative writing */
    CREATIVE
}

/**
 * Tone type for context-aware translation.
 */
@Serializable
enum class TranslationToneType {
    /** Neutral, balanced tone */
    NEUTRAL,
    /** Formal, professional tone */
    FORMAL,
    /** Casual, everyday language */
    CASUAL,
    /** Professional business tone */
    PROFESSIONAL,
    /** Humorous, light tone */
    HUMOROUS,
    /** Friendly, warm tone */
    FRIENDLY,
    /** Informal, relaxed tone */
    INFORMAL
}

/**
 * Configuration for translation plugins loaded from manifest metadata.
 * Used by TranslationPluginLoader to create plugin instances.
 */
@Serializable
data class TranslationPluginConfig(
    /** API endpoint URL */
    val apiUrl: String,
    /** API type (REST_JSON, REST_FORM, GRAPHQL) */
    val apiType: TranslationApiType = TranslationApiType.REST_JSON,
    /** Whether API key is required */
    val requiresApiKey: Boolean = false,
    /** Header name for API key (e.g., "Authorization") */
    val apiKeyHeader: String = "Authorization",
    /** Prefix for API key value (e.g., "Bearer ") */
    val apiKeyPrefix: String = "Bearer ",
    /** Maximum characters per request */
    val maxCharsPerRequest: Int = 4000,
    /** Rate limit delay in milliseconds */
    val rateLimitDelayMs: Long = 1000,
    /** Whether engine supports AI features */
    val supportsAI: Boolean = false,
    /** Whether engine supports context-aware translation */
    val supportsContextAware: Boolean = false,
    /** Whether engine supports style preservation */
    val supportsStylePreservation: Boolean = false,
    /** Whether engine is offline/local */
    val isOffline: Boolean = false,
    /** Supported language codes (comma-separated) */
    val supportedLanguages: List<String> = emptyList(),
    /** Request body template (JSON with placeholders) */
    val requestTemplate: String? = null,
    /** Response path to translated text (JSON path) */
    val responsePath: String? = null,
    /** Model name for AI engines */
    val model: String? = null,
    /** Temperature for AI engines (0.0-1.0) */
    val temperature: Float = 0.3f,
    /** Maximum tokens for AI response */
    val maxTokens: Int = 4000
)

/**
 * API type for translation plugins.
 */
@Serializable
enum class TranslationApiType {
    /** REST API with JSON body */
    REST_JSON,
    /** REST API with form data */
    REST_FORM,
    /** GraphQL API */
    GRAPHQL,
    /** OpenAI-compatible chat API */
    OPENAI_COMPATIBLE,
    /** Custom implementation (use plugin code) */
    CUSTOM
}

package ireader.domain.usecases.translate

import ireader.core.http.HttpClients
import ireader.domain.data.engines.ContentType
import ireader.domain.data.engines.ToneType
import ireader.domain.data.engines.TranslateEngine
import ireader.domain.data.engines.TranslationContext
import ireader.domain.plugins.PluginManager
import ireader.domain.plugins.PluginType
import ireader.domain.plugins.TranslationPlugin
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.i18n.UiText
import ireader.i18n.resources.Res
import ireader.i18n.resources.*

/**
 * Sealed class representing translation engine sources
 * Requirements: 4.1, 4.2
 */
sealed class TranslationEngineSource {
    /**
     * Built-in translation engine
     */
    data class BuiltIn(val engine: TranslateEngine) : TranslationEngineSource()
    
    /**
     * Plugin-based translation engine
     */
    data class Plugin(val plugin: TranslationPlugin) : TranslationEngineSource()
}

class TranslationEnginesManager(
    private val readerPreferences: ReaderPreferences,
    private val httpClients: HttpClients,
    private val pluginManager: PluginManager? = null
) {

    // Built-in translation engines:
    // - Google ML Kit (offline, default)
    // - Gemini API (requires API key, for users who prefer Google's AI)
    // - OpenRouter AI (requires API key, access to multiple AI models)
    // - NVIDIA NIM (requires API key, access to NVIDIA-optimized AI models)
    // Other engines (like Ollama) are available as plugins from the Feature Store
    private val builtInEngines = listOf(
        GoogleTranslateML(),  // id=0, offline, default
        GeminiTranslateEngine(httpClients, readerPreferences),  // id=8, requires API key
        OpenRouterTranslateEngine(httpClients, readerPreferences),  // id=9, requires API key
        NvidiaTranslateEngine(httpClients, readerPreferences)  // id=10, requires API key
    )
    
    // Cache for translation results to improve performance
    // Requirements: 4.4
    private val translationCache = mutableMapOf<String, String>()

    fun get(): TranslateEngine {
        val engineId = readerPreferences.translatorEngine().get()
        
        // First check built-in engines
        val builtIn = builtInEngines.find { it.id == engineId }
        if (builtIn != null) {
            return builtIn
        }
        
        // Check if it's a plugin engine (by hash ID)
        val plugins = getTranslationPlugins()
        val plugin = plugins.find { it.manifest.id.hashCode().toLong() == engineId }
        if (plugin != null) {
            // Return a wrapper that adapts the plugin to TranslateEngine interface
            return PluginTranslateEngineAdapter(plugin, this)
        }
        
        // Fall back to first built-in engine
        return builtInEngines.first()
    }
    
    /**
     * Get the engine ID for the currently selected engine
     * This handles both built-in and plugin engines
     */
    fun getSelectedEngineId(): Long {
        val engineId = readerPreferences.translatorEngine().get()
        
        // Check if it's a built-in engine
        if (builtInEngines.any { it.id == engineId }) {
            return engineId
        }
        
        // Check if it's a plugin engine
        val plugins = getTranslationPlugins()
        val plugin = plugins.find { it.manifest.id.hashCode().toLong() == engineId }
        if (plugin != null) {
            return engineId
        }
        
        // Fall back to first built-in engine
        return builtInEngines.first().id
    }
    
    /**
     * Get all available translation engines combining built-in and plugin engines
     * Requirements: 4.1, 4.2
     */
    fun getAvailableEngines(): List<TranslationEngineSource> {
        val builtIn = builtInEngines.map { TranslationEngineSource.BuiltIn(it) }
        val plugins = getTranslationPlugins().map { TranslationEngineSource.Plugin(it) }
        return builtIn + plugins
    }
    
    /**
     * Get translation plugins from plugin manager
     * Requirements: 4.1
     */
    private fun getTranslationPlugins(): List<TranslationPlugin> {
        return pluginManager?.getEnabledPlugins()
            ?.filter { it.manifest.type == PluginType.TRANSLATION }
            ?.filterIsInstance<TranslationPlugin>()
            ?: emptyList()
    }
    
    /**
     * Get a specific translation engine source by ID
     * For built-in engines, use the engine ID
     * For plugin engines, use the plugin manifest ID
     * Requirements: 4.2
     */
    fun getEngineById(id: String): TranslationEngineSource? {
        // Try to find built-in engine by ID
        val engineId = id.toLongOrNull()
        if (engineId != null) {
            val builtIn = builtInEngines.find { it.id == engineId }
            if (builtIn != null) {
                return TranslationEngineSource.BuiltIn(builtIn)
            }
        }
        
        // Try to find plugin by manifest ID
        val plugin = getTranslationPlugins().find { it.manifest.id == id }
        return plugin?.let { TranslationEngineSource.Plugin(it) }
    }
    
    /**
     * Get the currently selected translation engine (built-in or plugin)
     * Requirements: 4.2
     */
    fun getSelectedEngine(): TranslationEngineSource {
        // Check if a plugin is selected
        val pluginId = readerPreferences.translatorPluginId().get()
        if (pluginId.isNotEmpty()) {
            val plugin = getTranslationPlugins().find { it.manifest.id == pluginId }
            if (plugin != null) {
                return TranslationEngineSource.Plugin(plugin)
            }
        }
        
        // Fall back to built-in engine
        val engineId = readerPreferences.translatorEngine().get()
        val engine = builtInEngines.find { it.id == engineId } ?: builtInEngines.first()
        return TranslationEngineSource.BuiltIn(engine)
    }
    
    /**
     * Set the selected translation engine
     * Requirements: 4.2
     */
    fun setSelectedEngine(engine: TranslationEngineSource) {
        when (engine) {
            is TranslationEngineSource.BuiltIn -> {
                readerPreferences.translatorEngine().set(engine.engine.id)
                readerPreferences.translatorPluginId().set("")
            }
            is TranslationEngineSource.Plugin -> {
                readerPreferences.translatorPluginId().set(engine.plugin.manifest.id)
            }
        }
    }
    
    /**
     * Translate text using specified engine (built-in or plugin)
     * Requirements: 4.2, 4.3, 4.4, 4.5
     */
    suspend fun translate(
        text: String,
        from: String,
        to: String,
        engine: TranslationEngineSource
    ): Result<String> {
        // Check cache first
        val cacheKey = "$text|$from|$to|${getEngineKey(engine)}"
        translationCache[cacheKey]?.let { cached ->
            return Result.success(cached)
        }
        
        return try {
            when (engine) {
                is TranslationEngineSource.BuiltIn -> {
                    translateWithBuiltIn(text, from, to, engine.engine)
                }
                is TranslationEngineSource.Plugin -> {
                    translateWithPlugin(text, from, to, engine.plugin)
                }
            }
        } catch (e: Exception) {
            // Handle plugin errors with fallback to built-in engines
            // Requirements: 4.4
            handleTranslationError(text, from, to, e)
        }
    }
    
    /**
     * Translate batch of texts using specified engine
     * Requirements: 4.3, 4.5
     */
    suspend fun translateBatch(
        texts: List<String>,
        from: String,
        to: String,
        engine: TranslationEngineSource
    ): Result<List<String>> {
        return try {
            when (engine) {
                is TranslationEngineSource.BuiltIn -> {
                    translateBatchWithBuiltIn(texts, from, to, engine.engine)
                }
                is TranslationEngineSource.Plugin -> {
                    translateBatchWithPlugin(texts, from, to, engine.plugin)
                }
            }
        } catch (e: Exception) {
            // Handle plugin errors with fallback to built-in engines
            // Requirements: 4.4
            handleBatchTranslationError(texts, from, to, e)
        }
    }
    
    /**
     * Validate if engine supports the requested language pair
     * Requirements: 4.5
     */
    fun validateLanguagePair(
        from: String,
        to: String,
        engine: TranslationEngineSource
    ): Boolean {
        return when (engine) {
            is TranslationEngineSource.BuiltIn -> {
                // Built-in engines support all language pairs in their list
                val supportedLangs = engine.engine.supportedLanguages.map { it.first }
                supportedLangs.contains(from) && supportedLangs.contains(to)
            }
            is TranslationEngineSource.Plugin -> {
                // Check plugin's supported language pairs
                val supportedPairs = engine.plugin.getSupportedLanguages()
                supportedPairs.any { it.from == from && it.to == to } ||
                supportedPairs.any { it.from == "*" || it.to == "*" } // Wildcard support
            }
        }
    }
    
    /**
     * Get engine key for caching
     */
    private fun getEngineKey(engine: TranslationEngineSource): String {
        return when (engine) {
            is TranslationEngineSource.BuiltIn -> "builtin_${engine.engine.id}"
            is TranslationEngineSource.Plugin -> "plugin_${engine.plugin.manifest.id}"
        }
    }
    
    /**
     * Translate using built-in engine
     */
    private suspend fun translateWithBuiltIn(
        text: String,
        from: String,
        to: String,
        engine: TranslateEngine
    ): Result<String> {
        var result: String? = null
        var error: UiText? = null
        
        engine.translate(
            texts = listOf(text),
            source = from,
            target = to,
            onSuccess = { translations ->
                result = translations.firstOrNull()
            },
            onError = { err ->
                error = err
            }
        )
        
        return if (result != null) {
            // Cache the result
            val cacheKey = "$text|$from|$to|builtin_${engine.id}"
            translationCache[cacheKey] = result!!
            Result.success(result!!)
        } else {
            Result.failure(Exception(error?.toString() ?: "Translation failed"))
        }
    }
    
    /**
     * Translate using plugin
     * Requirements: 4.2, 4.3
     */
    private suspend fun translateWithPlugin(
        text: String,
        from: String,
        to: String,
        plugin: TranslationPlugin
    ): Result<String> {
        // Validate language pair
        if (!validateLanguagePair(from, to, TranslationEngineSource.Plugin(plugin))) {
            return Result.failure(Exception("Language pair $from -> $to not supported by plugin ${plugin.manifest.name}"))
        }
        
        // Check if plugin requires API key
        if (plugin.requiresApiKey()) {
            // API key should be configured through plugin settings
            // This is handled by the plugin itself
        }
        
        val result = plugin.translate(text, from, to)
        
        // Cache successful result
        if (result.isSuccess) {
            val cacheKey = "$text|$from|$to|plugin_${plugin.manifest.id}"
            translationCache[cacheKey] = result.getOrNull()!!
        }
        
        return result
    }
    
    /**
     * Translate batch using built-in engine
     */
    private suspend fun translateBatchWithBuiltIn(
        texts: List<String>,
        from: String,
        to: String,
        engine: TranslateEngine
    ): Result<List<String>> {
        var result: List<String>? = null
        var error: UiText? = null
        
        engine.translate(
            texts = texts,
            source = from,
            target = to,
            onSuccess = { translations ->
                result = translations
            },
            onError = { err ->
                error = err
            }
        )
        
        return if (result != null) {
            // Cache the results
            texts.zip(result!!).forEach { (original, translated) ->
                val cacheKey = "$original|$from|$to|builtin_${engine.id}"
                translationCache[cacheKey] = translated
            }
            Result.success(result!!)
        } else {
            Result.failure(Exception(error?.toString() ?: "Translation failed"))
        }
    }
    
    /**
     * Translate batch using plugin
     * Requirements: 4.3, 4.5
     */
    private suspend fun translateBatchWithPlugin(
        texts: List<String>,
        from: String,
        to: String,
        plugin: TranslationPlugin
    ): Result<List<String>> {
        // Validate language pair
        if (!validateLanguagePair(from, to, TranslationEngineSource.Plugin(plugin))) {
            return Result.failure(Exception("Language pair $from -> $to not supported by plugin ${plugin.manifest.name}"))
        }
        
        val result = plugin.translateBatch(texts, from, to)
        
        // Cache successful results
        if (result.isSuccess) {
            val translations = result.getOrNull()!!
            texts.zip(translations).forEach { (original, translated) ->
                val cacheKey = "$original|$from|$to|plugin_${plugin.manifest.id}"
                translationCache[cacheKey] = translated
            }
        }
        
        return result
    }
    
    /**
     * Handle translation error with fallback to built-in engines
     * Requirements: 4.4
     */
    private suspend fun handleTranslationError(
        text: String,
        from: String,
        to: String,
        error: Exception
    ): Result<String> {
        println("Translation error: ${error.message}, attempting fallback to built-in engine")
        
        // Try to use the default built-in engine as fallback
        val fallbackEngine = builtInEngines.firstOrNull()
        if (fallbackEngine != null) {
            return try {
                translateWithBuiltIn(text, from, to, fallbackEngine)
            } catch (e: Exception) {
                Result.failure(Exception("Translation failed: ${error.message}. Fallback also failed: ${e.message}"))
            }
        }
        
        return Result.failure(error)
    }
    
    /**
     * Handle batch translation error with fallback to built-in engines
     * Requirements: 4.4
     */
    private suspend fun handleBatchTranslationError(
        texts: List<String>,
        from: String,
        to: String,
        error: Exception
    ): Result<List<String>> {
        println("Batch translation error: ${error.message}, attempting fallback to built-in engine")
        
        // Try to use the default built-in engine as fallback
        val fallbackEngine = builtInEngines.firstOrNull()
        if (fallbackEngine != null) {
            return try {
                translateBatchWithBuiltIn(texts, from, to, fallbackEngine)
            } catch (e: Exception) {
                Result.failure(Exception("Batch translation failed: ${error.message}. Fallback also failed: ${e.message}"))
            }
        }
        
        return Result.failure(error)
    }
    
    /**
     * Generate content using the current translation engine's AI capabilities
     * This is used for tasks like generating image prompts from text
     * 
     * @param systemPrompt The system instruction for the AI
     * @param userPrompt The user's request/input
     * @param temperature Creativity level (0.0-1.0, default 0.7)
     * @param maxTokens Maximum tokens to generate (default 500)
     * @return Result with generated text or error
     */
    suspend fun generateContent(
        systemPrompt: String,
        userPrompt: String,
        temperature: Float = 0.7f,
        maxTokens: Int = 500
    ): Result<String> {
        val engine = get()
        
        // Check if engine supports AI content generation
        if (!engine.supportsAI) {
            return Result.failure(Exception("Current translation engine (${engine.engineName}) does not support AI content generation. Please select an AI-powered engine like Gemini, OpenAI, or DeepSeek."))
        }
        
        // Validate API key if required
        if (engine.requiresApiKey) {
            val apiKeyError = validateApiKey(engine)
            if (apiKeyError != null) {
                return Result.failure(Exception("API key not configured for ${engine.engineName}. Please set it in Settings > Translation."))
            }
        }
        
        return engine.generateContent(systemPrompt, userPrompt, temperature, maxTokens)
    }
    
    /**
     * Get the current translation engine (for external use)
     */
    fun getCurrentEngine(): TranslateEngine {
        return get()
    }
    
    /**
     * Get API key for the current engine
     */
    fun getApiKeyForCurrentEngine(): String {
        val engine = get()
        return when (engine.id) {
            2L -> readerPreferences.openAIApiKey().get()
            3L -> readerPreferences.deepSeekApiKey().get()
            8L -> readerPreferences.geminiApiKey().get()
            9L -> readerPreferences.openRouterApiKey().get()
            10L -> readerPreferences.nvidiaApiKey().get()
            else -> ""
        }
    }
    
    /**
     * Clear translation cache
     */
    fun clearCache() {
        translationCache.clear()
    }
    
    /**
     * Refresh available translation engines
     * This reloads the list of available engines including plugins
     */
    suspend fun refreshEngines() {
        // Clear the translation cache when refreshing engines
        clearCache()
        
        // Refresh plugin manager to discover new plugins
        pluginManager?.refreshPlugins()
    }
    
    /**
     * Translate with context-aware settings for AI-powered engines
     */
    suspend fun translateWithContext(
        texts: List<String>,
        source: String,
        target: String,
        contentType: ContentType = ContentType.GENERAL,
        toneType: ToneType = ToneType.NEUTRAL,
        preserveStyle: Boolean = true,
        onProgress: (Int) -> Unit = {},
        onSuccess: (List<String>) -> Unit,
        onError: (UiText) -> Unit
    ) {
        // Check if texts is empty or null
        if (texts.isNullOrEmpty()) {
            println("Translation error: No text to translate")
            onError(TranslationError.NoTextToTranslate.toUiText())
            return
        }
        
        try {
            // Get the active translation engine
            val engine = get()
            
            // Log which engine is being used
            println("Using translation engine: ${engine.engineName} (ID: ${engine.id})")
            
            // Validate API keys if required - use TranslationError for better messages
            if (engine.requiresApiKey) {
                val apiKeyError = validateApiKey(engine)
                if (apiKeyError != null) {
                    println("Translation error: API key not configured for ${engine.engineName}")
                    onError(apiKeyError.toUiText())
                    return
                }
            }
            
            val context = TranslationContext(
                contentType = contentType,
                toneType = toneType,
                preserveStyle = preserveStyle
            )
            
            try {
                if (engine.supportsContextAwareTranslation) {
                    engine.translateWithContext(texts, source, target, context, onProgress, onSuccess, onError)
                } else {
                    engine.translate(texts, source, target, onProgress, onSuccess, onError)
                }
            } catch (e: Exception) {
                // Log the error for debugging
                println("Translation error: ${e.message}")
                e.printStackTrace()
                
                // Try fallback to another engine if available
                // Requirements: 4.4
                val fallbackEngine = builtInEngines.firstOrNull { it.id != engine.id }
                if (fallbackEngine != null) {
                    println("Attempting fallback to ${fallbackEngine.engineName}")
                    try {
                        fallbackEngine.translate(texts, source, target, onProgress, onSuccess, onError)
                        return
                    } catch (fallbackError: Exception) {
                        println("Fallback also failed: ${fallbackError.message}")
                    }
                }
                
                // Use TranslationError for user-friendly error messages
                val translationError = TranslationError.fromException(
                    exception = e,
                    engineName = engine.engineName,
                    sourceLanguage = source,
                    targetLanguage = target
                )
                onError(translationError.toUiText())
            }
        } catch (e: Exception) {
            // Log the error for debugging
            println("Translation error: ${e}")
            e.printStackTrace()
            
            // Use TranslationError for consistent error handling
            val translationError = TranslationError.fromException(
                exception = e,
                engineName = get().engineName
            )
            onError(translationError.toUiText())
        }
    }
    
    /**
     * Validate API key for the given engine
     * @return TranslationError if API key is not configured, null if valid
     */
    private fun validateApiKey(engine: TranslateEngine): TranslationError? {
        return when (engine.id) {
            2L -> { // OpenAI
                val apiKey = readerPreferences.openAIApiKey().get()
                if (apiKey.isBlank()) TranslationError.ApiKeyNotSet("OpenAI") else null
            }
            3L -> { // DeepSeek
                val apiKey = readerPreferences.deepSeekApiKey().get()
                if (apiKey.isBlank()) TranslationError.ApiKeyNotSet("DeepSeek") else null
            }
            8L -> { // Gemini
                val apiKey = readerPreferences.geminiApiKey().get()
                if (apiKey.isBlank()) TranslationError.ApiKeyNotSet("Google Gemini") else null
            }
            9L -> { // OpenRouter
                val apiKey = readerPreferences.openRouterApiKey().get()
                if (apiKey.isBlank()) TranslationError.ApiKeyNotSet("OpenRouter AI") else null
            }
            10L -> { // NVIDIA NIM
                val apiKey = readerPreferences.nvidiaApiKey().get()
                if (apiKey.isBlank()) TranslationError.ApiKeyNotSet("NVIDIA NIM") else null
            }
            else -> null
        }
    }
}
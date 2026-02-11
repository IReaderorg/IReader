package ireader.domain.usecases.translate

import ireader.core.log.Log
import ireader.domain.data.engines.TranslateEngine
import ireader.domain.plugins.TranslationPlugin

/**
 * Utility functions for working with translation plugins
 * Requirements: 4.1, 4.2, 4.3, 4.4, 4.5
 */

/**
 * Get display name for a translation engine source
 */
fun TranslationEngineSource.getDisplayName(): String {
    return when (this) {
        is TranslationEngineSource.BuiltIn -> engine.engineName
        is TranslationEngineSource.Plugin -> plugin.manifest.name
    }
}

/**
 * Get unique identifier for a translation engine source
 */
fun TranslationEngineSource.getId(): String {
    return when (this) {
        is TranslationEngineSource.BuiltIn -> engine.id.toString()
        is TranslationEngineSource.Plugin -> plugin.manifest.id
    }
}

/**
 * Check if engine requires API key
 */
fun TranslationEngineSource.requiresApiKey(): Boolean {
    return when (this) {
        is TranslationEngineSource.BuiltIn -> engine.requiresApiKey
        is TranslationEngineSource.Plugin -> plugin.requiresApiKey()
    }
}

/**
 * Check if engine supports AI features
 */
fun TranslationEngineSource.supportsAI(): Boolean {
    return when (this) {
        is TranslationEngineSource.BuiltIn -> engine.supportsAI
        is TranslationEngineSource.Plugin -> false // Plugins don't expose this yet
    }
}

/**
 * Get supported languages for an engine
 */
fun TranslationEngineSource.getSupportedLanguages(): List<Pair<String, String>> {
    return when (this) {
        is TranslationEngineSource.BuiltIn -> engine.supportedLanguages
        is TranslationEngineSource.Plugin -> {
            // Convert plugin language pairs to display format
            plugin.getSupportedLanguages()
                .map { it.from to it.to }
                .distinctBy { it.first }
        }
    }
}

/**
 * Adapter to wrap TranslationPlugin as TranslateEngine for backward compatibility
 * This allows plugins to work with existing code that expects TranslateEngine
 */
class PluginTranslateEngineAdapter(
    private val plugin: TranslationPlugin,
    private val manager: TranslationEnginesManager
) : TranslateEngine() {
    
    override val id: Long = plugin.manifest.id.hashCode().toLong()
    override val engineName: String = plugin.manifest.name
    override val requiresApiKey: Boolean = plugin.requiresApiKey()
    override val supportsAI: Boolean = plugin.supportsAI
    override val supportsContextAwareTranslation: Boolean = plugin.supportsContextAwareTranslation
    override val supportsStylePreservation: Boolean = plugin.supportsStylePreservation
    override val maxCharsPerRequest: Int = plugin.maxCharsPerRequest
    override val rateLimitDelayMs: Long = plugin.rateLimitDelayMs
    override val isOffline: Boolean = plugin.isOffline
    
    override val supportedLanguages: List<Pair<String, String>> = plugin.getAvailableLanguages()
    
    override suspend fun translate(
        texts: List<String>,
        source: String,
        target: String,
        onProgress: (Int) -> Unit,
        onSuccess: (List<String>) -> Unit,
        onError: (ireader.i18n.UiText) -> Unit
    ) {
        val TAG = "PluginTranslateEngine"
        
        Log.debug { "$TAG: translate() called for ${plugin.manifest.name}" }
        Log.debug { "$TAG: Source: $source, Target: $target" }
        Log.debug { "$TAG: Texts count: ${texts.size}, first text preview: ${texts.firstOrNull()?.take(100)}..." }
        Log.debug { "$TAG: Plugin requiresApiKey: ${plugin.requiresApiKey()}, hasApiKey: ${plugin.getApiKey()?.isNotBlank() == true}" }
        
        try {
            val result = plugin.translateBatch(texts, source, target)
            
            Log.debug { "$TAG: translateBatch() returned: isSuccess=${result.isSuccess}" }
            
            result.fold(
                onSuccess = { translations ->
                    Log.debug { "$TAG: Translations received: ${translations.size}" }
                    
                    // Log each translation comparison
                    texts.indices.forEach { i ->
                        val original = texts.getOrNull(i) ?: ""
                        val translated = translations.getOrNull(i) ?: ""
                        val isUnchanged = original.trim() == translated.trim()
                        Log.debug { "$TAG: [$i] Unchanged=$isUnchanged, Original: ${original.take(50)}... -> Translated: ${translated.take(50)}..." }
                    }
                    
                    // Check if all translations are identical to originals (potential silent failure)
                    // Only check if source and target are different languages
                    if (source != target && source != "auto") {
                        val allUnchanged = texts.size == translations.size &&
                            texts.indices.all { i ->
                                texts[i].trim() == translations[i].trim()
                            }
                        
                        Log.debug { "$TAG: All unchanged check: $allUnchanged" }
                        
                        if (allUnchanged && texts.isNotEmpty()) {
                            // Translation returned same text - this may indicate a silent failure
                            Log.error { "$TAG: DETECTED SILENT FAILURE - All translations identical to originals!" }
                            Log.error { "$TAG: This usually means the plugin caught an error but returned original text instead of propagating it" }
                            Log.error { "$TAG: Check if the plugin has proper error handling in translateBatch()" }
                            onError(TranslationError.SameAsOriginal(engineName).toUiText())
                            return@fold
                        }
                    }
                    
                    Log.info { "$TAG: Translation successful for ${plugin.manifest.name}" }
                    onProgress(100)
                    onSuccess(translations)
                },
                onFailure = { error ->
                    Log.error { "$TAG: Translation failed with error: ${error.message}" }
                    Log.error { "$TAG: Error type: ${error::class.simpleName}" }
                    error.printStackTrace()
                    
                    val translationError = TranslationError.fromException(
                        exception = error as? Exception ?: Exception(error.message),
                        engineName = engineName,
                        sourceLanguage = source,
                        targetLanguage = target
                    )
                    onError(translationError.toUiText())
                }
            )
        } catch (e: Exception) {
            Log.error { "$TAG: Exception during translation: ${e.message}" }
            Log.error { "$TAG: Exception type: ${e::class.simpleName}" }
            e.printStackTrace()
            
            val translationError = TranslationError.fromException(
                exception = e,
                engineName = engineName,
                sourceLanguage = source,
                targetLanguage = target
            )
            onError(translationError.toUiText())
        }
    }
}

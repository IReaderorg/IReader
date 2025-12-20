package ireader.domain.usecases.translate

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
        try {
            val result = plugin.translateBatch(texts, source, target)
            
            result.fold(
                onSuccess = { translations ->
                    onProgress(100)
                    onSuccess(translations)
                },
                onFailure = { error ->
                    onError(ireader.i18n.UiText.DynamicString(error.message ?: "Translation failed"))
                }
            )
        } catch (e: Exception) {
            onError(ireader.i18n.UiText.DynamicString(e.message ?: "Translation failed"))
        }
    }
}

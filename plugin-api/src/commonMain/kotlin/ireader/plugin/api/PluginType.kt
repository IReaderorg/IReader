package ireader.plugin.api

import kotlinx.serialization.Serializable

/**
 * Types of plugins supported by IReader.
 */
@Serializable
enum class PluginType {
    /**
     * Theme plugins provide custom visual themes.
     * Implement [ThemePlugin] interface.
     */
    THEME,
    
    /**
     * Translation plugins provide text translation services.
     * Implement [TranslationPlugin] interface.
     */
    TRANSLATION,
    
    /**
     * TTS plugins provide text-to-speech engines.
     * Implement [TTSPlugin] interface.
     */
    TTS,
    
    /**
     * Feature plugins add custom functionality.
     * Implement [FeaturePlugin] interface.
     */
    FEATURE
}

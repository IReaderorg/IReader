package ireader.domain.plugins

import kotlinx.serialization.Serializable

/**
 * Types of plugins supported by IReader
 * Requirements: 1.1, 3.1, 4.1, 5.1, 6.1
 */
@Serializable
enum class PluginType {
    /**
     * Theme plugins provide custom visual themes
     */
    THEME,
    
    /**
     * Translation plugins provide text translation services
     */
    TRANSLATION,
    
    /**
     * TTS plugins provide text-to-speech engines
     */
    TTS,
    
    /**
     * Feature plugins add custom functionality
     */
    FEATURE
}

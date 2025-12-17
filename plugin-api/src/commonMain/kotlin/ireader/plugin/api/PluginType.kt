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
    FEATURE,
    
    /**
     * AI plugins provide intelligent text processing.
     * Implement [AIPlugin] interface.
     * Supports local (llama.cpp, ONNX) and cloud (OpenAI, Claude, Gemini) providers.
     */
    AI,
    
    /**
     * Catalog plugins provide content sources (novels, manga, etc.).
     * Implement [CatalogPlugin] interface.
     * Supports LNReader catalogs, UserSource, and custom sources.
     */
    CATALOG,
    
    /**
     * Image processing plugins for upscaling, enhancement, etc.
     * Implement [ImageProcessingPlugin] interface.
     * Can connect to local servers (Real-ESRGAN, etc.) or cloud services.
     */
    IMAGE_PROCESSING,
    
    /**
     * Sync plugins for data synchronization.
     * Implement [SyncPlugin] interface.
     * Supports local server sync and cloud sync.
     */
    SYNC,
    
    /**
     * Community screen plugins add custom UI screens.
     * Implement [CommunityScreenPlugin] interface.
     * Forums, recommendations, social features, etc.
     */
    COMMUNITY_SCREEN,
    
    /**
     * Glossary plugins for term/dictionary management.
     * Implement [GlossaryPlugin] interface.
     * Character databases, translation glossaries, etc.
     */
    GLOSSARY,
    
    /**
     * Gradio TTS plugins for Gradio-based TTS services.
     * Implement [GradioTTSPlugin] interface.
     * Coqui TTS, XTTS, and other Gradio endpoints.
     */
    GRADIO_TTS,
    
    /**
     * JavaScript engine plugins provide JS execution capabilities.
     * Implement [JSEnginePlugin] interface.
     * GraalVM (Desktop), J2V8 (Android), QuickJS, etc.
     * 
     * These plugins enable running LNReader-compatible source plugins
     * without bundling the JS engine with the base app.
     */
    JS_ENGINE
}

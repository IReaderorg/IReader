package ireader.domain.catalogs.service

import ireader.domain.models.entities.JSPluginCatalog

/**
 * Interface for catalog loaders that support asynchronous JS plugin loading.
 * Implemented by platform-specific loaders (Desktop, Android) to enable
 * background loading of plugins after initial stub load.
 */
interface AsyncPluginLoader {
    /**
     * Load .iplugin files (engine plugins like J2V8, Piper TTS, etc.) before JS plugins.
     * This ensures the JS engine is available when JS plugins are loaded.
     * 
     * This should be called before loadJSPluginsAsync() to ensure engine plugins
     * are loaded first.
     */
    suspend fun loadEnginePlugins()
    
    /**
     * Load JS plugins asynchronously in the background.
     * @param onPluginLoaded Callback invoked when each plugin is loaded
     */
    suspend fun loadJSPluginsAsync(onPluginLoaded: (JSPluginCatalog) -> Unit)
    
    /**
     * Check if JS engine is missing (plugins installed but can't be loaded).
     * Returns true if there are JS plugin files but no JS engine to run them.
     */
    fun isJSEngineMissing(): Boolean = false
    
    /**
     * Get the number of JS plugins pending due to missing engine.
     */
    fun getPendingJSPluginsCount(): Int = 0
}

package ireader.domain.plugins

/**
 * Base interface for all IReader plugins
 * Requirements: 1.1, 1.2, 1.3, 1.4, 1.5
 */
interface Plugin {
    /**
     * Plugin metadata and configuration
     */
    val manifest: PluginManifest
    
    /**
     * Initialize the plugin with the provided context
     * Called when the plugin is loaded and enabled
     */
    fun initialize(context: PluginContext)
    
    /**
     * Cleanup plugin resources
     * Called when the plugin is disabled or unloaded
     */
    fun cleanup()
}

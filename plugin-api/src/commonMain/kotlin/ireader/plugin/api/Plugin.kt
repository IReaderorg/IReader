package ireader.plugin.api

/**
 * Base interface for all IReader plugins.
 * 
 * Plugins extend IReader's functionality by providing:
 * - Custom themes (ThemePlugin)
 * - Text-to-speech engines (TTSPlugin)
 * - Translation services (TranslationPlugin)
 * - Custom features (FeaturePlugin)
 * 
 * Example implementation:
 * ```kotlin
 * class MyThemePlugin : ThemePlugin {
 *     override val manifest = PluginManifest(
 *         id = "com.example.mytheme",
 *         name = "My Theme",
 *         version = "1.0.0",
 *         versionCode = 1,
 *         description = "A beautiful custom theme",
 *         author = PluginAuthor("Developer Name"),
 *         type = PluginType.THEME,
 *         permissions = emptyList(),
 *         minIReaderVersion = "1.0.0",
 *         platforms = listOf(Platform.ANDROID, Platform.IOS, Platform.DESKTOP)
 *     )
 *     
 *     override fun initialize(context: PluginContext) {
 *         // Initialize plugin resources
 *     }
 *     
 *     override fun cleanup() {
 *         // Release resources
 *     }
 *     
 *     // ... implement theme methods
 * }
 * ```
 */
interface Plugin {
    /**
     * Plugin metadata and configuration.
     * Must be provided by all plugin implementations.
     */
    val manifest: PluginManifest
    
    /**
     * Initialize the plugin with the provided context.
     * Called when the plugin is loaded and enabled.
     * 
     * @param context Sandboxed context for accessing app resources
     */
    fun initialize(context: PluginContext)
    
    /**
     * Cleanup plugin resources.
     * Called when the plugin is disabled or unloaded.
     * Release any held resources here.
     */
    fun cleanup()
}

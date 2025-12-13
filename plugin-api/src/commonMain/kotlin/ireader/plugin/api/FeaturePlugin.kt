package ireader.plugin.api

import kotlinx.serialization.Serializable

/**
 * Plugin interface for custom features.
 * Feature plugins add custom functionality to IReader.
 * 
 * Example:
 * ```kotlin
 * class DictionaryPlugin : FeaturePlugin {
 *     override val manifest = PluginManifest(
 *         id = "com.example.dictionary",
 *         name = "Dictionary Lookup",
 *         type = PluginType.FEATURE,
 *         permissions = listOf(PluginPermission.READER_CONTEXT, PluginPermission.NETWORK),
 *         // ... other manifest fields
 *     )
 *     
 *     override fun getMenuItems(): List<PluginMenuItem> {
 *         return listOf(
 *             PluginMenuItem(
 *                 id = "lookup",
 *                 label = "Look up word",
 *                 icon = "dictionary"
 *             )
 *         )
 *     }
 *     
 *     override fun onReaderContext(context: ReaderContext): PluginAction? {
 *         context.selectedText?.let { word ->
 *             return PluginAction.Navigate("dictionary/$word")
 *         }
 *         return null
 *     }
 *     
 *     // ... other implementations
 * }
 * ```
 */
interface FeaturePlugin : Plugin {
    /**
     * Get menu items to add to the reader interface.
     * These appear in the reader's overflow menu or toolbar.
     * 
     * @return List of plugin menu items
     */
    fun getMenuItems(): List<PluginMenuItem>
    
    /**
     * Get custom screens provided by this plugin.
     * 
     * @return List of plugin screens
     */
    fun getScreens(): List<PluginScreen>
    
    /**
     * Handle reader context events.
     * Called when user interacts with reader (e.g., selects text).
     * 
     * @param context Current reading context
     * @return Plugin action to execute, or null
     */
    fun onReaderContext(context: ReaderContext): PluginAction?
    
    /**
     * Get preferences screen for plugin configuration (optional).
     * 
     * @return Plugin screen for preferences, or null
     */
    fun getPreferencesScreen(): PluginScreen? = null
}

/**
 * Menu item for plugin features.
 */
@Serializable
data class PluginMenuItem(
    /** Unique identifier for the menu item */
    val id: String,
    /** Display label */
    val label: String,
    /** Icon name (optional) */
    val icon: String? = null,
    /** Sort order (lower = higher priority) */
    val order: Int = 0
)

/**
 * Screen provided by a plugin.
 */
data class PluginScreen(
    /** Navigation route for the screen */
    val route: String,
    /** Screen title */
    val title: String,
    /** Screen content (Composable function reference) */
    val content: Any
)

/**
 * Reading context provided to feature plugins.
 * Contains information about the current reading state.
 */
@Serializable
data class ReaderContext(
    /** Current book ID */
    val bookId: Long,
    /** Current chapter ID */
    val chapterId: Long,
    /** Currently selected text (if any) */
    val selectedText: String? = null,
    /** Current reading position (paragraph index) */
    val currentPosition: Int
)

/**
 * Action that a plugin can execute.
 */
sealed class PluginAction {
    /**
     * Navigate to a plugin screen.
     */
    data class Navigate(val route: String) : PluginAction()
    
    /**
     * Show a notification.
     */
    data class ShowNotification(val title: String, val message: String) : PluginAction()
    
    /**
     * Execute custom action.
     */
    data class Custom(val actionId: String, val data: Map<String, Any> = emptyMap()) : PluginAction()
}

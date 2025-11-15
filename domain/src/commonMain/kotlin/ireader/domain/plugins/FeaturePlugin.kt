package ireader.domain.plugins

import kotlinx.serialization.Serializable

/**
 * Plugin interface for custom features
 * Requirements: 6.1, 6.2, 6.3, 6.4, 6.5
 */
interface FeaturePlugin : Plugin {
    /**
     * Get menu items to add to the reader interface
     * @return List of plugin menu items
     */
    fun getMenuItems(): List<PluginMenuItem>
    
    /**
     * Get custom screens provided by this plugin
     * @return List of plugin screens
     */
    fun getScreens(): List<PluginScreen>
    
    /**
     * Handle reader context events
     * @param context Current reading context
     * @return Plugin action to execute, or null
     */
    fun onReaderContext(context: ReaderContext): PluginAction?
    
    /**
     * Get preferences screen for plugin configuration (optional)
     * @return Plugin screen for preferences, or null
     */
    fun getPreferencesScreen(): PluginScreen?
}

/**
 * Menu item for plugin features
 */
@Serializable
data class PluginMenuItem(
    val id: String,
    val label: String,
    val icon: String? = null,
    val order: Int = 0
)

/**
 * Screen provided by a plugin
 */
data class PluginScreen(
    val route: String,
    val title: String,
    val content: Any // Composable function reference
)

/**
 * Reading context provided to feature plugins
 */
@Serializable
data class ReaderContext(
    val bookId: Long,
    val chapterId: Long,
    val selectedText: String? = null,
    val currentPosition: Int
)

/**
 * Action that a plugin can execute
 */
sealed class PluginAction {
    /**
     * Navigate to a plugin screen
     */
    data class Navigate(val route: String) : PluginAction()
    
    /**
     * Show a notification
     */
    data class ShowNotification(val title: String, val message: String) : PluginAction()
    
    /**
     * Execute custom action
     */
    data class Custom(val actionId: String, val data: Map<String, Any> = emptyMap()) : PluginAction()
}

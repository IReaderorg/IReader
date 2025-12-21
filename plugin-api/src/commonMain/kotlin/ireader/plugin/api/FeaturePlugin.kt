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
 * The content is a Composable lambda that receives PluginScreenContext.
 */
data class PluginScreen(
    /** Navigation route for the screen */
    val route: String,
    /** Screen title */
    val title: String,
    /** 
     * Screen content - should be a @Composable (PluginScreenContext) -> Unit lambda.
     * The app will invoke this with the appropriate context.
     */
    val content: Any
)

/**
 * Context passed to plugin screens for rendering.
 */
data class PluginScreenContext(
    /** Current book ID (if in reader context) */
    val bookId: Long? = null,
    /** Current chapter ID (if in reader context) */
    val chapterId: Long? = null,
    /** Chapter title */
    val chapterTitle: String? = null,
    /** Book title */
    val bookTitle: String? = null,
    /** Selected text (if any) */
    val selectedText: String? = null,
    /** Chapter content (for AI processing) */
    val chapterContent: String? = null,
    /** Callback to dismiss the screen */
    val onDismiss: () -> Unit = {},
    /** Plugin's data storage */
    val dataStorage: PluginDataStorageApi? = null
)

/**
 * Simple data storage API for plugins to persist data.
 */
interface PluginDataStorageApi {
    suspend fun putString(key: String, value: String)
    suspend fun getString(key: String, defaultValue: String = ""): String
    suspend fun putLong(key: String, value: Long)
    suspend fun getLong(key: String, defaultValue: Long = 0L): Long
    suspend fun putBoolean(key: String, value: Boolean)
    suspend fun getBoolean(key: String, defaultValue: Boolean = false): Boolean
    suspend fun remove(key: String)
}

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
     * Show plugin menu items.
     */
    data class ShowMenu(val menuItemIds: List<String>) : PluginAction()
    
    /**
     * Show a bottom sheet with plugin content.
     * The content lambda receives PluginScreenContext.
     */
    data class ShowBottomSheet(
        val title: String,
        val content: Any // @Composable (PluginScreenContext) -> Unit
    ) : PluginAction()
    
    /**
     * Execute custom action.
     */
    data class Custom(val actionId: String, val data: Map<String, Any> = emptyMap()) : PluginAction()
}

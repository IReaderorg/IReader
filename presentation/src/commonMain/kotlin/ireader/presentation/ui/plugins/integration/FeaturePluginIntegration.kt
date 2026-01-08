package ireader.presentation.ui.plugins.integration

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import ireader.domain.plugins.PluginManager
import ireader.domain.plugins.PluginStatus
import ireader.domain.plugins.PluginType
import ireader.plugin.api.FeaturePlugin
import ireader.plugin.api.Plugin
import ireader.plugin.api.PluginAction
import ireader.plugin.api.PluginMenuItem
import ireader.plugin.api.PluginScreen
import ireader.plugin.api.PluginScreenContext
import ireader.plugin.api.ReaderContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Data class representing an incompatible plugin that needs update
 */
data class IncompatiblePlugin(
    val pluginId: String,
    val pluginName: String,
    val currentVersion: String,
    val errorMessage: String
)

/**
 * Integration layer for Feature Plugins with app navigation and reader
 * Requirements: 6.1, 6.2, 6.3, 6.4, 6.5
 */
class FeaturePluginIntegration(
    private val pluginManager: PluginManager,
    private val pluginDataStorage: PluginDataStorage
) {
    
    // Track incompatible plugins
    private val _incompatiblePlugins = MutableStateFlow<List<IncompatiblePlugin>>(emptyList())
    val incompatiblePlugins: StateFlow<List<IncompatiblePlugin>> = _incompatiblePlugins.asStateFlow()
    
    // Track skipped plugins (user chose not to update)
    private val skippedPluginIds = mutableSetOf<String>()
    
    /**
     * Get menu items from all enabled feature plugins
     * Requirements: 6.1
     * 
     * @return List of plugin menu items sorted by order
     */
    fun getPluginMenuItems(): List<PluginMenuItem> {
        return try {
            val featurePlugins = getEnabledFeaturePlugins()
            val incompatible = mutableListOf<IncompatiblePlugin>()
            
            val menuItems = featurePlugins.flatMap { plugin ->
                try {
                    plugin.getMenuItems()
                } catch (e: Throwable) {
                    // Track incompatible plugin
                    if (!skippedPluginIds.contains(plugin.manifest.id)) {
                        incompatible.add(IncompatiblePlugin(
                            pluginId = plugin.manifest.id,
                            pluginName = plugin.manifest.name,
                            currentVersion = plugin.manifest.version,
                            errorMessage = e.message ?: "Unknown error"
                        ))
                    }
                    emptyList()
                }
            }.sortedBy { it.order }
            
            // Update incompatible plugins state
            if (incompatible.isNotEmpty()) {
                _incompatiblePlugins.value = incompatible
            }
            
            menuItems
        } catch (e: Throwable) {
            emptyList()
        }
    }
    
    /**
     * Get screens from all enabled feature plugins
     * Requirements: 6.2
     * 
     * @return List of plugin screens
     */
    fun getPluginScreens(): List<PluginScreen> {
        return try {
            val featurePlugins = getEnabledFeaturePlugins()
            val incompatible = mutableListOf<IncompatiblePlugin>()
            
            val screens = featurePlugins.flatMap { plugin ->
                try {
                    plugin.getScreens()
                } catch (e: Throwable) {
                    // Track incompatible plugin
                    if (!skippedPluginIds.contains(plugin.manifest.id)) {
                        incompatible.add(IncompatiblePlugin(
                            pluginId = plugin.manifest.id,
                            pluginName = plugin.manifest.name,
                            currentVersion = plugin.manifest.version,
                            errorMessage = e.message ?: "Unknown error"
                        ))
                    }
                    emptyList()
                }
            }
            
            // Update incompatible plugins state
            if (incompatible.isNotEmpty()) {
                val current = _incompatiblePlugins.value.toMutableList()
                incompatible.forEach { newPlugin ->
                    if (current.none { it.pluginId == newPlugin.pluginId }) {
                        current.add(newPlugin)
                    }
                }
                _incompatiblePlugins.value = current
            }
            
            screens
        } catch (e: Throwable) {
            emptyList()
        }
    }
    
    /**
     * Find a plugin screen by route pattern
     */
    fun findScreenByRoute(route: String): PluginScreen? {
        return getPluginScreens().find { screen ->
            // Match route patterns like "notes/{bookId}/{chapterId}" with actual routes
            val pattern = screen.route.replace(Regex("\\{[^}]+\\}"), "[^/]+")
            route.matches(Regex(pattern))
        }
    }
    
    /**
     * Handle reader context events and notify all feature plugins
     * Requirements: 6.3
     * 
     * @param context Current reading context
     * @param scope Coroutine scope for executing actions
     * @param navController Navigation controller for navigation actions
     * @return List of actions from plugins
     */
    fun handleReaderContext(
        context: ReaderContext,
        scope: CoroutineScope,
        navController: NavHostController
    ): List<PluginAction> {
        val actions = mutableListOf<PluginAction>()
        
        try {
            val featurePlugins = getEnabledFeaturePlugins()
            
            featurePlugins.forEach { plugin ->
                try {
                    val action = plugin.onReaderContext(context)
                    action?.let { actions.add(it) }
                } catch (e: Throwable) {
                    // Log error but continue with other plugins
                }
            }
        } catch (e: Throwable) {
            // Don't disrupt main app functionality
        }
        
        return actions
    }
    
    /**
     * Execute a plugin action
     * Requirements: 6.1, 6.2
     * 
     * @param action Plugin action to execute
     * @param navController Navigation controller for navigation actions
     */
    fun executePluginAction(
        action: PluginAction,
        navController: NavHostController
    ) {
        try {
            when (action) {
                is PluginAction.Navigate -> {
                    navController.navigate(action.route)
                }
                is PluginAction.ShowNotification -> {
                    // This would integrate with the app's notification system
                }
                is PluginAction.ShowMenu -> {
                    // Menu items are handled by the UI layer
                }
                is PluginAction.ShowBottomSheet -> {
                    // Bottom sheets are handled by the UI layer
                }
                is PluginAction.Custom -> {
                    // Custom actions would be handled by specific plugin implementations
                }
            }
        } catch (e: Exception) {
            // Log error but don't crash
        }
    }
    
    /**
     * Register plugin screens with the navigation system
     * Requirements: 6.2
     * 
     * @param navGraphBuilder Navigation graph builder
     */
    fun registerPluginScreens(navGraphBuilder: NavGraphBuilder) {
        try {
            val screens = getPluginScreens()
            
            screens.forEach { screen ->
                try {
                    navGraphBuilder.composable(screen.route) {
                        // Cast the content to a Composable function
                        val composableContent = screen.content as? @Composable () -> Unit
                        composableContent?.invoke() ?: run {
                            // Fallback if content is not a valid Composable
                            ErrorScreen("Invalid plugin screen content")
                        }
                    }
                } catch (e: Exception) {
                    // Log error but continue with other screens
                }
            }
        } catch (e: Exception) {
            // Don't disrupt navigation setup
        }
    }
    
    /**
     * Get plugin data storage API for a specific plugin
     * Requirements: 6.5
     * 
     * @param pluginId Plugin identifier
     * @return Plugin-specific data storage
     */
    fun getPluginDataStorage(pluginId: String): PluginDataStore {
        return pluginDataStorage.getDataStore(pluginId)
    }
    
    /**
     * Get all enabled feature plugins
     * 
     * @return List of enabled feature plugins
     */
    private fun getEnabledFeaturePlugins(): List<FeaturePlugin> {
        return pluginManager.getEnabledPlugins()
            .filter { it.manifest.type == ireader.plugin.api.PluginType.FEATURE ||
                     it.manifest.type == ireader.plugin.api.PluginType.AI }
            .filterIsInstance<FeaturePlugin>()
    }
    
    /**
     * Check if any feature plugins are available
     */
    fun hasFeaturePlugins(): Boolean {
        return getEnabledFeaturePlugins().isNotEmpty()
    }
    
    /**
     * Check if there are incompatible plugins that need update
     */
    fun hasIncompatiblePlugins(): Boolean {
        return _incompatiblePlugins.value.isNotEmpty()
    }
    
    /**
     * Skip updating a plugin (user chose not to update)
     */
    fun skipPlugin(pluginId: String) {
        skippedPluginIds.add(pluginId)
        _incompatiblePlugins.value = _incompatiblePlugins.value.filter { it.pluginId != pluginId }
    }
    
    /**
     * Skip all incompatible plugins
     */
    fun skipAllIncompatiblePlugins() {
        _incompatiblePlugins.value.forEach { skippedPluginIds.add(it.pluginId) }
        _incompatiblePlugins.value = emptyList()
    }
    
    /**
     * Clear incompatible plugin after successful update
     */
    fun clearIncompatiblePlugin(pluginId: String) {
        _incompatiblePlugins.value = _incompatiblePlugins.value.filter { it.pluginId != pluginId }
        skippedPluginIds.remove(pluginId)
    }
    
    /**
     * Get the PluginManager for reinstalling plugins
     */
    fun getPluginManager(): PluginManager = pluginManager
}

/**
 * Error screen shown when plugin content fails to load
 */
@Composable
private fun ErrorScreen(message: String) {
    // Simple error display - would be styled properly in production
    Box(
        modifier = Modifier.padding(16.dp)
    ) {
        androidx.compose.material3.Text(text = "Plugin Error: $message")
    }
}

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
import ireader.domain.plugins.FeaturePlugin
import ireader.domain.plugins.PluginAction
import ireader.domain.plugins.PluginManager
import ireader.domain.plugins.PluginMenuItem
import ireader.domain.plugins.PluginScreen
import ireader.domain.plugins.PluginStatus
import ireader.domain.plugins.PluginType
import ireader.domain.plugins.ReaderContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Integration layer for Feature Plugins with app navigation and reader
 * Requirements: 6.1, 6.2, 6.3, 6.4, 6.5
 */
class FeaturePluginIntegration(
    private val pluginManager: PluginManager,
    private val pluginDataStorage: PluginDataStorage
) {
    
    /**
     * Get menu items from all enabled feature plugins
     * Requirements: 6.1
     * 
     * @return List of plugin menu items sorted by order
     */
    fun getPluginMenuItems(): List<PluginMenuItem> {
        return try {
            val featurePlugins = getEnabledFeaturePlugins()
            
            featurePlugins.flatMap { plugin ->
                try {
                    plugin.getMenuItems()
                } catch (e: Exception) {
                    // Log error but don't disrupt functionality
                    emptyList()
                }
            }.sortedBy { it.order }
        } catch (e: Exception) {
            // Return empty list if there's an error
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
            
            featurePlugins.flatMap { plugin ->
                try {
                    plugin.getScreens()
                } catch (e: Exception) {
                    // Log error but don't disrupt functionality
                    emptyList()
                }
            }
        } catch (e: Exception) {
            // Return empty list if there's an error
            emptyList()
        }
    }
    
    /**
     * Handle reader context events and notify all feature plugins
     * Requirements: 6.3
     * 
     * @param context Current reading context
     * @param scope Coroutine scope for executing actions
     * @param navController Navigation controller for navigation actions
     */
    fun handleReaderContext(
        context: ReaderContext,
        scope: CoroutineScope,
        navController: NavHostController
    ) {
        scope.launch {
            try {
                val featurePlugins = getEnabledFeaturePlugins()
                
                featurePlugins.forEach { plugin ->
                    try {
                        val action = plugin.onReaderContext(context)
                        action?.let { executePluginAction(it, navController) }
                    } catch (e: Exception) {
                        // Log error but continue with other plugins
                    }
                }
            } catch (e: Exception) {
                // Don't disrupt main app functionality
            }
        }
    }
    
    /**
     * Execute a plugin action
     * Requirements: 6.1, 6.2
     * 
     * @param action Plugin action to execute
     * @param navController Navigation controller for navigation actions
     */
    private fun executePluginAction(
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
                    // For now, we'll just log it
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
            .filter { it.manifest.type == PluginType.FEATURE }
            .filterIsInstance<FeaturePlugin>()
    }
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

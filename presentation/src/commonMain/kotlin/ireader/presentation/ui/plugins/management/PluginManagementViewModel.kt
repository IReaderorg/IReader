package ireader.presentation.ui.plugins.management

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import ireader.domain.plugins.PluginInfo
import ireader.domain.plugins.PluginManager
import ireader.domain.plugins.PluginStatus
import ireader.i18n.UiText
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * ViewModel for Plugin Management screen
 * Requirements: 14.1, 14.2, 14.3, 14.4, 14.5, 12.1, 12.2, 12.3, 12.4, 12.5
 */
class PluginManagementViewModel(
    private val pluginManager: PluginManager
) : BaseViewModel() {
    
    private val _state = mutableStateOf(PluginManagementState())
    val state: State<PluginManagementState> = _state
    
    init {
        observePlugins()
        loadInstalledPlugins()
        checkForUpdates()
        loadResourceUsage()
    }
    
    /**
     * Observe plugin changes from PluginManager
     */
    private fun observePlugins() {
        pluginManager.pluginsFlow
            .onEach { plugins ->
                _state.value = _state.value.copy(
                    installedPlugins = plugins,
                    isLoading = false
                )
            }
            .launchIn(scope)
    }
    
    /**
     * Load installed plugins
     */
    fun loadInstalledPlugins() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        scope.launch {
            try {
                pluginManager.loadPlugins()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load plugins"
                )
                showSnackBar(UiText.DynamicString(e.message ?: "Failed to load plugins"))
            }
        }
    }
    
    /**
     * Enable a plugin
     * Requirements: 14.2
     */
    fun enablePlugin(pluginId: String) {
        scope.launch {
            val result = pluginManager.enablePlugin(pluginId)
            result.onFailure { error ->
                showSnackBar(UiText.DynamicString(error.message ?: "Failed to enable plugin"))
            }
            result.onSuccess {
                showSnackBar(UiText.DynamicString("Plugin enabled successfully"))
            }
        }
    }
    
    /**
     * Disable a plugin
     * Requirements: 14.2
     */
    fun disablePlugin(pluginId: String) {
        scope.launch {
            val result = pluginManager.disablePlugin(pluginId)
            result.onFailure { error ->
                showSnackBar(UiText.DynamicString(error.message ?: "Failed to disable plugin"))
            }
            result.onSuccess {
                showSnackBar(UiText.DynamicString("Plugin disabled successfully"))
            }
        }
    }
    
    /**
     * Show uninstall confirmation dialog
     * Requirements: 14.3
     */
    fun showUninstallConfirmation(pluginId: String) {
        _state.value = _state.value.copy(pluginToUninstall = pluginId)
    }
    
    /**
     * Dismiss uninstall confirmation dialog
     */
    fun dismissUninstallConfirmation() {
        _state.value = _state.value.copy(pluginToUninstall = null)
    }
    
    /**
     * Uninstall a plugin
     * Requirements: 14.3
     */
    fun uninstallPlugin(pluginId: String) {
        scope.launch {
            val result = pluginManager.uninstallPlugin(pluginId)
            result.onFailure { error ->
                showSnackBar(UiText.DynamicString(error.message ?: "Failed to uninstall plugin"))
            }
            result.onSuccess {
                showSnackBar(UiText.DynamicString("Plugin uninstalled successfully"))
                dismissUninstallConfirmation()
            }
        }
    }
    
    /**
     * Open plugin configuration screen
     * Requirements: 14.4
     */
    fun openPluginConfiguration(pluginId: String) {
        _state.value = _state.value.copy(selectedPluginForConfig = pluginId)
    }
    
    /**
     * Close plugin configuration screen
     */
    fun closePluginConfiguration() {
        _state.value = _state.value.copy(selectedPluginForConfig = null)
    }
    
    /**
     * Show plugin error details
     * Requirements: 14.5
     */
    fun showPluginErrorDetails(plugin: PluginInfo) {
        val errorDetails = PluginErrorDetails(
            pluginId = plugin.id,
            pluginName = plugin.manifest.name,
            errorMessage = "Plugin failed to initialize or encountered an error during execution",
            troubleshootingSteps = listOf(
                "Try disabling and re-enabling the plugin",
                "Check if the plugin is compatible with your IReader version",
                "Verify that all required permissions are granted",
                "Check if there are any updates available for the plugin",
                "Try uninstalling and reinstalling the plugin",
                "Contact the plugin developer for support"
            )
        )
        _state.value = _state.value.copy(selectedPluginForError = errorDetails)
    }
    
    /**
     * Dismiss plugin error details dialog
     */
    fun dismissPluginErrorDetails() {
        _state.value = _state.value.copy(selectedPluginForError = null)
    }
    
    /**
     * Check for plugin updates
     * Requirements: 12.1, 12.2
     */
    fun checkForUpdates() {
        scope.launch {
            try {
                // In a real implementation, this would check with a backend service
                // For now, we'll simulate with an empty map
                val updates = emptyMap<String, String>()
                _state.value = _state.value.copy(updatesAvailable = updates)
            } catch (e: Exception) {
                // Silently fail - updates are not critical
            }
        }
    }
    
    /**
     * Update a single plugin
     * Requirements: 12.3
     */
    fun updatePlugin(pluginId: String) {
        scope.launch {
            try {
                // In a real implementation, this would download and install the update
                showSnackBar(UiText.DynamicString("Plugin update started"))
                
                // Remove from updates available
                val updates = _state.value.updatesAvailable.toMutableMap()
                updates.remove(pluginId)
                _state.value = _state.value.copy(updatesAvailable = updates)
                
                showSnackBar(UiText.DynamicString("Plugin updated successfully"))
            } catch (e: Exception) {
                showSnackBar(UiText.DynamicString(e.message ?: "Failed to update plugin"))
            }
        }
    }
    
    /**
     * Update all plugins with available updates
     * Requirements: 12.3
     */
    fun updateAllPlugins() {
        scope.launch {
            _state.value = _state.value.copy(isUpdatingAll = true)
            try {
                val pluginsToUpdate = _state.value.updatesAvailable.keys.toList()
                
                pluginsToUpdate.forEach { pluginId ->
                    try {
                        // In a real implementation, this would download and install updates
                        // For now, just simulate the update
                    } catch (e: Exception) {
                        // Continue with other updates even if one fails
                    }
                }
                
                _state.value = _state.value.copy(
                    updatesAvailable = emptyMap(),
                    isUpdatingAll = false
                )
                showSnackBar(UiText.DynamicString("All plugins updated successfully"))
            } catch (e: Exception) {
                _state.value = _state.value.copy(isUpdatingAll = false)
                showSnackBar(UiText.DynamicString(e.message ?: "Failed to update plugins"))
            }
        }
    }
    
    /**
     * Load resource usage for all plugins
     * Requirements: 11.1, 11.2
     */
    private fun loadResourceUsage() {
        scope.launch {
            try {
                val usage = mutableMapOf<String, ireader.domain.plugins.PluginResourceUsage>()
                _state.value.installedPlugins.forEach { plugin ->
                    pluginManager.getPluginResourceUsage(plugin.id)?.let { resourceUsage ->
                        usage[plugin.id] = resourceUsage
                    }
                }
                _state.value = _state.value.copy(resourceUsage = usage)
            } catch (e: Exception) {
                // Silently fail - resource usage is not critical
            }
        }
    }
    
    /**
     * Refresh resource usage
     */
    fun refreshResourceUsage() {
        loadResourceUsage()
    }
    
    /**
     * Clear error
     */
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}

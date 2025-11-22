package ireader.presentation.ui.plugins.management

import ireader.domain.plugins.PluginInfo
import ireader.domain.plugins.PluginPerformanceInfo
import ireader.domain.plugins.PluginResourceUsage

/**
 * State for the Plugin Management screen
 * Requirements: 14.1, 14.2, 14.3, 14.4, 14.5, 12.1, 12.2, 12.3, 12.4, 12.5
 */
data class PluginManagementState(
    val installedPlugins: List<PluginInfo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedPluginForConfig: String? = null,
    val selectedPluginForError: PluginErrorDetails? = null,
    val pluginToUninstall: String? = null,
    val updatesAvailable: Map<String, String> = emptyMap(), // pluginId to new version
    val isUpdatingAll: Boolean = false,
    val resourceUsage: Map<String, PluginResourceUsage> = emptyMap(), // pluginId to usage
    val performanceMetrics: Map<String, PluginPerformanceInfo> = emptyMap(), // pluginId to performance metrics
    val showEnablePluginPrompt: Boolean = false // Show prompt to enable JS plugins in settings
)

/**
 * Details about a plugin error
 */
data class PluginErrorDetails(
    val pluginId: String,
    val pluginName: String,
    val errorMessage: String,
    val troubleshootingSteps: List<String>
)

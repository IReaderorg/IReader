package ireader.presentation.ui.settings.appearance

import androidx.compose.runtime.*
import ireader.domain.models.theme.Theme
import ireader.domain.plugins.PluginManager
import ireader.presentation.ui.core.theme.PluginThemeManager
import ireader.presentation.ui.core.theme.ThemeErrorHandler
import ireader.presentation.ui.core.theme.ThemeHotReloadManager
import ireader.presentation.ui.core.theme.ThemeOption
import kotlinx.coroutines.flow.StateFlow

/**
 * Extension to AppearanceViewModel for plugin theme integration
 * Requirements: 3.1, 3.2, 3.3, 3.4, 3.5
 */
class AppearanceViewModelPluginExtension(
    private val pluginManager: PluginManager,
    private val baseViewModel: AppearanceViewModel
) {
    private val pluginThemeManager = PluginThemeManager(
        pluginManager = pluginManager,
        builtInThemes = baseViewModel.vmThemes
    )
    
    private val errorHandler = ThemeErrorHandler(pluginThemeManager)
    
    private val hotReloadManager = ThemeHotReloadManager(
        pluginManager = pluginManager,
        pluginThemeManager = pluginThemeManager
    )
    
    /**
     * Get all available themes including plugin themes
     * Requirements: 3.1
     */
    fun getAllThemes(): List<ThemeOption> {
        return pluginThemeManager.getAvailableThemes()
    }
    
    /**
     * Get available themes as a flow
     * Requirements: 3.1, 3.5
     */
    fun getAllThemesFlow(): StateFlow<List<ThemeOption>> {
        return pluginThemeManager.getAvailableThemesFlow() as StateFlow<List<ThemeOption>>
    }
    
    /**
     * Apply a theme option (built-in or plugin)
     * Requirements: 3.2, 3.5, 9.6, 9.7, 9.8
     */
    fun applyTheme(themeOption: ThemeOption): Theme {
        return try {
            when (themeOption) {
                is ThemeOption.BuiltIn -> {
                    // Built-in themes are already Theme objects
                    themeOption.theme
                }
                is ThemeOption.Plugin -> {
                    // Apply plugin theme using PluginThemeManager
                    val result = pluginThemeManager.applyTheme(themeOption)
                    if (result.isSuccess) {
                        result.getOrThrow()
                    } else {
                        // Fall back to default theme on error
                        val error = result.exceptionOrNull()
                        baseViewModel.showSnackBar(
                            ireader.i18n.UiText.DynamicString(
                                "Failed to apply plugin theme: ${error?.message ?: "Unknown error"}. Using default theme."
                            )
                        )
                        pluginThemeManager.getDefaultTheme()
                    }
                }
            }
        } catch (e: Exception) {
            // Fall back to default theme on any error
            baseViewModel.showSnackBar(
                ireader.i18n.UiText.DynamicString(
                    "Error applying theme: ${e.message}. Using default theme."
                )
            )
            pluginThemeManager.getDefaultTheme()
        }
    }
    
    /**
     * Get theme option by ID
     */
    fun getThemeById(themeId: String): ThemeOption? {
        return pluginThemeManager.findThemeById(themeId)
    }
    
    /**
     * Get the error handler
     */
    fun getErrorHandler(): ThemeErrorHandler {
        return errorHandler
    }
    
    /**
     * Get the hot reload manager
     */
    fun getHotReloadManager(): ThemeHotReloadManager {
        return hotReloadManager
    }
    
    /**
     * Check if a theme is a plugin theme
     */
    fun isPluginTheme(themeId: String): Boolean {
        return getThemeById(themeId) is ThemeOption.Plugin
    }
    
    /**
     * Get plugin themes only
     */
    fun getPluginThemes(): List<ThemeOption.Plugin> {
        return getAllThemes().filterIsInstance<ThemeOption.Plugin>()
    }
    
    /**
     * Get built-in themes only
     */
    fun getBuiltInThemes(): List<ThemeOption.BuiltIn> {
        return getAllThemes().filterIsInstance<ThemeOption.BuiltIn>()
    }
    
    /**
     * Reload plugin themes
     * Requirements: 3.5
     */
    suspend fun reloadPluginThemes() {
        pluginManager.loadPlugins(forceReload = true)
        hotReloadManager.triggerReload()
    }
    
    /**
     * Reload a specific plugin theme
     * Requirements: 3.5
     */
    suspend fun reloadPluginTheme(pluginId: String): kotlin.Result<Unit> {
        return hotReloadManager.reloadThemePlugin(pluginId)
    }
    
    /**
     * Check if the currently selected plugin theme is still available
     * If not, revert to default theme
     * Requirements: 9.8
     */
    fun checkAndRevertIfPluginUninstalled() {
        val selectedThemeId = baseViewModel.uiPreferences.selectedPluginTheme().get()
        if (selectedThemeId.isNotEmpty()) {
            val themeOption = getThemeById(selectedThemeId)
            if (themeOption == null || (themeOption is ThemeOption.Plugin && !isPluginInstalled(themeOption.plugin.manifest.id))) {
                // Plugin theme is no longer available, revert to default
                baseViewModel.uiPreferences.selectedPluginTheme().set("")
                val defaultTheme = pluginThemeManager.getDefaultTheme()
                baseViewModel.colorTheme.value = defaultTheme.id
                baseViewModel.showSnackBar(
                    ireader.i18n.UiText.DynamicString(
                        "Plugin theme is no longer available. Reverted to default theme."
                    )
                )
            }
        }
    }
    
    /**
     * Check if a plugin is installed
     */
    private fun isPluginInstalled(pluginId: String): Boolean {
        return pluginManager.getEnabledPlugins().any { it.manifest.id == pluginId }
    }
}

/**
 * Composable for integrating plugin themes into appearance settings
 * Requirements: 3.1, 3.2, 3.3
 */
@Composable
fun rememberPluginThemeIntegration(
    pluginManager: PluginManager,
    viewModel: AppearanceViewModel
): AppearanceViewModelPluginExtension {
    return remember(pluginManager, viewModel) {
        AppearanceViewModelPluginExtension(pluginManager, viewModel)
    }
}

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
     * Requirements: 3.2, 3.5
     * TODO: Implement when plugin theme system is complete
     */
    fun applyTheme(themeOption: ThemeOption): Theme {
        // TODO: Implement proper theme application with error handling
        // For now, return a default theme
        return baseViewModel.vmThemes.firstOrNull() ?: throw IllegalStateException("No themes available")
        
        /* Commented out until Result type is properly defined
        val result = errorHandler.applyThemeWithFallback(themeOption) { error ->
            baseViewModel.showSnackBar(
                ireader.i18n.UiText.DynamicString(error.toUserMessage())
            )
        }
        // Convert custom Result to Theme
        return when (result) {
            is ireader.presentation.ui.core.theme.Result.Success -> result.value
            is ireader.presentation.ui.core.theme.Result.Error -> throw result.error
        }
        */
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
        pluginManager.loadPlugins()
        hotReloadManager.triggerReload()
    }
    
    /**
     * Reload a specific plugin theme
     * Requirements: 3.5
     */
    suspend fun reloadPluginTheme(pluginId: String): kotlin.Result<Unit> {
        return hotReloadManager.reloadThemePlugin(pluginId)
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

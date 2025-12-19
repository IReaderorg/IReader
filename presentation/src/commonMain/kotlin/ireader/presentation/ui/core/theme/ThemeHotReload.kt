package ireader.presentation.ui.core.theme

import androidx.compose.runtime.*
import ireader.domain.plugins.PluginManager
import ireader.domain.plugins.ThemePlugin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Hot-reload support for theme plugins during development
 * Requirements: 3.5
 */
class ThemeHotReloadManager(
    private val pluginManager: PluginManager,
    private val pluginThemeManager: PluginThemeManager
) {
    private val _reloadTrigger = MutableStateFlow(0)
    val reloadTrigger: StateFlow<Int> = _reloadTrigger.asStateFlow()
    
    private var isWatching = false
    
    /**
     * Start watching for theme plugin changes
     * In development mode, this will periodically check for plugin updates
     */
    suspend fun startWatching(intervalMs: Long = 5000) {
        if (isWatching) return
        isWatching = true
        
        while (isWatching) {
            delay(intervalMs)
            checkForChanges()
        }
    }
    
    /**
     * Stop watching for changes
     */
    fun stopWatching() {
        isWatching = false
    }
    
    /**
     * Manually trigger a reload
     */
    fun triggerReload() {
        _reloadTrigger.value++
    }
    
    /**
     * Check for plugin changes and reload if necessary
     */
    private suspend fun checkForChanges() {
        try {
            // Reload plugins from disk
            pluginManager.loadPlugins(forceReload = true)
            
            // Trigger UI update
            _reloadTrigger.value++
        } catch (e: Exception) {
            // Log error but don't crash
        }
    }
    
    /**
     * Reload a specific theme plugin
     */
    suspend fun reloadThemePlugin(pluginId: String): kotlin.Result<Unit> {
        return try {
            // Disable the plugin
            pluginManager.disablePlugin(pluginId)
            
            // Re-enable it (which will reload it)
            pluginManager.enablePlugin(pluginId)
            
            // Trigger UI update
            _reloadTrigger.value++
            
            kotlin.Result.success(Unit)
        } catch (e: Exception) {
            kotlin.Result.failure(e)
        }
    }
}

/**
 * Composable hook for hot-reload support
 * Requirements: 3.5
 */
@Composable
fun rememberThemeHotReload(
    hotReloadManager: ThemeHotReloadManager,
    enabled: Boolean = false
): State<Int> {
    val reloadTrigger by hotReloadManager.reloadTrigger.collectAsState()
    
    LaunchedEffect(enabled) {
        if (enabled) {
            hotReloadManager.startWatching()
        } else {
            hotReloadManager.stopWatching()
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            hotReloadManager.stopWatching()
        }
    }
    
    return remember { derivedStateOf { reloadTrigger } }
}

/**
 * Development mode indicator
 */
object ThemeDevelopmentMode {
    var isEnabled: Boolean = false
        private set
    
    fun enable() {
        isEnabled = true
    }
    
    fun disable() {
        isEnabled = false
    }
}

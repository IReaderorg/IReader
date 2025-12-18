package ireader.domain.plugins

import ireader.core.util.createICoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

/**
 * Service for checking if required plugins are installed and available.
 * Used to determine if features like JS sources or Piper TTS can be used.
 */
class RequiredPluginChecker(
    private val pluginManager: PluginManager
) {
    private val scope = createICoroutineScope()
    
    companion object {
        const val JS_ENGINE_PLUGIN_ID = "io.github.ireaderorg.plugins.j2v8-engine"
        const val GRAALVM_ENGINE_PLUGIN_ID = "io.github.ireaderorg.plugins.graalvm-engine"
        const val PIPER_TTS_PLUGIN_ID = "io.github.ireaderorg.plugins.piper-tts"
    }
    
    private val _jsEngineRequired = MutableStateFlow(false)
    val jsEngineRequired: StateFlow<Boolean> = _jsEngineRequired.asStateFlow()
    
    private val _piperTTSRequired = MutableStateFlow(false)
    val piperTTSRequired: StateFlow<Boolean> = _piperTTSRequired.asStateFlow()
    
    /**
     * Check if a JS engine plugin is installed and enabled.
     * On Android, this checks for J2V8. On Desktop, this checks for GraalVM.
     */
    fun isJSEngineAvailable(): Boolean {
        val plugins = pluginManager.pluginsFlow.value
        return plugins.any { plugin ->
            (plugin.id == JS_ENGINE_PLUGIN_ID || plugin.id == GRAALVM_ENGINE_PLUGIN_ID) &&
            plugin.status == PluginStatus.ENABLED
        }
    }
    
    /**
     * Check if Piper TTS plugin is installed and enabled.
     */
    fun isPiperTTSAvailable(): Boolean {
        val plugins = pluginManager.pluginsFlow.value
        return plugins.any { plugin ->
            plugin.id == PIPER_TTS_PLUGIN_ID && plugin.status == PluginStatus.ENABLED
        }
    }
    
    /**
     * Get the installed JS engine plugin info, if any.
     */
    fun getJSEnginePlugin(): PluginInfo? {
        val plugins = pluginManager.pluginsFlow.value
        return plugins.find { plugin ->
            plugin.id == JS_ENGINE_PLUGIN_ID || plugin.id == GRAALVM_ENGINE_PLUGIN_ID
        }
    }
    
    /**
     * Get the installed Piper TTS plugin info, if any.
     */
    fun getPiperTTSPlugin(): PluginInfo? {
        val plugins = pluginManager.pluginsFlow.value
        return plugins.find { plugin ->
            plugin.id == PIPER_TTS_PLUGIN_ID
        }
    }
    
    /**
     * Flow that emits true when JS engine becomes available.
     */
    fun observeJSEngineAvailability(): Flow<Boolean> {
        return pluginManager.pluginsFlow.map { plugins ->
            plugins.any { plugin ->
                (plugin.id == JS_ENGINE_PLUGIN_ID || plugin.id == GRAALVM_ENGINE_PLUGIN_ID) &&
                plugin.status == PluginStatus.ENABLED
            }
        }
    }
    
    /**
     * Flow that emits true when Piper TTS becomes available.
     */
    fun observePiperTTSAvailability(): Flow<Boolean> {
        return pluginManager.pluginsFlow.map { plugins ->
            plugins.any { plugin ->
                plugin.id == PIPER_TTS_PLUGIN_ID && plugin.status == PluginStatus.ENABLED
            }
        }
    }
    
    /**
     * Mark that JS engine is required (user tried to use a JS source).
     * This can be used to trigger showing the required plugin dialog.
     */
    fun requestJSEngine() {
        if (!isJSEngineAvailable()) {
            _jsEngineRequired.value = true
        }
    }
    
    /**
     * Mark that Piper TTS is required (user tried to use Piper TTS).
     * This can be used to trigger showing the required plugin dialog.
     */
    fun requestPiperTTS() {
        if (!isPiperTTSAvailable()) {
            _piperTTSRequired.value = true
        }
    }
    
    /**
     * Clear the JS engine required flag (after dialog is dismissed or plugin installed).
     */
    fun clearJSEngineRequest() {
        _jsEngineRequired.value = false
    }
    
    /**
     * Clear the Piper TTS required flag (after dialog is dismissed or plugin installed).
     */
    fun clearPiperTTSRequest() {
        _piperTTSRequired.value = false
    }
    
    /**
     * Get the appropriate JS engine plugin ID for the current platform.
     * Returns J2V8 for Android, GraalVM for Desktop.
     */
    fun getJSEnginePluginIdForPlatform(): String {
        // This will be determined by the platform at runtime
        // For now, return J2V8 as default (Android is the primary platform)
        return JS_ENGINE_PLUGIN_ID
    }
    
    /**
     * Observe a CatalogStore's JS engine missing status.
     * This no longer auto-shows the dialog - instead, the dialog is shown when user
     * tries to use a JS source (click on it in the sources list).
     * 
     * Call this during app initialization to track JS engine status.
     */
    fun observeCatalogStoreJSEngineStatus(jsEngineMissingFlow: StateFlow<Boolean>) {
        ireader.core.log.Log.info("RequiredPluginChecker: Starting to observe JS engine missing status")
        jsEngineMissingFlow
            .onEach { isMissing ->
                ireader.core.log.Log.info("RequiredPluginChecker: JS engine missing status changed: $isMissing, isJSEngineAvailable=${isJSEngineAvailable()}")
                // Don't auto-show dialog - let user see JS sources first
                // Dialog will be shown when user clicks on a JS source
            }
            .launchIn(scope)
    }
    
    /**
     * Number of pending JS plugins that need the engine.
     * Updated when observeCatalogStoreJSEngineStatus is called.
     */
    private val _pendingJSPluginsCount = MutableStateFlow(0)
    val pendingJSPluginsCount: StateFlow<Int> = _pendingJSPluginsCount.asStateFlow()
    
    /**
     * Update the pending JS plugins count.
     */
    fun updatePendingJSPluginsCount(count: Int) {
        _pendingJSPluginsCount.value = count
    }
}

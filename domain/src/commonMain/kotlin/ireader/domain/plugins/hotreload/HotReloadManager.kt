package ireader.domain.plugins.hotreload

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ireader.core.util.createICoroutineScope
import ireader.domain.plugins.Plugin
import ireader.domain.plugins.PluginManager
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Manager for plugin hot reload functionality.
 * Only active in development mode.
 */
class HotReloadManager(
    private val pluginManager: PluginManager,
    private val fileWatcher: FileWatcher,
    private val config: HotReloadConfig = HotReloadConfig()
) {
    private val scope: CoroutineScope = createICoroutineScope()
    private var watchJob: Job? = null
    
    private val _events = MutableSharedFlow<HotReloadEvent>(replay = 0)
    val events: Flow<HotReloadEvent> = _events.asSharedFlow()
    
    private val _pluginStates = MutableStateFlow<Map<String, HotReloadPluginState>>(emptyMap())
    val pluginStates: StateFlow<Map<String, HotReloadPluginState>> = _pluginStates.asStateFlow()
    
    private val _isWatching = MutableStateFlow(false)
    val isWatching: StateFlow<Boolean> = _isWatching.asStateFlow()
    
    private val snapshots = mutableMapOf<String, PluginSnapshot>()
    private val fileHashes = mutableMapOf<String, String>()

    /**
     * Start watching for plugin changes.
     */
    fun startWatching() {
        if (!config.enabled || _isWatching.value) return
        
        _isWatching.value = true
        watchJob = scope.launch {
            while (isActive) {
                checkForChanges()
                delay(config.watchIntervalMs)
            }
        }
    }
    
    /**
     * Stop watching for plugin changes.
     */
    fun stopWatching() {
        watchJob?.cancel()
        watchJob = null
        _isWatching.value = false
    }
    
    /**
     * Check for plugin file changes.
     */
    private suspend fun checkForChanges() {
        val changes = fileWatcher.detectChanges(config.watchPaths, config.watchPatterns)
        
        for (change in changes) {
            val pluginId = change.pluginId ?: continue
            val currentHash = fileHashes[change.path]
            val newHash = fileWatcher.computeFileHash(change.path)
            
            if (currentHash != newHash) {
                fileHashes[change.path] = newHash
                _events.emit(HotReloadEvent.FileChanged(pluginId, change.path, change.timestamp))
                
                if (config.autoReload) {
                    reloadPlugin(pluginId)
                }
            }
        }
    }
    
    /**
     * Manually trigger a plugin reload.
     */
    suspend fun reloadPlugin(pluginId: String): Result<Unit> {
        val startTime = currentTimeToLong()
        
        updatePluginState(pluginId) { 
            it.copy(status = HotReloadStatus.RELOADING) 
        }
        _events.emit(HotReloadEvent.ReloadStarted(pluginId))
        
        return try {
            // Step 1: Check if plugin can be reloaded
            val plugin = pluginManager.getPlugin(pluginId)
            if (plugin is HotReloadablePlugin && !plugin.canReload()) {
                throw IllegalStateException("Plugin is in critical operation and cannot be reloaded")
            }
            
            // Step 2: Create snapshot for rollback
            _events.emit(HotReloadEvent.ReloadProgress(pluginId, "Creating snapshot", 0.1f))
            createSnapshot(pluginId, plugin)
            
            // Step 3: Preserve state
            var preservedState: Map<String, String> = emptyMap()
            if (config.preserveState && plugin is HotReloadablePlugin) {
                _events.emit(HotReloadEvent.ReloadProgress(pluginId, "Preserving state", 0.2f))
                preservedState = plugin.onBeforeReload()
                _events.emit(HotReloadEvent.StatePreserved(pluginId, preservedState.keys.toList()))
            }
            
            // Step 4: Disable the plugin
            _events.emit(HotReloadEvent.ReloadProgress(pluginId, "Disabling plugin", 0.3f))
            pluginManager.disablePlugin(pluginId)
            
            // Step 5: Reload the plugin
            _events.emit(HotReloadEvent.ReloadProgress(pluginId, "Loading new version", 0.5f))
            pluginManager.loadPlugins()
            
            // Step 6: Enable the plugin
            _events.emit(HotReloadEvent.ReloadProgress(pluginId, "Enabling plugin", 0.7f))
            pluginManager.enablePlugin(pluginId)
            
            // Step 7: Restore state
            val reloadedPlugin = pluginManager.getPlugin(pluginId)
            if (config.preserveState && reloadedPlugin is HotReloadablePlugin && preservedState.isNotEmpty()) {
                _events.emit(HotReloadEvent.ReloadProgress(pluginId, "Restoring state", 0.9f))
                reloadedPlugin.onAfterReload(preservedState)
                _events.emit(HotReloadEvent.StateRestored(pluginId))
            }
            
            val duration = currentTimeToLong() - startTime
            updatePluginState(pluginId) { state ->
                state.copy(
                    status = HotReloadStatus.RELOAD_SUCCESS,
                    lastReloadTime = currentTimeToLong(),
                    reloadCount = state.reloadCount + 1,
                    preservedState = preservedState,
                    errorMessage = null
                )
            }
            _events.emit(HotReloadEvent.ReloadCompleted(pluginId, duration))
            
            Result.success(Unit)
        } catch (e: Exception) {
            val canRollback = snapshots.containsKey(pluginId)
            updatePluginState(pluginId) { 
                it.copy(
                    status = HotReloadStatus.RELOAD_FAILED,
                    errorMessage = e.message
                ) 
            }
            _events.emit(HotReloadEvent.ReloadFailed(pluginId, e.message ?: "Unknown error", canRollback))
            Result.failure(e)
        }
    }
    
    /**
     * Rollback a plugin to its previous version.
     */
    suspend fun rollbackPlugin(pluginId: String): Result<Unit> {
        val snapshot = snapshots[pluginId]
            ?: return Result.failure(IllegalStateException("No snapshot available for rollback"))
        
        return try {
            // Disable current plugin
            pluginManager.disablePlugin(pluginId)
            
            // Restore from snapshot (platform-specific implementation needed)
            restoreFromSnapshot(snapshot)
            
            // Re-enable plugin
            pluginManager.enablePlugin(pluginId)
            
            // Restore state
            val plugin = pluginManager.getPlugin(pluginId)
            if (plugin is HotReloadablePlugin && snapshot.preservedState.isNotEmpty()) {
                plugin.onAfterReload(snapshot.preservedState)
            }
            
            updatePluginState(pluginId) { 
                it.copy(status = HotReloadStatus.ROLLED_BACK) 
            }
            _events.emit(HotReloadEvent.RolledBack(pluginId))
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun createSnapshot(pluginId: String, plugin: Plugin?) {
        val preservedState = if (plugin is HotReloadablePlugin) {
            plugin.onBeforeReload()
        } else {
            emptyMap()
        }
        
        snapshots[pluginId] = PluginSnapshot(
            pluginId = pluginId,
            timestamp = currentTimeToLong(),
            pluginData = ByteArray(0), // Would be populated with actual plugin data
            preservedState = preservedState,
            manifest = plugin?.manifest?.toString() ?: ""
        )
    }
    
    private suspend fun restoreFromSnapshot(snapshot: PluginSnapshot) {
        // Platform-specific implementation to restore plugin from snapshot
        // This would involve writing the plugin data back to disk
    }
    
    private fun updatePluginState(pluginId: String, update: (HotReloadPluginState) -> HotReloadPluginState) {
        val current = _pluginStates.value[pluginId] ?: HotReloadPluginState(
            pluginId = pluginId,
            lastModified = currentTimeToLong(),
            lastReloadTime = null,
            reloadCount = 0,
            status = HotReloadStatus.IDLE
        )
        _pluginStates.value = _pluginStates.value + (pluginId to update(current))
    }
    
    /**
     * Clear all snapshots to free memory.
     */
    fun clearSnapshots() {
        snapshots.clear()
    }
    
    /**
     * Get reload statistics.
     */
    fun getStatistics(): HotReloadStatistics {
        val states = _pluginStates.value.values
        return HotReloadStatistics(
            totalReloads = states.sumOf { it.reloadCount },
            successfulReloads = states.count { it.status == HotReloadStatus.RELOAD_SUCCESS },
            failedReloads = states.count { it.status == HotReloadStatus.RELOAD_FAILED },
            rollbacks = states.count { it.status == HotReloadStatus.ROLLED_BACK },
            watchedPlugins = states.size
        )
    }
}

/**
 * Statistics for hot reload operations.
 */
data class HotReloadStatistics(
    val totalReloads: Int,
    val successfulReloads: Int,
    val failedReloads: Int,
    val rollbacks: Int,
    val watchedPlugins: Int
)

/**
 * Interface for file system watching.
 */
interface FileWatcher {
    suspend fun detectChanges(paths: List<String>, patterns: List<String>): List<FileChange>
    suspend fun computeFileHash(path: String): String
    fun startWatching(paths: List<String>, patterns: List<String>)
    fun stopWatching()
}

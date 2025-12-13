package ireader.domain.plugins.hotreload

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Plugin Hot-Reload System
 * Enables live reloading of plugins during development without app restart.
 * 
 * Features:
 * - File system watching for plugin changes
 * - Automatic plugin recompilation detection
 * - State preservation during reload
 * - Rollback on reload failure
 */

/**
 * Hot reload configuration.
 */
@Serializable
data class HotReloadConfig(
    /** Enable hot reload (development mode only) */
    val enabled: Boolean = false,
    /** Watch interval in milliseconds */
    val watchIntervalMs: Long = 1000,
    /** Auto-reload on file change */
    val autoReload: Boolean = true,
    /** Preserve plugin state during reload */
    val preserveState: Boolean = true,
    /** Show reload notifications */
    val showNotifications: Boolean = true,
    /** Paths to watch for changes */
    val watchPaths: List<String> = emptyList(),
    /** File patterns to watch */
    val watchPatterns: List<String> = listOf("*.iplugin", "*.jar", "*.dex")
)

/**
 * State of a plugin in the hot reload system.
 */
@Serializable
data class HotReloadPluginState(
    val pluginId: String,
    val lastModified: Long,
    val lastReloadTime: Long?,
    val reloadCount: Int,
    val status: HotReloadStatus,
    val preservedState: Map<String, String>? = null,
    val errorMessage: String? = null
)

@Serializable
enum class HotReloadStatus {
    IDLE,
    WATCHING,
    DETECTING_CHANGES,
    RELOADING,
    RELOAD_SUCCESS,
    RELOAD_FAILED,
    ROLLED_BACK
}

/**
 * Events emitted during hot reload.
 */
sealed class HotReloadEvent {
    data class FileChanged(val pluginId: String, val filePath: String, val timestamp: Long) : HotReloadEvent()
    data class ReloadStarted(val pluginId: String) : HotReloadEvent()
    data class ReloadProgress(val pluginId: String, val stage: String, val progress: Float) : HotReloadEvent()
    data class ReloadCompleted(val pluginId: String, val durationMs: Long) : HotReloadEvent()
    data class ReloadFailed(val pluginId: String, val error: String, val canRollback: Boolean) : HotReloadEvent()
    data class RolledBack(val pluginId: String) : HotReloadEvent()
    data class StatePreserved(val pluginId: String, val stateKeys: List<String>) : HotReloadEvent()
    data class StateRestored(val pluginId: String) : HotReloadEvent()
}

/**
 * Interface for plugins that support hot reload.
 */
interface HotReloadablePlugin {
    /**
     * Called before hot reload to save state.
     * @return Map of state key-value pairs to preserve
     */
    fun onBeforeReload(): Map<String, String>
    
    /**
     * Called after hot reload to restore state.
     * @param preservedState Previously saved state
     */
    fun onAfterReload(preservedState: Map<String, String>)
    
    /**
     * Check if the plugin can be safely reloaded.
     * @return true if reload is safe, false if plugin is in critical operation
     */
    fun canReload(): Boolean
    
    /**
     * Get the current version hash for change detection.
     */
    fun getVersionHash(): String
}

/**
 * File change information.
 */
@Serializable
data class FileChange(
    val path: String,
    val pluginId: String?,
    val changeType: FileChangeType,
    val timestamp: Long,
    val fileSize: Long?
)

@Serializable
enum class FileChangeType {
    CREATED,
    MODIFIED,
    DELETED
}

/**
 * Snapshot of plugin state for rollback.
 */
data class PluginSnapshot(
    val pluginId: String,
    val timestamp: Long,
    val pluginData: ByteArray,
    val preservedState: Map<String, String>,
    val manifest: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as PluginSnapshot
        return pluginId == other.pluginId && timestamp == other.timestamp
    }
    
    override fun hashCode(): Int {
        var result = pluginId.hashCode()
        result = 31 * result + timestamp.hashCode()
        return result
    }
}

package ireader.domain.services

import kotlinx.coroutines.flow.Flow

/**
 * Service for watching extension directory changes.
 * Platform-specific implementations monitor the extensions directory and emit events.
 */
expect class ExtensionWatcherService {
    /**
     * Flow of extension change events
     */
    val events: Flow<ExtensionChangeEvent>
    
    /**
     * Start watching for extension changes
     */
    fun start()
    
    /**
     * Stop watching for extension changes
     */
    fun stop()
    
    /**
     * Check if the watcher is currently running
     */
    fun isRunning(): Boolean
}

/**
 * Events emitted when extensions change
 */
sealed class ExtensionChangeEvent {
    data class Added(val extensionName: String) : ExtensionChangeEvent()
    data class Removed(val extensionName: String) : ExtensionChangeEvent()
}

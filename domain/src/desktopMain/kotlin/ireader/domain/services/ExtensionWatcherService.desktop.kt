package ireader.domain.services

import ireader.core.log.Log
import ireader.core.storage.ExtensionDir
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Desktop implementation of ExtensionWatcherService using Java WatchService
 */
actual class ExtensionWatcherService {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val watcher = ExtensionWatcher(ExtensionDir, scope)
    
    actual val events: Flow<ExtensionChangeEvent> = watcher.events.map { event ->
        when (event) {
            is ExtensionEvent.Added -> {
                Log.info("Extension added: ${event.extensionDir.name}")
                ExtensionChangeEvent.Added(event.extensionDir.name)
            }
            is ExtensionEvent.Removed -> {
                Log.info("Extension removed: ${event.extensionDir.name}")
                ExtensionChangeEvent.Removed(event.extensionDir.name)
            }
        }
    }
    
    actual fun start() {
        watcher.start()
    }
    
    actual fun stop() {
        watcher.stop()
    }
    
    actual fun isRunning(): Boolean = watcher.isRunning()
}

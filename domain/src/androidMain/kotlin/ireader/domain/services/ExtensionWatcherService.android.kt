package ireader.domain.services

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * Android implementation of ExtensionWatcherService (no-op)
 * Extension watching is only supported on desktop platforms
 */
actual class ExtensionWatcherService {
    actual val events: Flow<ExtensionChangeEvent> = emptyFlow()
    
    actual fun start() {
        // No-op on Android
    }
    
    actual fun stop() {
        // No-op on Android
    }
    
    actual fun isRunning(): Boolean = false
}

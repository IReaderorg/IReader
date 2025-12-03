package ireader.domain.services

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * iOS implementation of ExtensionWatcherService
 * 
 * On iOS, extensions are JS plugins that don't need file system watching
 */
actual class ExtensionWatcherService {
    actual val extensionChanges: Flow<ExtensionChangeEvent>
        get() = emptyFlow()
    
    actual fun start() {
        // No-op on iOS - JS plugins are managed differently
    }
    
    actual fun stop() {
        // No-op
    }
}

sealed class ExtensionChangeEvent {
    data class Added(val path: String) : ExtensionChangeEvent()
    data class Removed(val path: String) : ExtensionChangeEvent()
    data class Modified(val path: String) : ExtensionChangeEvent()
}

package ireader.presentation.core

import ireader.presentation.ui.update.AppUpdateState
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Desktop implementation of DownloadEventHandler (no-op)
 */
actual class DownloadEventHandler actual constructor(
    updateState: MutableStateFlow<AppUpdateState>
) {
    actual fun cleanup() {
        // No-op for desktop
    }
}

/**
 * Create desktop-specific download event handler
 */
actual fun createDownloadEventHandler(updateState: MutableStateFlow<AppUpdateState>): DownloadEventHandler {
    return DownloadEventHandler(updateState)
}
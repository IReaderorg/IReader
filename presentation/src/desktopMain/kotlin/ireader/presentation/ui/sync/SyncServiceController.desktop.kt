package ireader.presentation.ui.sync

/**
 * Desktop implementation of SyncServiceController.
 * 
 * Currently a no-op implementation. Desktop doesn't require a foreground service
 * since the app typically runs in the foreground during sync.
 * 
 * Future enhancement: Could implement background task management if needed.
 */
actual class SyncServiceController {
    
    actual fun startService(deviceName: String) {
        // No-op on Desktop - sync runs in foreground
    }

    actual fun updateProgress(progress: Int, currentItem: String, currentIndex: Int, totalItems: Int) {
        // No-op on Desktop
    }

    actual fun stopService() {
        // No-op on Desktop
    }

    actual fun cancelSync() {
        // No-op on Desktop
    }
    
    actual fun showCompletionNotification(deviceName: String, syncedItems: Int, durationMs: Long) {
        // No-op on Desktop - could show system notification in future
    }
    
    actual fun showErrorNotification(deviceName: String?, errorMessage: String, suggestion: String?) {
        // No-op on Desktop - could show system notification in future
    }
}

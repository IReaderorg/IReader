package ireader.presentation.ui.sync

/**
 * iOS implementation of SyncServiceController.
 * 
 * Currently a no-op implementation. iOS background task management would be
 * implemented here in the future if needed.
 * 
 * Future enhancement: Could implement iOS background task API integration.
 */
actual class SyncServiceController {
    
    actual fun startService(deviceName: String) {
        // No-op on iOS - would implement background task here
    }

    actual fun updateProgress(progress: Int, currentItem: String, currentIndex: Int, totalItems: Int) {
        // No-op on iOS
    }

    actual fun stopService() {
        // No-op on iOS
    }

    actual fun cancelSync() {
        // No-op on iOS
    }
    
    actual fun showCompletionNotification(deviceName: String, syncedItems: Int, durationMs: Long) {
        // No-op on iOS - could show local notification in future
    }
    
    actual fun showErrorNotification(deviceName: String?, errorMessage: String, suggestion: String?) {
        // No-op on iOS - could show local notification in future
    }
}

package ireader.presentation.ui.sync

import android.content.Context

/**
 * Android implementation of SyncServiceController.
 * 
 * Controls the SyncForegroundService to manage sync operations in the background.
 */
actual class SyncServiceController(private val context: Context) {
    
    actual fun startService(deviceName: String) {
        SyncForegroundService.startSync(context, deviceName)
    }

    actual fun updateProgress(progress: Int, currentItem: String, currentIndex: Int, totalItems: Int) {
        SyncForegroundService.updateProgress(context, progress, currentItem, currentIndex, totalItems)
    }

    actual fun stopService() {
        SyncForegroundService.stopSync(context)
    }

    actual fun cancelSync() {
        SyncForegroundService.cancelSync(context)
    }
    
    actual fun setCancelCallback(callback: () -> Unit) {
        SyncForegroundService.onCancelCallback = callback
    }
    
    actual fun showCompletionNotification(deviceName: String, syncedItems: Int, durationMs: Long) {
        SyncForegroundService.showCompletionNotification(context, deviceName, syncedItems, durationMs)
    }
    
    actual fun showErrorNotification(deviceName: String?, errorMessage: String, suggestion: String?) {
        SyncForegroundService.showErrorNotification(context, deviceName, errorMessage, suggestion)
    }
}

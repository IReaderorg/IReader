package ireader.presentation.ui.sync

/**
 * Platform-agnostic interface for controlling the sync service.
 * 
 * This expect/actual pattern allows the SyncViewModel to control the sync service
 * without knowing platform-specific details.
 * 
 * - Android: Controls the foreground service
 * - Desktop: Could control a background task (future implementation)
 * - iOS: Could control a background task (future implementation)
 */
expect class SyncServiceController {
    /**
     * Start the sync service for the specified device.
     * 
     * @param deviceName Name of the device being synced with
     */
    fun startService(deviceName: String)

    /**
     * Update the sync progress.
     * 
     * @param progress Progress percentage (0-100)
     * @param currentItem Name of the item currently being synced
     * @param currentIndex Current item index (1-based)
     * @param totalItems Total number of items to sync
     */
    fun updateProgress(progress: Int, currentItem: String, currentIndex: Int = 0, totalItems: Int = 0)

    /**
     * Stop the sync service.
     */
    fun stopService()

    /**
     * Cancel the sync operation.
     */
    fun cancelSync()
    
    /**
     * Set the callback to be invoked when the user cancels sync from the notification.
     * 
     * @param callback Function to be called when cancel is triggered
     */
    fun setCancelCallback(callback: () -> Unit)
    
    /**
     * Show a completion notification when sync finishes successfully.
     * 
     * @param deviceName Name of the device that was synced with
     * @param syncedItems Number of items that were synced
     * @param durationMs Duration of the sync operation in milliseconds
     */
    fun showCompletionNotification(deviceName: String, syncedItems: Int, durationMs: Long)
    
    /**
     * Show an error notification when sync fails.
     * 
     * @param deviceName Name of the device (null if failure occurred before device selection)
     * @param errorMessage User-friendly error message
     * @param suggestion Optional suggestion for resolving the error
     */
    fun showErrorNotification(deviceName: String?, errorMessage: String, suggestion: String? = null)
}

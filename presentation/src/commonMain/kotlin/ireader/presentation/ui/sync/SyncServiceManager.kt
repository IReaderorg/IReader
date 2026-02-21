package ireader.presentation.ui.sync

/**
 * Manager for tracking sync service state across platforms.
 * 
 * This class provides a platform-agnostic way to track the state of the sync service.
 * Platform-specific implementations (Android foreground service, Desktop background task)
 * should use this manager to maintain consistent state.
 * 
 * Following TDD - tests were written first in SyncServiceManagerTest.kt
 */
class SyncServiceManager {
    private var serviceRunning: Boolean = false
    private var deviceName: String = ""
    private var currentProgress: Int = 0
    private var currentItem: String = ""

    /**
     * Check if the service is currently running.
     */
    fun isServiceRunning(): Boolean = serviceRunning

    /**
     * Start the service with the specified device name.
     * 
     * @param deviceName Name of the device being synced with
     */
    fun startService(deviceName: String) {
        this.serviceRunning = true
        this.deviceName = deviceName
        this.currentProgress = 0
        this.currentItem = ""
    }

    /**
     * Stop the service and clear all state.
     */
    fun stopService() {
        this.serviceRunning = false
        this.deviceName = ""
        this.currentProgress = 0
        this.currentItem = ""
    }

    /**
     * Update the current sync progress.
     * 
     * @param progress Progress percentage (0-100), will be clamped to valid range
     * @param currentItem Name of the item currently being synced
     */
    fun updateProgress(progress: Int, currentItem: String) {
        this.currentProgress = progress.coerceIn(0, 100)
        this.currentItem = currentItem
    }

    /**
     * Get the current progress percentage.
     */
    fun getCurrentProgress(): Int = currentProgress

    /**
     * Get the name of the item currently being synced.
     */
    fun getCurrentItem(): String = currentItem

    /**
     * Get the name of the device being synced with.
     */
    fun getDeviceName(): String = deviceName
}

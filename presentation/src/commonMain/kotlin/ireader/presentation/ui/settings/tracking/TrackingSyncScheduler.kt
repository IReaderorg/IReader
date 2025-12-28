package ireader.presentation.ui.settings.tracking

/**
 * Platform-specific scheduler for tracking sync.
 * Android uses WorkManager, Desktop/iOS use platform-appropriate alternatives.
 */
interface TrackingSyncScheduler {
    /**
     * Schedule periodic tracking sync.
     * @param intervalMinutes Interval between syncs in minutes
     * @param requireWifi Whether to only sync over WiFi
     */
    fun schedule(intervalMinutes: Int, requireWifi: Boolean = false)
    
    /**
     * Cancel scheduled tracking sync.
     */
    fun cancel()
    
    /**
     * Check if tracking sync is currently scheduled.
     */
    fun isScheduled(): Boolean
}

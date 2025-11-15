package ireader.domain.js.update

/**
 * Interface for scheduling periodic plugin update checks.
 * Platform-specific implementations handle the actual scheduling mechanism.
 */
expect class JSPluginUpdateScheduler {
    
    /**
     * Schedules periodic update checks.
     * 
     * @param intervalHours Interval between checks in hours
     */
    fun schedulePeriodicCheck(intervalHours: Int)
    
    /**
     * Cancels scheduled update checks.
     */
    fun cancelPeriodicCheck()
    
    /**
     * Checks if periodic updates are scheduled.
     */
    fun isScheduled(): Boolean
}

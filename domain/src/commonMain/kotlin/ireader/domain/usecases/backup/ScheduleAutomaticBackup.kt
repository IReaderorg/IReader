package ireader.domain.usecases.backup

import ireader.domain.models.prefs.PreferenceValues

/**
 * Use case for scheduling automatic backups
 */
interface ScheduleAutomaticBackup {
    /**
     * Schedule automatic backups based on the frequency
     */
    fun schedule(frequency: PreferenceValues.AutomaticBackup)
    
    /**
     * Cancel scheduled automatic backups
     */
    fun cancel()
    
    /**
     * Check if automatic backups are scheduled
     */
    fun isScheduled(): Boolean
}

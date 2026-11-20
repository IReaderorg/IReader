package ireader.domain.usecases.backup

import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.preferences.prefs.UiPreferences

/**
 * Restores automatic backup scheduling on app start. The actual work is done by
 * [AutoBackupWorker]; this keeps startup and the settings screen on one code path.
 */
class AutomaticBackup(
    private val uiPreferences: UiPreferences,
    private val scheduleAutomaticBackup: ScheduleAutomaticBackup
) {
    fun initialize() {
        val frequency = uiPreferences.automaticBackupTime().get()
        if (frequency != PreferenceValues.AutomaticBackup.Off) {
            scheduleAutomaticBackup.schedule(frequency)
        }
    }
}

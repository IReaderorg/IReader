package ireader.domain.usecases.backup

import ireader.domain.models.common.Uri
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.usecases.backup.v2.BackupOrchestrator
import ireader.domain.usecases.files.GetSimpleStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okio.FileSystem

/**
 * Desktop implementation of automatic backup scheduling.
 */
class ScheduleAutomaticBackupImpl(
    private val backupOrchestrator: BackupOrchestrator,
    private val uiPreferences: UiPreferences,
    private val simpleStorage: GetSimpleStorage,
    private val backupRetentionStorage: BackupRetentionStorage
) : ScheduleAutomaticBackup {
    private var scheduledJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val fileSystem = FileSystem.SYSTEM

    override fun schedule(frequency: PreferenceValues.AutomaticBackup) {
        cancel()
        val intervalMillis = AutomaticBackupPolicy.interval(frequency)?.inWholeMilliseconds ?: return

        scheduledJob = scope.launch {
            // Catch up immediately if a backup is already overdue, then keep the interval.
            runCatching { performBackup(frequency) }
            while (isActive) {
                delay(intervalMillis)
                runCatching { performBackup(frequency) }
            }
        }
    }

    override fun cancel() {
        scheduledJob?.cancel()
        scheduledJob = null
    }

    override fun isScheduled(): Boolean = scheduledJob?.isActive == true

    private suspend fun performBackup(frequency: PreferenceValues.AutomaticBackup) {
        val now = System.currentTimeMillis()
        if (!AutomaticBackupPolicy.isDue(frequency, uiPreferences.lastBackUpTime().get(), now)) return

        val backupDir = simpleStorage.automaticBackupDirectory
        fileSystem.createDirectories(backupDir)
        val backupPath = backupDir / AutomaticBackupPolicy.fileName(now)

        val result = backupOrchestrator.createBackup(
            uri = Uri(backupPath.toString()),
            options = AutomaticBackupPolicy.backupOptions()
        )
        if (!result.isSuccess) {
            println("[AutoBackup] Backup failed: ${result.exceptionOrNull()}")
            return
        }

        uiPreferences.lastBackUpTime().set(now)
        backupRetentionStorage.rotateAutomaticBackups(BackupRetentionPolicy.fromPreferences(uiPreferences))
        println("[AutoBackup] Backup created: $backupPath")
    }
}

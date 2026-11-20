package ireader.domain.usecases.backup

import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.usecases.backup.v2.BackupOptions
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

/**
 * Pure timing and retention rules for automatic backups, shared by every platform.
 */
object AutomaticBackupPolicy {

    @OptIn(ExperimentalTime::class)
    fun interval(frequency: PreferenceValues.AutomaticBackup): Duration? = when (frequency) {
        PreferenceValues.AutomaticBackup.Every6Hours -> 6.hours
        PreferenceValues.AutomaticBackup.Every12Hours -> 12.hours
        PreferenceValues.AutomaticBackup.Daily -> 1.days
        PreferenceValues.AutomaticBackup.Every2Days -> 2.days
        PreferenceValues.AutomaticBackup.Weekly -> 7.days
        PreferenceValues.AutomaticBackup.Off -> null
    }

    fun isDue(
        frequency: PreferenceValues.AutomaticBackup,
        lastBackupTimeMillis: Long,
        nowMillis: Long
    ): Boolean {
        val interval = interval(frequency)?.inWholeMilliseconds ?: return false
        if (lastBackupTimeMillis <= 0L) return true
        return nowMillis - lastBackupTimeMillis >= interval
    }

    fun fileName(timestampMillis: Long): String = "IReader_auto_backup_$timestampMillis.gz"

    fun fileNamePrefix(): String = "IReader_auto_backup_"

    /**
     * Automatic backups must be identical to a full manual backup — especially
     * chapter text, which the legacy CreateBackup path drops entirely.
     */
    fun backupOptions(): BackupOptions = BackupOptions(includeChapterContent = true)
}

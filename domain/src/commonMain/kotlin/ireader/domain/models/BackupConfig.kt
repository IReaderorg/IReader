package ireader.domain.models

import ireader.domain.models.prefs.PreferenceValues

/**
 * Configuration for automatic backups
 */
data class BackupConfig(
    val enabled: Boolean = false,
    val frequency: PreferenceValues.AutomaticBackup = PreferenceValues.AutomaticBackup.Off,
    val maxBackupFiles: Int = 2,
    val includeReadingHistory: Boolean = true,
    val includeSettings: Boolean = true,
    val lastBackupTime: Long = 0
)

/**
 * Result of a backup operation
 */
sealed class BackupResult {
    data class Success(val filePath: String, val timestamp: Long) : BackupResult()
    data class Error(val message: String, val exception: Throwable? = null) : BackupResult()
}

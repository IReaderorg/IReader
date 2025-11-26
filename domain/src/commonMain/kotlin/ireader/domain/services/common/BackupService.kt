package ireader.domain.services.common

import kotlinx.coroutines.flow.StateFlow

/**
 * Common backup and restore service
 */
interface BackupService : PlatformService {
    /**
     * Current service state
     */
    val state: StateFlow<ServiceState>
    
    /**
     * Backup progress
     */
    val backupProgress: StateFlow<BackupProgress?>
    
    /**
     * Restore progress
     */
    val restoreProgress: StateFlow<RestoreProgress?>
    
    /**
     * Create local backup
     */
    suspend fun createBackup(
        includeLibrary: Boolean = true,
        includeChapters: Boolean = false,
        includeSettings: Boolean = true,
        includeExtensions: Boolean = true,
        destination: String? = null
    ): ServiceResult<BackupResult>
    
    /**
     * Restore from local backup
     */
    suspend fun restoreBackup(
        backupPath: String,
        restoreLibrary: Boolean = true,
        restoreChapters: Boolean = false,
        restoreSettings: Boolean = true,
        restoreExtensions: Boolean = true
    ): ServiceResult<RestoreResult>
    
    /**
     * List available backups
     */
    suspend fun listBackups(
        location: BackupLocation = BackupLocation.LOCAL
    ): ServiceResult<List<BackupInfo>>
    
    /**
     * Delete backup
     */
    suspend fun deleteBackup(
        backupPath: String
    ): ServiceResult<Unit>
    
    /**
     * Schedule automatic backups
     */
    suspend fun scheduleAutoBackup(
        intervalHours: Int,
        includeChapters: Boolean = false
    ): ServiceResult<String>
    
    /**
     * Cancel scheduled auto-backup
     */
    suspend fun cancelAutoBackup(): ServiceResult<Unit>
    
    /**
     * Validate backup file
     */
    suspend fun validateBackup(
        backupPath: String
    ): ServiceResult<BackupValidation>
}

/**
 * Backup location
 */
enum class BackupLocation {
    LOCAL,
    CLOUD,
    EXTERNAL_STORAGE
}

/**
 * Backup progress
 */
data class BackupProgress(
    val currentStep: BackupStep,
    val progress: Float = 0f,
    val itemsProcessed: Int = 0,
    val totalItems: Int = 0,
    val message: String = ""
)

/**
 * Backup steps
 */
enum class BackupStep {
    PREPARING,
    BACKING_UP_LIBRARY,
    BACKING_UP_CHAPTERS,
    BACKING_UP_SETTINGS,
    BACKING_UP_EXTENSIONS,
    COMPRESSING,
    FINALIZING
}

/**
 * Restore progress
 */
data class RestoreProgress(
    val currentStep: RestoreStep,
    val progress: Float = 0f,
    val itemsProcessed: Int = 0,
    val totalItems: Int = 0,
    val message: String = ""
)

/**
 * Restore steps
 */
enum class RestoreStep {
    VALIDATING,
    EXTRACTING,
    RESTORING_LIBRARY,
    RESTORING_CHAPTERS,
    RESTORING_SETTINGS,
    RESTORING_EXTENSIONS,
    FINALIZING
}

/**
 * Backup result
 */
data class BackupResult(
    val backupPath: String,
    val fileSize: Long,
    val booksCount: Int,
    val chaptersCount: Int,
    val timestamp: Long
)

/**
 * Restore result
 */
data class RestoreResult(
    val booksRestored: Int,
    val chaptersRestored: Int,
    val settingsRestored: Boolean,
    val extensionsRestored: Int,
    val errors: List<String> = emptyList()
)

/**
 * Backup information
 */
data class BackupInfo(
    val path: String,
    val name: String,
    val size: Long,
    val timestamp: Long,
    val booksCount: Int,
    val chaptersCount: Int,
    val hasSettings: Boolean,
    val hasExtensions: Boolean
)

/**
 * Backup validation result
 */
data class BackupValidation(
    val isValid: Boolean,
    val version: String,
    val booksCount: Int,
    val chaptersCount: Int,
    val errors: List<String> = emptyList()
)

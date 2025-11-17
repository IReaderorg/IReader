package ireader.domain.data.repository

import ireader.domain.models.backup.*
import ireader.domain.models.common.Uri
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing library backup and restore operations
 */
interface LibraryBackupRepository {
    
    // Backup creation
    suspend fun createBackup(
        uri: Uri,
        backupType: BackupType = BackupType.FULL,
        includeCustomCovers: Boolean = true
    ): Boolean
    
    suspend fun createIncrementalBackup(
        uri: Uri,
        lastBackupTimestamp: Long
    ): Boolean
    
    suspend fun createScheduledBackup(): Boolean
    
    // Backup restoration
    suspend fun restoreBackup(
        uri: Uri,
        options: RestoreOptions = RestoreOptions()
    ): Boolean
    
    fun getRestoreProgress(): Flow<RestoreProgress>
    suspend fun cancelRestore(): Boolean
    
    // Backup validation
    suspend fun validateBackup(uri: Uri): BackupValidationResult
    suspend fun getBackupInfo(uri: Uri): BackupMetadata?
    
    // Backup management
    suspend fun getBackupHistory(): List<BackupRecord>
    suspend fun deleteBackup(backupId: String): Boolean
    suspend fun getBackupSize(backupType: BackupType): Long
    
    // Cloud storage integration
    suspend fun uploadToCloud(uri: Uri, provider: CloudProvider): Boolean
    suspend fun downloadFromCloud(backupId: String, provider: CloudProvider): Uri?
    suspend fun getCloudBackups(provider: CloudProvider): List<CloudBackup>
    suspend fun deleteCloudBackup(backupId: String, provider: CloudProvider): Boolean
    
    // Automatic backup settings
    suspend fun getBackupSettings(): BackupSettings
    suspend fun updateBackupSettings(settings: BackupSettings): Boolean
    suspend fun scheduleAutomaticBackup(): Boolean
    suspend fun cancelAutomaticBackup(): Boolean
}

/**
 * Backup validation result
 */
data class BackupValidationResult(
    val isValid: Boolean,
    val version: Int,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
    val metadata: BackupMetadata? = null
)

/**
 * Backup record for history tracking
 */
data class BackupRecord(
    val id: String,
    val timestamp: Long,
    val backupType: BackupType,
    val size: Long,
    val location: String,
    val isSuccessful: Boolean,
    val error: String? = null,
    val metadata: BackupMetadata
)

/**
 * Cloud storage providers
 */
enum class CloudProvider {
    GOOGLE_DRIVE,
    DROPBOX,
    ONEDRIVE,
    ICLOUD
}

/**
 * Cloud backup information
 */
data class CloudBackup(
    val id: String,
    val name: String,
    val timestamp: Long,
    val size: Long,
    val provider: CloudProvider,
    val metadata: BackupMetadata
)

/**
 * Backup settings
 */
data class BackupSettings(
    val automaticBackupEnabled: Boolean = false,
    val backupInterval: Long = 7 * 24 * 60 * 60 * 1000L, // 7 days
    val backupType: BackupType = BackupType.FULL,
    val includeCustomCovers: Boolean = true,
    val maxBackupCount: Int = 10,
    val requiresWifi: Boolean = true,
    val cloudProvider: CloudProvider? = null,
    val cloudBackupEnabled: Boolean = false,
    val backupLocation: String = "",
    val compressionEnabled: Boolean = true,
    val encryptionEnabled: Boolean = false
)
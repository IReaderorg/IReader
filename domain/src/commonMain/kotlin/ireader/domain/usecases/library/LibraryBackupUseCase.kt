package ireader.domain.usecases.library

import ireader.domain.data.repository.LibraryBackupRepository
import ireader.domain.models.backup.*
import ireader.domain.models.common.Uri
import kotlinx.coroutines.flow.Flow

/**
 * Use case for managing library backup and restore operations
 */
class LibraryBackupUseCase(
    private val backupRepository: LibraryBackupRepository
) {
    
    /**
     * Create a full library backup
     */
    suspend fun createFullBackup(
        uri: Uri,
        includeCustomCovers: Boolean = true
    ): Boolean {
        return backupRepository.createBackup(
            uri = uri,
            backupType = BackupType.FULL,
            includeCustomCovers = includeCustomCovers
        )
    }
    
    /**
     * Create a library-only backup (no settings)
     */
    suspend fun createLibraryBackup(uri: Uri): Boolean {
        return backupRepository.createBackup(
            uri = uri,
            backupType = BackupType.LIBRARY_ONLY,
            includeCustomCovers = true
        )
    }
    
    /**
     * Create a settings-only backup
     */
    suspend fun createSettingsBackup(uri: Uri): Boolean {
        return backupRepository.createBackup(
            uri = uri,
            backupType = BackupType.SETTINGS_ONLY,
            includeCustomCovers = false
        )
    }
    
    /**
     * Create an incremental backup
     */
    suspend fun createIncrementalBackup(
        uri: Uri,
        lastBackupTimestamp: Long
    ): Boolean {
        return backupRepository.createIncrementalBackup(uri, lastBackupTimestamp)
    }
    
    /**
     * Create a scheduled backup
     */
    suspend fun createScheduledBackup(): Boolean {
        return backupRepository.createScheduledBackup()
    }
    
    /**
     * Restore a backup
     */
    suspend fun restoreBackup(
        uri: Uri,
        options: RestoreOptions = RestoreOptions()
    ): Boolean {
        return backupRepository.restoreBackup(uri, options)
    }
    
    /**
     * Get restore progress
     */
    fun getRestoreProgress(): Flow<RestoreProgress> {
        return backupRepository.getRestoreProgress()
    }
    
    /**
     * Cancel an ongoing restore
     */
    suspend fun cancelRestore(): Boolean {
        return backupRepository.cancelRestore()
    }
    
    /**
     * Validate a backup file
     */
    suspend fun validateBackup(uri: Uri): ireader.domain.data.repository.BackupValidationResult {
        return backupRepository.validateBackup(uri)
    }
    
    /**
     * Get backup information
     */
    suspend fun getBackupInfo(uri: Uri): BackupMetadata? {
        return backupRepository.getBackupInfo(uri)
    }
    
    /**
     * Get backup history
     */
    suspend fun getBackupHistory(): List<ireader.domain.data.repository.BackupRecord> {
        return backupRepository.getBackupHistory()
    }
    
    /**
     * Delete a backup
     */
    suspend fun deleteBackup(backupId: String): Boolean {
        return backupRepository.deleteBackup(backupId)
    }
    
    /**
     * Get estimated backup size
     */
    suspend fun getBackupSize(backupType: BackupType): Long {
        return backupRepository.getBackupSize(backupType)
    }
    
    /**
     * Upload backup to cloud storage
     */
    suspend fun uploadToCloud(uri: Uri, provider: ireader.domain.data.repository.CloudProvider): Boolean {
        return backupRepository.uploadToCloud(uri, provider)
    }
    
    /**
     * Download backup from cloud storage
     */
    suspend fun downloadFromCloud(backupId: String, provider: ireader.domain.data.repository.CloudProvider): Uri? {
        return backupRepository.downloadFromCloud(backupId, provider)
    }
    
    /**
     * Get cloud backups
     */
    suspend fun getCloudBackups(provider: ireader.domain.data.repository.CloudProvider): List<ireader.domain.data.repository.CloudBackup> {
        return backupRepository.getCloudBackups(provider)
    }
    
    /**
     * Delete cloud backup
     */
    suspend fun deleteCloudBackup(backupId: String, provider: ireader.domain.data.repository.CloudProvider): Boolean {
        return backupRepository.deleteCloudBackup(backupId, provider)
    }
    
    /**
     * Get backup settings
     */
    suspend fun getBackupSettings(): ireader.domain.data.repository.BackupSettings {
        return backupRepository.getBackupSettings()
    }
    
    /**
     * Update backup settings
     */
    suspend fun updateBackupSettings(settings: ireader.domain.data.repository.BackupSettings): Boolean {
        return backupRepository.updateBackupSettings(settings)
    }
    
    /**
     * Schedule automatic backup
     */
    suspend fun scheduleAutomaticBackup(): Boolean {
        return backupRepository.scheduleAutomaticBackup()
    }
    
    /**
     * Cancel automatic backup
     */
    suspend fun cancelAutomaticBackup(): Boolean {
        return backupRepository.cancelAutomaticBackup()
    }
}

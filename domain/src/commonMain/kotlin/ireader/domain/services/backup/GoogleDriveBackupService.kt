package ireader.domain.services.backup

import ireader.domain.models.backup.BackupData
import ireader.domain.models.backup.BackupInfo

/**
 * Service interface for Google Drive backup operations
 */
interface GoogleDriveBackupService {
    /**
     * Authenticate with Google Drive
     * @return Result containing account email on success
     */
    suspend fun authenticate(): Result<String>
    
    /**
     * Disconnect from Google Drive
     */
    suspend fun disconnect(): Result<Unit>
    
    /**
     * Check if currently authenticated
     */
    suspend fun isAuthenticated(): Boolean
    
    /**
     * Create and upload a backup to Google Drive
     * @param data The backup data to upload
     * @return Result containing backup ID on success
     */
    suspend fun createBackup(data: BackupData): Result<String>
    
    /**
     * List all available backups from Google Drive
     * @return Result containing list of backup information
     */
    suspend fun listBackups(): Result<List<BackupInfo>>
    
    /**
     * Download a backup from Google Drive
     * @param backupId The ID of the backup to download
     * @return Result containing the backup data
     */
    suspend fun downloadBackup(backupId: String): Result<BackupData>
    
    /**
     * Delete a backup from Google Drive
     * @param backupId The ID of the backup to delete
     */
    suspend fun deleteBackup(backupId: String): Result<Unit>
}

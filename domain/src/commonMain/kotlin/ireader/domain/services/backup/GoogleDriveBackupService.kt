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

    /**
     * Upload raw backup file bytes directly to Google Drive
     * @param fileName The name of the file
     * @param content The byte content of the file
     * @param mimeType The MIME type of the file
     * @return Result containing file ID on success
     */
    suspend fun uploadRawBackup(
        fileName: String,
        content: ByteArray,
        mimeType: String = "application/gzip"
    ): Result<String>

    /**
     * Download raw backup file bytes directly from Google Drive
     * @param fileId The ID of the file to download
     * @return Result containing byte array on success
     */
    suspend fun downloadRawBackup(fileId: String): Result<ByteArray>
}

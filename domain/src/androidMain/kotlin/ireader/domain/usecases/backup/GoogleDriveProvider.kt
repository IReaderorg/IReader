package ireader.domain.usecases.backup

import ireader.domain.models.BackupResult
import ireader.domain.services.backup.GoogleDriveBackupService
import ireader.domain.utils.extensions.ioDispatcher
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android implementation of Google Drive provider
 * 
 * Delegates to GoogleDriveBackupService which handles the actual
 * Google Drive API operations and authentication.
 */
actual class GoogleDriveProvider : CloudStorageProvider, KoinComponent {
    override val providerName: String = "Google Drive"
    
    // Use the service from data module via DI
    private val googleDriveService: GoogleDriveBackupService by inject()
    
    override suspend fun isAuthenticated(): Boolean = withContext(ioDispatcher) {
        try {
            googleDriveService.isAuthenticated()
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun authenticate(): Result<Unit> {
        return try {
            val result = googleDriveService.authenticate()
            result.map { Unit }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun signOut(): Result<Unit> = withContext(ioDispatcher) {
        try {
            googleDriveService.disconnect()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun uploadBackup(
        localFilePath: String,
        fileName: String
    ): BackupResult = withContext(ioDispatcher) {
        // Note: This method is not directly supported by GoogleDriveBackupService
        // The service uses BackupData objects instead of file paths
        // For now, return an error indicating to use the service directly
        BackupResult.Error(
            "Use GoogleDriveBackupService.createBackup() for backup operations. " +
            "This provider is for CloudBackupManager compatibility."
        )
    }
    
    override suspend fun downloadBackup(
        cloudFileName: String,
        localFilePath: String
    ): BackupResult = withContext(ioDispatcher) {
        // Note: This method is not directly supported by GoogleDriveBackupService
        // The service returns BackupData objects instead of writing to file paths
        BackupResult.Error(
            "Use GoogleDriveBackupService.downloadBackup() for restore operations. " +
            "This provider is for CloudBackupManager compatibility."
        )
    }
    
    override suspend fun listBackups(): Result<List<CloudBackupFile>> = withContext(ioDispatcher) {
        try {
            val result = googleDriveService.listBackups()
            result.map { backupInfoList ->
                backupInfoList.map { info ->
                    CloudBackupFile(
                        fileName = info.name,
                        size = info.size,
                        timestamp = info.timestamp,
                        cloudId = info.id
                    )
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteBackup(fileName: String): Result<Unit> = withContext(ioDispatcher) {
        try {
            // The service uses backup ID, but we receive fileName
            // Try to find the backup by name first
            val backupsResult = googleDriveService.listBackups()
            if (backupsResult.isFailure) {
                return@withContext Result.failure(
                    backupsResult.exceptionOrNull() ?: Exception("Failed to list backups")
                )
            }
            
            val backup = backupsResult.getOrThrow().find { it.name == fileName }
                ?: return@withContext Result.failure(Exception("Backup not found: $fileName"))
            
            googleDriveService.deleteBackup(backup.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

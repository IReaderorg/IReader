package ireader.domain.usecases.backup

import ireader.domain.models.BackupResult

/**
 * Google Drive cloud storage provider
 * Platform-specific implementation required
 */
expect class GoogleDriveProvider() : CloudStorageProvider

/**
 * Default implementation for platforms without Google Drive support
 */
class GoogleDriveProviderStub : CloudStorageProvider {
    override val providerName: String = "Google Drive"
    
    override suspend fun isAuthenticated(): Boolean = false
    
    override suspend fun authenticate(): Result<Unit> {
        return Result.failure(Exception("Google Drive not supported on this platform"))
    }
    
    override suspend fun signOut(): Result<Unit> {
        return Result.failure(Exception("Google Drive not supported on this platform"))
    }
    
    override suspend fun uploadBackup(
        localFilePath: String,
        fileName: String
    ): BackupResult {
        return BackupResult.Error("Google Drive not supported on this platform")
    }
    
    override suspend fun downloadBackup(
        cloudFileName: String,
        localFilePath: String
    ): BackupResult {
        return BackupResult.Error("Google Drive not supported on this platform")
    }
    
    override suspend fun listBackups(): Result<List<CloudBackupFile>> {
        return Result.failure(Exception("Google Drive not supported on this platform"))
    }
    
    override suspend fun deleteBackup(fileName: String): Result<Unit> {
        return Result.failure(Exception("Google Drive not supported on this platform"))
    }
}

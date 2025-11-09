package ireader.domain.usecases.backup

import ireader.domain.models.BackupResult

/**
 * Dropbox cloud storage provider
 * Platform-specific implementation required
 */
expect class DropboxProvider() : CloudStorageProvider

/**
 * Default implementation for platforms without Dropbox support
 */
class DropboxProviderStub : CloudStorageProvider {
    override val providerName: String = "Dropbox"
    
    override suspend fun isAuthenticated(): Boolean = false
    
    override suspend fun authenticate(): Result<Unit> {
        return Result.failure(Exception("Dropbox not supported on this platform"))
    }
    
    override suspend fun signOut(): Result<Unit> {
        return Result.failure(Exception("Dropbox not supported on this platform"))
    }
    
    override suspend fun uploadBackup(
        localFilePath: String,
        fileName: String
    ): BackupResult {
        return BackupResult.Error("Dropbox not supported on this platform")
    }
    
    override suspend fun downloadBackup(
        cloudFileName: String,
        localFilePath: String
    ): BackupResult {
        return BackupResult.Error("Dropbox not supported on this platform")
    }
    
    override suspend fun listBackups(): Result<List<CloudBackupFile>> {
        return Result.failure(Exception("Dropbox not supported on this platform"))
    }
    
    override suspend fun deleteBackup(fileName: String): Result<Unit> {
        return Result.failure(Exception("Dropbox not supported on this platform"))
    }
}

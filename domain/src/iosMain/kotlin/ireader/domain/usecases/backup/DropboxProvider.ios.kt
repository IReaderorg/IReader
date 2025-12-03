package ireader.domain.usecases.backup

import ireader.domain.models.BackupResult

/**
 * iOS implementation of DropboxProvider
 * 
 * TODO: Full implementation using Dropbox SDK for iOS
 */
actual class DropboxProvider actual constructor() : CloudStorageProvider {
    override val providerName: String = "Dropbox"
    
    override suspend fun isAuthenticated(): Boolean = false
    
    override suspend fun authenticate(): Result<Unit> {
        return Result.failure(Exception("Dropbox not implemented on iOS"))
    }
    
    override suspend fun signOut(): Result<Unit> {
        return Result.success(Unit)
    }
    
    override suspend fun uploadBackup(localFilePath: String, fileName: String): BackupResult {
        return BackupResult.Error("Dropbox not implemented on iOS")
    }
    
    override suspend fun downloadBackup(cloudFileName: String, localFilePath: String): BackupResult {
        return BackupResult.Error("Dropbox not implemented on iOS")
    }
    
    override suspend fun listBackups(): Result<List<CloudBackupFile>> {
        return Result.success(emptyList())
    }
    
    override suspend fun deleteBackup(fileName: String): Result<Unit> {
        return Result.failure(Exception("Dropbox not implemented on iOS"))
    }
}

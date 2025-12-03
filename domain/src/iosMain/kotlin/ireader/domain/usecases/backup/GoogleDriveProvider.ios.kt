package ireader.domain.usecases.backup

import ireader.domain.models.BackupResult

/**
 * iOS implementation of GoogleDriveProvider
 * 
 * TODO: Full implementation using Google Sign-In SDK and Google Drive API
 */
actual class GoogleDriveProvider actual constructor() : CloudStorageProvider {
    override val providerName: String = "Google Drive"
    
    override suspend fun isAuthenticated(): Boolean = false
    
    override suspend fun authenticate(): Result<Unit> {
        return Result.failure(Exception("Google Drive not implemented on iOS"))
    }
    
    override suspend fun signOut(): Result<Unit> {
        return Result.success(Unit)
    }
    
    override suspend fun uploadBackup(localFilePath: String, fileName: String): BackupResult {
        return BackupResult.Error("Google Drive not implemented on iOS")
    }
    
    override suspend fun downloadBackup(cloudFileName: String, localFilePath: String): BackupResult {
        return BackupResult.Error("Google Drive not implemented on iOS")
    }
    
    override suspend fun listBackups(): Result<List<CloudBackupFile>> {
        return Result.success(emptyList())
    }
    
    override suspend fun deleteBackup(fileName: String): Result<Unit> {
        return Result.failure(Exception("Google Drive not implemented on iOS"))
    }
}

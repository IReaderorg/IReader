package ireader.domain.usecases.backup

import ireader.domain.models.common.Uri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * iOS implementation of GoogleDriveProvider
 * 
 * TODO: Implement using Google Sign-In SDK and Google Drive API
 */
actual class GoogleDriveProvider : CloudStorageProvider {
    override val name: String = "Google Drive"
    override val isAvailable: Boolean = false
    
    override suspend fun authenticate(): Result<Unit> {
        return Result.failure(Exception("Google Drive not implemented on iOS"))
    }
    
    override suspend fun isAuthenticated(): Boolean = false
    
    override suspend fun disconnect(): Result<Unit> {
        return Result.success(Unit)
    }
    
    override suspend fun upload(data: ByteArray, fileName: String): Result<String> {
        return Result.failure(Exception("Not implemented"))
    }
    
    override suspend fun download(fileId: String): Result<ByteArray> {
        return Result.failure(Exception("Not implemented"))
    }
    
    override suspend fun listBackups(): Result<List<CloudBackupInfo>> {
        return Result.success(emptyList())
    }
    
    override suspend fun delete(fileId: String): Result<Unit> {
        return Result.failure(Exception("Not implemented"))
    }
    
    override fun observeAuthState(): Flow<Boolean> = flowOf(false)
}

package ireader.domain.usecases.backup

import ireader.domain.models.BackupResult
import ireader.domain.services.backup.GoogleDriveBackupService
import ireader.domain.utils.extensions.currentTimeToLong
import ireader.domain.utils.extensions.ioDispatcher
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer
import okio.use
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android implementation of Google Drive provider
 * 
 * Delegates to GoogleDriveBackupService which handles the actual
 * Google Drive API operations and authentication.
 */
class GoogleDriveProvider : CloudStorageProvider, KoinComponent {
    override val providerName: String = "Google Drive"
    
    // Use the service from data module via DI
    private val googleDriveService: GoogleDriveBackupService by inject()
    private val fileSystem: FileSystem by inject()
    
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
        try {
            val path = localFilePath.toPath()
            if (!fileSystem.exists(path)) {
                return@withContext BackupResult.Error("Local file not found: $localFilePath")
            }
            val content = fileSystem.source(path).buffer().use { it.readByteArray() }
            val result = googleDriveService.uploadRawBackup(fileName, content)
            if (result.isSuccess) {
                BackupResult.Success(filePath = localFilePath, timestamp = currentTimeToLong())
            } else {
                BackupResult.Error(result.exceptionOrNull()?.message ?: "Upload failed")
            }
        } catch (e: Exception) {
            BackupResult.Error("Upload error: ${e.message}", e)
        }
    }
    
    override suspend fun downloadBackup(
        cloudFileName: String,
        localFilePath: String
    ): BackupResult = withContext(ioDispatcher) {
        try {
            val listResult = googleDriveService.listBackups()
            if (listResult.isFailure) {
                return@withContext BackupResult.Error(listResult.exceptionOrNull()?.message ?: "Failed to list backups")
            }
            val fileInfo = listResult.getOrThrow().find { it.name == cloudFileName }
                ?: return@withContext BackupResult.Error("Cloud backup not found: $cloudFileName")

            val downloadResult = googleDriveService.downloadRawBackup(fileInfo.id)
            if (downloadResult.isFailure) {
                return@withContext BackupResult.Error(downloadResult.exceptionOrNull()?.message ?: "Download failed")
            }

            val path = localFilePath.toPath()
            path.parent?.let { parent ->
                if (!fileSystem.exists(parent)) fileSystem.createDirectories(parent)
            }
            fileSystem.sink(path).buffer().use { it.write(downloadResult.getOrThrow()) }
            BackupResult.Success(filePath = localFilePath, timestamp = fileInfo.timestamp.takeIf { it > 0 } ?: currentTimeToLong())
        } catch (e: Exception) {
            BackupResult.Error("Download error: ${e.message}", e)
        }
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

/**
 * Creates a GoogleDriveProvider instance for Android
 */
actual fun createGoogleDriveProvider(): CloudStorageProvider = GoogleDriveProvider()

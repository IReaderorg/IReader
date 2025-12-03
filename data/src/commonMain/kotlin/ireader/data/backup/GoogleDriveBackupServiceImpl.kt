package ireader.data.backup

import ireader.domain.models.backup.BackupData
import ireader.domain.models.backup.BackupInfo
import ireader.domain.services.backup.GoogleDriveBackupService
import ireader.domain.utils.extensions.currentTimeToLong
import ireader.domain.utils.extensions.formatForFilename
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.toInstant
import kotlinx.serialization.json.Json
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.time.ExperimentalTime

/**
 * Implementation of Google Drive backup service
 * 
 * Note: This implementation requires GoogleDriveAuthenticator to be injected.
 * The authenticator handles platform-specific OAuth2 flows:
 * - Android: GoogleSignInClient with ActivityResultContracts
 * - Desktop: Browser-based OAuth with local callback server
 * - iOS: Google Sign-In iOS SDK
 * 
 * Authentication tokens are stored securely using platform-appropriate storage.
 */
class GoogleDriveBackupServiceImpl(
    private val authenticator: GoogleDriveAuthenticator
) : GoogleDriveBackupService {
    
    private var accountEmail: String? = null
    private var driveClient: GoogleDriveClient? = null
    
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    override suspend fun authenticate(): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Use the injected authenticator to perform platform-specific OAuth2 flow
            val result = authenticator.authenticate()
            
            if (result.isSuccess) {
                accountEmail = result.getOrNull()
                
                // Initialize Drive client with token provider
                driveClient = GoogleDriveClient(
                    getAccessToken = { authenticator.getAccessToken() }
                )
                
                Result.success(accountEmail ?: "")
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Authentication failed"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Authentication failed: ${e.message}", e))
        }
    }
    
    override suspend fun disconnect(): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Revoke tokens and clean up
            authenticator.disconnect()
            driveClient?.close()
            driveClient = null
            accountEmail = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Disconnect failed: ${e.message}", e))
        }
    }
    
    override suspend fun isAuthenticated(): Boolean {
        return authenticator.isAuthenticated()
    }
    
    override suspend fun createBackup(data: BackupData): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            if (!isAuthenticated()) {
                return@withContext Result.failure(Exception("Not authenticated with Google Drive"))
            }
            
            val client = driveClient 
                ?: return@withContext Result.failure(Exception("Drive client not initialized"))
            
            // Serialize backup data to JSON
            val jsonString = json.encodeToString(data)
            
            // Compress with GZIP using existing method
            val compressedData = compressData(jsonString.toByteArray())
            
            // Generate filename with timestamp
            val timestamp = currentTimeToLong().formatForFilename()
            val fileName = "ireader_backup_$timestamp.json.gz"
            
            // Upload to Google Drive appDataFolder
            val uploadResult = client.uploadFile(
                fileName = fileName,
                content = compressedData,
                mimeType = "application/gzip"
            )
            
            if (uploadResult.isSuccess) {
                Result.success(uploadResult.getOrThrow())
            } else {
                Result.failure(uploadResult.exceptionOrNull() ?: Exception("Upload failed"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Backup creation failed: ${e.message}", e))
        }
    }
    
    override suspend fun listBackups(): Result<List<BackupInfo>> = withContext(Dispatchers.IO) {
        return@withContext try {
            if (!isAuthenticated()) {
                return@withContext Result.failure(Exception("Not authenticated with Google Drive"))
            }
            
            val client = driveClient 
                ?: return@withContext Result.failure(Exception("Drive client not initialized"))
            
            // Query Google Drive for backup files in appDataFolder
            val listResult = client.listFiles("ireader_backup_*.json.gz")
            
            if (listResult.isFailure) {
                return@withContext Result.failure(
                    listResult.exceptionOrNull() ?: Exception("Failed to list files")
                )
            }
            
            val driveFiles = listResult.getOrThrow()
            
            // Convert DriveFile to BackupInfo
            val backupInfoList = driveFiles.mapNotNull { driveFile ->
                try {
                    // Parse timestamp from filename (ireader_backup_yyyyMMdd_HHmmss.json.gz)
                    val timestampStr = driveFile.name
                        .removePrefix("ireader_backup_")
                        .removeSuffix(".json.gz")
                    
                    // Parse timestamp from filename format: yyyyMMdd_HHmmss
                    val timestamp = parseFilenameTimestamp(timestampStr)
                    
                    BackupInfo(
                        id = driveFile.id,
                        name = driveFile.name,
                        timestamp = timestamp,
                        size = driveFile.size?.toLongOrNull() ?: 0L
                    )
                } catch (e: Exception) {
                    // Skip files with invalid names
                    null
                }
            }.sortedByDescending { it.timestamp }
            
            Result.success(backupInfoList)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to list backups: ${e.message}", e))
        }
    }
    
    override suspend fun downloadBackup(backupId: String): Result<BackupData> = withContext(Dispatchers.IO) {
        return@withContext try {
            if (!isAuthenticated()) {
                return@withContext Result.failure(Exception("Not authenticated with Google Drive"))
            }
            
            val client = driveClient 
                ?: return@withContext Result.failure(Exception("Drive client not initialized"))
            
            // Download file from Google Drive by ID
            val downloadResult = client.downloadFile(backupId)
            
            if (downloadResult.isFailure) {
                return@withContext Result.failure(
                    downloadResult.exceptionOrNull() ?: Exception("Download failed")
                )
            }
            
            val compressedData = downloadResult.getOrThrow()
            
            // Decompress GZIP using existing method
            val decompressedData = decompressData(compressedData)
            
            // Deserialize JSON to BackupData using existing json instance
            val jsonString = String(decompressedData, Charsets.UTF_8)
            val backupData = json.decodeFromString<BackupData>(jsonString)
            
            Result.success(backupData)
        } catch (e: Exception) {
            Result.failure(Exception("Backup download failed: ${e.message}", e))
        }
    }
    
    override suspend fun deleteBackup(backupId: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            if (!isAuthenticated()) {
                return@withContext Result.failure(Exception("Not authenticated with Google Drive"))
            }
            
            val client = driveClient 
                ?: return@withContext Result.failure(Exception("Drive client not initialized"))
            
            // Delete file from Google Drive using Drive API
            // The client handles 404 errors gracefully
            val deleteResult = client.deleteFile(backupId)
            
            if (deleteResult.isSuccess) {
                Result.success(Unit)
            } else {
                Result.failure(deleteResult.exceptionOrNull() ?: Exception("Delete failed"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Backup deletion failed: ${e.message}", e))
        }
    }
    
    /**
     * Compress data using GZIP
     */
    private fun compressData(data: ByteArray): ByteArray {
        val outputStream = ByteArrayOutputStream()
        GZIPOutputStream(outputStream).use { gzip ->
            gzip.write(data)
        }
        return outputStream.toByteArray()
    }
    
    /**
     * Decompress GZIP data
     */
    private fun decompressData(compressedData: ByteArray): ByteArray {
        val inputStream = ByteArrayInputStream(compressedData)
        val outputStream = ByteArrayOutputStream()
        GZIPInputStream(inputStream).use { gzip ->
            gzip.copyTo(outputStream)
        }
        return outputStream.toByteArray()
    }
    
    /**
     * Parse timestamp from filename format: yyyyMMdd_HHmmss
     */
    @OptIn(ExperimentalTime::class)
    private fun parseFilenameTimestamp(timestampStr: String): Long {
        return try {
            // Format: 20251203_143025
            if (timestampStr.length != 15 || timestampStr[8] != '_') return 0L
            
            val year = timestampStr.substring(0, 4).toIntOrNull() ?: return 0L
            val month = timestampStr.substring(4, 6).toIntOrNull() ?: return 0L
            val day = timestampStr.substring(6, 8).toIntOrNull() ?: return 0L
            val hour = timestampStr.substring(9, 11).toIntOrNull() ?: return 0L
            val minute = timestampStr.substring(11, 13).toIntOrNull() ?: return 0L
            val second = timestampStr.substring(13, 15).toIntOrNull() ?: return 0L
            
            val dateTime = kotlinx.datetime.LocalDateTime(year, month, day, hour, minute, second)
            dateTime.toInstant(kotlinx.datetime.TimeZone.currentSystemDefault()).toEpochMilliseconds()
        } catch (e: Exception) {
            0L
        }
    }
}

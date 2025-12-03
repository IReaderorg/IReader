package ireader.domain.usecases.backup

import ireader.domain.models.BackupResult
import platform.Foundation.*
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM
import okio.buffer
import okio.use

/**
 * iOS implementation of DropboxProvider
 * 
 * Uses Dropbox HTTP API for backup operations.
 */
@OptIn(ExperimentalForeignApi::class)
actual class DropboxProvider actual constructor() : CloudStorageProvider {
    
    override val providerName: String = "Dropbox"
    
    private val httpClient = HttpClient()
    private val json = Json { ignoreUnknownKeys = true }
    private val fileSystem = FileSystem.SYSTEM
    
    // Dropbox API endpoints
    private val apiUrl = "https://api.dropboxapi.com/2"
    private val contentUrl = "https://content.dropboxapi.com/2"
    
    // Backup folder path in Dropbox
    private val backupFolderPath = "/IReader Backups"
    
    // Access token - should be obtained via OAuth
    private var accessToken: String? = null
    
    /**
     * Set the access token for API calls
     */
    fun setAccessToken(token: String) {
        accessToken = token
    }
    
    override suspend fun isAuthenticated(): Boolean {
        return accessToken != null
    }
    
    override suspend fun authenticate(): Result<Unit> {
        return if (accessToken != null) {
            ensureBackupFolder()
            Result.success(Unit)
        } else {
            Result.failure(Exception("Not authenticated. Please authenticate with Dropbox first."))
        }
    }
    
    override suspend fun signOut(): Result<Unit> {
        val token = accessToken
        if (token != null) {
            try {
                httpClient.post("$apiUrl/auth/token/revoke") {
                    header("Authorization", "Bearer $token")
                }
            } catch (e: Exception) {
                // Ignore revocation errors
            }
        }
        
        accessToken = null
        return Result.success(Unit)
    }
    
    override suspend fun uploadBackup(localFilePath: String, fileName: String): BackupResult {
        val token = accessToken ?: return BackupResult.Error("Not authenticated")
        
        return try {
            ensureBackupFolder()
            
            val filePath = localFilePath.toPath()
            if (!fileSystem.exists(filePath)) {
                return BackupResult.Error("File not found: $localFilePath")
            }
            
            val fileContent = fileSystem.source(filePath).buffer().use { it.readByteArray() }
            val dropboxPath = "$backupFolderPath/$fileName"
            
            val response = httpClient.post("$contentUrl/files/upload") {
                header("Authorization", "Bearer $token")
                header("Dropbox-API-Arg", buildJsonObject {
                    put("path", dropboxPath)
                    put("mode", "overwrite")
                    put("autorename", false)
                    put("mute", false)
                }.toString())
                contentType(ContentType.Application.OctetStream)
                setBody(fileContent)
            }
            
            if (response.status.isSuccess()) {
                BackupResult.Success(fileName, (NSDate().timeIntervalSince1970 * 1000).toLong())
            } else {
                BackupResult.Error("Upload failed: ${response.status}")
            }
        } catch (e: Exception) {
            BackupResult.Error("Upload failed: ${e.message}")
        }
    }
    
    override suspend fun downloadBackup(cloudFileName: String, localFilePath: String): BackupResult {
        val token = accessToken ?: return BackupResult.Error("Not authenticated")
        
        return try {
            val dropboxPath = "$backupFolderPath/$cloudFileName"
            
            val response = httpClient.post("$contentUrl/files/download") {
                header("Authorization", "Bearer $token")
                header("Dropbox-API-Arg", buildJsonObject {
                    put("path", dropboxPath)
                }.toString())
            }
            
            if (!response.status.isSuccess()) {
                return BackupResult.Error("Download failed: ${response.status}")
            }
            
            val content = response.readBytes()
            
            val localPath = localFilePath.toPath()
            localPath.parent?.let { parent ->
                if (!fileSystem.exists(parent)) {
                    fileSystem.createDirectories(parent)
                }
            }
            
            fileSystem.sink(localPath).buffer().use { it.write(content) }
            
            BackupResult.Success(localFilePath, (NSDate().timeIntervalSince1970 * 1000).toLong())
        } catch (e: Exception) {
            BackupResult.Error("Download failed: ${e.message}")
        }
    }
    
    override suspend fun listBackups(): Result<List<CloudBackupFile>> {
        val token = accessToken ?: return Result.failure(Exception("Not authenticated"))
        
        return try {
            val response = httpClient.post("$apiUrl/files/list_folder") {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("path", backupFolderPath)
                    put("recursive", false)
                    put("include_deleted", false)
                }.toString())
            }
            
            if (!response.status.isSuccess()) {
                return Result.success(emptyList())
            }
            
            val responseText = response.bodyAsText()
            val jsonResponse = json.parseToJsonElement(responseText).jsonObject
            
            val files = jsonResponse["entries"]?.jsonArray?.mapNotNull { entry ->
                val obj = entry.jsonObject
                val tag = obj[".tag"]?.jsonPrimitive?.content
                
                if (tag != "file") return@mapNotNull null
                
                val name = obj["name"]?.jsonPrimitive?.content ?: return@mapNotNull null
                val size = obj["size"]?.jsonPrimitive?.longOrNull ?: 0L
                val modifiedTime = parseDropboxDate(obj["server_modified"]?.jsonPrimitive?.content)
                val cloudId = obj["id"]?.jsonPrimitive?.content ?: ""
                
                CloudBackupFile(
                    fileName = name,
                    size = size,
                    timestamp = modifiedTime,
                    cloudId = cloudId
                )
            } ?: emptyList()
            
            Result.success(files)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteBackup(fileName: String): Result<Unit> {
        val token = accessToken ?: return Result.failure(Exception("Not authenticated"))
        
        return try {
            val dropboxPath = "$backupFolderPath/$fileName"
            
            val response = httpClient.post("$apiUrl/files/delete_v2") {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("path", dropboxPath)
                }.toString())
            }
            
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Delete failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun ensureBackupFolder() {
        val token = accessToken ?: return
        
        try {
            httpClient.post("$apiUrl/files/create_folder_v2") {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("path", backupFolderPath)
                    put("autorename", false)
                }.toString())
            }
        } catch (e: Exception) {
            // Folder might already exist
        }
    }
    
    private fun parseDropboxDate(dateString: String?): Long {
        if (dateString == null) return 0L
        
        val formatter = NSDateFormatter().apply {
            dateFormat = "yyyy-MM-dd'T'HH:mm:ssZ"
        }
        
        val date = formatter.dateFromString(dateString)
        return (date?.timeIntervalSince1970?.toLong() ?: 0L) * 1000
    }
}

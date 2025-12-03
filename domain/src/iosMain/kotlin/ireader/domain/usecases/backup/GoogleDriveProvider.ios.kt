package ireader.domain.usecases.backup

import ireader.domain.models.BackupResult
import platform.Foundation.*
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM
import okio.buffer
import okio.use

/**
 * iOS implementation of GoogleDriveProvider
 */
@OptIn(ExperimentalForeignApi::class)
actual class GoogleDriveProvider actual constructor() : CloudStorageProvider {
    
    override val providerName: String = "Google Drive"
    
    private val httpClient = HttpClient()
    private val json = Json { ignoreUnknownKeys = true }
    private val fileSystem = FileSystem.SYSTEM
    
    private val backupFolderName = "IReader Backups"
    private var backupFolderId: String? = null
    private var accessToken: String? = null
    
    fun setAccessToken(token: String) {
        accessToken = token
    }
    
    override suspend fun isAuthenticated(): Boolean = accessToken != null
    
    override suspend fun authenticate(): Result<Unit> {
        return if (accessToken != null) {
            ensureBackupFolder()
            Result.success(Unit)
        } else {
            Result.failure(Exception("Not authenticated."))
        }
    }
    
    override suspend fun signOut(): Result<Unit> {
        accessToken = null
        backupFolderId = null
        return Result.success(Unit)
    }

    override suspend fun uploadBackup(localFilePath: String, fileName: String): BackupResult {
        val token = accessToken ?: return BackupResult.Error("Not authenticated")
        
        return try {
            val folderId = ensureBackupFolder() ?: return BackupResult.Error("Failed to create backup folder")
            
            val filePath = localFilePath.toPath()
            if (!fileSystem.exists(filePath)) {
                return BackupResult.Error("File not found: $localFilePath")
            }
            
            val fileContent = fileSystem.source(filePath).buffer().use { it.readByteArray() }
            val existingFileId = findFile(fileName, folderId)
            
            if (existingFileId != null) {
                updateFile(existingFileId, fileContent, token)
            } else {
                createFile(fileName, folderId, fileContent, token)
            }
            
            BackupResult.Success(fileName, (NSDate().timeIntervalSince1970 * 1000).toLong())
        } catch (e: Exception) {
            BackupResult.Error("Upload failed: ${e.message}")
        }
    }
    
    override suspend fun downloadBackup(cloudFileName: String, localFilePath: String): BackupResult {
        val token = accessToken ?: return BackupResult.Error("Not authenticated")
        
        return try {
            val folderId = backupFolderId ?: ensureBackupFolder() ?: return BackupResult.Error("Backup folder not found")
            val fileId = findFile(cloudFileName, folderId) ?: return BackupResult.Error("File not found: $cloudFileName")
            
            val response = httpClient.get("https://www.googleapis.com/drive/v3/files/$fileId?alt=media") {
                header("Authorization", "Bearer $token")
            }
            
            val content = response.readBytes()
            val localPath = localFilePath.toPath()
            localPath.parent?.let { parent ->
                if (!fileSystem.exists(parent)) fileSystem.createDirectories(parent)
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
            val folderId = backupFolderId ?: ensureBackupFolder() ?: return Result.success(emptyList())
            
            val query = "'$folderId' in parents and trashed = false"
            val response = httpClient.get("https://www.googleapis.com/drive/v3/files") {
                header("Authorization", "Bearer $token")
                parameter("q", query)
                parameter("fields", "files(id,name,size,modifiedTime)")
            }
            
            val responseText = response.bodyAsText()
            val jsonResponse = json.parseToJsonElement(responseText).jsonObject
            
            val files = jsonResponse["files"]?.jsonArray?.mapNotNull { file ->
                val obj = file.jsonObject
                val name = obj["name"]?.jsonPrimitive?.content ?: return@mapNotNull null
                val size = obj["size"]?.jsonPrimitive?.longOrNull ?: 0L
                val modifiedTime = parseIsoDate(obj["modifiedTime"]?.jsonPrimitive?.content)
                val cloudId = obj["id"]?.jsonPrimitive?.content ?: ""
                
                CloudBackupFile(fileName = name, size = size, timestamp = modifiedTime, cloudId = cloudId)
            } ?: emptyList()
            
            Result.success(files)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteBackup(fileName: String): Result<Unit> {
        val token = accessToken ?: return Result.failure(Exception("Not authenticated"))
        
        return try {
            val folderId = backupFolderId ?: return Result.failure(Exception("Backup folder not found"))
            val fileId = findFile(fileName, folderId) ?: return Result.failure(Exception("File not found: $fileName"))
            
            httpClient.delete("https://www.googleapis.com/drive/v3/files/$fileId") {
                header("Authorization", "Bearer $token")
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun ensureBackupFolder(): String? {
        if (backupFolderId != null) return backupFolderId
        val token = accessToken ?: return null
        
        val query = "name = '$backupFolderName' and mimeType = 'application/vnd.google-apps.folder' and trashed = false"
        val searchResponse = httpClient.get("https://www.googleapis.com/drive/v3/files") {
            header("Authorization", "Bearer $token")
            parameter("q", query)
            parameter("fields", "files(id)")
        }
        
        val searchJson = json.parseToJsonElement(searchResponse.bodyAsText()).jsonObject
        val existingFolder = searchJson["files"]?.jsonArray?.firstOrNull()
        
        if (existingFolder != null) {
            backupFolderId = existingFolder.jsonObject["id"]?.jsonPrimitive?.content
            return backupFolderId
        }
        
        val metadata = buildJsonObject {
            put("name", backupFolderName)
            put("mimeType", "application/vnd.google-apps.folder")
        }
        
        val createResponse = httpClient.post("https://www.googleapis.com/drive/v3/files") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(metadata.toString())
        }
        
        val createJson = json.parseToJsonElement(createResponse.bodyAsText()).jsonObject
        backupFolderId = createJson["id"]?.jsonPrimitive?.content
        return backupFolderId
    }
    
    private suspend fun findFile(fileName: String, folderId: String): String? {
        val token = accessToken ?: return null
        val query = "name = '$fileName' and '$folderId' in parents and trashed = false"
        val response = httpClient.get("https://www.googleapis.com/drive/v3/files") {
            header("Authorization", "Bearer $token")
            parameter("q", query)
            parameter("fields", "files(id)")
        }
        val jsonResponse = json.parseToJsonElement(response.bodyAsText()).jsonObject
        return jsonResponse["files"]?.jsonArray?.firstOrNull()?.jsonObject?.get("id")?.jsonPrimitive?.content
    }
    
    private suspend fun createFile(fileName: String, folderId: String, content: ByteArray, token: String) {
        val metadata = buildJsonObject {
            put("name", fileName)
            putJsonArray("parents") { add(folderId) }
        }
        httpClient.post("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart") {
            header("Authorization", "Bearer $token")
            setBody(MultiPartFormDataContent(formData {
                append("metadata", metadata.toString(), Headers.build { append(HttpHeaders.ContentType, "application/json") })
                append("file", content, Headers.build {
                    append(HttpHeaders.ContentType, "application/octet-stream")
                    append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                })
            }))
        }
    }
    
    private suspend fun updateFile(fileId: String, content: ByteArray, token: String) {
        httpClient.patch("https://www.googleapis.com/upload/drive/v3/files/$fileId?uploadType=media") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.OctetStream)
            setBody(content)
        }
    }
    
    private fun parseIsoDate(dateString: String?): Long {
        if (dateString == null) return 0L
        val formatter = NSDateFormatter().apply { dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ" }
        val date = formatter.dateFromString(dateString)
        return (date?.timeIntervalSince1970?.toLong() ?: 0L) * 1000
    }
}

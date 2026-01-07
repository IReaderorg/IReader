package ireader.data.backup

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.math.min
import kotlin.math.pow

/**
 * Google Drive API v3 client for backup operations
 * 
 * This client provides methods to interact with Google Drive API for:
 * - Creating/finding the "IReader" folder in user's Drive
 * - Uploading backup files to the IReader folder (visible to user)
 * - Listing backup files
 * - Downloading and deleting backups
 * 
 * All operations include exponential backoff retry for network errors.
 */
class GoogleDriveClient(
    private val getAccessToken: suspend () -> String?
) {
    
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = false
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 60_000
            connectTimeoutMillis = 30_000
            socketTimeoutMillis = 30_000
        }
    }
    
    // Cache the folder ID to avoid repeated lookups
    private var ireaderFolderId: String? = null
    
    companion object {
        private const val DRIVE_API_BASE = "https://www.googleapis.com/drive/v3"
        private const val UPLOAD_API_BASE = "https://www.googleapis.com/upload/drive/v3"
        
        // Folder name visible in user's Google Drive
        const val IREADER_FOLDER_NAME = "IReader"
        
        // MIME type for Google Drive folder
        private const val FOLDER_MIME_TYPE = "application/vnd.google-apps.folder"
        
        // Retry configuration
        private const val MAX_RETRIES = 3
        private const val INITIAL_BACKOFF_MS = 1000L
        private const val MAX_BACKOFF_MS = 10000L
    }
    
    /**
     * Get or create the IReader folder in user's Google Drive root
     * 
     * @return Folder ID on success
     */
    suspend fun getOrCreateIReaderFolder(): Result<String> {
        // Return cached folder ID if available
        ireaderFolderId?.let { return Result.success(it) }
        
        return withRetry {
            val token = getAccessToken()
                ?: return@withRetry Result.failure(Exception("Not authenticated"))
            
            try {
                // First, search for existing IReader folder
                val searchResponse: HttpResponse = httpClient.get("$DRIVE_API_BASE/files") {
                    bearerAuth(token)
                    parameter("q", "name = '$IREADER_FOLDER_NAME' and mimeType = '$FOLDER_MIME_TYPE' and trashed = false and 'root' in parents")
                    parameter("fields", "files(id,name)")
                    parameter("spaces", "drive")
                }
                
                if (searchResponse.status.isSuccess()) {
                    val fileList = searchResponse.body<DriveFileList>()
                    
                    if (fileList.files.isNotEmpty()) {
                        // Folder exists, cache and return its ID
                        val folderId = fileList.files.first().id
                        ireaderFolderId = folderId
                        return@withRetry Result.success(folderId)
                    }
                }
                
                // Folder doesn't exist, create it
                val createResponse: HttpResponse = httpClient.post("$DRIVE_API_BASE/files") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody(
                        CreateFolderRequest(
                            name = IREADER_FOLDER_NAME,
                            mimeType = FOLDER_MIME_TYPE,
                            parents = listOf("root")
                        )
                    )
                }
                
                if (createResponse.status.isSuccess()) {
                    val folderResponse = createResponse.body<DriveFileResponse>()
                    ireaderFolderId = folderResponse.id
                    Result.success(folderResponse.id)
                } else {
                    val errorBody = createResponse.bodyAsText()
                    Result.failure(Exception("Failed to create IReader folder: ${createResponse.status} - $errorBody"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Failed to get/create IReader folder: ${e.message}", e))
            }
        }
    }
    
    /**
     * Upload a file to the IReader folder in Google Drive
     * 
     * @param fileName Name of the file
     * @param content File content as ByteArray
     * @param mimeType MIME type of the file
     * @return File ID on success
     */
    suspend fun uploadFile(
        fileName: String,
        content: ByteArray,
        mimeType: String = "application/gzip"
    ): Result<String> {
        // First ensure we have the IReader folder
        val folderResult = getOrCreateIReaderFolder()
        if (folderResult.isFailure) {
            return Result.failure(folderResult.exceptionOrNull() ?: Exception("Failed to get IReader folder"))
        }
        val folderId = folderResult.getOrThrow()
        
        return withRetry {
            val token = getAccessToken() 
                ?: return@withRetry Result.failure(Exception("Not authenticated"))
            
            try {
                // Create file metadata with IReader folder as parent
                val metadata = DriveFileMetadata(
                    name = fileName,
                    parents = listOf(folderId),
                    mimeType = mimeType
                )
                
                // Use multipart upload for files
                val response: HttpResponse = httpClient.post("$UPLOAD_API_BASE/files?uploadType=multipart") {
                    bearerAuth(token)
                    setBody(
                        MultiPartFormDataContent(
                            formData {
                                // Part 1: Metadata
                                append(
                                    "metadata",
                                    Json.encodeToString(DriveFileMetadata.serializer(), metadata),
                                    Headers.build {
                                        append(HttpHeaders.ContentType, "application/json; charset=UTF-8")
                                    }
                                )
                                // Part 2: File content
                                append(
                                    "file",
                                    content,
                                    Headers.build {
                                        append(HttpHeaders.ContentType, mimeType)
                                    }
                                )
                            }
                        )
                    )
                }
                
                if (response.status.isSuccess()) {
                    val fileResponse = response.body<DriveFileResponse>()
                    Result.success(fileResponse.id)
                } else {
                    val errorBody = response.bodyAsText()
                    Result.failure(Exception("Upload failed: ${response.status} - $errorBody"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Upload failed: ${e.message}", e))
            }
        }
    }
    
    /**
     * List backup files in the IReader folder
     * 
     * @param namePattern Pattern to match file names (e.g., "ireader_backup_*.json.gz")
     * @return List of file metadata
     */
    suspend fun listFiles(namePattern: String): Result<List<DriveFile>> {
        // First ensure we have the IReader folder
        val folderResult = getOrCreateIReaderFolder()
        if (folderResult.isFailure) {
            // If folder doesn't exist, return empty list (no backups yet)
            return Result.success(emptyList())
        }
        val folderId = folderResult.getOrThrow()
        
        return withRetry {
            val token = getAccessToken()
                ?: return@withRetry Result.failure(Exception("Not authenticated"))
            
            try {
                // Build query for files in IReader folder
                val query = buildString {
                    append("'$folderId' in parents")
                    append(" and trashed = false")
                    
                    // Handle simple wildcard patterns
                    if (namePattern.contains("*")) {
                        val prefix = namePattern.substringBefore("*")
                        val suffix = namePattern.substringAfter("*")
                        if (prefix.isNotEmpty()) {
                            append(" and name contains '$prefix'")
                        }
                        if (suffix.isNotEmpty()) {
                            append(" and name contains '$suffix'")
                        }
                    } else {
                        append(" and name = '$namePattern'")
                    }
                }
                
                val response: HttpResponse = httpClient.get("$DRIVE_API_BASE/files") {
                    bearerAuth(token)
                    parameter("q", query)
                    parameter("fields", "files(id,name,size,modifiedTime,createdTime)")
                    parameter("orderBy", "modifiedTime desc")
                    parameter("spaces", "drive")
                }
                
                if (response.status.isSuccess()) {
                    val fileList = response.body<DriveFileList>()
                    Result.success(fileList.files)
                } else {
                    val errorBody = response.bodyAsText()
                    Result.failure(Exception("List files failed: ${response.status} - $errorBody"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("List files failed: ${e.message}", e))
            }
        }
    }
    
    /**
     * Download a file from Google Drive by ID
     * 
     * @param fileId ID of the file to download
     * @return File content as ByteArray
     */
    suspend fun downloadFile(fileId: String): Result<ByteArray> = withRetry {
        val token = getAccessToken()
            ?: return@withRetry Result.failure(Exception("Not authenticated"))
        
        try {
            val response: HttpResponse = httpClient.get("$DRIVE_API_BASE/files/$fileId") {
                bearerAuth(token)
                parameter("alt", "media")
            }
            
            if (response.status.isSuccess()) {
                val content = response.body<ByteArray>()
                Result.success(content)
            } else {
                val errorBody = response.bodyAsText()
                Result.failure(Exception("Download failed: ${response.status} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Download failed: ${e.message}", e))
        }
    }
    
    /**
     * Delete a file from Google Drive by ID
     * 
     * @param fileId ID of the file to delete
     */
    suspend fun deleteFile(fileId: String): Result<Unit> = withRetry {
        val token = getAccessToken()
            ?: return@withRetry Result.failure(Exception("Not authenticated"))
        
        try {
            val response: HttpResponse = httpClient.delete("$DRIVE_API_BASE/files/$fileId") {
                bearerAuth(token)
            }
            
            when {
                response.status.isSuccess() -> Result.success(Unit)
                response.status == HttpStatusCode.NotFound -> {
                    // File not found is not an error - it's already gone
                    Result.success(Unit)
                }
                else -> {
                    val errorBody = response.bodyAsText()
                    Result.failure(Exception("Delete failed: ${response.status} - $errorBody"))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Delete failed: ${e.message}", e))
        }
    }
    
    /**
     * Execute an operation with exponential backoff retry
     */
    private suspend fun <T> withRetry(
        operation: suspend () -> Result<T>
    ): Result<T> {
        var lastException: Exception? = null
        
        repeat(MAX_RETRIES) { attempt ->
            try {
                val result = operation()
                if (result.isSuccess) {
                    return result
                }
                
                // Store the exception for potential retry
                lastException = result.exceptionOrNull() as? Exception
                
                // Check if error is retryable
                val errorMessage = lastException?.message ?: ""
                val isRetryable = errorMessage.contains("timeout", ignoreCase = true) ||
                        errorMessage.contains("connection", ignoreCase = true) ||
                        errorMessage.contains("network", ignoreCase = true) ||
                        errorMessage.contains("503") ||
                        errorMessage.contains("429")
                
                if (!isRetryable) {
                    return result
                }
                
                // Calculate backoff delay
                if (attempt < MAX_RETRIES - 1) {
                    val backoffMs = min(
                        INITIAL_BACKOFF_MS * 2.0.pow(attempt).toLong(),
                        MAX_BACKOFF_MS
                    )
                    delay(backoffMs)
                }
            } catch (e: Exception) {
                lastException = e
                
                // Retry on network errors
                if (attempt < MAX_RETRIES - 1) {
                    val backoffMs = min(
                        INITIAL_BACKOFF_MS * 2.0.pow(attempt).toLong(),
                        MAX_BACKOFF_MS
                    )
                    delay(backoffMs)
                }
            }
        }
        
        return Result.failure(
            lastException ?: Exception("Operation failed after $MAX_RETRIES retries")
        )
    }
    
    /**
     * Clear cached folder ID (useful when user disconnects)
     */
    fun clearCache() {
        ireaderFolderId = null
    }
    
    fun close() {
        ireaderFolderId = null
        httpClient.close()
    }
}

/**
 * Drive API data models
 */
@Serializable
private data class DriveFileMetadata(
    @SerialName("name") val name: String,
    @SerialName("parents") val parents: List<String>,
    @SerialName("mimeType") val mimeType: String
)

@Serializable
private data class CreateFolderRequest(
    @SerialName("name") val name: String,
    @SerialName("mimeType") val mimeType: String,
    @SerialName("parents") val parents: List<String>
)

@Serializable
data class DriveFileResponse(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String? = null,
    @SerialName("mimeType") val mimeType: String? = null
)

@Serializable
data class DriveFile(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String = "",
    @SerialName("size") val size: String? = null,
    @SerialName("modifiedTime") val modifiedTime: String? = null,
    @SerialName("createdTime") val createdTime: String? = null
)

@Serializable
private data class DriveFileList(
    @SerialName("files") val files: List<DriveFile> = emptyList()
)

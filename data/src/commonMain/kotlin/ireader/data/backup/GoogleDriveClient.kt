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
import io.ktor.utils.io.core.*
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
 * - Uploading files to appDataFolder
 * - Listing files in appDataFolder
 * - Downloading files by ID
 * - Deleting files by ID
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
    
    companion object {
        private const val DRIVE_API_BASE = "https://www.googleapis.com/drive/v3"
        private const val UPLOAD_API_BASE = "https://www.googleapis.com/upload/drive/v3"
        private const val APP_DATA_FOLDER = "appDataFolder"
        
        // Retry configuration
        private const val MAX_RETRIES = 3
        private const val INITIAL_BACKOFF_MS = 1000L
        private const val MAX_BACKOFF_MS = 10000L
    }
    
    /**
     * Upload a file to Google Drive's appDataFolder
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
    ): Result<String> = withRetry {
        val token = getAccessToken() 
            ?: return@withRetry Result.failure(Exception("Not authenticated"))
        
        try {
            // Create file metadata
            val metadata = DriveFileMetadata(
                name = fileName,
                parents = listOf(APP_DATA_FOLDER),
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
    
    /**
     * List files in appDataFolder matching a name pattern
     * 
     * @param namePattern Pattern to match file names (e.g., "ireader_backup_*.json.gz")
     * @return List of file metadata
     */
    suspend fun listFiles(namePattern: String): Result<List<DriveFile>> = withRetry {
        val token = getAccessToken()
            ?: return@withRetry Result.failure(Exception("Not authenticated"))
        
        try {
            // Convert pattern to query (simple wildcard support)
            val query = buildString {
                append("'$APP_DATA_FOLDER' in parents")
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
                parameter("spaces", APP_DATA_FOLDER)
                parameter("q", query)
                parameter("fields", "files(id,name,size,modifiedTime,createdTime)")
                parameter("orderBy", "modifiedTime desc")
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
    
    fun close() {
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
data class DriveFileResponse(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String? = null,
    @SerialName("mimeType") val mimeType: String? = null
)

@Serializable
data class DriveFile(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("size") val size: String? = null,
    @SerialName("modifiedTime") val modifiedTime: String,
    @SerialName("createdTime") val createdTime: String? = null
)

@Serializable
private data class DriveFileList(
    @SerialName("files") val files: List<DriveFile>
)

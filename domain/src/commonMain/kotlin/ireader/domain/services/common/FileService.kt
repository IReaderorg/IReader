package ireader.domain.services.common

import kotlinx.coroutines.flow.Flow

/**
 * Common file service for file operations across platforms
 */
interface FileService : PlatformService {
    /**
     * Write text to file
     */
    suspend fun writeText(path: String, content: String): ServiceResult<Unit>
    
    /**
     * Write bytes to file
     */
    suspend fun writeBytes(path: String, content: ByteArray): ServiceResult<Unit>
    
    /**
     * Read text from file
     */
    suspend fun readText(path: String): ServiceResult<String>
    
    /**
     * Read bytes from file
     */
    suspend fun readBytes(path: String): ServiceResult<ByteArray>
    
    /**
     * Delete file
     */
    suspend fun deleteFile(path: String): ServiceResult<Unit>
    
    /**
     * Check if file exists
     */
    suspend fun fileExists(path: String): Boolean
    
    /**
     * Get file size
     */
    suspend fun getFileSize(path: String): ServiceResult<Long>
    
    /**
     * Create directory
     */
    suspend fun createDirectory(path: String): ServiceResult<Unit>
    
    /**
     * List files in directory
     */
    suspend fun listFiles(path: String): ServiceResult<List<FileInfo>>
    
    /**
     * Copy file
     */
    suspend fun copyFile(source: String, destination: String): ServiceResult<Unit>
    
    /**
     * Move file
     */
    suspend fun moveFile(source: String, destination: String): ServiceResult<Unit>
    
    /**
     * Get available storage space
     */
    suspend fun getAvailableSpace(): ServiceResult<Long>
}

/**
 * File information
 */
data class FileInfo(
    val name: String,
    val path: String,
    val size: Long,
    val isDirectory: Boolean,
    val lastModified: Long
)

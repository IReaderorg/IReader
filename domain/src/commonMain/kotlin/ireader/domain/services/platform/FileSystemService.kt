package ireader.domain.services.platform

import ireader.domain.models.common.Uri
import ireader.domain.services.common.PlatformService
import ireader.domain.services.common.ServiceResult

/**
 * Platform-agnostic file system service for file and directory operations
 * 
 * This service abstracts platform-specific file picking, directory selection,
 * and file system operations across Android, Desktop, and iOS.
 */
interface FileSystemService : PlatformService {
    
    /**
     * Pick a single file from the file system
     * 
     * @param initialDirectory Starting directory (optional)
     * @param fileTypes List of allowed file extensions (e.g., ["epub", "pdf"])
     * @param title Dialog title (optional)
     * @return Result containing the selected file URI or error
     */
    suspend fun pickFile(
        initialDirectory: String? = null,
        fileTypes: List<String> = emptyList(),
        title: String? = null
    ): ServiceResult<Uri>
    
    /**
     * Pick multiple files from the file system
     * 
     * @param initialDirectory Starting directory (optional)
     * @param fileTypes List of allowed file extensions
     * @param title Dialog title (optional)
     * @return Result containing list of selected file URIs or error
     */
    suspend fun pickMultipleFiles(
        initialDirectory: String? = null,
        fileTypes: List<String> = emptyList(),
        title: String? = null
    ): ServiceResult<List<Uri>>
    
    /**
     * Pick a directory from the file system
     * 
     * @param initialDirectory Starting directory (optional)
     * @param title Dialog title (optional)
     * @return Result containing the selected directory URI or error
     */
    suspend fun pickDirectory(
        initialDirectory: String? = null,
        title: String? = null
    ): ServiceResult<Uri>
    
    /**
     * Save a file to the file system
     * 
     * @param defaultFileName Suggested file name
     * @param fileExtension File extension (e.g., "epub", "json")
     * @param initialDirectory Starting directory (optional)
     * @param title Dialog title (optional)
     * @return Result containing the save location URI or error
     */
    suspend fun saveFile(
        defaultFileName: String,
        fileExtension: String,
        initialDirectory: String? = null,
        title: String? = null
    ): ServiceResult<Uri>
    
    /**
     * Check if a file exists
     * 
     * @param uri File URI to check
     * @return true if file exists, false otherwise
     */
    suspend fun fileExists(uri: Uri): Boolean
    
    /**
     * Get file size in bytes
     * 
     * @param uri File URI
     * @return File size in bytes or null if file doesn't exist
     */
    suspend fun getFileSize(uri: Uri): Long?
    
    /**
     * Delete a file
     * 
     * @param uri File URI to delete
     * @return Result indicating success or error
     */
    suspend fun deleteFile(uri: Uri): ServiceResult<Unit>
    
    /**
     * Read file content as bytes
     * 
     * @param uri File URI to read
     * @return Result containing file bytes or error
     */
    suspend fun readFileBytes(uri: Uri): ServiceResult<ByteArray>
    
    /**
     * Write bytes to a file
     * 
     * @param uri File URI to write to
     * @param bytes Data to write
     * @return Result indicating success or error
     */
    suspend fun writeFileBytes(uri: Uri, bytes: ByteArray): ServiceResult<Unit>
}

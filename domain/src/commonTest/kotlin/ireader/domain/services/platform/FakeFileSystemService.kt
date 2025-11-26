package ireader.domain.services.platform

import ireader.domain.models.common.Uri
import ireader.domain.services.common.ServiceResult

/**
 * Fake implementation of FileSystemService for testing
 */
class FakeFileSystemService : FileSystemService {
    
    var pickFileResult: ServiceResult<Uri> = ServiceResult.Error("Not configured")
    var pickMultipleFilesResult: ServiceResult<List<Uri>> = ServiceResult.Error("Not configured")
    var pickDirectoryResult: ServiceResult<Uri> = ServiceResult.Error("Not configured")
    var saveFileResult: ServiceResult<Uri> = ServiceResult.Error("Not configured")
    
    private val files = mutableMapOf<String, ByteArray>()
    
    override suspend fun initialize() {
        // No-op for testing
    }
    
    override suspend fun cleanup() {
        // No-op for testing
    }
    
    override suspend fun pickFile(
        initialDirectory: String?,
        fileTypes: List<String>,
        title: String?
    ): ServiceResult<Uri> {
        return pickFileResult
    }
    
    override suspend fun pickMultipleFiles(
        initialDirectory: String?,
        fileTypes: List<String>,
        title: String?
    ): ServiceResult<List<Uri>> {
        return pickMultipleFilesResult
    }
    
    override suspend fun pickDirectory(
        initialDirectory: String?,
        title: String?
    ): ServiceResult<Uri> {
        return pickDirectoryResult
    }
    
    override suspend fun saveFile(
        defaultFileName: String,
        fileExtension: String,
        initialDirectory: String?,
        title: String?
    ): ServiceResult<Uri> {
        return saveFileResult
    }
    
    override suspend fun fileExists(uri: Uri): Boolean {
        return files.containsKey(uri.path)
    }
    
    override suspend fun getFileSize(uri: Uri): Long? {
        return files[uri.path]?.size?.toLong()
    }
    
    override suspend fun deleteFile(uri: Uri): ServiceResult<Unit> {
        return if (files.remove(uri.path) != null) {
            ServiceResult.Success(Unit)
        } else {
            ServiceResult.Error("File not found")
        }
    }
    
    override suspend fun readFileBytes(uri: Uri): ServiceResult<ByteArray> {
        return files[uri.path]?.let { ServiceResult.Success(it) }
            ?: ServiceResult.Error("File not found")
    }
    
    override suspend fun writeFileBytes(uri: Uri, bytes: ByteArray): ServiceResult<Unit> {
        files[uri.path] = bytes
        return ServiceResult.Success(Unit)
    }
    
    // Test helpers
    fun addFile(path: String, content: ByteArray) {
        files[path] = content
    }
    
    fun reset() {
        files.clear()
        pickFileResult = ServiceResult.Error("Not configured")
        pickMultipleFilesResult = ServiceResult.Error("Not configured")
        pickDirectoryResult = ServiceResult.Error("Not configured")
        saveFileResult = ServiceResult.Error("Not configured")
    }
}

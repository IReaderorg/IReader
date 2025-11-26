package ireader.domain.services.platform

import ireader.domain.models.common.Uri
import ireader.domain.services.common.ServiceResult

/**
 * Fake implementation of ShareService for testing
 */
class FakeShareService : ShareService {
    
    var sharedText: String? = null
        private set
    
    var sharedFile: Uri? = null
        private set
    
    var sharedFiles: List<Uri>? = null
        private set
    
    var sharedUrl: String? = null
        private set
    
    var shareTextResult: ServiceResult<Unit> = ServiceResult.Success(Unit)
    var shareFileResult: ServiceResult<Unit> = ServiceResult.Success(Unit)
    var shareFilesResult: ServiceResult<Unit> = ServiceResult.Success(Unit)
    var shareUrlResult: ServiceResult<Unit> = ServiceResult.Success(Unit)
    
    override suspend fun initialize() {
        // No-op for testing
    }
    
    override suspend fun cleanup() {
        // No-op for testing
    }
    
    override suspend fun shareText(text: String, title: String?): ServiceResult<Unit> {
        sharedText = text
        return shareTextResult
    }
    
    override suspend fun shareFile(
        uri: Uri,
        mimeType: String,
        title: String?
    ): ServiceResult<Unit> {
        sharedFile = uri
        return shareFileResult
    }
    
    override suspend fun shareFiles(
        uris: List<Uri>,
        mimeType: String,
        title: String?
    ): ServiceResult<Unit> {
        sharedFiles = uris
        return shareFilesResult
    }
    
    override suspend fun shareUrl(url: String, title: String?): ServiceResult<Unit> {
        sharedUrl = url
        return shareUrlResult
    }
    
    override fun isSharingSupported(): Boolean = true
    
    // Test helpers
    fun reset() {
        sharedText = null
        sharedFile = null
        sharedFiles = null
        sharedUrl = null
        shareTextResult = ServiceResult.Success(Unit)
        shareFileResult = ServiceResult.Success(Unit)
        shareFilesResult = ServiceResult.Success(Unit)
        shareUrlResult = ServiceResult.Success(Unit)
    }
}

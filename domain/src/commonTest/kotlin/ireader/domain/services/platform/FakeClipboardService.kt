package ireader.domain.services.platform

import ireader.domain.services.common.ServiceResult

/**
 * Fake implementation of ClipboardService for testing
 */
class FakeClipboardService : ClipboardService {
    
    var copiedText: String? = null
        private set
    
    var copyTextResult: ServiceResult<Unit> = ServiceResult.Success(Unit)
    var getTextResult: ServiceResult<String>? = null
    
    override suspend fun initialize() {
        // No-op for testing
    }
    
    override suspend fun cleanup() {
        // No-op for testing
    }
    
    override suspend fun copyText(text: String, label: String?): ServiceResult<Unit> {
        copiedText = text
        return copyTextResult
    }
    
    override suspend fun getText(): ServiceResult<String> {
        return getTextResult ?: copiedText?.let { ServiceResult.Success(it) }
            ?: ServiceResult.Error("Clipboard is empty")
    }
    
    override suspend fun hasText(): Boolean {
        return copiedText != null
    }
    
    override suspend fun clear(): ServiceResult<Unit> {
        copiedText = null
        return ServiceResult.Success(Unit)
    }
    
    // Test helpers
    fun reset() {
        copiedText = null
        copyTextResult = ServiceResult.Success(Unit)
        getTextResult = null
    }
}

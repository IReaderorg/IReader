package ireader.domain.services.platform

import ireader.domain.models.common.Uri
import ireader.domain.services.common.PlatformService
import ireader.domain.services.common.ServiceResult

/**
 * Platform-agnostic sharing service
 * 
 * Handles sharing content (text, files, URLs) using platform-specific mechanisms.
 */
interface ShareService : PlatformService {
    
    /**
     * Share text content
     * 
     * @param text Text to share
     * @param title Optional title for the share dialog
     * @return Result indicating success or error
     */
    suspend fun shareText(text: String, title: String? = null): ServiceResult<Unit>
    
    /**
     * Share a file
     * 
     * @param uri File URI to share
     * @param mimeType MIME type of the file (e.g., "application/epub+zip")
     * @param title Optional title for the share dialog
     * @return Result indicating success or error
     */
    suspend fun shareFile(
        uri: Uri,
        mimeType: String,
        title: String? = null
    ): ServiceResult<Unit>
    
    /**
     * Share multiple files
     * 
     * @param uris List of file URIs to share
     * @param mimeType MIME type of the files
     * @param title Optional title for the share dialog
     * @return Result indicating success or error
     */
    suspend fun shareFiles(
        uris: List<Uri>,
        mimeType: String,
        title: String? = null
    ): ServiceResult<Unit>
    
    /**
     * Share a URL
     * 
     * @param url URL to share
     * @param title Optional title for the share dialog
     * @return Result indicating success or error
     */
    suspend fun shareUrl(url: String, title: String? = null): ServiceResult<Unit>
    
    /**
     * Check if sharing is supported on this platform
     * 
     * @return true if sharing is available
     */
    fun isSharingSupported(): Boolean
}

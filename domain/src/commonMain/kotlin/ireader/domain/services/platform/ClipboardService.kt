package ireader.domain.services.platform

import ireader.domain.services.common.PlatformService
import ireader.domain.services.common.ServiceResult

/**
 * Platform-agnostic clipboard service
 * 
 * Handles clipboard operations for text and other data types.
 */
interface ClipboardService : PlatformService {
    
    /**
     * Copy text to clipboard
     * 
     * @param text Text to copy
     * @param label Optional label for the clipboard entry
     * @return Result indicating success or error
     */
    suspend fun copyText(text: String, label: String? = null): ServiceResult<Unit>
    
    /**
     * Get text from clipboard
     * 
     * @return Result containing clipboard text or error
     */
    suspend fun getText(): ServiceResult<String>
    
    /**
     * Check if clipboard has text
     * 
     * @return true if clipboard contains text
     */
    suspend fun hasText(): Boolean
    
    /**
     * Clear clipboard
     * 
     * @return Result indicating success or error
     */
    suspend fun clear(): ServiceResult<Unit>
}

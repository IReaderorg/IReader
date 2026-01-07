package ireader.domain.usecases.backup

/**
 * Handler for Google Drive OAuth callbacks
 * 
 * This object stores pending OAuth data that needs to be processed
 * by the Google Drive backup screen after the OAuth redirect.
 */
object GoogleDriveOAuthHandler {
    /**
     * Pending authorization code from OAuth redirect
     */
    var pendingAuthCode: String? = null
    
    /**
     * Pending URI string from OAuth redirect (contains full callback data)
     */
    var pendingUri: String? = null
    
    /**
     * Pending error from OAuth redirect
     */
    var pendingError: String? = null
    
    /**
     * Clear all pending data after processing
     */
    fun clear() {
        pendingAuthCode = null
        pendingUri = null
        pendingError = null
    }
    
    /**
     * Check if there's pending OAuth data to process
     */
    fun hasPendingData(): Boolean {
        return pendingAuthCode != null || pendingError != null
    }
}

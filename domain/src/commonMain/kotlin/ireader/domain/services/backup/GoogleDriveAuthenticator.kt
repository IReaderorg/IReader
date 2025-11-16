package ireader.domain.services.backup

/**
 * Platform-specific Google Drive authentication interface
 * 
 * Handles OAuth2 authentication flow for Google Drive API access.
 * Each platform (Android, Desktop, iOS) provides its own implementation.
 */
interface GoogleDriveAuthenticator {
    /**
     * Authenticate with Google Drive using platform-specific OAuth2 flow
     * 
     * @return Result containing the user's email address on success, or error on failure
     */
    suspend fun authenticate(): Result<String>
    
    /**
     * Refresh the access token if it has expired
     * 
     * @return Result indicating success or failure of token refresh
     */
    suspend fun refreshToken(): Result<Unit>
    
    /**
     * Check if the user is currently authenticated with valid tokens
     * 
     * @return true if authenticated with valid tokens, false otherwise
     */
    fun isAuthenticated(): Boolean
    
    /**
     * Get the current access token for API requests
     * 
     * @return The access token if authenticated, null otherwise
     */
    fun getAccessToken(): String?
    
    /**
     * Disconnect and revoke authentication tokens
     * 
     * @return Result indicating success or failure of disconnection
     */
    suspend fun disconnect(): Result<Unit>
}

package ireader.data.backup

/**
 * Platform-specific Google Drive authenticator interface
 * 
 * Implementations should handle:
 * - OAuth2 authentication flow for the platform
 * - Secure token storage (EncryptedSharedPreferences, Keychain, etc.)
 * - Token refresh when expired
 * - Token revocation on disconnect
 * 
 * Platform implementations:
 * - Android: Use GoogleSignInClient with ActivityResultContracts
 * - Desktop: Browser-based OAuth with local callback server
 * - iOS: Google Sign-In iOS SDK
 */
interface GoogleDriveAuthenticator {
    
    /**
     * Authenticate with Google Drive using platform-specific OAuth2 flow
     * 
     * @return Result containing user email on success
     */
    suspend fun authenticate(): Result<String>
    
    /**
     * Get current access token
     * 
     * @return Access token if authenticated and valid, null otherwise
     */
    suspend fun getAccessToken(): String?
    
    /**
     * Check if currently authenticated with valid tokens
     * 
     * @return true if authenticated and tokens are valid
     */
    suspend fun isAuthenticated(): Boolean
    
    /**
     * Disconnect and revoke tokens
     */
    suspend fun disconnect()
    
    /**
     * Refresh access token if expired
     * 
     * @return Result with success or failure
     */
    suspend fun refreshToken(): Result<Unit>
}

package ireader.data.backup

/**
 * Configuration holder for Google Drive backup feature.
 * 
 * This object holds the Google Drive OAuth client ID which must be set
 * from the platform-specific code (e.g., Android app module) before
 * using Google Drive backup features.
 */
object GoogleDriveConfig {
    /**
     * Google OAuth2 client ID from Google Cloud Console.
     * Must be set before using Google Drive backup features.
     */
    var clientId: String? = null
        private set

    /**
     * Google OAuth2 client secret from Google Cloud Console (for Desktop/Web OAuth).
     */
    var clientSecret: String? = null
        private set
    
    /**
     * Initialize the Google Drive configuration.
     * Call this from the app startup with the client ID from BuildConfig.
     * 
     * @param clientId The OAuth2 client ID from Google Cloud Console
     * @param clientSecret Optional OAuth2 client secret
     */
    fun initialize(clientId: String, clientSecret: String? = null) {
        this.clientId = clientId.trim()
        if (!clientSecret.isNullOrBlank()) {
            this.clientSecret = clientSecret.trim()
        }
    }

    fun setCredentials(clientId: String, clientSecret: String? = null) {
        this.clientId = clientId.trim()
        this.clientSecret = clientSecret?.trim()
    }
    
    /**
     * Check if the configuration has been initialized.
     */
    fun isInitialized(): Boolean = !clientId.isNullOrBlank()
    
    /**
     * Get the client ID, throwing if not initialized.
     */
    fun requireClientId(): String = clientId 
        ?: throw IllegalStateException("Google Drive client ID not configured. Call GoogleDriveConfig.initialize() first.")
}

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
     * Initialize the Google Drive configuration.
     * Call this from the app startup with the client ID from BuildConfig.
     * 
     * @param clientId The OAuth2 client ID from Google Cloud Console
     */
    fun initialize(clientId: String) {
        this.clientId = clientId
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

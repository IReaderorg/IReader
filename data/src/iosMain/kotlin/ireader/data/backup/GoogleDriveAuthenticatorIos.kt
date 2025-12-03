package ireader.data.backup

/**
 * iOS implementation of Google Drive authenticator
 * 
 * TODO: Implement using Google Sign-In SDK for iOS:
 * - GIDSignIn for authentication
 * - GIDGoogleUser for user info
 * - Store tokens in Keychain
 * 
 * This is a stub implementation that needs to be completed.
 */
class GoogleDriveAuthenticatorIos : GoogleDriveAuthenticator {
    
    override suspend fun authenticate(): Result<String> {
        return Result.failure(
            Exception("Google Drive authentication not yet implemented for iOS. " +
                    "This requires Google Sign-In SDK integration.")
        )
    }
    
    override suspend fun getAccessToken(): String? {
        return null
    }
    
    override suspend fun isAuthenticated(): Boolean {
        return false
    }
    
    override suspend fun disconnect() {
        // TODO: Sign out and clear Keychain
    }
    
    override suspend fun refreshToken(): Result<Unit> {
        return Result.failure(Exception("Token refresh not implemented"))
    }
}

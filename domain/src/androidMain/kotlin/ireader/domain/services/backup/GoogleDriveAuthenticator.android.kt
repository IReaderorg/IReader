package ireader.domain.services.backup

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

/**
 * Android implementation of Google Drive authentication using OAuth2
 * 
 * This implementation uses:
 * - EncryptedSharedPreferences for secure token storage
 * - Manual OAuth2 flow (browser-based) since GoogleSignInClient requires Activity context
 * 
 * Note: For production use, you need to:
 * 1. Configure OAuth2 credentials in Google Cloud Console
 * 2. Add redirect URI: com.yourapp:/oauth2redirect
 * 3. Enable Google Drive API
 * 4. Set CLIENT_ID and CLIENT_SECRET
 */
class AndroidGoogleDriveAuthenticator(
    private val context: Context,
    private val clientId: String,
    private val clientSecret: String
) : GoogleDriveAuthenticator {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    private val encryptedPrefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    @Serializable
    private data class TokenResponse(
        val access_token: String,
        val refresh_token: String? = null,
        val expires_in: Int,
        val token_type: String
    )
    
    @Serializable
    private data class TokenErrorResponse(
        val error: String,
        val error_description: String? = null
    )
    
    override suspend fun authenticate(): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            // This method should be called after the OAuth2 flow completes
            // The actual OAuth2 flow needs to be initiated from an Activity
            // This is a limitation that requires UI integration
            
            // For now, return an error indicating that authentication needs to be
            // initiated from the UI layer with proper Activity context
            Result.failure(
                Exception(
                    "Authentication must be initiated from UI layer. " +
                    "Use Activity context to start OAuth2 flow with browser intent."
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception("Authentication failed: ${e.message}", e))
        }
    }
    
    /**
     * Exchange authorization code for access and refresh tokens
     * This should be called after receiving the authorization code from OAuth2 redirect
     */
    suspend fun exchangeAuthorizationCode(authCode: String): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val url = URL(TOKEN_ENDPOINT)
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            connection.doOutput = true
            
            val postData = buildString {
                append("code=").append(authCode)
                append("&client_id=").append(clientId)
                append("&client_secret=").append(clientSecret)
                append("&redirect_uri=").append(REDIRECT_URI)
                append("&grant_type=authorization_code")
            }
            
            connection.outputStream.use { it.write(postData.toByteArray()) }
            
            val responseCode = connection.responseCode
            val responseBody = if (responseCode == 200) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream.bufferedReader().use { it.readText() }
            }
            
            if (responseCode == 200) {
                val tokenResponse = json.decodeFromString<TokenResponse>(responseBody)
                
                // Store tokens securely
                storeTokens(
                    accessToken = tokenResponse.access_token,
                    refreshToken = tokenResponse.refresh_token ?: "",
                    expiresIn = tokenResponse.expires_in
                )
                
                // Get user email from token info
                val email = getUserEmail(tokenResponse.access_token)
                Result.success(email)
            } else {
                val errorResponse = json.decodeFromString<TokenErrorResponse>(responseBody)
                Result.failure(
                    Exception("Token exchange failed: ${errorResponse.error} - ${errorResponse.error_description}")
                )
            }
        } catch (e: Exception) {
            Result.failure(Exception("Token exchange failed: ${e.message}", e))
        }
    }
    
    override suspend fun refreshToken(): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val refreshToken = getRefreshToken()
                ?: return@withContext Result.failure(Exception("No refresh token available"))
            
            val url = URL(TOKEN_ENDPOINT)
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            connection.doOutput = true
            
            val postData = buildString {
                append("refresh_token=").append(refreshToken)
                append("&client_id=").append(clientId)
                append("&client_secret=").append(clientSecret)
                append("&grant_type=refresh_token")
            }
            
            connection.outputStream.use { it.write(postData.toByteArray()) }
            
            val responseCode = connection.responseCode
            val responseBody = if (responseCode == 200) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream.bufferedReader().use { it.readText() }
            }
            
            if (responseCode == 200) {
                val tokenResponse = json.decodeFromString<TokenResponse>(responseBody)
                
                // Update access token, keep existing refresh token if not provided
                storeTokens(
                    accessToken = tokenResponse.access_token,
                    refreshToken = tokenResponse.refresh_token ?: refreshToken,
                    expiresIn = tokenResponse.expires_in
                )
                
                Result.success(Unit)
            } else {
                val errorResponse = json.decodeFromString<TokenErrorResponse>(responseBody)
                Result.failure(
                    Exception("Token refresh failed: ${errorResponse.error} - ${errorResponse.error_description}")
                )
            }
        } catch (e: Exception) {
            Result.failure(Exception("Token refresh failed: ${e.message}", e))
        }
    }
    
    override fun isAuthenticated(): Boolean {
        val accessToken = getAccessToken() ?: return false
        val expiryTime = encryptedPrefs.getLong(KEY_TOKEN_EXPIRY, 0)
        val currentTime = System.currentTimeMillis()
        
        return accessToken.isNotEmpty() && currentTime < expiryTime
    }
    
    override fun getAccessToken(): String? {
        return encryptedPrefs.getString(KEY_ACCESS_TOKEN, null)
    }
    
    override suspend fun disconnect(): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val accessToken = getAccessToken()
            
            // Revoke token if available
            if (accessToken != null) {
                try {
                    val url = URL("$REVOKE_ENDPOINT?token=$accessToken")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "POST"
                    connection.responseCode // Execute request
                } catch (e: Exception) {
                    // Continue even if revocation fails
                }
            }
            
            // Clear stored tokens
            encryptedPrefs.edit()
                .remove(KEY_ACCESS_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .remove(KEY_TOKEN_EXPIRY)
                .remove(KEY_USER_EMAIL)
                .apply()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Disconnect failed: ${e.message}", e))
        }
    }
    
    /**
     * Get the OAuth2 authorization URL for browser-based authentication
     */
    fun getAuthorizationUrl(): String {
        return buildString {
            append(AUTH_ENDPOINT)
            append("?client_id=").append(clientId)
            append("&redirect_uri=").append(REDIRECT_URI)
            append("&response_type=code")
            append("&scope=").append(SCOPE)
            append("&access_type=offline")
            append("&prompt=consent")
        }
    }
    
    private fun storeTokens(accessToken: String, refreshToken: String, expiresIn: Int) {
        val expiryTime = System.currentTimeMillis() + (expiresIn * 1000L) - EXPIRY_BUFFER_MS
        
        encryptedPrefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putLong(KEY_TOKEN_EXPIRY, expiryTime)
            .apply()
    }
    
    private fun getRefreshToken(): String? {
        return encryptedPrefs.getString(KEY_REFRESH_TOKEN, null)
    }
    
    private suspend fun getUserEmail(accessToken: String): String = withContext(Dispatchers.IO) {
        try {
            val url = URL("$USERINFO_ENDPOINT?access_token=$accessToken")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            
            val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
            val userInfo = json.decodeFromString<Map<String, String>>(responseBody)
            val email = userInfo["email"] ?: "unknown@example.com"
            
            // Store email
            encryptedPrefs.edit()
                .putString(KEY_USER_EMAIL, email)
                .apply()
            
            email
        } catch (e: Exception) {
            "unknown@example.com"
        }
    }
    
    companion object {
        private const val PREFS_NAME = "google_drive_auth"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_EXPIRY = "token_expiry"
        private const val KEY_USER_EMAIL = "user_email"
        
        private const val AUTH_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth"
        private const val TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token"
        private const val REVOKE_ENDPOINT = "https://oauth2.googleapis.com/revoke"
        private const val USERINFO_ENDPOINT = "https://www.googleapis.com/oauth2/v1/userinfo"
        
        private const val REDIRECT_URI = "com.ireader.app:/oauth2redirect"
        private const val SCOPE = "https://www.googleapis.com/auth/drive.file"
        
        // Buffer time before token expiry to trigger refresh (5 minutes)
        private const val EXPIRY_BUFFER_MS = 5 * 60 * 1000L
    }
}

package ireader.domain.services.backup

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.awt.Desktop
import java.io.File
import java.net.HttpURLConnection
import java.net.ServerSocket
import java.net.URI
import java.net.URL
import java.util.prefs.Preferences
import kotlin.random.Random

/**
 * Desktop implementation of Google Drive authentication using OAuth2
 * 
 * This implementation uses:
 * - Browser-based OAuth2 flow
 * - Local HTTP server on random port to receive OAuth callback
 * - Java Preferences API for secure token storage
 * 
 * Note: For production use, you need to:
 * 1. Configure OAuth2 credentials in Google Cloud Console
 * 2. Add redirect URI: http://localhost:PORT/oauth2callback (with various ports)
 * 3. Enable Google Drive API
 * 4. Set CLIENT_ID and CLIENT_SECRET
 */
class DesktopGoogleDriveAuthenticator(
    private val clientId: String,
    private val clientSecret: String
) : GoogleDriveAuthenticator {
    
    private val json = Json { ignoreUnknownKeys = true }
    private val prefs: Preferences = Preferences.userNodeForPackage(DesktopGoogleDriveAuthenticator::class.java)
    
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
            // Find an available port
            val port = findAvailablePort()
            val redirectUri = "http://localhost:$port/oauth2callback"
            
            // Start local HTTP server to receive callback
            val serverSocket = ServerSocket(port)
            
            // Build authorization URL
            val authUrl = buildString {
                append(AUTH_ENDPOINT)
                append("?client_id=").append(clientId)
                append("&redirect_uri=").append(redirectUri)
                append("&response_type=code")
                append("&scope=").append(SCOPE)
                append("&access_type=offline")
                append("&prompt=consent")
            }
            
            // Open browser with authorization URL
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI(authUrl))
            } else {
                return@withContext Result.failure(
                    Exception("Cannot open browser. Please navigate to: $authUrl")
                )
            }
            
            // Wait for callback
            val authCode = try {
                val socket = serverSocket.accept()
                val reader = socket.getInputStream().bufferedReader()
                val requestLine = reader.readLine()
                
                // Send response to browser
                val response = """
                    HTTP/1.1 200 OK
                    Content-Type: text/html
                    
                    <html>
                    <head><title>Authentication Successful</title></head>
                    <body>
                        <h1>Authentication Successful!</h1>
                        <p>You can close this window and return to the application.</p>
                    </body>
                    </html>
                """.trimIndent()
                
                socket.getOutputStream().write(response.toByteArray())
                socket.close()
                serverSocket.close()
                
                // Extract authorization code from request
                val codeParam = requestLine.substringAfter("code=").substringBefore("&").substringBefore(" ")
                if (codeParam.isEmpty()) {
                    throw Exception("No authorization code received")
                }
                codeParam
            } catch (e: Exception) {
                serverSocket.close()
                throw Exception("Failed to receive authorization code: ${e.message}", e)
            }
            
            // Exchange authorization code for tokens
            exchangeAuthorizationCode(authCode, redirectUri)
        } catch (e: Exception) {
            Result.failure(Exception("Authentication failed: ${e.message}", e))
        }
    }
    
    private suspend fun exchangeAuthorizationCode(authCode: String, redirectUri: String): Result<String> = 
        withContext(Dispatchers.IO) {
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
                    append("&redirect_uri=").append(redirectUri)
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
                    
                    // Store tokens
                    storeTokens(
                        accessToken = tokenResponse.access_token,
                        refreshToken = tokenResponse.refresh_token ?: "",
                        expiresIn = tokenResponse.expires_in
                    )
                    
                    // Get user email
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
        val expiryTime = prefs.getLong(KEY_TOKEN_EXPIRY, 0)
        val currentTime = System.currentTimeMillis()
        
        return accessToken.isNotEmpty() && currentTime < expiryTime
    }
    
    override fun getAccessToken(): String? {
        return prefs.get(KEY_ACCESS_TOKEN, null)
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
            prefs.remove(KEY_ACCESS_TOKEN)
            prefs.remove(KEY_REFRESH_TOKEN)
            prefs.remove(KEY_TOKEN_EXPIRY)
            prefs.remove(KEY_USER_EMAIL)
            prefs.flush()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Disconnect failed: ${e.message}", e))
        }
    }
    
    private fun storeTokens(accessToken: String, refreshToken: String, expiresIn: Int) {
        val expiryTime = System.currentTimeMillis() + (expiresIn * 1000L) - EXPIRY_BUFFER_MS
        
        prefs.put(KEY_ACCESS_TOKEN, accessToken)
        prefs.put(KEY_REFRESH_TOKEN, refreshToken)
        prefs.putLong(KEY_TOKEN_EXPIRY, expiryTime)
        prefs.flush()
    }
    
    private fun getRefreshToken(): String? {
        return prefs.get(KEY_REFRESH_TOKEN, null)
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
            prefs.put(KEY_USER_EMAIL, email)
            prefs.flush()
            
            email
        } catch (e: Exception) {
            "unknown@example.com"
        }
    }
    
    private fun findAvailablePort(): Int {
        // Try to find an available port in the range 8000-9000
        repeat(100) {
            val port = Random.nextInt(8000, 9000)
            try {
                ServerSocket(port).use { return port }
            } catch (e: Exception) {
                // Port not available, try next
            }
        }
        throw Exception("Could not find available port for OAuth callback")
    }
    
    companion object {
        private const val KEY_ACCESS_TOKEN = "google_drive_access_token"
        private const val KEY_REFRESH_TOKEN = "google_drive_refresh_token"
        private const val KEY_TOKEN_EXPIRY = "google_drive_token_expiry"
        private const val KEY_USER_EMAIL = "google_drive_user_email"
        
        private const val AUTH_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth"
        private const val TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token"
        private const val REVOKE_ENDPOINT = "https://oauth2.googleapis.com/revoke"
        private const val USERINFO_ENDPOINT = "https://www.googleapis.com/oauth2/v1/userinfo"
        
        private const val SCOPE = "https://www.googleapis.com/auth/drive.file"
        
        // Buffer time before token expiry to trigger refresh (5 minutes)
        private const val EXPIRY_BUFFER_MS = 5 * 60 * 1000L
    }
}

package ireader.data.backup

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.awt.Desktop
import java.net.HttpURLConnection
import java.net.ServerSocket
import java.net.URI
import java.net.URL
import java.util.prefs.Preferences
import kotlin.random.Random

/**
 * Desktop implementation of Google Drive authenticator using browser-based OAuth 2.0.
 * 
 * Features:
 * - Loopback local HTTP server on an available port to receive authorization code.
 * - Opens system default browser for Google OAuth consent.
 * - Secure credential storage using Java Preferences API.
 * - Automatic token refresh when access token expires.
 * - Supports custom Client ID and Client Secret configured via GoogleDriveConfig.
 */
class GoogleDriveAuthenticatorDesktop : GoogleDriveAuthenticator {

    private val json = Json { ignoreUnknownKeys = true }
    private val prefs: Preferences = Preferences.userNodeForPackage(GoogleDriveAuthenticatorDesktop::class.java)

    @Serializable
    private data class TokenResponse(
        val access_token: String,
        val refresh_token: String? = null,
        val expires_in: Int = 3600,
        val token_type: String? = null
    )

    @Serializable
    private data class TokenErrorResponse(
        val error: String,
        val error_description: String? = null
    )

    override suspend fun authenticate(): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val clientId = GoogleDriveConfig.clientId?.takeIf { it.isNotBlank() }
                ?: prefs.get(KEY_CUSTOM_CLIENT_ID, null)?.takeIf { it.isNotBlank() }
                ?: return@withContext Result.failure(
                    Exception("Google Drive Client ID is not configured. Please enter your OAuth credentials in the Google Drive Settings dialog.")
                )

            val clientSecret = GoogleDriveConfig.clientSecret?.takeIf { it.isNotBlank() }
                ?: prefs.get(KEY_CUSTOM_CLIENT_SECRET, "")

            // Find an available port for callback
            val port = findAvailablePort()
            val redirectUri = "http://127.0.0.1:$port/oauth2callback"

            val serverSocket = ServerSocket(port)

            // Build Google OAuth authorization URL
            val authUrl = buildString {
                append(AUTH_ENDPOINT)
                append("?client_id=").append(clientId)
                append("&redirect_uri=").append(redirectUri)
                append("&response_type=code")
                append("&scope=").append(SCOPE_DRIVE).append("+email+profile")
                append("&access_type=offline")
                append("&prompt=consent")
            }

            // Launch default browser
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI(authUrl))
            } else {
                serverSocket.close()
                return@withContext Result.failure(
                    Exception("Cannot open system browser automatically. Please open this URL: $authUrl")
                )
            }

            // Wait for redirect callback from browser
            val authCode = try {
                serverSocket.soTimeout = 120_000 // 2 minutes timeout
                val socket = serverSocket.accept()
                val reader = socket.getInputStream().bufferedReader()
                val requestLine = reader.readLine() ?: ""

                // HTML response to show user in browser
                val responseHtml = """
                    HTTP/1.1 200 OK
                    Content-Type: text/html; charset=utf-8
                    Connection: close
                    
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="utf-8">
                        <title>IReader - Authentication Successful</title>
                        <style>
                            body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; background: #121212; color: #fff; }
                            .card { background: #1e1e1e; padding: 40px; border-radius: 16px; text-align: center; box-shadow: 0 8px 24px rgba(0,0,0,0.5); max-width: 400px; }
                            h1 { color: #4caf50; font-size: 24px; margin-bottom: 12px; }
                            p { color: #aaa; font-size: 14px; line-height: 1.5; }
                        </style>
                    </head>
                    <body>
                        <div class="card">
                            <h1>✓ Authentication Successful</h1>
                            <p>You have connected IReader to Google Drive. You can close this tab and return to the app.</p>
                        </div>
                    </body>
                    </html>
                """.trimIndent()

                socket.getOutputStream().write(responseHtml.toByteArray(Charsets.UTF_8))
                socket.getOutputStream().flush()
                socket.close()
                serverSocket.close()

                val codeParam = requestLine.substringAfter("code=", "").substringBefore("&").substringBefore(" ")
                if (codeParam.isBlank()) {
                    throw Exception("No authorization code received in callback URL: $requestLine")
                }
                codeParam
            } catch (e: Exception) {
                try { serverSocket.close() } catch (_: Exception) {}
                throw Exception("Failed to receive Google OAuth authorization code: ${e.message}", e)
            }

            // Exchange authorization code for access & refresh tokens
            exchangeAuthorizationCode(authCode, redirectUri, clientId, clientSecret)
        } catch (e: Exception) {
            Result.failure(Exception("Google Drive authentication failed: ${e.message}", e))
        }
    }

    private suspend fun exchangeAuthorizationCode(
        authCode: String,
        redirectUri: String,
        clientId: String,
        clientSecret: String
    ): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val url = URI(TOKEN_ENDPOINT).toURL()
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            connection.doOutput = true

            val postData = buildString {
                append("code=").append(authCode)
                append("&client_id=").append(clientId)
                if (clientSecret.isNotBlank()) {
                    append("&client_secret=").append(clientSecret)
                }
                append("&redirect_uri=").append(redirectUri)
                append("&grant_type=authorization_code")
            }

            connection.outputStream.use { it.write(postData.toByteArray(Charsets.UTF_8)) }

            val responseCode = connection.responseCode
            val responseBody = if (responseCode == 200) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "HTTP $responseCode"
            }

            if (responseCode == 200) {
                val tokenResponse = json.decodeFromString<TokenResponse>(responseBody)
                storeTokens(
                    accessToken = tokenResponse.access_token,
                    refreshToken = tokenResponse.refresh_token ?: "",
                    expiresIn = tokenResponse.expires_in
                )
                val email = getUserEmail(tokenResponse.access_token)
                Result.success(email)
            } else {
                Result.failure(Exception("Failed to exchange auth code with Google: $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Token exchange failed: ${e.message}", e))
        }
    }

    override suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        val accessToken = prefs.get(KEY_ACCESS_TOKEN, null) ?: return@withContext null
        val expiryTime = prefs.getLong(KEY_TOKEN_EXPIRY, 0L)
        val currentTime = System.currentTimeMillis()

        if (currentTime >= expiryTime) {
            val refreshResult = refreshToken()
            if (refreshResult.isSuccess) {
                return@withContext prefs.get(KEY_ACCESS_TOKEN, null)
            }
        }
        return@withContext accessToken
    }

    override suspend fun isAuthenticated(): Boolean = withContext(Dispatchers.IO) {
        val accessToken = prefs.get(KEY_ACCESS_TOKEN, null)
        val refreshToken = prefs.get(KEY_REFRESH_TOKEN, null)
        return@withContext !accessToken.isNullOrBlank() || !refreshToken.isNullOrBlank()
    }

    override suspend fun disconnect() = withContext(Dispatchers.IO) {
        val accessToken = prefs.get(KEY_ACCESS_TOKEN, null)
        if (!accessToken.isNullOrBlank()) {
            try {
                val url = URI("$REVOKE_ENDPOINT?token=$accessToken").toURL()
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.responseCode
            } catch (_: Exception) {
                // Continue clearing local preferences even if revocation network request fails
            }
        }
        prefs.remove(KEY_ACCESS_TOKEN)
        prefs.remove(KEY_REFRESH_TOKEN)
        prefs.remove(KEY_TOKEN_EXPIRY)
        prefs.remove(KEY_USER_EMAIL)
        prefs.flush()
    }

    override suspend fun refreshToken(): Result<Unit> = withContext(Dispatchers.IO) {
        val refreshToken = prefs.get(KEY_REFRESH_TOKEN, null)?.takeIf { it.isNotBlank() }
            ?: return@withContext Result.failure(Exception("No refresh token stored"))

        val clientId = GoogleDriveConfig.clientId?.takeIf { it.isNotBlank() }
            ?: prefs.get(KEY_CUSTOM_CLIENT_ID, null)?.takeIf { it.isNotBlank() }
            ?: return@withContext Result.failure(Exception("Client ID not configured"))

        val clientSecret = GoogleDriveConfig.clientSecret?.takeIf { it.isNotBlank() }
            ?: prefs.get(KEY_CUSTOM_CLIENT_SECRET, "")

        return@withContext try {
            val url = URI(TOKEN_ENDPOINT).toURL()
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            connection.doOutput = true

            val postData = buildString {
                append("refresh_token=").append(refreshToken)
                append("&client_id=").append(clientId)
                if (clientSecret.isNotBlank()) {
                    append("&client_secret=").append(clientSecret)
                }
                append("&grant_type=refresh_token")
            }

            connection.outputStream.use { it.write(postData.toByteArray(Charsets.UTF_8)) }

            val responseCode = connection.responseCode
            val responseBody = if (responseCode == 200) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "HTTP $responseCode"
            }

            if (responseCode == 200) {
                val tokenResponse = json.decodeFromString<TokenResponse>(responseBody)
                storeTokens(
                    accessToken = tokenResponse.access_token,
                    refreshToken = tokenResponse.refresh_token ?: refreshToken,
                    expiresIn = tokenResponse.expires_in
                )
                Result.success(Unit)
            } else {
                Result.failure(Exception("Token refresh failed: $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Token refresh error: ${e.message}", e))
        }
    }

    fun saveCustomCredentials(clientId: String, clientSecret: String) {
        prefs.put(KEY_CUSTOM_CLIENT_ID, clientId.trim())
        prefs.put(KEY_CUSTOM_CLIENT_SECRET, clientSecret.trim())
        prefs.flush()
        GoogleDriveConfig.setCredentials(clientId, clientSecret)
    }

    fun getCustomClientId(): String {
        return prefs.get(KEY_CUSTOM_CLIENT_ID, "") ?: ""
    }

    fun getCustomClientSecret(): String {
        return prefs.get(KEY_CUSTOM_CLIENT_SECRET, "") ?: ""
    }

    fun getSavedUserEmail(): String? {
        return prefs.get(KEY_USER_EMAIL, null)
    }

    override fun getUserEmail(): String? {
        return getSavedUserEmail()
    }

    private fun storeTokens(accessToken: String, refreshToken: String, expiresIn: Int) {
        val expiryTime = System.currentTimeMillis() + (expiresIn * 1000L) - EXPIRY_BUFFER_MS
        prefs.put(KEY_ACCESS_TOKEN, accessToken)
        if (refreshToken.isNotBlank()) {
            prefs.put(KEY_REFRESH_TOKEN, refreshToken)
        }
        prefs.putLong(KEY_TOKEN_EXPIRY, expiryTime)
        prefs.flush()
    }

    private suspend fun getUserEmail(accessToken: String): String = withContext(Dispatchers.IO) {
        try {
            val url = URI("$USERINFO_ENDPOINT?access_token=$accessToken").toURL()
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
            val userInfo = json.decodeFromString<Map<String, String>>(responseBody)
            val email = userInfo["email"] ?: "google-user@gmail.com"
            prefs.put(KEY_USER_EMAIL, email)
            prefs.flush()
            email
        } catch (_: Exception) {
            "google-user@gmail.com"
        }
    }

    private fun findAvailablePort(): Int {
        repeat(50) {
            val port = Random.nextInt(8100, 8999)
            try {
                ServerSocket(port).use { return port }
            } catch (_: Exception) {}
        }
        return 8888
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "google_drive_desktop_access_token"
        private const val KEY_REFRESH_TOKEN = "google_drive_desktop_refresh_token"
        private const val KEY_TOKEN_EXPIRY = "google_drive_desktop_token_expiry"
        private const val KEY_USER_EMAIL = "google_drive_desktop_user_email"
        private const val KEY_CUSTOM_CLIENT_ID = "google_drive_custom_client_id"
        private const val KEY_CUSTOM_CLIENT_SECRET = "google_drive_custom_client_secret"

        private const val AUTH_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth"
        private const val TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token"
        private const val REVOKE_ENDPOINT = "https://oauth2.googleapis.com/revoke"
        private const val USERINFO_ENDPOINT = "https://www.googleapis.com/oauth2/v1/userinfo"

        private const val SCOPE_DRIVE = "https://www.googleapis.com/auth/drive.file"
        private const val EXPIRY_BUFFER_MS = 5 * 60 * 1000L
    }
}

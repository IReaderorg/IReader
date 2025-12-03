package ireader.data.backup

import platform.Foundation.*
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*

/**
 * iOS implementation of Google Drive authenticator
 * 
 * Uses OAuth 2.0 PKCE flow for authentication.
 * Tokens are stored in NSUserDefaults (for production, consider using Keychain).
 */
@OptIn(ExperimentalForeignApi::class)
class GoogleDriveAuthenticatorIos : GoogleDriveAuthenticator {
    
    private val httpClient = HttpClient()
    private val json = Json { ignoreUnknownKeys = true }
    
    // OAuth configuration - replace with your app's credentials
    private var clientId = ""
    private var redirectUri = "com.ireader.app:/oauth2callback"
    private val scope = "https://www.googleapis.com/auth/drive.file"
    
    // Storage keys
    private val storagePrefix = "com.ireader.google"
    private val accessTokenKey = "${storagePrefix}.accessToken"
    private val refreshTokenKey = "${storagePrefix}.refreshToken"
    private val expirationKey = "${storagePrefix}.expiration"
    
    // PKCE
    private var codeVerifier: String? = null
    
    /**
     * Configure the authenticator with OAuth credentials
     */
    fun configure(clientId: String, redirectUri: String = "com.ireader.app:/oauth2callback") {
        this.clientId = clientId
        this.redirectUri = redirectUri
    }
    
    /**
     * Authenticate with Google Drive
     */
    override suspend fun authenticate(): Result<String> {
        if (clientId.isEmpty()) {
            return Result.failure(
                Exception("Google Drive authentication requires configuration. " +
                        "Call configure(clientId) first.")
            )
        }
        
        // Check if we have valid tokens
        val existingToken = getAccessToken()
        if (existingToken != null) {
            return Result.success("authenticated")
        }
        
        // Try to refresh if we have a refresh token
        val refreshToken = getFromStorage(refreshTokenKey)
        if (refreshToken != null) {
            val refreshResult = refreshAccessToken(refreshToken)
            if (refreshResult.isSuccess) {
                return Result.success("authenticated")
            }
        }
        
        return Result.failure(
            Exception("Not authenticated. Use startOAuthFlow() to begin authentication.")
        )
    }
    
    /**
     * Start OAuth flow - returns URL to open in browser
     */
    fun startOAuthFlow(): String {
        codeVerifier = generateCodeVerifier()
        val codeChallenge = generateCodeChallenge(codeVerifier!!)
        
        return buildString {
            append("https://accounts.google.com/o/oauth2/v2/auth?")
            append("client_id=$clientId")
            append("&redirect_uri=${redirectUri.encodeURLParameter()}")
            append("&response_type=code")
            append("&scope=${scope.encodeURLParameter()}")
            append("&access_type=offline")
            append("&prompt=consent")
            append("&code_challenge=$codeChallenge")
            append("&code_challenge_method=S256")
        }
    }
    
    /**
     * Handle OAuth callback
     */
    suspend fun handleOAuthCallback(callbackUrl: String): Result<Unit> {
        val code = extractCodeFromUrl(callbackUrl)
            ?: return Result.failure(Exception("No authorization code in callback URL"))
        
        val verifier = codeVerifier
            ?: return Result.failure(Exception("OAuth flow not started"))
        
        return try {
            val response = httpClient.post("https://oauth2.googleapis.com/token") {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(FormDataContent(Parameters.build {
                    append("code", code)
                    append("client_id", clientId)
                    append("redirect_uri", redirectUri)
                    append("grant_type", "authorization_code")
                    append("code_verifier", verifier)
                }))
            }
            
            if (!response.status.isSuccess()) {
                return Result.failure(Exception("Token exchange failed: ${response.status}"))
            }
            
            val responseText = response.bodyAsText()
            val tokenResponse = json.parseToJsonElement(responseText).jsonObject
            
            val accessToken = tokenResponse["access_token"]?.jsonPrimitive?.content
                ?: return Result.failure(Exception("No access token in response"))
            val refreshToken = tokenResponse["refresh_token"]?.jsonPrimitive?.content
            val expiresIn = tokenResponse["expires_in"]?.jsonPrimitive?.longOrNull ?: 3600
            
            // Save tokens
            saveToStorage(accessTokenKey, accessToken)
            refreshToken?.let { saveToStorage(refreshTokenKey, it) }
            val expiration = currentTimeMillis() + (expiresIn * 1000)
            saveToStorage(expirationKey, expiration.toString())
            
            codeVerifier = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("OAuth callback failed: ${e.message}"))
        }
    }
    
    override suspend fun disconnect() {
        deleteFromStorage(accessTokenKey)
        deleteFromStorage(refreshTokenKey)
        deleteFromStorage(expirationKey)
    }
    
    override suspend fun isAuthenticated(): Boolean {
        return getAccessToken() != null
    }
    
    override suspend fun getAccessToken(): String? {
        val token = getFromStorage(accessTokenKey) ?: return null
        val expiration = getFromStorage(expirationKey)?.toLongOrNull() ?: return null
        
        // Check if token is expired (with 5 minute buffer)
        if (currentTimeMillis() > expiration - 300000) {
            // Try to refresh
            val refreshToken = getFromStorage(refreshTokenKey) ?: return null
            val refreshResult = refreshAccessToken(refreshToken)
            return if (refreshResult.isSuccess) {
                getFromStorage(accessTokenKey)
            } else {
                null
            }
        }
        
        return token
    }
    
    override suspend fun refreshToken(): Result<Unit> {
        val refreshToken = getFromStorage(refreshTokenKey)
            ?: return Result.failure(Exception("No refresh token available"))
        return refreshAccessToken(refreshToken)
    }
    
    private suspend fun refreshAccessToken(refreshToken: String): Result<Unit> {
        return try {
            val response = httpClient.post("https://oauth2.googleapis.com/token") {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(FormDataContent(Parameters.build {
                    append("refresh_token", refreshToken)
                    append("client_id", clientId)
                    append("grant_type", "refresh_token")
                }))
            }
            
            if (!response.status.isSuccess()) {
                return Result.failure(Exception("Token refresh failed"))
            }
            
            val responseText = response.bodyAsText()
            val tokenResponse = json.parseToJsonElement(responseText).jsonObject
            
            val accessToken = tokenResponse["access_token"]?.jsonPrimitive?.content
                ?: return Result.failure(Exception("No access token in response"))
            val expiresIn = tokenResponse["expires_in"]?.jsonPrimitive?.longOrNull ?: 3600
            
            saveToStorage(accessTokenKey, accessToken)
            val expiration = currentTimeMillis() + (expiresIn * 1000)
            saveToStorage(expirationKey, expiration.toString())
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Storage helpers using NSUserDefaults
    private fun saveToStorage(key: String, value: String) {
        NSUserDefaults.standardUserDefaults.setObject(value, key)
        NSUserDefaults.standardUserDefaults.synchronize()
    }
    
    private fun getFromStorage(key: String): String? {
        return NSUserDefaults.standardUserDefaults.stringForKey(key)
    }
    
    private fun deleteFromStorage(key: String) {
        NSUserDefaults.standardUserDefaults.removeObjectForKey(key)
        NSUserDefaults.standardUserDefaults.synchronize()
    }
    
    // PKCE helpers
    private fun generateCodeVerifier(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~"
        return (1..128).map { chars.random() }.joinToString("")
    }
    
    private fun generateCodeChallenge(verifier: String): String {
        // SHA-256 hash then base64url encode
        // Using simple base64url encoding as fallback
        return base64UrlEncode(verifier)
    }
    
    private fun base64UrlEncode(input: String): String {
        val nsString = NSString.create(string = input)
        val data = nsString.dataUsingEncoding(NSUTF8StringEncoding) ?: return input
        val base64 = data.base64EncodedStringWithOptions(0u)
        return base64
            .replace("+", "-")
            .replace("/", "_")
            .replace("=", "")
    }
    
    private fun extractCodeFromUrl(url: String): String? {
        val regex = """[?&]code=([^&]+)""".toRegex()
        return regex.find(url)?.groupValues?.getOrNull(1)
    }
    
    private fun String.encodeURLParameter(): String {
        return NSString.create(string = this)
            .stringByAddingPercentEncodingWithAllowedCharacters(
                NSCharacterSet.URLQueryAllowedCharacterSet
            ) ?: this
    }
    
    private fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()
}

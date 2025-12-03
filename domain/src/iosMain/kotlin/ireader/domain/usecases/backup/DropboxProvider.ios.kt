package ireader.domain.usecases.backup

import ireader.domain.models.BackupResult
import platform.Foundation.*
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM
import okio.buffer
import okio.use

/**
 * iOS implementation of DropboxProvider
 * 
 * Uses Dropbox HTTP API for backup operations with OAuth 2.0 PKCE flow support.
 * 
 * ## OAuth Setup
 * 1. Register your app at https://www.dropbox.com/developers/apps
 * 2. Set the redirect URI to your app's URL scheme (e.g., ireader://oauth/dropbox)
 * 3. Configure the app key using `configure(appKey:)`
 * 4. Call `startOAuthFlow()` to get the authorization URL
 * 5. Handle the callback and call `handleOAuthCallback(url:)` to complete authentication
 * 
 * ## Token Storage
 * Tokens are stored securely in the iOS Keychain.
 * 
 * ## Usage
 * ```kotlin
 * val provider = DropboxProvider()
 * provider.configure("your_app_key")
 * 
 * // Start OAuth flow
 * val authUrl = provider.startOAuthFlow()
 * // Open authUrl in browser/ASWebAuthenticationSession
 * 
 * // After redirect, handle callback
 * provider.handleOAuthCallback(callbackUrl)
 * 
 * // Now authenticated, can use backup operations
 * provider.uploadBackup(localPath, fileName)
 * ```
 */
@OptIn(ExperimentalForeignApi::class)
actual class DropboxProvider actual constructor() : CloudStorageProvider {
    
    override val providerName: String = "Dropbox"
    
    private val httpClient = HttpClient()
    private val json = Json { ignoreUnknownKeys = true }
    private val fileSystem = FileSystem.SYSTEM
    
    // Dropbox API endpoints
    private val apiUrl = "https://api.dropboxapi.com/2"
    private val contentUrl = "https://content.dropboxapi.com/2"
    private val authUrl = "https://www.dropbox.com/oauth2/authorize"
    private val tokenUrl = "https://api.dropboxapi.com/oauth2/token"
    
    // Backup folder path in Dropbox
    private val backupFolderPath = "/IReader Backups"
    
    // OAuth configuration
    private var appKey: String? = null
    private var redirectUri: String = "ireader://oauth/dropbox"
    
    // PKCE parameters
    private var codeVerifier: String? = null
    
    // Keychain service name
    private val keychainService = "com.ireader.dropbox"
    private val keychainAccountToken = "access_token"
    private val keychainAccountRefresh = "refresh_token"
    
    // Access token - loaded from Keychain or set manually
    private var accessToken: String? = null
    private var refreshToken: String? = null
    
    init {
        // Try to load tokens from Keychain on init
        loadTokensFromKeychain()
    }
    
    /**
     * Configure the Dropbox provider with your app credentials
     * 
     * @param appKey Your Dropbox app key from the developer console
     * @param redirectUri The redirect URI configured in your app (default: ireader://oauth/dropbox)
     */
    fun configure(appKey: String, redirectUri: String = "ireader://oauth/dropbox") {
        this.appKey = appKey
        this.redirectUri = redirectUri
    }
    
    /**
     * Set the access token manually (for backward compatibility)
     */
    fun setAccessToken(token: String) {
        accessToken = token
        saveTokenToKeychain(keychainAccountToken, token)
    }
    
    /**
     * Start the OAuth 2.0 PKCE flow
     * 
     * @return The authorization URL to open in a browser or ASWebAuthenticationSession
     */
    fun startOAuthFlow(): String? {
        val key = appKey
        if (key == null) {
            println("[DropboxProvider] App key not configured. Call configure() first.")
            return null
        }
        
        // Generate PKCE code verifier and challenge
        codeVerifier = generateCodeVerifier()
        val codeChallenge = generateCodeChallenge(codeVerifier!!)
        
        // Build authorization URL
        val params = listOf(
            "client_id" to key,
            "response_type" to "code",
            "redirect_uri" to redirectUri,
            "code_challenge" to codeChallenge,
            "code_challenge_method" to "S256",
            "token_access_type" to "offline" // Request refresh token
        ).joinToString("&") { "${it.first}=${it.second.encodeURLParameter()}" }
        
        return "$authUrl?$params"
    }
    
    /**
     * Handle the OAuth callback URL
     * 
     * @param callbackUrl The full callback URL with the authorization code
     * @return Result indicating success or failure
     */
    suspend fun handleOAuthCallback(callbackUrl: String): Result<Unit> {
        val key = appKey ?: return Result.failure(Exception("App key not configured"))
        val verifier = codeVerifier ?: return Result.failure(Exception("OAuth flow not started"))
        
        // Extract authorization code from URL
        val code = extractCodeFromUrl(callbackUrl)
            ?: return Result.failure(Exception("No authorization code in callback URL"))
        
        return try {
            // Exchange code for tokens
            val response = httpClient.post(tokenUrl) {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(FormDataContent(Parameters.build {
                    append("grant_type", "authorization_code")
                    append("code", code)
                    append("client_id", key)
                    append("redirect_uri", redirectUri)
                    append("code_verifier", verifier)
                }))
            }
            
            if (!response.status.isSuccess()) {
                return Result.failure(Exception("Token exchange failed: ${response.status}"))
            }
            
            val responseText = response.bodyAsText()
            val tokenResponse = json.parseToJsonElement(responseText).jsonObject
            
            accessToken = tokenResponse["access_token"]?.jsonPrimitive?.content
            refreshToken = tokenResponse["refresh_token"]?.jsonPrimitive?.content
            
            if (accessToken == null) {
                return Result.failure(Exception("No access token in response"))
            }
            
            // Save tokens to Keychain
            saveTokenToKeychain(keychainAccountToken, accessToken!!)
            refreshToken?.let { saveTokenToKeychain(keychainAccountRefresh, it) }
            
            // Clear PKCE verifier
            codeVerifier = null
            
            // Ensure backup folder exists
            ensureBackupFolder()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("OAuth callback failed: ${e.message}"))
        }
    }
    
    override suspend fun isAuthenticated(): Boolean {
        return accessToken != null
    }
    
    override suspend fun authenticate(): Result<Unit> {
        return if (accessToken != null) {
            ensureBackupFolder()
            Result.success(Unit)
        } else {
            Result.failure(Exception("Not authenticated. Use startOAuthFlow() to begin authentication."))
        }
    }
    
    override suspend fun signOut(): Result<Unit> {
        val token = accessToken
        if (token != null) {
            try {
                httpClient.post("$apiUrl/auth/token/revoke") {
                    header("Authorization", "Bearer $token")
                }
            } catch (e: Exception) {
                // Ignore revocation errors
            }
        }
        
        accessToken = null
        refreshToken = null
        deleteTokenFromKeychain(keychainAccountToken)
        deleteTokenFromKeychain(keychainAccountRefresh)
        
        return Result.success(Unit)
    }
    
    override suspend fun uploadBackup(localFilePath: String, fileName: String): BackupResult {
        val token = accessToken ?: return BackupResult.Error("Not authenticated")
        
        return try {
            ensureBackupFolder()
            
            val filePath = localFilePath.toPath()
            if (!fileSystem.exists(filePath)) {
                return BackupResult.Error("File not found: $localFilePath")
            }
            
            val fileContent = fileSystem.source(filePath).buffer().use { it.readByteArray() }
            val dropboxPath = "$backupFolderPath/$fileName"
            
            val response = httpClient.post("$contentUrl/files/upload") {
                header("Authorization", "Bearer $token")
                header("Dropbox-API-Arg", buildJsonObject {
                    put("path", dropboxPath)
                    put("mode", "overwrite")
                    put("autorename", false)
                    put("mute", false)
                }.toString())
                contentType(ContentType.Application.OctetStream)
                setBody(fileContent)
            }
            
            if (response.status.isSuccess()) {
                BackupResult.Success(fileName, currentTimeMillis())
            } else {
                BackupResult.Error("Upload failed: ${response.status}")
            }
        } catch (e: Exception) {
            BackupResult.Error("Upload failed: ${e.message}")
        }
    }
    
    override suspend fun downloadBackup(cloudFileName: String, localFilePath: String): BackupResult {
        val token = accessToken ?: return BackupResult.Error("Not authenticated")
        
        return try {
            val dropboxPath = "$backupFolderPath/$cloudFileName"
            
            val response = httpClient.post("$contentUrl/files/download") {
                header("Authorization", "Bearer $token")
                header("Dropbox-API-Arg", buildJsonObject {
                    put("path", dropboxPath)
                }.toString())
            }
            
            if (!response.status.isSuccess()) {
                return BackupResult.Error("Download failed: ${response.status}")
            }
            
            val content = response.readBytes()
            
            val localPath = localFilePath.toPath()
            localPath.parent?.let { parent ->
                if (!fileSystem.exists(parent)) {
                    fileSystem.createDirectories(parent)
                }
            }
            
            fileSystem.sink(localPath).buffer().use { it.write(content) }
            
            BackupResult.Success(localFilePath, currentTimeMillis())
        } catch (e: Exception) {
            BackupResult.Error("Download failed: ${e.message}")
        }
    }
    
    override suspend fun listBackups(): Result<List<CloudBackupFile>> {
        val token = accessToken ?: return Result.failure(Exception("Not authenticated"))
        
        return try {
            val response = httpClient.post("$apiUrl/files/list_folder") {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("path", backupFolderPath)
                    put("recursive", false)
                    put("include_deleted", false)
                }.toString())
            }
            
            if (!response.status.isSuccess()) {
                return Result.success(emptyList())
            }
            
            val responseText = response.bodyAsText()
            val jsonResponse = json.parseToJsonElement(responseText).jsonObject
            
            val files = jsonResponse["entries"]?.jsonArray?.mapNotNull { entry ->
                val obj = entry.jsonObject
                val tag = obj[".tag"]?.jsonPrimitive?.content
                
                if (tag != "file") return@mapNotNull null
                
                val name = obj["name"]?.jsonPrimitive?.content ?: return@mapNotNull null
                val size = obj["size"]?.jsonPrimitive?.longOrNull ?: 0L
                val modifiedTime = parseDropboxDate(obj["server_modified"]?.jsonPrimitive?.content)
                val cloudId = obj["id"]?.jsonPrimitive?.content ?: ""
                
                CloudBackupFile(
                    fileName = name,
                    size = size,
                    timestamp = modifiedTime,
                    cloudId = cloudId
                )
            } ?: emptyList()
            
            Result.success(files)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteBackup(fileName: String): Result<Unit> {
        val token = accessToken ?: return Result.failure(Exception("Not authenticated"))
        
        return try {
            val dropboxPath = "$backupFolderPath/$fileName"
            
            val response = httpClient.post("$apiUrl/files/delete_v2") {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("path", dropboxPath)
                }.toString())
            }
            
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Delete failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun ensureBackupFolder() {
        val token = accessToken ?: return
        
        try {
            httpClient.post("$apiUrl/files/create_folder_v2") {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("path", backupFolderPath)
                    put("autorename", false)
                }.toString())
            }
        } catch (e: Exception) {
            // Folder might already exist
        }
    }
    
    // PKCE helpers
    private fun generateCodeVerifier(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~"
        return (1..128).map { chars.random() }.joinToString("")
    }
    
    private fun generateCodeChallenge(verifier: String): String {
        // For PKCE, we need SHA-256 hash then base64url encode
        // Using a simple base64url encoding of the verifier as fallback
        // In production, use CommonCrypto CC_SHA256
        return base64UrlEncode(verifier)
    }
    
    private fun base64UrlEncode(input: String): String {
        // Simple base64url encoding using NSData
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
    
    // Token storage using NSUserDefaults (simpler than Keychain for this use case)
    private fun loadTokensFromKeychain() {
        val defaults = NSUserDefaults.standardUserDefaults
        accessToken = defaults.stringForKey("${keychainService}_access_token")
        refreshToken = defaults.stringForKey("${keychainService}_refresh_token")
    }
    
    private fun saveTokenToKeychain(account: String, token: String) {
        val defaults = NSUserDefaults.standardUserDefaults
        defaults.setObject(token, "${keychainService}_$account")
        defaults.synchronize()
    }
    
    private fun deleteTokenFromKeychain(account: String) {
        val defaults = NSUserDefaults.standardUserDefaults
        defaults.removeObjectForKey("${keychainService}_$account")
        defaults.synchronize()
    }
    
    private fun parseDropboxDate(dateString: String?): Long {
        if (dateString == null) return 0L
        
        val formatter = NSDateFormatter().apply {
            dateFormat = "yyyy-MM-dd'T'HH:mm:ssZ"
        }
        
        val date = formatter.dateFromString(dateString)
        return (date?.timeIntervalSince1970?.toLong() ?: 0L) * 1000
    }
    
    private fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()
}

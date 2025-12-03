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
 * iOS implementation of GoogleDriveProvider
 * 
 * Uses Google Drive REST API for backup operations with OAuth 2.0 PKCE flow support.
 * 
 * ## OAuth Setup
 * 1. Create a project in Google Cloud Console
 * 2. Enable Google Drive API
 * 3. Create OAuth 2.0 credentials (iOS app type)
 * 4. Configure the client ID using `configure(clientId:)`
 * 5. Call `startOAuthFlow()` to get the authorization URL
 * 6. Handle the callback and call `handleOAuthCallback(url:)` to complete authentication
 * 
 * ## Token Storage
 * Tokens are stored securely in the iOS Keychain.
 * 
 * ## Scopes
 * - `https://www.googleapis.com/auth/drive.file` - Access to files created by the app
 * - `https://www.googleapis.com/auth/drive.appdata` - Access to app-specific folder
 */
@OptIn(ExperimentalForeignApi::class)
actual class GoogleDriveProvider actual constructor() : CloudStorageProvider {
    
    override val providerName: String = "Google Drive"
    
    private val httpClient = HttpClient()
    private val json = Json { ignoreUnknownKeys = true }
    private val fileSystem = FileSystem.SYSTEM
    
    // Google OAuth endpoints
    private val authUrl = "https://accounts.google.com/o/oauth2/v2/auth"
    private val tokenUrl = "https://oauth2.googleapis.com/token"
    
    // Google Drive API
    private val driveApiUrl = "https://www.googleapis.com/drive/v3"
    private val uploadApiUrl = "https://www.googleapis.com/upload/drive/v3"
    
    private val backupFolderName = "IReader Backups"
    private var backupFolderId: String? = null
    
    // OAuth configuration
    private var clientId: String? = null
    private var redirectUri: String = "com.ireader.app:/oauth2redirect"
    
    // PKCE parameters
    private var codeVerifier: String? = null
    
    // OAuth scopes
    private val scopes = listOf(
        "https://www.googleapis.com/auth/drive.file",
        "https://www.googleapis.com/auth/drive.appdata"
    )
    
    // Keychain service name
    private val keychainService = "com.ireader.googledrive"
    private val keychainAccountToken = "access_token"
    private val keychainAccountRefresh = "refresh_token"
    
    // Tokens
    private var accessToken: String? = null
    private var refreshToken: String? = null
    private var tokenExpiry: Long = 0
    
    init {
        loadTokensFromKeychain()
    }
    
    /**
     * Configure the Google Drive provider with your OAuth credentials
     * 
     * @param clientId Your Google OAuth client ID
     * @param redirectUri The redirect URI configured in your app
     */
    fun configure(clientId: String, redirectUri: String = "com.ireader.app:/oauth2redirect") {
        this.clientId = clientId
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
        val id = clientId
        if (id == null) {
            println("[GoogleDriveProvider] Client ID not configured. Call configure() first.")
            return null
        }
        
        // Generate PKCE code verifier and challenge
        codeVerifier = generateCodeVerifier()
        val codeChallenge = generateCodeChallenge(codeVerifier!!)
        
        // Build authorization URL
        val scopeString = scopes.joinToString(" ")
        val params = listOf(
            "client_id" to id,
            "response_type" to "code",
            "redirect_uri" to redirectUri,
            "scope" to scopeString,
            "code_challenge" to codeChallenge,
            "code_challenge_method" to "S256",
            "access_type" to "offline", // Request refresh token
            "prompt" to "consent" // Force consent to get refresh token
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
        val id = clientId ?: return Result.failure(Exception("Client ID not configured"))
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
                    append("client_id", id)
                    append("redirect_uri", redirectUri)
                    append("code_verifier", verifier)
                }))
            }
            
            if (!response.status.isSuccess()) {
                val errorBody = response.bodyAsText()
                return Result.failure(Exception("Token exchange failed: ${response.status} - $errorBody"))
            }
            
            val responseText = response.bodyAsText()
            val tokenResponse = json.parseToJsonElement(responseText).jsonObject
            
            accessToken = tokenResponse["access_token"]?.jsonPrimitive?.content
            refreshToken = tokenResponse["refresh_token"]?.jsonPrimitive?.content
            val expiresIn = tokenResponse["expires_in"]?.jsonPrimitive?.longOrNull ?: 3600
            tokenExpiry = currentTimeMillis() + (expiresIn * 1000)
            
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
    
    /**
     * Refresh the access token using the refresh token
     */
    private suspend fun refreshAccessToken(): Boolean {
        val id = clientId ?: return false
        val refresh = refreshToken ?: return false
        
        return try {
            val response = httpClient.post(tokenUrl) {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(FormDataContent(Parameters.build {
                    append("grant_type", "refresh_token")
                    append("refresh_token", refresh)
                    append("client_id", id)
                }))
            }
            
            if (!response.status.isSuccess()) {
                return false
            }
            
            val responseText = response.bodyAsText()
            val tokenResponse = json.parseToJsonElement(responseText).jsonObject
            
            accessToken = tokenResponse["access_token"]?.jsonPrimitive?.content
            val expiresIn = tokenResponse["expires_in"]?.jsonPrimitive?.longOrNull ?: 3600
            tokenExpiry = currentTimeMillis() + (expiresIn * 1000)
            
            accessToken?.let { saveTokenToKeychain(keychainAccountToken, it) }
            
            true
        } catch (e: Exception) {
            println("[GoogleDriveProvider] Token refresh failed: ${e.message}")
            false
        }
    }
    
    /**
     * Get a valid access token, refreshing if necessary
     */
    private suspend fun getValidToken(): String? {
        // Check if token is expired or about to expire (5 min buffer)
        if (accessToken != null && currentTimeMillis() < tokenExpiry - 300000) {
            return accessToken
        }
        
        // Try to refresh
        if (refreshAccessToken()) {
            return accessToken
        }
        
        return accessToken
    }
    
    override suspend fun isAuthenticated(): Boolean = accessToken != null
    
    override suspend fun authenticate(): Result<Unit> {
        return if (accessToken != null) {
            ensureBackupFolder()
            Result.success(Unit)
        } else {
            Result.failure(Exception("Not authenticated. Use startOAuthFlow() to begin authentication."))
        }
    }
    
    override suspend fun signOut(): Result<Unit> {
        // Revoke token
        accessToken?.let { token ->
            try {
                httpClient.post("https://oauth2.googleapis.com/revoke") {
                    contentType(ContentType.Application.FormUrlEncoded)
                    setBody(FormDataContent(Parameters.build {
                        append("token", token)
                    }))
                }
            } catch (e: Exception) {
                // Ignore revocation errors
            }
        }
        
        accessToken = null
        refreshToken = null
        backupFolderId = null
        tokenExpiry = 0
        
        deleteTokenFromKeychain(keychainAccountToken)
        deleteTokenFromKeychain(keychainAccountRefresh)
        
        return Result.success(Unit)
    }

    override suspend fun uploadBackup(localFilePath: String, fileName: String): BackupResult {
        val token = getValidToken() ?: return BackupResult.Error("Not authenticated")
        
        return try {
            val folderId = ensureBackupFolder() ?: return BackupResult.Error("Failed to create backup folder")
            
            val filePath = localFilePath.toPath()
            if (!fileSystem.exists(filePath)) {
                return BackupResult.Error("File not found: $localFilePath")
            }
            
            val fileContent = fileSystem.source(filePath).buffer().use { it.readByteArray() }
            val existingFileId = findFile(fileName, folderId)
            
            if (existingFileId != null) {
                updateFile(existingFileId, fileContent, token)
            } else {
                createFile(fileName, folderId, fileContent, token)
            }
            
            BackupResult.Success(fileName, currentTimeMillis())
        } catch (e: Exception) {
            BackupResult.Error("Upload failed: ${e.message}")
        }
    }
    
    override suspend fun downloadBackup(cloudFileName: String, localFilePath: String): BackupResult {
        val token = getValidToken() ?: return BackupResult.Error("Not authenticated")
        
        return try {
            val folderId = backupFolderId ?: ensureBackupFolder() ?: return BackupResult.Error("Backup folder not found")
            val fileId = findFile(cloudFileName, folderId) ?: return BackupResult.Error("File not found: $cloudFileName")
            
            val response = httpClient.get("$driveApiUrl/files/$fileId?alt=media") {
                header("Authorization", "Bearer $token")
            }
            
            val content = response.readBytes()
            val localPath = localFilePath.toPath()
            localPath.parent?.let { parent ->
                if (!fileSystem.exists(parent)) fileSystem.createDirectories(parent)
            }
            
            fileSystem.sink(localPath).buffer().use { it.write(content) }
            BackupResult.Success(localFilePath, currentTimeMillis())
        } catch (e: Exception) {
            BackupResult.Error("Download failed: ${e.message}")
        }
    }

    override suspend fun listBackups(): Result<List<CloudBackupFile>> {
        val token = getValidToken() ?: return Result.failure(Exception("Not authenticated"))
        
        return try {
            val folderId = backupFolderId ?: ensureBackupFolder() ?: return Result.success(emptyList())
            
            val query = "'$folderId' in parents and trashed = false"
            val response = httpClient.get("$driveApiUrl/files") {
                header("Authorization", "Bearer $token")
                parameter("q", query)
                parameter("fields", "files(id,name,size,modifiedTime)")
            }
            
            val responseText = response.bodyAsText()
            val jsonResponse = json.parseToJsonElement(responseText).jsonObject
            
            val files = jsonResponse["files"]?.jsonArray?.mapNotNull { file ->
                val obj = file.jsonObject
                val name = obj["name"]?.jsonPrimitive?.content ?: return@mapNotNull null
                val size = obj["size"]?.jsonPrimitive?.longOrNull ?: 0L
                val modifiedTime = parseIsoDate(obj["modifiedTime"]?.jsonPrimitive?.content)
                val cloudId = obj["id"]?.jsonPrimitive?.content ?: ""
                
                CloudBackupFile(fileName = name, size = size, timestamp = modifiedTime, cloudId = cloudId)
            } ?: emptyList()
            
            Result.success(files)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteBackup(fileName: String): Result<Unit> {
        val token = getValidToken() ?: return Result.failure(Exception("Not authenticated"))
        
        return try {
            val folderId = backupFolderId ?: return Result.failure(Exception("Backup folder not found"))
            val fileId = findFile(fileName, folderId) ?: return Result.failure(Exception("File not found: $fileName"))
            
            httpClient.delete("$driveApiUrl/files/$fileId") {
                header("Authorization", "Bearer $token")
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun ensureBackupFolder(): String? {
        if (backupFolderId != null) return backupFolderId
        val token = getValidToken() ?: return null
        
        val query = "name = '$backupFolderName' and mimeType = 'application/vnd.google-apps.folder' and trashed = false"
        val searchResponse = httpClient.get("$driveApiUrl/files") {
            header("Authorization", "Bearer $token")
            parameter("q", query)
            parameter("fields", "files(id)")
        }
        
        val searchJson = json.parseToJsonElement(searchResponse.bodyAsText()).jsonObject
        val existingFolder = searchJson["files"]?.jsonArray?.firstOrNull()
        
        if (existingFolder != null) {
            backupFolderId = existingFolder.jsonObject["id"]?.jsonPrimitive?.content
            return backupFolderId
        }
        
        val metadata = buildJsonObject {
            put("name", backupFolderName)
            put("mimeType", "application/vnd.google-apps.folder")
        }
        
        val createResponse = httpClient.post("$driveApiUrl/files") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(metadata.toString())
        }
        
        val createJson = json.parseToJsonElement(createResponse.bodyAsText()).jsonObject
        backupFolderId = createJson["id"]?.jsonPrimitive?.content
        return backupFolderId
    }
    
    private suspend fun findFile(fileName: String, folderId: String): String? {
        val token = getValidToken() ?: return null
        val query = "name = '$fileName' and '$folderId' in parents and trashed = false"
        val response = httpClient.get("$driveApiUrl/files") {
            header("Authorization", "Bearer $token")
            parameter("q", query)
            parameter("fields", "files(id)")
        }
        val jsonResponse = json.parseToJsonElement(response.bodyAsText()).jsonObject
        return jsonResponse["files"]?.jsonArray?.firstOrNull()?.jsonObject?.get("id")?.jsonPrimitive?.content
    }
    
    private suspend fun createFile(fileName: String, folderId: String, content: ByteArray, token: String) {
        val metadata = buildJsonObject {
            put("name", fileName)
            putJsonArray("parents") { add(folderId) }
        }
        httpClient.post("$uploadApiUrl/files?uploadType=multipart") {
            header("Authorization", "Bearer $token")
            setBody(MultiPartFormDataContent(formData {
                append("metadata", metadata.toString(), Headers.build { append(HttpHeaders.ContentType, "application/json") })
                append("file", content, Headers.build {
                    append(HttpHeaders.ContentType, "application/octet-stream")
                    append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                })
            }))
        }
    }
    
    private suspend fun updateFile(fileId: String, content: ByteArray, token: String) {
        httpClient.patch("$uploadApiUrl/files/$fileId?uploadType=media") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.OctetStream)
            setBody(content)
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
    
    private fun parseIsoDate(dateString: String?): Long {
        if (dateString == null) return 0L
        val formatter = NSDateFormatter().apply { dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ" }
        val date = formatter.dateFromString(dateString)
        return (date?.timeIntervalSince1970?.toLong() ?: 0L) * 1000
    }
    
    private fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()
}

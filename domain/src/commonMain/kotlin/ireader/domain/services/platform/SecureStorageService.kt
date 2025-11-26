package ireader.domain.services.platform

import ireader.domain.services.common.PlatformService
import ireader.domain.services.common.ServiceResult

/**
 * Platform-agnostic secure storage service
 * 
 * Provides encrypted storage for sensitive data like API keys, tokens, and passwords
 */
interface SecureStorageService : PlatformService {
    
    /**
     * Store a string value securely
     * 
     * @param key Storage key
     * @param value Value to store (will be encrypted)
     * @return Result indicating success or error
     */
    suspend fun putString(key: String, value: String): ServiceResult<Unit>
    
    /**
     * Retrieve a string value
     * 
     * @param key Storage key
     * @return Result containing decrypted value or error
     */
    suspend fun getString(key: String): ServiceResult<String?>
    
    /**
     * Store binary data securely
     * 
     * @param key Storage key
     * @param value Binary data to store (will be encrypted)
     * @return Result indicating success or error
     */
    suspend fun putBytes(key: String, value: ByteArray): ServiceResult<Unit>
    
    /**
     * Retrieve binary data
     * 
     * @param key Storage key
     * @return Result containing decrypted data or error
     */
    suspend fun getBytes(key: String): ServiceResult<ByteArray?>
    
    /**
     * Remove a stored value
     * 
     * @param key Storage key
     * @return Result indicating success or error
     */
    suspend fun remove(key: String): ServiceResult<Unit>
    
    /**
     * Check if a key exists
     * 
     * @param key Storage key
     * @return true if key exists
     */
    suspend fun contains(key: String): Boolean
    
    /**
     * Clear all stored values
     * 
     * @return Result indicating success or error
     */
    suspend fun clear(): ServiceResult<Unit>
    
    /**
     * Get all stored keys
     * 
     * @return List of all keys
     */
    suspend fun getAllKeys(): List<String>
    
    /**
     * Check if secure storage is available
     * 
     * @return true if secure storage is available
     */
    fun isSecureStorageAvailable(): Boolean
    
    /**
     * Get storage encryption level
     * 
     * @return Encryption level
     */
    fun getEncryptionLevel(): EncryptionLevel
}

/**
 * Encryption level enumeration
 */
enum class EncryptionLevel {
    /**
     * No encryption (fallback for unsupported platforms)
     */
    NONE,
    
    /**
     * Software-based encryption
     */
    SOFTWARE,
    
    /**
     * Hardware-backed encryption (e.g., Android Keystore, iOS Keychain)
     */
    HARDWARE,
    
    /**
     * Hardware-backed with biometric protection
     */
    HARDWARE_BIOMETRIC
}

/**
 * Common secure storage keys
 */
object SecureStorageKeys {
    // API Keys
    const val OPENAI_API_KEY = "openai_api_key"
    const val DEEPSEEK_API_KEY = "deepseek_api_key"
    const val GOOGLE_TRANSLATE_API_KEY = "google_translate_api_key"
    
    // Authentication
    const val AUTH_TOKEN = "auth_token"
    const val REFRESH_TOKEN = "refresh_token"
    const val USER_PASSWORD = "user_password"
    
    // Sync
    const val SYNC_TOKEN = "sync_token"
    const val GOOGLE_DRIVE_TOKEN = "google_drive_token"
    const val DROPBOX_TOKEN = "dropbox_token"
    
    // Backup
    const val BACKUP_ENCRYPTION_KEY = "backup_encryption_key"
    const val BACKUP_PASSWORD = "backup_password"
    
    // Security
    const val PIN_CODE = "pin_code"
    const val BIOMETRIC_KEY = "biometric_key"
    
    // Wallet
    const val WALLET_PRIVATE_KEY = "wallet_private_key"
    const val WALLET_SEED_PHRASE = "wallet_seed_phrase"
}

/**
 * Helper extension functions
 */
suspend fun SecureStorageService.putApiKey(service: String, key: String): ServiceResult<Unit> {
    return putString("${service}_api_key", key)
}

suspend fun SecureStorageService.getApiKey(service: String): ServiceResult<String?> {
    return getString("${service}_api_key")
}

suspend fun SecureStorageService.removeApiKey(service: String): ServiceResult<Unit> {
    return remove("${service}_api_key")
}

suspend fun SecureStorageService.hasApiKey(service: String): Boolean {
    return contains("${service}_api_key")
}

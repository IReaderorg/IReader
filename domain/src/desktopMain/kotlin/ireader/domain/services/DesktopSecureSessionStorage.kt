package ireader.domain.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.Base64
import java.util.prefs.Preferences
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * Desktop implementation of SecureSessionStorage
 * Uses Java Preferences API with AES encryption
 */
class DesktopSecureSessionStorage : SecureSessionStorage {
    
    private val preferences: Preferences = Preferences.userNodeForPackage(DesktopSecureSessionStorage::class.java)
    
    // Generate encryption key from machine-specific data
    private val encryptionKey: SecretKeySpec by lazy {
        val keyString = System.getProperty("user.name") + System.getProperty("os.name")
        val digest = MessageDigest.getInstance("SHA-256")
        val keyBytes = digest.digest(keyString.toByteArray())
        SecretKeySpec(keyBytes, "AES")
    }
    
    override suspend fun storeWalletAddress(walletAddress: String) = withContext(Dispatchers.IO) {
        val encrypted = encrypt(walletAddress)
        preferences.put(KEY_WALLET_ADDRESS, encrypted)
        preferences.flush()
    }
    
    override suspend fun getWalletAddress(): String? = withContext(Dispatchers.IO) {
        val encrypted = preferences.get(KEY_WALLET_ADDRESS, null) ?: return@withContext null
        try {
            decrypt(encrypted)
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun storeSessionToken(token: String) = withContext(Dispatchers.IO) {
        val encrypted = encrypt(token)
        preferences.put(KEY_SESSION_TOKEN, encrypted)
        preferences.putLong(KEY_SESSION_TIMESTAMP, System.currentTimeMillis())
        preferences.flush()
    }
    
    override suspend fun getSessionToken(): String? = withContext(Dispatchers.IO) {
        val encrypted = preferences.get(KEY_SESSION_TOKEN, null) ?: return@withContext null
        try {
            decrypt(encrypted)
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun clearSession() = withContext(Dispatchers.IO) {
        preferences.remove(KEY_WALLET_ADDRESS)
        preferences.remove(KEY_SESSION_TOKEN)
        preferences.remove(KEY_SESSION_TIMESTAMP)
        preferences.flush()
    }
    
    override suspend fun hasValidSession(): Boolean = withContext(Dispatchers.IO) {
        val walletAddress = getWalletAddress()
        val sessionToken = getSessionToken()
        val timestamp = preferences.getLong(KEY_SESSION_TIMESTAMP, 0)
        
        // Check if session exists and is not expired (30 days)
        val isExpired = System.currentTimeMillis() - timestamp > SESSION_EXPIRY_MS
        
        !walletAddress.isNullOrBlank() && !sessionToken.isNullOrBlank() && !isExpired
    }
    
    /**
     * Encrypt a string using AES
     */
    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey)
        val encrypted = cipher.doFinal(value.toByteArray())
        return Base64.getEncoder().encodeToString(encrypted)
    }
    
    /**
     * Decrypt a string using AES
     */
    private fun decrypt(encrypted: String): String {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, encryptionKey)
        val decoded = Base64.getDecoder().decode(encrypted)
        val decrypted = cipher.doFinal(decoded)
        return String(decrypted)
    }
    
    companion object {
        private const val KEY_WALLET_ADDRESS = "wallet_address"
        private const val KEY_SESSION_TOKEN = "session_token"
        private const val KEY_SESSION_TIMESTAMP = "session_timestamp"
        private const val SESSION_EXPIRY_MS = 30L * 24 * 60 * 60 * 1000 // 30 days
    }
}

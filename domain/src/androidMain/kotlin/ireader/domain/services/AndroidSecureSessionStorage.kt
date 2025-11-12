package ireader.domain.services

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Android implementation of SecureSessionStorage
 * Uses EncryptedSharedPreferences with AES256-GCM encryption
 */
class AndroidSecureSessionStorage(
    private val context: Context
) : SecureSessionStorage {
    
    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }
    
    private val sharedPreferences: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    override suspend fun storeWalletAddress(walletAddress: String) = withContext(Dispatchers.IO) {
        sharedPreferences.edit()
            .putString(KEY_WALLET_ADDRESS, walletAddress)
            .apply()
    }
    
    override suspend fun getWalletAddress(): String? = withContext(Dispatchers.IO) {
        sharedPreferences.getString(KEY_WALLET_ADDRESS, null)
    }
    
    override suspend fun storeSessionToken(token: String) = withContext(Dispatchers.IO) {
        sharedPreferences.edit()
            .putString(KEY_SESSION_TOKEN, token)
            .putLong(KEY_SESSION_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }
    
    override suspend fun getSessionToken(): String? = withContext(Dispatchers.IO) {
        sharedPreferences.getString(KEY_SESSION_TOKEN, null)
    }
    
    override suspend fun clearSession() = withContext(Dispatchers.IO) {
        sharedPreferences.edit()
            .remove(KEY_WALLET_ADDRESS)
            .remove(KEY_SESSION_TOKEN)
            .remove(KEY_SESSION_TIMESTAMP)
            .apply()
    }
    
    override suspend fun hasValidSession(): Boolean = withContext(Dispatchers.IO) {
        val walletAddress = sharedPreferences.getString(KEY_WALLET_ADDRESS, null)
        val sessionToken = sharedPreferences.getString(KEY_SESSION_TOKEN, null)
        val timestamp = sharedPreferences.getLong(KEY_SESSION_TIMESTAMP, 0)
        
        // Check if session exists and is not expired (30 days)
        val isExpired = System.currentTimeMillis() - timestamp > SESSION_EXPIRY_MS
        
        !walletAddress.isNullOrBlank() && !sessionToken.isNullOrBlank() && !isExpired
    }
    
    companion object {
        private const val PREFS_NAME = "ireader_secure_session"
        private const val KEY_WALLET_ADDRESS = "wallet_address"
        private const val KEY_SESSION_TOKEN = "session_token"
        private const val KEY_SESSION_TIMESTAMP = "session_timestamp"
        private const val SESSION_EXPIRY_MS = 30L * 24 * 60 * 60 * 1000 // 30 days
    }
}

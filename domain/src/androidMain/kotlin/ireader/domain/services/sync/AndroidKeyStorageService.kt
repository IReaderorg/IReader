package ireader.domain.services.sync

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Android implementation of KeyStorageService using Android Keystore System.
 * 
 * Phase 9.2.5: Secure key storage
 * 
 * Uses Android Keystore to protect encryption keys with hardware-backed security.
 * Keys are encrypted using a master key stored in Android Keystore.
 */
class AndroidKeyStorageService(
    private val context: Context
) : KeyStorageService {
    
    companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val MASTER_KEY_ALIAS = "ireader_sync_master_key"
        private const val PREFS_NAME = "ireader_sync_keys"
        private const val ALGORITHM = "AES/GCM/NoPadding"
        private const val KEY_SIZE = 256
        private const val IV_SIZE = 12
        private const val TAG_SIZE = 128
    }
    
    private val keyStore: KeyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
        load(null)
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    
    init {
        // Ensure master key exists
        if (!keyStore.containsAlias(MASTER_KEY_ALIAS)) {
            generateMasterKey()
        }
    }
    
    /**
     * Generate the master key in Android Keystore.
     * This key is used to encrypt/decrypt sync encryption keys.
     */
    private fun generateMasterKey() {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE_PROVIDER
        )
        
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            MASTER_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(KEY_SIZE)
            .setUserAuthenticationRequired(false) // No biometric for sync keys
            .build()
        
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }
    
    /**
     * Get the master key from Android Keystore.
     */
    private fun getMasterKey(): SecretKey {
        return keyStore.getKey(MASTER_KEY_ALIAS, null) as SecretKey
    }
    
    override suspend fun storeKey(alias: String, key: ByteArray): Result<Unit> {
        return try {
            val masterKey = getMasterKey()
            
            // Encrypt the key using master key
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, masterKey)
            
            val iv = cipher.iv
            val encryptedKey = cipher.doFinal(key)
            
            // Store IV + encrypted key in SharedPreferences
            val combined = iv + encryptedKey
            val encoded = Base64.encodeToString(combined, Base64.NO_WRAP)
            
            prefs.edit().putString(alias, encoded).apply()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun retrieveKey(alias: String): Result<ByteArray> {
        return try {
            val encoded = prefs.getString(alias, null)
                ?: return Result.failure(Exception("Key not found: $alias"))
            
            val combined = Base64.decode(encoded, Base64.NO_WRAP)
            
            // Extract IV and encrypted key
            val iv = combined.copyOfRange(0, IV_SIZE)
            val encryptedKey = combined.copyOfRange(IV_SIZE, combined.size)
            
            // Decrypt using master key
            val masterKey = getMasterKey()
            val cipher = Cipher.getInstance(ALGORITHM)
            val gcmSpec = GCMParameterSpec(TAG_SIZE, iv)
            cipher.init(Cipher.DECRYPT_MODE, masterKey, gcmSpec)
            
            val decryptedKey = cipher.doFinal(encryptedKey)
            
            Result.success(decryptedKey)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteKey(alias: String): Result<Unit> {
        return try {
            prefs.edit().remove(alias).apply()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun keyExists(alias: String): Boolean {
        return prefs.contains(alias)
    }
    
    override suspend fun listKeys(): List<String> {
        return prefs.all.keys.toList()
    }
}

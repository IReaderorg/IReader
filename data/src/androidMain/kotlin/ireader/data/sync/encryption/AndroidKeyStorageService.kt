package ireader.data.sync.encryption

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import ireader.domain.services.sync.KeyStorageService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Android implementation of KeyStorageService using Android Keystore System.
 * 
 * Task 9.2.5: Secure key storage (Android)
 * 
 * Features:
 * - Hardware-backed encryption when available
 * - Keys never leave secure hardware
 * - Automatic key generation with proper parameters
 * - AES-256-GCM for key wrapping
 */
class AndroidKeyStorageService(
    private val context: Context
) : KeyStorageService {
    
    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS_PREFIX = "ireader_sync_key_"
        private const val MASTER_KEY_ALIAS = "ireader_sync_master_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
    }
    
    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }
    
    init {
        // Ensure master key exists
        if (!keyStore.containsAlias(MASTER_KEY_ALIAS)) {
            generateMasterKey()
        }
    }
    
    override suspend fun storeKey(alias: String, key: ByteArray): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            require(key.size == 32) { "Key must be 256 bits (32 bytes)" }
            
            // Encrypt the key using master key before storing
            val encryptedKey = encryptWithMasterKey(key)
            
            // Store encrypted key in shared preferences
            val prefs = context.getSharedPreferences("sync_keys", Context.MODE_PRIVATE)
            val keyAlias = KEY_ALIAS_PREFIX + alias
            
            prefs.edit()
                .putString(keyAlias, encryptedKey.toBase64())
                .apply()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun retrieveKey(alias: String): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val prefs = context.getSharedPreferences("sync_keys", Context.MODE_PRIVATE)
            val keyAlias = KEY_ALIAS_PREFIX + alias
            
            val encryptedKeyBase64 = prefs.getString(keyAlias, null)
                ?: return@withContext Result.failure(Exception("Key not found: $alias"))
            
            val encryptedKey = encryptedKeyBase64.fromBase64()
            val decryptedKey = decryptWithMasterKey(encryptedKey)
            
            Result.success(decryptedKey)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteKey(alias: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val prefs = context.getSharedPreferences("sync_keys", Context.MODE_PRIVATE)
            val keyAlias = KEY_ALIAS_PREFIX + alias
            
            prefs.edit()
                .remove(keyAlias)
                .apply()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun keyExists(alias: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val prefs = context.getSharedPreferences("sync_keys", Context.MODE_PRIVATE)
            val keyAlias = KEY_ALIAS_PREFIX + alias
            prefs.contains(keyAlias)
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun listKeys(): List<String> = withContext(Dispatchers.IO) {
        try {
            val prefs = context.getSharedPreferences("sync_keys", Context.MODE_PRIVATE)
            prefs.all.keys
                .filter { it.startsWith(KEY_ALIAS_PREFIX) }
                .map { it.removePrefix(KEY_ALIAS_PREFIX) }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Generate master key in Android Keystore for encrypting sync keys.
     */
    private fun generateMasterKey() {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            MASTER_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(false)
            .build()
        
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }
    
    /**
     * Encrypt a key using the master key from Android Keystore.
     */
    private fun encryptWithMasterKey(data: ByteArray): ByteArray {
        val masterKey = keyStore.getKey(MASTER_KEY_ALIAS, null) as SecretKey
        
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, masterKey)
        
        val iv = cipher.iv
        val encryptedData = cipher.doFinal(data)
        
        // Return: IV (12 bytes) + encrypted data + tag (16 bytes)
        return iv + encryptedData
    }
    
    /**
     * Decrypt a key using the master key from Android Keystore.
     */
    private fun decryptWithMasterKey(encryptedData: ByteArray): ByteArray {
        require(encryptedData.size >= 28) { "Encrypted data too short" }
        
        val masterKey = keyStore.getKey(MASTER_KEY_ALIAS, null) as SecretKey
        
        // Extract IV (first 12 bytes)
        val iv = encryptedData.sliceArray(0 until 12)
        
        // Extract ciphertext + tag (remaining bytes)
        val ciphertextWithTag = encryptedData.sliceArray(12 until encryptedData.size)
        
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, masterKey, gcmSpec)
        
        return cipher.doFinal(ciphertextWithTag)
    }
    
    /**
     * Base64 encoding for Android.
     */
    private fun ByteArray.toBase64(): String {
        return android.util.Base64.encodeToString(this, android.util.Base64.NO_WRAP)
    }
    
    /**
     * Base64 decoding for Android.
     */
    private fun String.fromBase64(): ByteArray {
        return android.util.Base64.decode(this, android.util.Base64.NO_WRAP)
    }
}

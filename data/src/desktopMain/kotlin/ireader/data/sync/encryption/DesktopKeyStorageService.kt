package ireader.data.sync.encryption

import ireader.domain.services.sync.KeyStorageService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyStore
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Desktop (JVM) implementation of KeyStorageService using Java Keystore.
 * 
 * Task 9.2.5: Secure key storage (Desktop)
 * 
 * Features:
 * - Password-protected Java Keystore (JCEKS)
 * - Stored in user's home directory with restricted permissions
 * - AES-256-GCM for key wrapping
 * - Master key for encrypting sync keys
 */
class DesktopKeyStorageService : KeyStorageService {
    
    private val keystoreDir = File(System.getProperty("user.home"), ".ireader/keystore")
    private val keystoreFile = File(keystoreDir, "sync_keys.jceks")
    private val keystorePassword = "ireader_sync_keystore_password".toCharArray()
    
    companion object {
        private const val KEYSTORE_TYPE = "JCEKS"
        private const val MASTER_KEY_ALIAS = "master_key"
        private const val KEY_ALIAS_PREFIX = "sync_key_"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
    }
    
    init {
        if (!keystoreDir.exists()) {
            keystoreDir.mkdirs()
            // Set directory permissions (Unix-like systems)
            try {
                keystoreDir.setReadable(false, false)
                keystoreDir.setReadable(true, true)
                keystoreDir.setWritable(false, false)
                keystoreDir.setWritable(true, true)
                keystoreDir.setExecutable(false, false)
                keystoreDir.setExecutable(true, true)
            } catch (e: Exception) {
                // Ignore on Windows
            }
        }
        
        // Ensure master key exists
        if (!keystoreFile.exists() || !loadKeyStore().containsAlias(MASTER_KEY_ALIAS)) {
            generateMasterKey()
        }
    }
    
    override suspend fun storeKey(alias: String, key: ByteArray): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            require(key.size == 32) { "Key must be 256 bits (32 bytes)" }
            
            val keyStore = loadKeyStore()
            
            // Encrypt the key using master key before storing
            val encryptedKey = encryptWithMasterKey(key)
            
            // Wrap encrypted key as SecretKey for storage
            val wrappedKey = SecretKeySpec(encryptedKey, "AES")
            val keyAlias = KEY_ALIAS_PREFIX + alias
            
            // Store in keystore
            val protectionParam = KeyStore.SecretKeyEntry(wrappedKey)
            keyStore.setEntry(
                keyAlias,
                protectionParam,
                KeyStore.PasswordProtection(keystorePassword)
            )
            
            // Save keystore
            saveKeyStore(keyStore)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun retrieveKey(alias: String): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val keyStore = loadKeyStore()
            val keyAlias = KEY_ALIAS_PREFIX + alias
            
            if (!keyStore.containsAlias(keyAlias)) {
                return@withContext Result.failure(Exception("Key not found: $alias"))
            }
            
            val entry = keyStore.getEntry(
                keyAlias,
                KeyStore.PasswordProtection(keystorePassword)
            ) as KeyStore.SecretKeyEntry
            
            val encryptedKey = entry.secretKey.encoded
            val decryptedKey = decryptWithMasterKey(encryptedKey)
            
            Result.success(decryptedKey)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteKey(alias: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val keyStore = loadKeyStore()
            val keyAlias = KEY_ALIAS_PREFIX + alias
            
            if (keyStore.containsAlias(keyAlias)) {
                keyStore.deleteEntry(keyAlias)
                saveKeyStore(keyStore)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun keyExists(alias: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val keyStore = loadKeyStore()
            val keyAlias = KEY_ALIAS_PREFIX + alias
            keyStore.containsAlias(keyAlias)
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun listKeys(): List<String> = withContext(Dispatchers.IO) {
        try {
            val keyStore = loadKeyStore()
            keyStore.aliases().toList()
                .filter { it.startsWith(KEY_ALIAS_PREFIX) }
                .map { it.removePrefix(KEY_ALIAS_PREFIX) }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Load keystore from file or create new one.
     */
    private fun loadKeyStore(): KeyStore {
        val keyStore = KeyStore.getInstance(KEYSTORE_TYPE)
        
        if (keystoreFile.exists()) {
            FileInputStream(keystoreFile).use { fis ->
                keyStore.load(fis, keystorePassword)
            }
        } else {
            keyStore.load(null, keystorePassword)
        }
        
        return keyStore
    }
    
    /**
     * Save keystore to file.
     */
    private fun saveKeyStore(keyStore: KeyStore) {
        FileOutputStream(keystoreFile).use { fos ->
            keyStore.store(fos, keystorePassword)
        }
        
        // Set file permissions (Unix-like systems)
        try {
            keystoreFile.setReadable(false, false)
            keystoreFile.setReadable(true, true)
            keystoreFile.setWritable(false, false)
            keystoreFile.setWritable(true, true)
        } catch (e: Exception) {
            // Ignore on Windows
        }
    }
    
    /**
     * Generate master key for encrypting sync keys.
     */
    private fun generateMasterKey() {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        val masterKey = keyGenerator.generateKey()
        
        val keyStore = loadKeyStore()
        val protectionParam = KeyStore.SecretKeyEntry(masterKey)
        keyStore.setEntry(
            MASTER_KEY_ALIAS,
            protectionParam,
            KeyStore.PasswordProtection(keystorePassword)
        )
        
        saveKeyStore(keyStore)
    }
    
    /**
     * Encrypt a key using the master key.
     */
    private fun encryptWithMasterKey(data: ByteArray): ByteArray {
        val keyStore = loadKeyStore()
        val entry = keyStore.getEntry(
            MASTER_KEY_ALIAS,
            KeyStore.PasswordProtection(keystorePassword)
        ) as KeyStore.SecretKeyEntry
        val masterKey = entry.secretKey
        
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, masterKey)
        
        val iv = cipher.iv
        val encryptedData = cipher.doFinal(data)
        
        // Return: IV (12 bytes) + encrypted data + tag (16 bytes)
        return iv + encryptedData
    }
    
    /**
     * Decrypt a key using the master key.
     */
    private fun decryptWithMasterKey(encryptedData: ByteArray): ByteArray {
        require(encryptedData.size >= 28) { "Encrypted data too short" }
        
        val keyStore = loadKeyStore()
        val entry = keyStore.getEntry(
            MASTER_KEY_ALIAS,
            KeyStore.PasswordProtection(keystorePassword)
        ) as KeyStore.SecretKeyEntry
        val masterKey = entry.secretKey
        
        // Extract IV (first 12 bytes)
        val iv = encryptedData.sliceArray(0 until 12)
        
        // Extract ciphertext + tag (remaining bytes)
        val ciphertextWithTag = encryptedData.sliceArray(12 until encryptedData.size)
        
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, masterKey, gcmSpec)
        
        return cipher.doFinal(ciphertextWithTag)
    }
}

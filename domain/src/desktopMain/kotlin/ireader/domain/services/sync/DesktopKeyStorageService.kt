package ireader.domain.services.sync

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

/**
 * Desktop (JVM) implementation of KeyStorageService using Java Keystore (JKS).
 * 
 * Phase 9.2.5: Secure key storage
 * 
 * Uses Java Keystore to securely store encryption keys.
 * The keystore is password-protected and stored in the user's home directory.
 */
class DesktopKeyStorageService(
    private val keystorePath: String = getDefaultKeystorePath(),
    private val keystorePassword: CharArray = getDefaultKeystorePassword()
) : KeyStorageService {
    
    companion object {
        private const val KEYSTORE_TYPE = "JCEKS" // Supports secret keys
        private const val KEY_ALGORITHM = "AES"
        
        /**
         * Get default keystore path in user's home directory.
         */
        fun getDefaultKeystorePath(): String {
            val userHome = System.getProperty("user.home")
            val ireaderDir = File(userHome, ".ireader")
            if (!ireaderDir.exists()) {
                ireaderDir.mkdirs()
            }
            return File(ireaderDir, "sync_keystore.jks").absolutePath
        }
        
        /**
         * Get default keystore password.
         * In production, this should be derived from user credentials or system properties.
         */
        fun getDefaultKeystorePassword(): CharArray {
            // TODO: In production, derive from user credentials or system keyring
            return "ireader_sync_keystore_password".toCharArray()
        }
    }
    
    private val keyStore: KeyStore = KeyStore.getInstance(KEYSTORE_TYPE)
    
    init {
        loadOrCreateKeystore()
    }
    
    /**
     * Load existing keystore or create a new one.
     */
    private fun loadOrCreateKeystore() {
        val keystoreFile = File(keystorePath)
        
        if (keystoreFile.exists()) {
            FileInputStream(keystoreFile).use { fis ->
                keyStore.load(fis, keystorePassword)
            }
        } else {
            // Create new keystore
            keyStore.load(null, keystorePassword)
            saveKeystore()
        }
    }
    
    /**
     * Save keystore to disk.
     */
    private fun saveKeystore() {
        FileOutputStream(keystorePath).use { fos ->
            keyStore.store(fos, keystorePassword)
        }
    }
    
    override suspend fun storeKey(alias: String, key: ByteArray): Result<Unit> {
        return try {
            val secretKey = SecretKeySpec(key, KEY_ALGORITHM)
            val protectionParam = KeyStore.SecretKeyEntry(secretKey)
            val passwordProtection = KeyStore.PasswordProtection(keystorePassword)
            
            keyStore.setEntry(alias, protectionParam, passwordProtection)
            saveKeystore()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun retrieveKey(alias: String): Result<ByteArray> {
        return try {
            if (!keyStore.containsAlias(alias)) {
                return Result.failure(Exception("Key not found: $alias"))
            }
            
            val passwordProtection = KeyStore.PasswordProtection(keystorePassword)
            val entry = keyStore.getEntry(alias, passwordProtection) as? KeyStore.SecretKeyEntry
                ?: return Result.failure(Exception("Invalid key entry: $alias"))
            
            val secretKey = entry.secretKey
            val keyBytes = secretKey.encoded
            
            Result.success(keyBytes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteKey(alias: String): Result<Unit> {
        return try {
            if (keyStore.containsAlias(alias)) {
                keyStore.deleteEntry(alias)
                saveKeystore()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun keyExists(alias: String): Boolean {
        return keyStore.containsAlias(alias)
    }
    
    override suspend fun listKeys(): List<String> {
        return keyStore.aliases().toList()
    }
}

package ireader.domain.services.sync

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Android implementation of EncryptionService using AES-256-GCM.
 * 
 * Uses javax.crypto for encryption and Android's SecureRandom for key generation.
 * 
 * Phase 9.2.4: AES-256 payload encryption
 * Phase 9.2.5: Secure key storage (Android Keystore integration)
 */
actual class CommonEncryptionService actual constructor() : EncryptionService {
    
    companion object {
        private const val ALGORITHM = "AES/GCM/NoPadding"
        private const val KEY_SIZE_BYTES = 32 // 256 bits
        private const val IV_SIZE_BYTES = 12 // 96 bits (recommended for GCM)
        private const val TAG_SIZE_BITS = 128 // 128 bits authentication tag
    }
    
    private val secureRandom = SecureRandom()
    
    /**
     * Generate a cryptographically secure 256-bit key.
     */
    actual override fun generateKey(): ByteArray {
        val key = ByteArray(KEY_SIZE_BYTES)
        secureRandom.nextBytes(key)
        return key
    }
    
    /**
     * Encrypt plaintext using AES-256-GCM.
     * 
     * Format: Base64(IV + ciphertext + tag)
     */
    actual override fun encrypt(plaintext: String, key: ByteArray): String {
        require(key.size == KEY_SIZE_BYTES) {
            "Key must be $KEY_SIZE_BYTES bytes (256 bits), got ${key.size} bytes"
        }
        
        val plaintextBytes = plaintext.encodeToByteArray()
        val encryptedBytes = encryptBytes(plaintextBytes, key)
        
        return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
    }
    
    /**
     * Decrypt Base64-encoded ciphertext using AES-256-GCM.
     */
    actual override fun decrypt(ciphertext: String, key: ByteArray): String {
        require(key.size == KEY_SIZE_BYTES) {
            "Key must be $KEY_SIZE_BYTES bytes (256 bits), got ${key.size} bytes"
        }
        
        val encryptedBytes = Base64.decode(ciphertext, Base64.NO_WRAP)
        val decryptedBytes = decryptBytes(encryptedBytes, key)
        
        return decryptedBytes.decodeToString()
    }
    
    /**
     * Encrypt binary data using AES-256-GCM.
     * 
     * Format: IV (12 bytes) + ciphertext + tag (16 bytes)
     */
    actual override fun encryptBytes(plainBytes: ByteArray, key: ByteArray): ByteArray {
        require(key.size == KEY_SIZE_BYTES) {
            "Key must be $KEY_SIZE_BYTES bytes (256 bits), got ${key.size} bytes"
        }
        
        // Generate random IV
        val iv = ByteArray(IV_SIZE_BYTES)
        secureRandom.nextBytes(iv)
        
        // Initialize cipher
        val cipher = Cipher.getInstance(ALGORITHM)
        val secretKey = SecretKeySpec(key, "AES")
        val gcmSpec = GCMParameterSpec(TAG_SIZE_BITS, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)
        
        // Encrypt (includes authentication tag)
        val ciphertextWithTag = cipher.doFinal(plainBytes)
        
        // Combine IV + ciphertext + tag
        return iv + ciphertextWithTag
    }
    
    /**
     * Decrypt binary data using AES-256-GCM.
     */
    actual override fun decryptBytes(encryptedBytes: ByteArray, key: ByteArray): ByteArray {
        require(key.size == KEY_SIZE_BYTES) {
            "Key must be $KEY_SIZE_BYTES bytes (256 bits), got ${key.size} bytes"
        }
        
        require(encryptedBytes.size >= IV_SIZE_BYTES + TAG_SIZE_BITS / 8) {
            "Encrypted data too short: must be at least ${IV_SIZE_BYTES + TAG_SIZE_BITS / 8} bytes"
        }
        
        // Extract IV
        val iv = encryptedBytes.copyOfRange(0, IV_SIZE_BYTES)
        
        // Extract ciphertext + tag
        val ciphertextWithTag = encryptedBytes.copyOfRange(IV_SIZE_BYTES, encryptedBytes.size)
        
        // Initialize cipher
        val cipher = Cipher.getInstance(ALGORITHM)
        val secretKey = SecretKeySpec(key, "AES")
        val gcmSpec = GCMParameterSpec(TAG_SIZE_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
        
        // Decrypt and verify authentication tag
        return cipher.doFinal(ciphertextWithTag)
    }
}

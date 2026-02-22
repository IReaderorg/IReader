package ireader.data.sync.encryption

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Desktop (JVM) implementation of platform-specific cryptographic operations.
 * 
 * Uses Java's javax.crypto for AES-256-GCM encryption.
 * Uses Java's SecureRandom for cryptographically secure random generation.
 */
internal actual class PlatformCrypto {
    
    private val secureRandom = SecureRandom()
    
    /**
     * Generate cryptographically secure random bytes using Java's SecureRandom.
     */
    actual fun generateSecureRandom(size: Int): ByteArray {
        val bytes = ByteArray(size)
        secureRandom.nextBytes(bytes)
        return bytes
    }
    
    /**
     * Encrypt data using AES-256-GCM with Java's javax.crypto.
     * 
     * @param plaintext Data to encrypt
     * @param key 256-bit key (32 bytes)
     * @param iv 96-bit IV (12 bytes)
     * @return Ciphertext + 128-bit authentication tag (16 bytes)
     */
    actual fun encryptAesGcm(plaintext: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        require(key.size == 32) { "Key must be 256 bits (32 bytes)" }
        require(iv.size == 12) { "IV must be 96 bits (12 bytes) for GCM" }
        
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val secretKey = SecretKeySpec(key, "AES")
        val gcmSpec = GCMParameterSpec(128, iv) // 128-bit authentication tag
        
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)
        return cipher.doFinal(plaintext)
    }
    
    /**
     * Decrypt data using AES-256-GCM with Java's javax.crypto.
     * 
     * @param ciphertextWithTag Ciphertext + 128-bit authentication tag
     * @param key 256-bit key (32 bytes)
     * @param iv 96-bit IV (12 bytes)
     * @return Decrypted plaintext
     * @throws Exception if authentication fails or decryption error
     */
    actual fun decryptAesGcm(ciphertextWithTag: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        require(key.size == 32) { "Key must be 256 bits (32 bytes)" }
        require(iv.size == 12) { "IV must be 96 bits (12 bytes) for GCM" }
        
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val secretKey = SecretKeySpec(key, "AES")
        val gcmSpec = GCMParameterSpec(128, iv) // 128-bit authentication tag
        
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
        return cipher.doFinal(ciphertextWithTag)
    }
}

/**
 * Base64 encoding using Java's Base64 utility.
 */
internal actual fun ByteArray.toBase64(): String {
    return Base64.getEncoder().encodeToString(this)
}

/**
 * Base64 decoding using Java's Base64 utility.
 */
internal actual fun String.fromBase64(): ByteArray {
    return Base64.getDecoder().decode(this)
}

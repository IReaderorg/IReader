package ireader.data.sync.encryption

import ireader.domain.services.sync.EncryptionService

/**
 * Common implementation of AES-256-GCM encryption service.
 * 
 * Task 9.2.4: AES-256 payload encryption implementation
 * 
 * Uses platform-specific crypto APIs via expect/actual pattern:
 * - Android: javax.crypto (Android Keystore compatible)
 * - Desktop: javax.crypto (JVM)
 * - iOS: CommonCrypto / Security framework
 * 
 * Encryption details:
 * - Algorithm: AES-256-GCM (Galois/Counter Mode)
 * - Key size: 256 bits (32 bytes)
 * - IV size: 96 bits (12 bytes) - recommended for GCM
 * - Tag size: 128 bits (16 bytes) - authentication tag
 * 
 * Security properties:
 * - Authenticated encryption (prevents tampering)
 * - Random IV for each encryption (prevents pattern analysis)
 * - No padding required (GCM is a stream cipher mode)
 */
class AesEncryptionService : EncryptionService {
    
    private val crypto = PlatformCrypto()
    
    override fun generateKey(): ByteArray {
        return crypto.generateSecureRandom(32) // 256 bits
    }
    
    override fun encrypt(plaintext: String, key: ByteArray): String {
        require(key.size == 32) { "Key must be 256 bits (32 bytes)" }
        
        val plaintextBytes = plaintext.encodeToByteArray()
        val encryptedBytes = encryptBytes(plaintextBytes, key)
        
        return encryptedBytes.toBase64()
    }
    
    override fun decrypt(ciphertext: String, key: ByteArray): String {
        require(key.size == 32) { "Key must be 256 bits (32 bytes)" }
        
        val encryptedBytes = ciphertext.fromBase64()
        val decryptedBytes = decryptBytes(encryptedBytes, key)
        
        return decryptedBytes.decodeToString()
    }
    
    override fun encryptBytes(plainBytes: ByteArray, key: ByteArray): ByteArray {
        require(key.size == 32) { "Key must be 256 bits (32 bytes)" }
        
        // Generate random IV (12 bytes for GCM)
        val iv = crypto.generateSecureRandom(12)
        
        // Encrypt with AES-256-GCM
        val ciphertext = crypto.encryptAesGcm(plainBytes, key, iv)
        
        // Return: IV (12 bytes) + ciphertext + tag (16 bytes)
        return iv + ciphertext
    }
    
    override fun decryptBytes(encryptedBytes: ByteArray, key: ByteArray): ByteArray {
        require(key.size == 32) { "Key must be 256 bits (32 bytes)" }
        require(encryptedBytes.size >= 28) { "Encrypted data too short (min: 12-byte IV + 16-byte tag)" }
        
        // Extract IV (first 12 bytes)
        val iv = encryptedBytes.sliceArray(0 until 12)
        
        // Extract ciphertext + tag (remaining bytes)
        val ciphertextWithTag = encryptedBytes.sliceArray(12 until encryptedBytes.size)
        
        // Decrypt with AES-256-GCM
        return crypto.decryptAesGcm(ciphertextWithTag, key, iv)
    }
}

/**
 * Platform-specific cryptographic operations.
 * 
 * Implementations:
 * - Android/Desktop: Uses javax.crypto
 * - iOS: Uses CommonCrypto
 */
internal expect class PlatformCrypto() {
    /**
     * Generate cryptographically secure random bytes.
     */
    fun generateSecureRandom(size: Int): ByteArray
    
    /**
     * Encrypt data using AES-256-GCM.
     * 
     * @param plaintext Data to encrypt
     * @param key 256-bit key (32 bytes)
     * @param iv 96-bit IV (12 bytes)
     * @return Ciphertext + 128-bit authentication tag (16 bytes)
     */
    fun encryptAesGcm(plaintext: ByteArray, key: ByteArray, iv: ByteArray): ByteArray
    
    /**
     * Decrypt data using AES-256-GCM.
     * 
     * @param ciphertextWithTag Ciphertext + 128-bit authentication tag
     * @param key 256-bit key (32 bytes)
     * @param iv 96-bit IV (12 bytes)
     * @return Decrypted plaintext
     * @throws Exception if authentication fails or decryption error
     */
    fun decryptAesGcm(ciphertextWithTag: ByteArray, key: ByteArray, iv: ByteArray): ByteArray
}

/**
 * Base64 encoding/decoding extensions for ByteArray.
 */
internal expect fun ByteArray.toBase64(): String
internal expect fun String.fromBase64(): ByteArray

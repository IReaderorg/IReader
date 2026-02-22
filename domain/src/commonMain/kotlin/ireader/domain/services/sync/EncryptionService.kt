package ireader.domain.services.sync

/**
 * Service for encrypting and decrypting sync data using AES-256 encryption.
 * 
 * Task 9.2.4: AES-256 payload encryption
 * 
 * This service provides:
 * - Secure key generation (256-bit)
 * - AES-256-GCM encryption with authentication
 * - Automatic IV (Initialization Vector) generation
 * - String and binary data encryption
 */
interface EncryptionService {
    
    /**
     * Generate a new 256-bit encryption key.
     * 
     * @return ByteArray containing 32 bytes (256 bits) of cryptographically secure random data
     */
    fun generateKey(): ByteArray
    
    /**
     * Encrypt plaintext string using AES-256-GCM.
     * 
     * The encrypted output includes:
     * - 12-byte IV (prepended)
     * - Ciphertext
     * - 16-byte authentication tag (appended)
     * 
     * The result is Base64-encoded for safe transmission.
     * 
     * @param plaintext The string to encrypt
     * @param key The 256-bit encryption key (32 bytes)
     * @return Base64-encoded encrypted data (IV + ciphertext + tag)
     * @throws IllegalArgumentException if key size is not 32 bytes
     */
    fun encrypt(plaintext: String, key: ByteArray): String
    
    /**
     * Decrypt Base64-encoded ciphertext using AES-256-GCM.
     * 
     * @param ciphertext Base64-encoded encrypted data (IV + ciphertext + tag)
     * @param key The 256-bit encryption key (32 bytes)
     * @return Decrypted plaintext string
     * @throws IllegalArgumentException if key size is not 32 bytes
     * @throws Exception if decryption fails (wrong key, corrupted data, or authentication failure)
     */
    fun decrypt(ciphertext: String, key: ByteArray): String
    
    /**
     * Encrypt binary data using AES-256-GCM.
     * 
     * @param plainBytes The binary data to encrypt
     * @param key The 256-bit encryption key (32 bytes)
     * @return Encrypted data (IV + ciphertext + tag)
     * @throws IllegalArgumentException if key size is not 32 bytes
     */
    fun encryptBytes(plainBytes: ByteArray, key: ByteArray): ByteArray
    
    /**
     * Decrypt binary data using AES-256-GCM.
     * 
     * @param encryptedBytes Encrypted data (IV + ciphertext + tag)
     * @param key The 256-bit encryption key (32 bytes)
     * @return Decrypted binary data
     * @throws IllegalArgumentException if key size is not 32 bytes
     * @throws Exception if decryption fails
     */
    fun decryptBytes(encryptedBytes: ByteArray, key: ByteArray): ByteArray
}

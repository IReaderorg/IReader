package ireader.domain.services.sync

import kotlin.random.Random

/**
 * Common implementation of EncryptionService for KMP.
 * 
 * This is a minimal GREEN phase implementation to make tests pass.
 * Platform-specific implementations will provide actual AES-256-GCM encryption.
 * 
 * Phase 9.2.4: AES-256 payload encryption
 */
expect class CommonEncryptionService() : EncryptionService {
    override fun generateKey(): ByteArray
    override fun encrypt(plaintext: String, key: ByteArray): String
    override fun decrypt(ciphertext: String, key: ByteArray): String
    override fun encryptBytes(plainBytes: ByteArray, key: ByteArray): ByteArray
    override fun decryptBytes(encryptedBytes: ByteArray, key: ByteArray): ByteArray
}

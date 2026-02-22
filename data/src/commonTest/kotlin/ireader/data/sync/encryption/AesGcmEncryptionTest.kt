package ireader.data.sync.encryption

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Tests for AES-256-GCM encryption/decryption across all platforms.
 * 
 * Verifies that encryption is working correctly and produces expected results.
 */
class AesGcmEncryptionTest {
    
    private val crypto = PlatformCrypto()
    
    @Test
    fun `encryptAesGcm and decryptAesGcm should roundtrip successfully`() {
        val plaintext = "Hello, World! This is a test message.".encodeToByteArray()
        val key = crypto.generateSecureRandom(32) // 256-bit key
        val iv = crypto.generateSecureRandom(12) // 96-bit IV for GCM
        
        val ciphertextWithTag = crypto.encryptAesGcm(plaintext, key, iv)
        val decrypted = crypto.decryptAesGcm(ciphertextWithTag, key, iv)
        
        assertContentEquals(plaintext, decrypted, "Decrypted text should match original plaintext")
    }
    
    @Test
    fun `encryptAesGcm should produce different ciphertext with different IVs`() {
        val plaintext = "Test message".encodeToByteArray()
        val key = crypto.generateSecureRandom(32)
        val iv1 = crypto.generateSecureRandom(12)
        val iv2 = crypto.generateSecureRandom(12)
        
        val ciphertext1 = crypto.encryptAesGcm(plaintext, key, iv1)
        val ciphertext2 = crypto.encryptAesGcm(plaintext, key, iv2)
        
        assertNotEquals(
            ciphertext1.contentToString(),
            ciphertext2.contentToString(),
            "Different IVs should produce different ciphertexts"
        )
    }
    
    @Test
    fun `encryptAesGcm should include authentication tag`() {
        val plaintext = "Test".encodeToByteArray()
        val key = crypto.generateSecureRandom(32)
        val iv = crypto.generateSecureRandom(12)
        
        val ciphertextWithTag = crypto.encryptAesGcm(plaintext, key, iv)
        
        // GCM tag is 16 bytes, so ciphertext should be plaintext.size + 16
        assertEquals(
            plaintext.size + 16,
            ciphertextWithTag.size,
            "Ciphertext should include 16-byte authentication tag"
        )
    }
    
    @Test
    fun `encryptAesGcm should handle empty plaintext`() {
        val plaintext = ByteArray(0)
        val key = crypto.generateSecureRandom(32)
        val iv = crypto.generateSecureRandom(12)
        
        val ciphertextWithTag = crypto.encryptAesGcm(plaintext, key, iv)
        val decrypted = crypto.decryptAesGcm(ciphertextWithTag, key, iv)
        
        assertContentEquals(plaintext, decrypted, "Empty plaintext should roundtrip correctly")
    }
    
    @Test
    fun `encryptAesGcm should handle large plaintext`() {
        val plaintext = ByteArray(10000) { it.toByte() }
        val key = crypto.generateSecureRandom(32)
        val iv = crypto.generateSecureRandom(12)
        
        val ciphertextWithTag = crypto.encryptAesGcm(plaintext, key, iv)
        val decrypted = crypto.decryptAesGcm(ciphertextWithTag, key, iv)
        
        assertContentEquals(plaintext, decrypted, "Large plaintext should roundtrip correctly")
    }
}

package ireader.data.sync.encryption

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

/**
 * TDD Tests for AES-256 encryption service.
 * 
 * Task 9.2.4: Write test for AES-256 payload encryption, then implement
 * 
 * These tests verify:
 * - Key generation produces valid 256-bit keys
 * - Encryption produces different ciphertext for same plaintext (due to IV)
 * - Decryption correctly recovers original plaintext
 * - Encrypted data is not equal to plaintext
 * - Empty data handling
 * - Large data handling
 */
class AesEncryptionServiceTest {
    
    @Test
    fun `generateKey should produce 256-bit key`() {
        // Arrange
        val encryptionService = AesEncryptionService()
        
        // Act
        val key = encryptionService.generateKey()
        
        // Assert
        assertNotNull(key)
        assertEquals(32, key.size) // 256 bits = 32 bytes
    }
    
    @Test
    fun `generateKey should produce different keys each time`() {
        // Arrange
        val encryptionService = AesEncryptionService()
        
        // Act
        val key1 = encryptionService.generateKey()
        val key2 = encryptionService.generateKey()
        
        // Assert
        assertNotEquals(key1.contentToString(), key2.contentToString())
    }
    
    @Test
    fun `encrypt should produce ciphertext different from plaintext`() {
        // Arrange
        val encryptionService = AesEncryptionService()
        val key = encryptionService.generateKey()
        val plaintext = "Hello, World! This is a test message."
        
        // Act
        val ciphertext = encryptionService.encrypt(plaintext, key)
        
        // Assert
        assertNotNull(ciphertext)
        assertNotEquals(plaintext, ciphertext)
    }
    
    @Test
    fun `encrypt should produce different ciphertext for same plaintext due to IV`() {
        // Arrange
        val encryptionService = AesEncryptionService()
        val key = encryptionService.generateKey()
        val plaintext = "Same message"
        
        // Act
        val ciphertext1 = encryptionService.encrypt(plaintext, key)
        val ciphertext2 = encryptionService.encrypt(plaintext, key)
        
        // Assert
        assertNotEquals(ciphertext1, ciphertext2) // Different IVs produce different ciphertext
    }
    
    @Test
    fun `decrypt should recover original plaintext`() {
        // Arrange
        val encryptionService = AesEncryptionService()
        val key = encryptionService.generateKey()
        val originalPlaintext = "This is a secret message that needs encryption."
        
        // Act
        val ciphertext = encryptionService.encrypt(originalPlaintext, key)
        val decryptedPlaintext = encryptionService.decrypt(ciphertext, key)
        
        // Assert
        assertEquals(originalPlaintext, decryptedPlaintext)
    }
    
    @Test
    fun `encrypt and decrypt should work with empty string`() {
        // Arrange
        val encryptionService = AesEncryptionService()
        val key = encryptionService.generateKey()
        val plaintext = ""
        
        // Act
        val ciphertext = encryptionService.encrypt(plaintext, key)
        val decryptedPlaintext = encryptionService.decrypt(ciphertext, key)
        
        // Assert
        assertEquals(plaintext, decryptedPlaintext)
    }
    
    @Test
    fun `encrypt and decrypt should work with large data`() {
        // Arrange
        val encryptionService = AesEncryptionService()
        val key = encryptionService.generateKey()
        val plaintext = "A".repeat(10000) // 10KB of data
        
        // Act
        val ciphertext = encryptionService.encrypt(plaintext, key)
        val decryptedPlaintext = encryptionService.decrypt(ciphertext, key)
        
        // Assert
        assertEquals(plaintext, decryptedPlaintext)
    }
    
    @Test
    fun `encrypt and decrypt should work with special characters`() {
        // Arrange
        val encryptionService = AesEncryptionService()
        val key = encryptionService.generateKey()
        val plaintext = "Special chars: 你好世界 🌍 émojis ñ ü"
        
        // Act
        val ciphertext = encryptionService.encrypt(plaintext, key)
        val decryptedPlaintext = encryptionService.decrypt(ciphertext, key)
        
        // Assert
        assertEquals(plaintext, decryptedPlaintext)
    }
    
    @Test
    fun `decrypt with wrong key should fail`() {
        // Arrange
        val encryptionService = AesEncryptionService()
        val correctKey = encryptionService.generateKey()
        val wrongKey = encryptionService.generateKey()
        val plaintext = "Secret message"
        
        // Act
        val ciphertext = encryptionService.encrypt(plaintext, correctKey)
        
        // Assert
        assertFailsWith<Exception> {
            encryptionService.decrypt(ciphertext, wrongKey)
        }
    }
    
    @Test
    fun `decrypt with corrupted ciphertext should fail`() {
        // Arrange
        val encryptionService = AesEncryptionService()
        val key = encryptionService.generateKey()
        val plaintext = "Secret message"
        val ciphertext = encryptionService.encrypt(plaintext, key)
        
        // Act - Corrupt the ciphertext
        val corruptedCiphertext = ciphertext.dropLast(5) + "xxxxx"
        
        // Assert
        assertFailsWith<Exception> {
            encryptionService.decrypt(corruptedCiphertext, key)
        }
    }
    
    @Test
    fun `encryptBytes should encrypt binary data`() {
        // Arrange
        val encryptionService = AesEncryptionService()
        val key = encryptionService.generateKey()
        val plainBytes = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        
        // Act
        val encryptedBytes = encryptionService.encryptBytes(plainBytes, key)
        
        // Assert
        assertNotNull(encryptedBytes)
        assertTrue(encryptedBytes.isNotEmpty())
        assertNotEquals(plainBytes.contentToString(), encryptedBytes.contentToString())
    }
    
    @Test
    fun `decryptBytes should recover original binary data`() {
        // Arrange
        val encryptionService = AesEncryptionService()
        val key = encryptionService.generateKey()
        val originalBytes = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        
        // Act
        val encryptedBytes = encryptionService.encryptBytes(originalBytes, key)
        val decryptedBytes = encryptionService.decryptBytes(encryptedBytes, key)
        
        // Assert
        assertEquals(originalBytes.contentToString(), decryptedBytes.contentToString())
    }
}

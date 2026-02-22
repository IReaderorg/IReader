package ireader.domain.services.sync

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * TDD Tests for EncryptionService (Phase 9.2.4)
 * 
 * Following RED-GREEN-REFACTOR methodology:
 * 1. Write test first (RED - should fail)
 * 2. Implement minimal code to pass (GREEN)
 * 3. Refactor while keeping tests green
 */
class EncryptionServiceTest {
    
    // Use the actual implementation for testing
    private lateinit var encryptionService: EncryptionService
    
    @Test
    fun `generateKey should return 32 bytes for AES-256`() {
        // Arrange
        encryptionService = CommonEncryptionService()
        
        // Act
        val key = encryptionService.generateKey()
        
        // Assert
        assertEquals(32, key.size, "AES-256 key must be 32 bytes (256 bits)")
    }
    
    @Test
    fun `generateKey should return different keys on each call`() {
        // Arrange
        encryptionService = CommonEncryptionService()
        
        // Act
        val key1 = encryptionService.generateKey()
        val key2 = encryptionService.generateKey()
        
        // Assert
        assertNotEquals(
            key1.contentToString(),
            key2.contentToString(),
            "Each generated key should be unique"
        )
    }
    
    @Test
    fun `encrypt should return non-empty string`() {
        // Arrange
        encryptionService = CommonEncryptionService()
        val plaintext = "Hello, World!"
        val key = encryptionService.generateKey()
        
        // Act
        val encrypted = encryptionService.encrypt(plaintext, key)
        
        // Assert
        assertTrue(encrypted.isNotEmpty(), "Encrypted text should not be empty")
        assertNotEquals(plaintext, encrypted, "Encrypted text should differ from plaintext")
    }
    
    @Test
    fun `decrypt should return original plaintext`() {
        // Arrange
        encryptionService = CommonEncryptionService()
        val plaintext = "Hello, World!"
        val key = encryptionService.generateKey()
        
        // Act
        val encrypted = encryptionService.encrypt(plaintext, key)
        val decrypted = encryptionService.decrypt(encrypted, key)
        
        // Assert
        assertEquals(plaintext, decrypted, "Decrypted text should match original plaintext")
    }
    
    @Test
    fun `encrypt with different keys should produce different ciphertext`() {
        // Arrange
        encryptionService = CommonEncryptionService()
        val plaintext = "Hello, World!"
        val key1 = encryptionService.generateKey()
        val key2 = encryptionService.generateKey()
        
        // Act
        val encrypted1 = encryptionService.encrypt(plaintext, key1)
        val encrypted2 = encryptionService.encrypt(plaintext, key2)
        
        // Assert
        assertNotEquals(
            encrypted1,
            encrypted2,
            "Same plaintext with different keys should produce different ciphertext"
        )
    }
    
    @Test
    fun `decrypt with wrong key should fail`() {
        // Arrange
        encryptionService = CommonEncryptionService()
        val plaintext = "Hello, World!"
        val correctKey = encryptionService.generateKey()
        val wrongKey = encryptionService.generateKey()
        
        // Act
        val encrypted = encryptionService.encrypt(plaintext, correctKey)
        
        // Assert
        assertFailsWith<Exception> {
            encryptionService.decrypt(encrypted, wrongKey)
        }
    }
    
    @Test
    fun `encrypt should reject invalid key size`() {
        // Arrange
        encryptionService = CommonEncryptionService()
        val plaintext = "Hello, World!"
        val invalidKey = ByteArray(16) // 128-bit key instead of 256-bit
        
        // Assert
        assertFailsWith<IllegalArgumentException> {
            encryptionService.encrypt(plaintext, invalidKey)
        }
    }
    
    @Test
    fun `decrypt should reject invalid key size`() {
        // Arrange
        encryptionService = CommonEncryptionService()
        val key = encryptionService.generateKey()
        val encrypted = encryptionService.encrypt("test", key)
        val invalidKey = ByteArray(16)
        
        // Assert
        assertFailsWith<IllegalArgumentException> {
            encryptionService.decrypt(encrypted, invalidKey)
        }
    }
    
    @Test
    fun `encryptBytes should encrypt binary data`() {
        // Arrange
        encryptionService = CommonEncryptionService()
        val plainBytes = "Hello, World!".encodeToByteArray()
        val key = encryptionService.generateKey()
        
        // Act
        val encrypted = encryptionService.encryptBytes(plainBytes, key)
        
        // Assert
        assertTrue(encrypted.isNotEmpty(), "Encrypted bytes should not be empty")
        assertNotEquals(
            plainBytes.contentToString(),
            encrypted.contentToString(),
            "Encrypted bytes should differ from plaintext bytes"
        )
    }
    
    @Test
    fun `decryptBytes should return original binary data`() {
        // Arrange
        encryptionService = CommonEncryptionService()
        val plainBytes = "Hello, World!".encodeToByteArray()
        val key = encryptionService.generateKey()
        
        // Act
        val encrypted = encryptionService.encryptBytes(plainBytes, key)
        val decrypted = encryptionService.decryptBytes(encrypted, key)
        
        // Assert
        assertEquals(
            plainBytes.contentToString(),
            decrypted.contentToString(),
            "Decrypted bytes should match original plaintext bytes"
        )
    }
    
    @Test
    fun `encrypt should handle empty string`() {
        // Arrange
        encryptionService = CommonEncryptionService()
        val plaintext = ""
        val key = encryptionService.generateKey()
        
        // Act
        val encrypted = encryptionService.encrypt(plaintext, key)
        val decrypted = encryptionService.decrypt(encrypted, key)
        
        // Assert
        assertEquals(plaintext, decrypted, "Empty string should encrypt and decrypt correctly")
    }
    
    @Test
    fun `encrypt should handle large text`() {
        // Arrange
        encryptionService = CommonEncryptionService()
        val plaintext = "A".repeat(10000) // 10KB of text
        val key = encryptionService.generateKey()
        
        // Act
        val encrypted = encryptionService.encrypt(plaintext, key)
        val decrypted = encryptionService.decrypt(encrypted, key)
        
        // Assert
        assertEquals(plaintext, decrypted, "Large text should encrypt and decrypt correctly")
    }
    
    @Test
    fun `encrypt should handle unicode characters`() {
        // Arrange
        encryptionService = CommonEncryptionService()
        val plaintext = "Hello 世界 🌍 مرحبا"
        val key = encryptionService.generateKey()
        
        // Act
        val encrypted = encryptionService.encrypt(plaintext, key)
        val decrypted = encryptionService.decrypt(encrypted, key)
        
        // Assert
        assertEquals(plaintext, decrypted, "Unicode text should encrypt and decrypt correctly")
    }
}

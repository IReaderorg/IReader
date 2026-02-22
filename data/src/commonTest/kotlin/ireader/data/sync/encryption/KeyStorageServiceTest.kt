package ireader.data.sync.encryption

import ireader.domain.services.sync.KeyStorageService
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * TDD Tests for KeyStorageService.
 * 
 * Task 9.2.5: Write tests for secure key storage, then implement
 * 
 * These tests verify:
 * - Key storage and retrieval
 * - Key deletion
 * - Key existence checks
 * - Key listing
 * - Error handling
 */
abstract class KeyStorageServiceTest {
    
    protected abstract fun createKeyStorageService(): KeyStorageService
    
    @Test
    fun `storeKey should save key securely`() = runTest {
        // Arrange
        val service = createKeyStorageService()
        val alias = "test-key-1"
        val key = ByteArray(32) { it.toByte() } // 256-bit key
        
        // Act
        val result = service.storeKey(alias, key)
        
        // Assert
        assertTrue(result.isSuccess)
    }
    
    @Test
    fun `retrieveKey should return stored key`() = runTest {
        // Arrange
        val service = createKeyStorageService()
        val alias = "test-key-2"
        val originalKey = ByteArray(32) { it.toByte() }
        
        service.storeKey(alias, originalKey)
        
        // Act
        val result = service.retrieveKey(alias)
        
        // Assert
        assertTrue(result.isSuccess)
        val retrievedKey = result.getOrNull()
        assertNotNull(retrievedKey)
        assertContentEquals(originalKey, retrievedKey)
    }
    
    @Test
    fun `retrieveKey should fail for non-existent key`() = runTest {
        // Arrange
        val service = createKeyStorageService()
        val alias = "non-existent-key"
        
        // Act
        val result = service.retrieveKey(alias)
        
        // Assert
        assertTrue(result.isFailure)
    }
    
    @Test
    fun `deleteKey should remove stored key`() = runTest {
        // Arrange
        val service = createKeyStorageService()
        val alias = "test-key-3"
        val key = ByteArray(32) { it.toByte() }
        
        service.storeKey(alias, key)
        assertTrue(service.keyExists(alias))
        
        // Act
        val result = service.deleteKey(alias)
        
        // Assert
        assertTrue(result.isSuccess)
        assertFalse(service.keyExists(alias))
    }
    
    @Test
    fun `keyExists should return true for existing key`() = runTest {
        // Arrange
        val service = createKeyStorageService()
        val alias = "test-key-4"
        val key = ByteArray(32) { it.toByte() }
        
        service.storeKey(alias, key)
        
        // Act
        val exists = service.keyExists(alias)
        
        // Assert
        assertTrue(exists)
    }
    
    @Test
    fun `keyExists should return false for non-existent key`() = runTest {
        // Arrange
        val service = createKeyStorageService()
        val alias = "non-existent-key-2"
        
        // Act
        val exists = service.keyExists(alias)
        
        // Assert
        assertFalse(exists)
    }
    
    @Test
    fun `listKeys should return all stored key aliases`() = runTest {
        // Arrange
        val service = createKeyStorageService()
        val aliases = listOf("key-a", "key-b", "key-c")
        val key = ByteArray(32) { it.toByte() }
        
        aliases.forEach { alias ->
            service.storeKey(alias, key)
        }
        
        // Act
        val storedAliases = service.listKeys()
        
        // Assert
        assertTrue(storedAliases.containsAll(aliases))
    }
    
    @Test
    fun `storeKey should overwrite existing key with same alias`() = runTest {
        // Arrange
        val service = createKeyStorageService()
        val alias = "test-key-5"
        val originalKey = ByteArray(32) { it.toByte() }
        val newKey = ByteArray(32) { (it * 2).toByte() }
        
        service.storeKey(alias, originalKey)
        
        // Act
        service.storeKey(alias, newKey)
        val result = service.retrieveKey(alias)
        
        // Assert
        assertTrue(result.isSuccess)
        val retrievedKey = result.getOrNull()
        assertNotNull(retrievedKey)
        assertContentEquals(newKey, retrievedKey)
    }
    
    @Test
    fun `storeKey should reject keys that are not 256 bits`() = runTest {
        // Arrange
        val service = createKeyStorageService()
        val alias = "invalid-key"
        val invalidKey = ByteArray(16) { it.toByte() } // 128-bit key (invalid)
        
        // Act
        val result = service.storeKey(alias, invalidKey)
        
        // Assert
        assertTrue(result.isFailure)
    }
}

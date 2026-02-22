package ireader.domain.services.sync

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * TDD Tests for KeyStorageService (Phase 9.2.5)
 * 
 * Testing secure key storage functionality.
 */
class KeyStorageServiceTest {
    
    private lateinit var keyStorageService: KeyStorageService
    private lateinit var encryptionService: EncryptionService
    
    @Test
    fun `storeKey should successfully store a key`() = runTest {
        // Arrange
        keyStorageService = createKeyStorageService()
        encryptionService = CommonEncryptionService()
        val alias = "test_key_1"
        val key = encryptionService.generateKey()
        
        // Act
        val result = keyStorageService.storeKey(alias, key)
        
        // Assert
        assertTrue(result.isSuccess, "Key storage should succeed")
    }
    
    @Test
    fun `retrieveKey should return stored key`() = runTest {
        // Arrange
        keyStorageService = createKeyStorageService()
        encryptionService = CommonEncryptionService()
        val alias = "test_key_2"
        val originalKey = encryptionService.generateKey()
        
        // Act
        keyStorageService.storeKey(alias, originalKey)
        val result = keyStorageService.retrieveKey(alias)
        
        // Assert
        assertTrue(result.isSuccess, "Key retrieval should succeed")
        assertEquals(
            originalKey.contentToString(),
            result.getOrNull()?.contentToString(),
            "Retrieved key should match stored key"
        )
    }
    
    @Test
    fun `retrieveKey should fail for non-existent key`() = runTest {
        // Arrange
        keyStorageService = createKeyStorageService()
        val alias = "non_existent_key"
        
        // Act
        val result = keyStorageService.retrieveKey(alias)
        
        // Assert
        assertTrue(result.isFailure, "Retrieving non-existent key should fail")
    }
    
    @Test
    fun `deleteKey should remove stored key`() = runTest {
        // Arrange
        keyStorageService = createKeyStorageService()
        encryptionService = CommonEncryptionService()
        val alias = "test_key_3"
        val key = encryptionService.generateKey()
        
        // Act
        keyStorageService.storeKey(alias, key)
        val deleteResult = keyStorageService.deleteKey(alias)
        val retrieveResult = keyStorageService.retrieveKey(alias)
        
        // Assert
        assertTrue(deleteResult.isSuccess, "Key deletion should succeed")
        assertTrue(retrieveResult.isFailure, "Deleted key should not be retrievable")
    }
    
    @Test
    fun `keyExists should return true for stored key`() = runTest {
        // Arrange
        keyStorageService = createKeyStorageService()
        encryptionService = CommonEncryptionService()
        val alias = "test_key_4"
        val key = encryptionService.generateKey()
        
        // Act
        keyStorageService.storeKey(alias, key)
        val exists = keyStorageService.keyExists(alias)
        
        // Assert
        assertTrue(exists, "Stored key should exist")
    }
    
    @Test
    fun `keyExists should return false for non-existent key`() = runTest {
        // Arrange
        keyStorageService = createKeyStorageService()
        val alias = "non_existent_key_2"
        
        // Act
        val exists = keyStorageService.keyExists(alias)
        
        // Assert
        assertFalse(exists, "Non-existent key should not exist")
    }
    
    @Test
    fun `listKeys should return all stored key aliases`() = runTest {
        // Arrange
        keyStorageService = createKeyStorageService()
        encryptionService = CommonEncryptionService()
        val alias1 = "test_key_5"
        val alias2 = "test_key_6"
        val key1 = encryptionService.generateKey()
        val key2 = encryptionService.generateKey()
        
        // Act
        keyStorageService.storeKey(alias1, key1)
        keyStorageService.storeKey(alias2, key2)
        val keys = keyStorageService.listKeys()
        
        // Assert
        assertTrue(keys.contains(alias1), "List should contain first key alias")
        assertTrue(keys.contains(alias2), "List should contain second key alias")
    }
    
    @Test
    fun `storeKey should overwrite existing key with same alias`() = runTest {
        // Arrange
        keyStorageService = createKeyStorageService()
        encryptionService = CommonEncryptionService()
        val alias = "test_key_7"
        val key1 = encryptionService.generateKey()
        val key2 = encryptionService.generateKey()
        
        // Act
        keyStorageService.storeKey(alias, key1)
        keyStorageService.storeKey(alias, key2)
        val result = keyStorageService.retrieveKey(alias)
        
        // Assert
        assertTrue(result.isSuccess, "Key retrieval should succeed")
        assertEquals(
            key2.contentToString(),
            result.getOrNull()?.contentToString(),
            "Retrieved key should match second stored key"
        )
    }
    
    @Test
    fun `stored keys should persist across service instances`() = runTest {
        // Arrange
        val encryptionService = CommonEncryptionService()
        val alias = "test_key_8"
        val key = encryptionService.generateKey()
        
        // Act
        val service1 = createKeyStorageService()
        service1.storeKey(alias, key)
        
        val service2 = createKeyStorageService()
        val result = service2.retrieveKey(alias)
        
        // Assert
        assertTrue(result.isSuccess, "Key should persist across service instances")
        assertEquals(
            key.contentToString(),
            result.getOrNull()?.contentToString(),
            "Retrieved key should match stored key"
        )
    }
}

/**
 * Platform-specific factory function for creating KeyStorageService.
 * Implemented in platform-specific test source sets.
 */
expect fun createKeyStorageService(): KeyStorageService

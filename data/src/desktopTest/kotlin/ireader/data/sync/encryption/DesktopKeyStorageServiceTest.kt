package ireader.data.sync.encryption

import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.*

/**
 * Tests for DesktopKeyStorageService.
 * 
 * Tests secure key storage with randomly generated keystore password.
 */
class DesktopKeyStorageServiceTest {
    
    private lateinit var tempDir: File
    private lateinit var service: DesktopKeyStorageService
    
    @BeforeTest
    fun setup() {
        // Create temporary directory for test keystore
        tempDir = createTempDir("ireader_test_keystore")
        service = DesktopKeyStorageService()
    }
    
    @AfterTest
    fun cleanup() {
        // Clean up temporary directory
        tempDir.deleteRecursively()
    }
    
    @Test
    fun `storeKey should successfully store a 256-bit key`() = runTest {
        val alias = "test_key"
        val key = ByteArray(32) { it.toByte() } // 256-bit key
        
        val result = service.storeKey(alias, key)
        
        assertTrue(result.isSuccess, "Storing key should succeed")
    }
    
    @Test
    fun `retrieveKey should retrieve previously stored key`() = runTest {
        val alias = "test_key"
        val originalKey = ByteArray(32) { it.toByte() }
        
        service.storeKey(alias, originalKey)
        val result = service.retrieveKey(alias)
        
        assertTrue(result.isSuccess, "Retrieving key should succeed")
        assertContentEquals(originalKey, result.getOrNull(), "Retrieved key should match original")
    }
    
    @Test
    fun `keyExists should return true for stored key`() = runTest {
        val alias = "test_key"
        val key = ByteArray(32) { it.toByte() }
        
        service.storeKey(alias, key)
        val exists = service.keyExists(alias)
        
        assertTrue(exists, "Key should exist after storing")
    }
    
    @Test
    fun `keyExists should return false for non-existent key`() = runTest {
        val exists = service.keyExists("non_existent_key")
        
        assertFalse(exists, "Non-existent key should return false")
    }
    
    @Test
    fun `deleteKey should remove stored key`() = runTest {
        val alias = "test_key"
        val key = ByteArray(32) { it.toByte() }
        
        service.storeKey(alias, key)
        service.deleteKey(alias)
        val exists = service.keyExists(alias)
        
        assertFalse(exists, "Key should not exist after deletion")
    }
    
    @Test
    fun `listKeys should return all stored keys`() = runTest {
        val aliases = listOf("key1", "key2", "key3")
        val key = ByteArray(32) { it.toByte() }
        
        aliases.forEach { alias ->
            service.storeKey(alias, key)
        }
        
        val listedKeys = service.listKeys()
        
        assertTrue(listedKeys.containsAll(aliases), "All stored keys should be listed")
    }
    
    @Test
    fun `storeKey should reject keys that are not 256 bits`() = runTest {
        val alias = "test_key"
        val invalidKey = ByteArray(16) // 128-bit key
        
        val result = service.storeKey(alias, invalidKey)
        
        assertTrue(result.isFailure, "Storing non-256-bit key should fail")
    }
}

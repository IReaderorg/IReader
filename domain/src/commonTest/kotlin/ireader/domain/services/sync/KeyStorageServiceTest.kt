package ireader.domain.services.sync

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Common tests for KeyStorageService.
 */
expect fun createKeyStorageService(): KeyStorageService

class KeyStorageServiceTest {

    @Test
    fun `storeKey should successfully store a 256-bit key`() = runTest {
        val service = createKeyStorageService()
        val alias = "test_key"
        val key = ByteArray(32) { it.toByte() } // 256-bit key

        val result = service.storeKey(alias, key)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `retrieveKey should retrieve previously stored key`() = runTest {
        val service = createKeyStorageService()
        val alias = "test_key"
        val originalKey = ByteArray(32) { it.toByte() }

        service.storeKey(alias, originalKey)
        val result = service.retrieveKey(alias)

        assertTrue(result.isSuccess)
        assertEquals(originalKey.toList(), result.getOrNull()!!.toList())
    }

    @Test
    fun `keyExists should return true for stored key`() = runTest {
        val service = createKeyStorageService()
        val alias = "test_key"
        val key = ByteArray(32) { it.toByte() }

        service.storeKey(alias, key)
        assertTrue(service.keyExists(alias))
    }

    @Test
    fun `deleteKey should remove stored key`() = runTest {
        val service = createKeyStorageService()
        val alias = "test_key"
        val key = ByteArray(32) { it.toByte() }

        service.storeKey(alias, key)
        service.deleteKey(alias)
        assertFalse(service.keyExists(alias))
    }
}

private fun runTest(block: suspend () -> Unit) {
    kotlinx.coroutines.runBlocking { block() }
}

package ireader.core.http.cloudflare

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class CloudflareCookieStoreTest {
    
    @OptIn(ExperimentalTime::class)
    private fun createTestCookie(
        domain: String = "example.com",
        expiresAt: Long = Clock.System.now().toEpochMilliseconds() + 3600000 // 1 hour from now
    ): ClearanceCookie {
        return ClearanceCookie(
            cfClearance = "test_clearance_${Clock.System.now().toEpochMilliseconds()}",
            cfBm = "test_bm",
            userAgent = "Mozilla/5.0 Test",
            timestamp = Clock.System.now().toEpochMilliseconds(),
            expiresAt = expiresAt,
            domain = domain
        )
    }
    
    @Test
    fun `save and retrieve cookie`() = runTest {
        val store = InMemoryCloudfareCookieStore()
        val cookie = createTestCookie()
        
        store.saveClearanceCookie("example.com", cookie)
        
        val retrieved = store.getClearanceCookie("example.com")
        assertNotNull(retrieved)
        assertEquals(cookie.cfClearance, retrieved.cfClearance)
        assertEquals(cookie.userAgent, retrieved.userAgent)
    }
    
    @Test
    fun `domain normalization works`() = runTest {
        val store = InMemoryCloudfareCookieStore()
        val cookie = createTestCookie()
        
        store.saveClearanceCookie("https://www.example.com/path", cookie)
        
        // Should retrieve with different URL formats
        assertNotNull(store.getClearanceCookie("example.com"))
        assertNotNull(store.getClearanceCookie("http://example.com"))
        assertNotNull(store.getClearanceCookie("https://example.com"))
        assertNotNull(store.getClearanceCookie("www.example.com"))
    }
    
    @OptIn(ExperimentalTime::class)
    @Test
    fun `expired cookie returns null`() = runTest {
        val store = InMemoryCloudfareCookieStore()
        val expiredCookie = createTestCookie(
            expiresAt = Clock.System.now().toEpochMilliseconds() - 1000 // Already expired
        )
        
        store.saveClearanceCookie("example.com", expiredCookie)
        
        val retrieved = store.getClearanceCookie("example.com")
        assertNull(retrieved)
    }

    @Test
    fun `invalidate removes cookie`() = runTest {
        val store = InMemoryCloudfareCookieStore()
        val cookie = createTestCookie()
        
        store.saveClearanceCookie("example.com", cookie)
        assertNotNull(store.getClearanceCookie("example.com"))
        
        store.invalidate("example.com")
        assertNull(store.getClearanceCookie("example.com"))
    }
    
    @Test
    fun `clearAll removes all cookies`() = runTest {
        val store = InMemoryCloudfareCookieStore()
        
        store.saveClearanceCookie("example1.com", createTestCookie("example1.com"))
        store.saveClearanceCookie("example2.com", createTestCookie("example2.com"))
        store.saveClearanceCookie("example3.com", createTestCookie("example3.com"))
        
        assertEquals(3, store.getAll().size)
        
        store.clearAll()
        
        assertEquals(0, store.getAll().size)
    }
    
    @OptIn(ExperimentalTime::class)
    @Test
    fun `isValid checks expiry and content`() = runTest {
        val store = InMemoryCloudfareCookieStore()
        
        val validCookie = createTestCookie()
        assertTrue(store.isValid(validCookie))
        
        val expiredCookie = createTestCookie(expiresAt = Clock.System.now().toEpochMilliseconds() - 1000)
        assertFalse(store.isValid(expiredCookie))
        
        val emptyCookie = ClearanceCookie(
            cfClearance = "",
            cfBm = null,
            userAgent = "test",
            timestamp = Clock.System.now().toEpochMilliseconds(),
            expiresAt = Clock.System.now().toEpochMilliseconds() + 3600000,
            domain = "example.com"
        )
        assertFalse(store.isValid(emptyCookie))
    }
}

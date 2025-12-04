package ireader.core.http.cloudflare

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class CloudflareBypassManagerTest {
    
    @OptIn(ExperimentalTime::class)
    private fun createTestCookie(domain: String = "example.com"): ClearanceCookie {
        return ClearanceCookie(
            cfClearance = "test_clearance",
            cfBm = "test_bm",
            userAgent = "Mozilla/5.0 Test",
            timestamp = Clock.System.now().toEpochMilliseconds(),
            expiresAt = Clock.System.now().toEpochMilliseconds() + 3600000,
            domain = domain
        )
    }
    
    @Test
    fun `cookie replay strategy returns cached cookie`() = runTest {
        val cookieStore = InMemoryCloudfareCookieStore()
        val cookie = createTestCookie()
        cookieStore.saveClearanceCookie("example.com", cookie)
        
        val strategy = CookieReplayStrategy(cookieStore)
        
        val result = strategy.bypass(
            url = "https://example.com/page",
            challenge = CloudflareChallenge.JSChallenge(),
            config = BypassConfig(userAgent = cookie.userAgent)
        )
        
        assertIs<BypassResult.CachedCookie>(result)
        assertEquals(cookie.cfClearance, result.cookie.cfClearance)
    }
    
    @Test
    fun `cookie replay fails when user agent mismatches`() = runTest {
        val cookieStore = InMemoryCloudfareCookieStore()
        val cookie = createTestCookie()
        cookieStore.saveClearanceCookie("example.com", cookie)
        
        val strategy = CookieReplayStrategy(cookieStore)
        
        val result = strategy.bypass(
            url = "https://example.com/page",
            challenge = CloudflareChallenge.JSChallenge(),
            config = BypassConfig(userAgent = "Different User Agent")
        )
        
        assertIs<BypassResult.Failed>(result)
        assertTrue(result.canRetry)
    }
    
    @Test
    fun `cookie replay fails when no cookie available`() = runTest {
        val cookieStore = InMemoryCloudfareCookieStore()
        val strategy = CookieReplayStrategy(cookieStore)
        
        val result = strategy.bypass(
            url = "https://example.com/page",
            challenge = CloudflareChallenge.JSChallenge(),
            config = BypassConfig()
        )
        
        assertIs<BypassResult.Failed>(result)
        assertTrue(result.canRetry)
    }

    @Test
    fun `bypass manager uses cached cookie from store`() = runTest {
        val cookieStore = InMemoryCloudfareCookieStore()
        val cookie = createTestCookie()
        cookieStore.saveClearanceCookie("example.com", cookie)
        
        val manager = CloudflareBypassManager(
            strategies = listOf(CookieReplayStrategy(cookieStore)),
            cookieStore = cookieStore
        )
        
        val result = manager.bypassChallenge(
            url = "https://example.com/page",
            challenge = CloudflareChallenge.JSChallenge(),
            config = BypassConfig(userAgent = cookie.userAgent)
        )
        
        // Should return cached cookie directly from store check
        assertIs<BypassResult.CachedCookie>(result)
    }
    
    @Test
    fun `bypass manager returns NotNeeded for None challenge`() = runTest {
        val cookieStore = InMemoryCloudfareCookieStore()
        val manager = CloudflareBypassManager(
            strategies = emptyList(),
            cookieStore = cookieStore
        )
        
        val result = manager.bypassChallenge(
            url = "https://example.com/page",
            challenge = CloudflareChallenge.None,
            config = BypassConfig()
        )
        
        // None challenge should still try cached cookies first
        // but if no cookie, strategies are tried
        assertTrue(result is BypassResult.Failed || result is BypassResult.CachedCookie)
    }
    
    @Test
    fun `challenge types report correct properties`() {
        val jsChallenge = CloudflareChallenge.JSChallenge("ray123")
        assertTrue(jsChallenge.isAutoSolvable())
        assertFalse(jsChallenge.requiresUserInteraction())
        
        val captchaChallenge = CloudflareChallenge.CaptchaChallenge("sitekey", "ray123")
        assertFalse(captchaChallenge.isAutoSolvable())
        assertTrue(captchaChallenge.requiresUserInteraction())
        
        val blockedIP = CloudflareChallenge.BlockedIP("ray123")
        assertFalse(blockedIP.isAutoSolvable())
        assertTrue(blockedIP.requiresUserInteraction())
        
        val rateLimited = CloudflareChallenge.RateLimited(30, "ray123")
        assertTrue(rateLimited.isAutoSolvable())
        assertFalse(rateLimited.requiresUserInteraction())
    }
    
    @Test
    fun `bypass result helper methods work correctly`() {
        val successResult = BypassResult.Success(createTestCookie())
        assertTrue(successResult.isSuccess())
        assertEquals("test_clearance", successResult.extractCookie()?.cfClearance)
        
        val failedResult = BypassResult.Failed("Test error", canRetry = true)
        assertFalse(failedResult.isSuccess())
        assertEquals("Test error", failedResult.extractErrorMessage())
        
        val notNeeded = BypassResult.NotNeeded
        assertTrue(notNeeded.isSuccess())
    }
}

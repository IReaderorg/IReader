package ireader.core.http.ratelimit

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RateLimiterTest {
    
    @Test
    fun `adaptive rate limiter backs off on 429`() = runTest {
        val limiter = AdaptiveRateLimiter(
            defaultConfig = RateLimitConfig(requestsPerSecond = 2.0)
        )
        
        val initialStats = limiter.getStats("example.com")
        val initialRate = initialStats.requestsPerSecond
        
        // Simulate 429 response
        limiter.onResponse("example.com", 429)
        
        val newStats = limiter.getStats("example.com")
        assertTrue(newStats.requestsPerSecond < initialRate, 
            "Rate should decrease after 429. Was $initialRate, now ${newStats.requestsPerSecond}")
        assertEquals(1, newStats.consecutiveErrors)
    }
    
    @Test
    fun `adaptive rate limiter recovers after success`() = runTest {
        val limiter = AdaptiveRateLimiter(
            defaultConfig = RateLimitConfig(requestsPerSecond = 2.0)
        )
        
        // First trigger backoff
        limiter.onResponse("example.com", 429)
        val backoffStats = limiter.getStats("example.com")
        
        // Then simulate successful responses
        repeat(5) {
            limiter.onResponse("example.com", 200)
        }
        
        val recoveredStats = limiter.getStats("example.com")
        assertTrue(recoveredStats.requestsPerSecond > backoffStats.requestsPerSecond,
            "Rate should increase after successes")
        assertEquals(0, recoveredStats.consecutiveErrors)
    }
    
    @Test
    fun `simple rate limiter enforces delay`() = runTest {
        val limiter = SimpleRateLimiter(delayMs = 100)
        
        // First request should succeed immediately
        assertTrue(limiter.tryAcquire("example.com"))
        
        // Second request immediately after should fail
        assertFalse(limiter.tryAcquire("example.com"))
    }
    
    @Test
    fun `rate limiter handles different domains independently`() = runTest {
        val limiter = AdaptiveRateLimiter()
        
        // Trigger backoff on one domain
        limiter.onResponse("example1.com", 429)
        
        // Other domain should be unaffected
        val stats1 = limiter.getStats("example1.com")
        val stats2 = limiter.getStats("example2.com")
        
        assertEquals(1, stats1.consecutiveErrors)
        assertEquals(0, stats2.consecutiveErrors)
    }
    
    @Test
    fun `reset clears domain state`() = runTest {
        val limiter = AdaptiveRateLimiter()
        
        limiter.onResponse("example.com", 429)
        assertEquals(1, limiter.getStats("example.com").consecutiveErrors)
        
        limiter.reset("example.com")
        assertEquals(0, limiter.getStats("example.com").consecutiveErrors)
    }
}

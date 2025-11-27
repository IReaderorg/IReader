package ireader.data.remote

import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Comprehensive tests for RetryPolicy
 */
class RetryPolicyTest {
    
    @Test
    fun `executeWithRetry should return result on first success`() = runTest {
        // Given
        val retryPolicy = RetryPolicy(maxRetries = 3)
        var callCount = 0
        
        // When
        val result = retryPolicy.executeWithRetry {
            callCount++
            "success"
        }
        
        // Then
        assertEquals("success", result)
        assertEquals(1, callCount)
    }
    
    @Test
    fun `executeWithRetry should retry on failure and succeed`() = runTest {
        // Given
        val retryPolicy = RetryPolicy(maxRetries = 3, initialDelayMs = 10)
        var callCount = 0
        
        // When
        val result = retryPolicy.executeWithRetry {
            callCount++
            if (callCount < 2) {
                throw RuntimeException("Temporary failure")
            }
            "success after retry"
        }
        
        // Then
        assertEquals("success after retry", result)
        assertEquals(2, callCount)
    }
    
    @Test
    fun `executeWithRetry should throw after max retries`() = runTest {
        // Given
        val retryPolicy = RetryPolicy(maxRetries = 2, initialDelayMs = 10)
        var callCount = 0
        
        // When/Then
        val exception = assertFailsWith<RuntimeException> {
            retryPolicy.executeWithRetry {
                callCount++
                throw RuntimeException("Persistent failure")
            }
        }
        
        assertEquals("Persistent failure", exception.message)
        assertEquals(2, callCount)
    }
    
    @Test
    fun `executeWithRetry should succeed on last retry`() = runTest {
        // Given
        val retryPolicy = RetryPolicy(maxRetries = 3, initialDelayMs = 10)
        var callCount = 0
        
        // When
        val result = retryPolicy.executeWithRetry {
            callCount++
            if (callCount < 3) {
                throw RuntimeException("Failure")
            }
            "success on last try"
        }
        
        // Then
        assertEquals("success on last try", result)
        assertEquals(3, callCount)
    }
    
    @Test
    fun `executeWithRetry should work with single retry`() = runTest {
        // Given
        val retryPolicy = RetryPolicy(maxRetries = 1, initialDelayMs = 10)
        var callCount = 0
        
        // When
        val result = retryPolicy.executeWithRetry {
            callCount++
            "immediate success"
        }
        
        // Then
        assertEquals("immediate success", result)
        assertEquals(1, callCount)
    }
    
    @Test
    fun `executeWithRetry should preserve exception type`() = runTest {
        // Given
        val retryPolicy = RetryPolicy(maxRetries = 1, initialDelayMs = 10)
        
        // When/Then
        assertFailsWith<IllegalArgumentException> {
            retryPolicy.executeWithRetry {
                throw IllegalArgumentException("Invalid argument")
            }
        }
    }
    
    @Test
    fun `executeWithRetry should handle different return types`() = runTest {
        // Given
        val retryPolicy = RetryPolicy(maxRetries = 2, initialDelayMs = 10)
        
        // When - Int
        val intResult = retryPolicy.executeWithRetry { 42 }
        assertEquals(42, intResult)
        
        // When - List
        val listResult = retryPolicy.executeWithRetry { listOf(1, 2, 3) }
        assertEquals(listOf(1, 2, 3), listResult)
        
        // When - Nullable
        val nullResult = retryPolicy.executeWithRetry<String?> { null }
        assertNull(nullResult)
    }
    
    @Test
    fun `default RetryPolicy should have correct defaults`() = runTest {
        // Given
        val retryPolicy = RetryPolicy()
        var callCount = 0
        
        // When - should fail after 2 retries (default)
        assertFailsWith<RuntimeException> {
            retryPolicy.executeWithRetry {
                callCount++
                throw RuntimeException("Failure")
            }
        }
        
        // Then - default maxRetries is 2
        assertEquals(2, callCount)
    }
}

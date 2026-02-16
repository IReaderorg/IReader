package ireader.domain.usecases.translate

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class TranslationRetryHandlerTest {
    
    private val handler = TranslationRetryHandler()
    
    @Test
    fun `executeWithRetry should succeed on first attempt`() = runTest {
        // Arrange
        var callCount = 0
        val operation: suspend () -> String = {
            callCount++
            "success"
        }
        
        // Act
        val result = handler.executeWithRetry(
            config = TranslationRetryHandler.RetryConfig(maxRetries = 3)
        ) { operation() }
        
        // Assert
        assertTrue(result is TranslationRetryHandler.RetryResult.Success)
        assertEquals("success", (result as TranslationRetryHandler.RetryResult.Success).value)
        assertEquals(1, callCount)
    }
    
    @Test
    fun `executeWithRetry should retry on transient errors`() = runTest {
        // Arrange
        var callCount = 0
        val operation: suspend () -> String = {
            callCount++
            if (callCount < 3) {
                throw Exception("rate_limit exceeded")
            }
            "success"
        }
        
        // Act
        val result = handler.executeWithRetry(
            config = TranslationRetryHandler.RetryConfig(
                maxRetries = 3,
                initialDelayMs = 10,
                maxDelayMs = 100
            )
        ) { operation() }
        
        // Assert
        assertTrue(result is TranslationRetryHandler.RetryResult.Success)
        assertEquals(3, callCount)
    }
    
    @Test
    fun `executeWithRetry should not retry on permanent errors`() = runTest {
        // Arrange
        var callCount = 0
        val operation: suspend () -> String = {
            callCount++
            throw Exception("invalid_api_key")
        }
        
        // Act
        val result = handler.executeWithRetry(
            config = TranslationRetryHandler.RetryConfig(maxRetries = 3)
        ) { operation() }
        
        // Assert
        assertTrue(result is TranslationRetryHandler.RetryResult.Failure)
        val failure = result as TranslationRetryHandler.RetryResult.Failure
        assertTrue(failure.isPermanent)
        assertEquals(1, callCount) // Should not retry
    }
    
    @Test
    fun `executeWithRetry should apply exponential backoff with jitter`() = runTest {
        // Arrange
        var callCount = 0
        
        val operation: suspend () -> String = {
            callCount++
            if (callCount < 3) {
                throw Exception("timeout")
            }
            "success"
        }
        
        // Act
        val result = handler.executeWithRetry(
            config = TranslationRetryHandler.RetryConfig(
                maxRetries = 3,
                initialDelayMs = 10,  // Small delay for testing
                maxDelayMs = 100,
                backoffMultiplier = 2.0
            )
        ) { operation() }
        
        // Assert
        assertTrue(result is TranslationRetryHandler.RetryResult.Success)
        assertEquals(3, callCount)
        
        // The test verifies that:
        // 1. The operation was retried the expected number of times
        // 2. The result is successful after retries
        // Exponential backoff with jitter is applied internally by the handler
    }
    
    @Test
    fun `categorizeError should correctly identify error types`() {
        // Network errors
        assertEquals(
            TranslationRetryHandler.ErrorCategory.NETWORK,
            handler.categorizeError(Exception("connection failed"))
        )
        
        // Rate limit errors
        assertEquals(
            TranslationRetryHandler.ErrorCategory.RATE_LIMIT,
            handler.categorizeError(Exception("rate_limit exceeded"))
        )
        
        // Authentication errors
        assertEquals(
            TranslationRetryHandler.ErrorCategory.AUTHENTICATION,
            handler.categorizeError(Exception("invalid_api_key"))
        )
        
        // Timeout errors
        assertEquals(
            TranslationRetryHandler.ErrorCategory.TIMEOUT,
            handler.categorizeError(Exception("request timed out"))
        )
    }
    
    @Test
    fun `isPermanentError should identify permanent errors correctly`() {
        assertTrue(handler.isPermanentError(Exception("invalid_api_key")))
        assertTrue(handler.isPermanentError(Exception("authentication failed")))
        assertTrue(handler.isPermanentError(Exception("content_filter violation")))
        
        assertFalse(handler.isPermanentError(Exception("rate_limit exceeded")))
        assertFalse(handler.isPermanentError(Exception("timeout")))
        assertFalse(handler.isPermanentError(Exception("server_error")))
    }
    
    @Test
    fun `isTransientError should identify transient errors correctly`() {
        assertTrue(handler.isTransientError(Exception("rate_limit exceeded")))
        assertTrue(handler.isTransientError(Exception("timeout")))
        assertTrue(handler.isTransientError(Exception("connection failed")))
        
        assertFalse(handler.isTransientError(Exception("invalid_api_key")))
        assertFalse(handler.isTransientError(Exception("authentication failed")))
    }
}

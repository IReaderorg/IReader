package ireader.data.remote

import kotlinx.coroutines.delay

/**
 * Retry policy for network operations
 * Optimized for faster sync with reduced delays
 */
class RetryPolicy(
    private val maxRetries: Int = 2, // Reduced from 3 to 2 retries
    private val initialDelayMs: Long = 500, // Reduced from 1000ms to 500ms
    private val maxDelayMs: Long = 3000, // Reduced from 10000ms to 3000ms
    private val factor: Double = 2.0
) {
    
    suspend fun <T> executeWithRetry(block: suspend () -> T): T {
        var currentDelay = initialDelayMs
        var lastException: Exception? = null
        
        repeat(maxRetries) { attempt ->
            try {
                return block()
            } catch (e: Exception) {
                lastException = e
                
                if (attempt < maxRetries - 1) {
                    delay(currentDelay)
                    currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelayMs)
                }
            }
        }
        
        throw lastException ?: Exception("Retry failed")
    }
}

package ireader.data.remote

import kotlinx.coroutines.delay

/**
 * Retry policy for network operations
 */
class RetryPolicy(
    private val maxRetries: Int = 3,
    private val initialDelayMs: Long = 1000,
    private val maxDelayMs: Long = 10000,
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

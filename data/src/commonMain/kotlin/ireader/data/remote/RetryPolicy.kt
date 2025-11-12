package ireader.data.remote

import kotlinx.coroutines.delay

/**
 * Retry policy with exponential backoff for failed operations
 */
class RetryPolicy {
    
    companion object {
        const val MAX_RETRIES = 3
        const val INITIAL_DELAY_MS = 1000L
        const val MAX_DELAY_MS = 10000L
        const val BACKOFF_MULTIPLIER = 2.0
    }
    
    /**
     * Executes an operation with retry logic and exponential backoff
     * 
     * @param operation The suspend function to execute
     * @return Result containing the operation result or the last exception
     */
    suspend fun <T> executeWithRetry(
        operation: suspend () -> T
    ): Result<T> {
        var currentDelay = INITIAL_DELAY_MS
        var lastException: Exception? = null
        
        repeat(MAX_RETRIES) { attempt ->
            try {
                return Result.success(operation())
            } catch (e: Exception) {
                lastException = e
                if (attempt < MAX_RETRIES - 1) {
                    delay(currentDelay)
                    currentDelay = (currentDelay * BACKOFF_MULTIPLIER)
                        .toLong()
                        .coerceAtMost(MAX_DELAY_MS)
                }
            }
        }
        
        return Result.failure(lastException ?: Exception("Unknown error"))
    }
}

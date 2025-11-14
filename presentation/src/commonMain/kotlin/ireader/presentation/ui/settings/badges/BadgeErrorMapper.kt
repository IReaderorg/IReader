package ireader.presentation.ui.settings.badges

import ireader.domain.models.badge.BadgeError

/**
 * Maps domain badge errors to user-friendly messages for display in the UI.
 */
object BadgeErrorMapper {
    
    /**
     * Converts a BadgeError to a user-friendly error message.
     */
    fun toUserMessage(error: BadgeError): String = when (error) {
        is BadgeError.InvalidWalletAddress -> 
            "Invalid wallet address format. Please check and try again."
        
        is BadgeError.PaymentProofRequired -> 
            "Payment proof is required to complete purchase."
        
        is BadgeError.BadgeAlreadyOwned -> 
            "You already own this badge."
        
        is BadgeError.NetworkError -> 
            "Network error. Please check your connection and try again."
        
        is BadgeError.VerificationFailed -> 
            "Verification failed. Please try again later."
        
        is BadgeError.InsufficientPermissions -> 
            "You don't have permission to perform this action."
        
        is BadgeError.ServerError -> 
            "Server error. Please try again later."
        
        is BadgeError.MaxFeaturedBadgesExceeded -> 
            "You can only select up to 3 featured badges."
        
        is BadgeError.BadgeNotFound -> 
            "Badge not found."
        
        is BadgeError.InvalidBadgeSelection -> 
            "Invalid badge selection. Please try again."
    }
    
    /**
     * Converts a generic Throwable to a user-friendly error message.
     */
    fun toUserMessage(throwable: Throwable): String = when (throwable) {
        is BadgeError -> toUserMessage(throwable)
        else -> "An unexpected error occurred: ${throwable.message ?: "Unknown error"}"
    }
}

/**
 * Configuration for retry logic with exponential backoff.
 */
data class RetryConfig(
    val maxRetries: Int,
    val initialDelayMs: Long,
    val maxDelayMs: Long,
    val factor: Double = 2.0
) {
    companion object {
        /**
         * Retry configuration for badge fetching operations.
         * Max 3 retries with exponential backoff starting at 1 second.
         */
        val BADGE_FETCH = RetryConfig(
            maxRetries = 3,
            initialDelayMs = 1000,
            maxDelayMs = 8000
        )
        
        /**
         * Retry configuration for NFT verification operations.
         * Max 2 retries with exponential backoff starting at 1 second.
         */
        val NFT_VERIFICATION = RetryConfig(
            maxRetries = 2,
            initialDelayMs = 1000,
            maxDelayMs = 4000
        )
        
        /**
         * Retry configuration for payment proof submission.
         * Max 2 retries with exponential backoff starting at 1 second.
         */
        val PAYMENT_SUBMISSION = RetryConfig(
            maxRetries = 2,
            initialDelayMs = 1000,
            maxDelayMs = 4000
        )
    }
}

/**
 * Executes a suspending operation with exponential backoff retry logic.
 * 
 * @param config The retry configuration to use
 * @param operation The suspending operation to execute
 * @return The result of the operation
 */
suspend fun <T> retryWithExponentialBackoff(
    config: RetryConfig,
    operation: suspend () -> Result<T>
): Result<T> {
    var currentDelay = config.initialDelayMs
    var lastException: Throwable? = null
    
    repeat(config.maxRetries + 1) { attempt ->
        try {
            val result = operation()
            if (result.isSuccess) {
                return result
            }
            lastException = result.exceptionOrNull()
        } catch (e: Exception) {
            lastException = e
        }
        
        // Don't delay after the last attempt
        if (attempt < config.maxRetries) {
            kotlinx.coroutines.delay(currentDelay)
            currentDelay = (currentDelay * config.factor).toLong().coerceAtMost(config.maxDelayMs)
        }
    }
    
    // All retries failed, return the last error
    return Result.failure(lastException ?: Exception("Operation failed after ${config.maxRetries} retries"))
}

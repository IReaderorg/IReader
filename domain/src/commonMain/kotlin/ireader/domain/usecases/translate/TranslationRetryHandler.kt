package ireader.domain.usecases.translate

import ireader.core.log.Log
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Handles retry logic for translation operations with exponential backoff.
 * Provides robust error handling for transient failures.
 */
class TranslationRetryHandler {
    
    companion object {
        private const val TAG = "TranslationRetry"
        
        // Default retry configuration
        const val DEFAULT_MAX_RETRIES = 3
        const val DEFAULT_INITIAL_DELAY_MS = 1000L
        const val DEFAULT_MAX_DELAY_MS = 30000L
        const val DEFAULT_BACKOFF_MULTIPLIER = 2.0
        
        // Errors that should NOT be retried (permanent failures)
        private val PERMANENT_ERROR_PATTERNS = listOf(
            "invalid_api_key",
            "api_key_invalid",
            "authentication",
            "unauthorized",
            "forbidden",
            "not_found",
            "model_not_found",
            "insufficient_quota",
            "billing",
            "content_filter",
            "safety",
            "unsupported_language",
            "invalid_request"
        )
        
        // Errors that should be retried (transient failures)
        private val TRANSIENT_ERROR_PATTERNS = listOf(
            "rate_limit",
            "too_many_requests",
            "timeout",
            "timed out",
            "connection",
            "network",
            "server_error",
            "internal_error",
            "service_unavailable",
            "overloaded",
            "capacity",
            "temporarily"
        )
    }
    
    /**
     * Result of a retry operation
     */
    sealed class RetryResult<T> {
        data class Success<T>(val value: T) : RetryResult<T>()
        data class Failure<T>(
            val error: Throwable,
            val attempts: Int,
            val isPermanent: Boolean,
            val errorCategory: ErrorCategory
        ) : RetryResult<T>()
    }
    
    /**
     * Categories of translation errors
     */
    enum class ErrorCategory {
        NETWORK,           // Connection issues
        RATE_LIMIT,        // API rate limiting
        SERVER_ERROR,      // Server-side errors (5xx)
        AUTHENTICATION,    // API key issues
        CONTENT_FILTER,    // Content policy violations
        INVALID_REQUEST,   // Bad request format
        MODEL_ERROR,       // Model-specific errors
        TIMEOUT,           // Request timeout
        UNKNOWN            // Unclassified errors
    }
    
    /**
     * Configuration for retry behavior
     */
    data class RetryConfig(
        val maxRetries: Int = DEFAULT_MAX_RETRIES,
        val initialDelayMs: Long = DEFAULT_INITIAL_DELAY_MS,
        val maxDelayMs: Long = DEFAULT_MAX_DELAY_MS,
        val backoffMultiplier: Double = DEFAULT_BACKOFF_MULTIPLIER,
        val retryOnTransientOnly: Boolean = true
    )
    
    /**
     * Execute an operation with retry logic and exponential backoff.
     * 
     * @param config Retry configuration
     * @param operation The suspend operation to execute
     * @param onError Optional callback for each error (for logging/monitoring)
     * @return RetryResult containing either success or failure with details
     */
    suspend fun <T> executeWithRetry(
        config: RetryConfig = RetryConfig(),
        onError: ((Throwable, Int, ErrorCategory) -> Unit)? = null,
        operation: suspend () -> T
    ): RetryResult<T> {
        var lastError: Throwable? = null
        var currentDelay = config.initialDelayMs
        var attempts = 0
        
        while (attempts <= config.maxRetries) {
            attempts++
            
            try {
                val result = operation()
                if (attempts > 1) {
                    Log.info { "$TAG: Operation succeeded after $attempts attempts" }
                }
                return RetryResult.Success(result)
            } catch (e: Exception) {
                lastError = e
                val errorCategory = categorizeError(e)
                val isPermanent = isPermanentError(e, errorCategory)
                
                Log.warn { "$TAG: Attempt $attempts failed (${errorCategory.name}): ${e.message}" }
                
                // Notify error callback
                onError?.invoke(e, attempts, errorCategory)
                
                // Don't retry permanent errors
                if (isPermanent && config.retryOnTransientOnly) {
                    Log.error { "$TAG: Permanent error detected, not retrying: ${e.message}" }
                    return RetryResult.Failure(e, attempts, true, errorCategory)
                }
                
                // Check if we've exhausted retries
                if (attempts > config.maxRetries) {
                    Log.error { "$TAG: Max retries ($config.maxRetries) exceeded" }
                    return RetryResult.Failure(e, attempts, false, errorCategory)
                }
                
                // Apply exponential backoff
                Log.info { "$TAG: Retrying in ${currentDelay}ms (attempt ${attempts + 1}/${config.maxRetries + 1})" }
                delay(currentDelay)
                
                // Calculate next delay with exponential backoff
                currentDelay = (currentDelay * config.backoffMultiplier).toLong()
                    .coerceAtMost(config.maxDelayMs)
                
                // Add jitter (±10%) to prevent thundering herd
                val jitter = currentDelay * (0.9 + Random.nextDouble() * 0.2)
                currentDelay = jitter.toLong()
            }
        }
        
        // Should not reach here, but handle gracefully
        return RetryResult.Failure(
            lastError ?: Exception("Unknown error after $attempts attempts"),
            attempts,
            false,
            ErrorCategory.UNKNOWN
        )
    }
    
    /**
     * Categorize an error based on its message and type.
     */
    fun categorizeError(error: Throwable): ErrorCategory {
        val message = error.message?.lowercase() ?: ""
        
        return when {
            // Network/connection errors
            message.contains("timeout") || message.contains("timed out") -> ErrorCategory.TIMEOUT
            message.contains("connection") || message.contains("network") -> ErrorCategory.NETWORK
            
            // Rate limiting
            message.contains("rate_limit") || message.contains("too_many_requests") -> ErrorCategory.RATE_LIMIT
            
            // Authentication
            message.contains("api_key") || message.contains("authentication") || 
            message.contains("unauthorized") || message.contains("forbidden") -> ErrorCategory.AUTHENTICATION
            
            // Content filter
            message.contains("content_filter") || message.contains("safety") -> ErrorCategory.CONTENT_FILTER
            
            // Server errors
            message.contains("server_error") || message.contains("internal_error") || 
            message.contains("service_unavailable") || message.contains("500") || 
            message.contains("502") || message.contains("503") -> ErrorCategory.SERVER_ERROR
            
            // Model errors
            message.contains("model") && (message.contains("not_found") || message.contains("error")) -> ErrorCategory.MODEL_ERROR
            
            // Invalid request
            message.contains("invalid_request") || message.contains("bad_request") -> ErrorCategory.INVALID_REQUEST
            
            else -> ErrorCategory.UNKNOWN
        }
    }
    
    /**
     * Determine if an error is permanent (should not be retried).
     */
    fun isPermanentError(error: Throwable, category: ErrorCategory? = null): Boolean {
        val message = error.message?.lowercase() ?: ""
        val errorCategory = category ?: categorizeError(error)
        
        // Check error category first
        when (errorCategory) {
            ErrorCategory.AUTHENTICATION,
            ErrorCategory.CONTENT_FILTER,
            ErrorCategory.INVALID_REQUEST -> return true
            else -> {}
        }
        
        // Check message patterns
        return PERMANENT_ERROR_PATTERNS.any { pattern -> 
            message.contains(pattern) 
        }
    }
    
    /**
     * Determine if an error is transient (can be retried).
     */
    fun isTransientError(error: Throwable): Boolean {
        val message = error.message?.lowercase() ?: ""
        
        return TRANSIENT_ERROR_PATTERNS.any { pattern -> 
            message.contains(pattern) 
        }
    }
    
    /**
     * Get a user-friendly error message based on the error category.
     */
    fun getUserFriendlyMessage(error: Throwable, category: ErrorCategory? = null): String {
        val errorCategory = category ?: categorizeError(error)
        
        return when (errorCategory) {
            ErrorCategory.NETWORK -> "Connection error. Please check if the translation service is running and accessible."
            ErrorCategory.RATE_LIMIT -> "Translation service is busy. Please try again in a moment."
            ErrorCategory.SERVER_ERROR -> "Translation service is temporarily unavailable. Please try again later."
            ErrorCategory.AUTHENTICATION -> "API key is invalid or missing. Please check your settings."
            ErrorCategory.CONTENT_FILTER -> "Content could not be translated due to policy restrictions."
            ErrorCategory.INVALID_REQUEST -> "Invalid translation request. Please check your settings."
            ErrorCategory.MODEL_ERROR -> "The selected translation model is not available."
            ErrorCategory.TIMEOUT -> "Translation request timed out. The model may be loading, please wait and try again."
            ErrorCategory.UNKNOWN -> "Translation failed: ${error.message ?: "Unknown error"}"
        }
    }
    
    /**
     * Get a user-friendly error message, with special handling for offline engines.
     */
    fun getUserFriendlyMessage(error: Throwable, category: ErrorCategory?, isOfflineEngine: Boolean = false): String {
        val errorCategory = category ?: categorizeError(error)
        
        // For offline engines (like Ollama), provide more specific messages
        if (isOfflineEngine) {
            return when (errorCategory) {
                ErrorCategory.NETWORK -> "Cannot connect to local translation service. Please ensure Ollama or your local LLM server is running."
                ErrorCategory.TIMEOUT -> "Local translation is taking longer than expected. The model may be loading into memory. Please wait..."
                ErrorCategory.SERVER_ERROR -> "Local translation service error. The model may still be loading. Please wait and try again."
                else -> getUserFriendlyMessage(error, category)
            }
        }
        
        return getUserFriendlyMessage(error, category)
    }
    
    /**
     * Calculate the recommended wait time before retry based on error type.
     */
    @OptIn(ExperimentalTime::class)
    fun getRecommendedWaitTime(error: Throwable): Long {
        val category = categorizeError(error)
        
        return when (category) {
            ErrorCategory.RATE_LIMIT -> 60000L // 1 minute for rate limits
            ErrorCategory.SERVER_ERROR -> 30000L // 30 seconds for server errors
            ErrorCategory.NETWORK -> 5000L // 5 seconds for network issues
            ErrorCategory.TIMEOUT -> 10000L // 10 seconds for timeouts
            else -> DEFAULT_INITIAL_DELAY_MS
        }
    }
}

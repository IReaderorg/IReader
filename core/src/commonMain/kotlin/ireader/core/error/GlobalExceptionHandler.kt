package ireader.core.error

import ireader.core.log.IReaderLog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlin.coroutines.CoroutineContext

/**
 * Global exception handler for uncaught exceptions
 * Provides centralized error logging and crash reporting
 */
object GlobalExceptionHandler {
    
    private var crashCallback: ((Throwable) -> Unit)? = null
    private var isInitialized = false
    
    /**
     * Initialize the global exception handler
     */
    fun initialize(onCrash: (Throwable) -> Unit) {
        if (isInitialized) {
            IReaderLog.warn("GlobalExceptionHandler already initialized")
            return
        }
        
        crashCallback = onCrash
        isInitialized = true
        
        IReaderLog.info("GlobalExceptionHandler initialized")
    }
    
    /**
     * Handle an uncaught exception
     */
    fun handleException(throwable: Throwable, context: String = "Unknown") {
        IReaderLog.error(
            message = "Uncaught exception in context: $context",
            throwable = throwable,
            tag = "GlobalExceptionHandler"
        )
        
        // Convert to IReaderError for structured handling
        val error = throwable.toIReaderError()
        
        // Log additional context based on error type
        when (error) {
            is IReaderError.NetworkError -> {
                IReaderLog.error(
                    message = "Network error: ${error.message}, URL: ${error.url}, Status: ${error.statusCode}",
                    tag = "GlobalExceptionHandler"
                )
            }
            is IReaderError.DatabaseError -> {
                IReaderLog.error(
                    message = "Database error: ${error.message}, Operation: ${error.operation}",
                    tag = "GlobalExceptionHandler"
                )
            }
            is IReaderError.SourceError -> {
                IReaderLog.error(
                    message = "Source error: ${error.message}, Source: ${error.sourceName} (${error.sourceId})",
                    tag = "GlobalExceptionHandler"
                )
            }
            else -> {
                IReaderLog.error(
                    message = "Error: ${error.message}",
                    throwable = error,
                    tag = "GlobalExceptionHandler"
                )
            }
        }
        
        // Trigger crash callback
        crashCallback?.invoke(throwable)
    }
    
    /**
     * Create a CoroutineExceptionHandler for use in coroutine scopes
     */
    fun createCoroutineExceptionHandler(context: String = "Coroutine"): CoroutineExceptionHandler {
        return CoroutineExceptionHandler { coroutineContext, throwable ->
            handleException(throwable, context)
        }
    }
}

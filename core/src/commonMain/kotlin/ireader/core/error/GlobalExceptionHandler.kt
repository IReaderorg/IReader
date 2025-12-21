package ireader.core.error

import ireader.core.log.IReaderLog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlin.coroutines.CoroutineContext

/**
 * Global exception handler for uncaught exceptions
 * Provides centralized error logging and crash reporting
 * 
 * This handler is designed to gracefully handle crashes from dynamic
 * sources and plugins without crashing the main app.
 */
object GlobalExceptionHandler {
    
    private var crashCallback: ((Throwable) -> Unit)? = null
    private var sourceErrorCallback: ((Long, Throwable) -> Unit)? = null
    private var pluginErrorCallback: ((String, Throwable) -> Unit)? = null
    private var isInitialized = false
    
    // Track recent errors to avoid spam
    private val recentErrors = mutableMapOf<String, Long>()
    private const val ERROR_COOLDOWN_MS = 5000L // 5 seconds between same errors
    
    /**
     * Initialize the global exception handler
     */
    fun initialize(
        onCrash: (Throwable) -> Unit = {},
        onSourceError: ((Long, Throwable) -> Unit)? = null,
        onPluginError: ((String, Throwable) -> Unit)? = null
    ) {
        if (isInitialized) {
            IReaderLog.warn("GlobalExceptionHandler already initialized")
            return
        }
        
        crashCallback = onCrash
        sourceErrorCallback = onSourceError
        pluginErrorCallback = onPluginError
        isInitialized = true
        
        IReaderLog.info("GlobalExceptionHandler initialized")
    }
    
    /**
     * Handle an uncaught exception
     */
    fun handleException(throwable: Throwable, context: String = "Unknown") {
        // Check if this is a duplicate error within cooldown period
        val errorKey = "${throwable::class.simpleName}:${throwable.message?.take(50)}"
        val now = kotlin.time.TimeSource.Monotonic.markNow().elapsedNow().inWholeMilliseconds
        val lastOccurrence = recentErrors[errorKey]
        
        if (lastOccurrence != null && (now - lastOccurrence) < ERROR_COOLDOWN_MS) {
            // Skip duplicate error logging
            return
        }
        recentErrors[errorKey] = now
        
        // Clean up old entries
        if (recentErrors.size > 100) {
            val cutoff = now - ERROR_COOLDOWN_MS * 2
            recentErrors.entries.removeAll { it.value < cutoff }
        }
        
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
                // Notify source error callback
                error.sourceId?.let { sourceId ->
                    sourceErrorCallback?.invoke(sourceId, throwable)
                }
            }
            is IReaderError.PluginError -> {
                IReaderLog.error(
                    message = "Plugin error: ${error.message}, Plugin: ${error.pluginId}",
                    tag = "GlobalExceptionHandler"
                )
                // Notify plugin error callback
                error.pluginId?.let { pluginId ->
                    pluginErrorCallback?.invoke(pluginId, throwable)
                }
            }
            else -> {
                IReaderLog.error(
                    message = "Error: ${error.message}",
                    throwable = error,
                    tag = "GlobalExceptionHandler"
                )
            }
        }
        
        // Only trigger crash callback for non-recoverable errors
        if (!isRecoverableError(throwable)) {
            crashCallback?.invoke(throwable)
        }
    }
    
    /**
     * Handle a source-specific error.
     * These errors are always recoverable and won't crash the app.
     */
    fun handleSourceError(sourceId: Long, throwable: Throwable, context: String = "Source") {
        IReaderLog.error(
            message = "Source error for $sourceId in context: $context",
            throwable = throwable,
            tag = "GlobalExceptionHandler"
        )
        
        sourceErrorCallback?.invoke(sourceId, throwable)
    }
    
    /**
     * Handle a plugin-specific error.
     * These errors are always recoverable and won't crash the app.
     */
    fun handlePluginError(pluginId: String, throwable: Throwable, context: String = "Plugin") {
        IReaderLog.error(
            message = "Plugin error for $pluginId in context: $context",
            throwable = throwable,
            tag = "GlobalExceptionHandler"
        )
        
        pluginErrorCallback?.invoke(pluginId, throwable)
    }
    
    /**
     * Create a CoroutineExceptionHandler for use in coroutine scopes
     */
    fun createCoroutineExceptionHandler(context: String = "Coroutine"): CoroutineExceptionHandler {
        return CoroutineExceptionHandler { _, throwable ->
            handleException(throwable, context)
        }
    }
    
    /**
     * Create a CoroutineExceptionHandler for source operations.
     * Errors are handled gracefully without crashing.
     */
    fun createSourceExceptionHandler(sourceId: Long, context: String = "Source"): CoroutineExceptionHandler {
        return CoroutineExceptionHandler { _, throwable ->
            handleSourceError(sourceId, throwable, context)
        }
    }
    
    /**
     * Create a CoroutineExceptionHandler for plugin operations.
     * Errors are handled gracefully without crashing.
     */
    fun createPluginExceptionHandler(pluginId: String, context: String = "Plugin"): CoroutineExceptionHandler {
        return CoroutineExceptionHandler { _, throwable ->
            handlePluginError(pluginId, throwable, context)
        }
    }
    
    /**
     * Determine if an error is recoverable (shouldn't crash the app).
     */
    private fun isRecoverableError(throwable: Throwable): Boolean {
        return when {
            // Source/plugin errors are always recoverable
            throwable is IReaderError.SourceError -> true
            throwable is IReaderError.PluginError -> true
            
            // Network errors are recoverable
            throwable is IReaderError.NetworkError -> true
            
            // Parse errors are recoverable
            throwable is IReaderError.ParseError -> true
            
            // Class loading errors from plugins are recoverable
            throwable is ClassNotFoundException -> true
            throwable is NoClassDefFoundError -> true
            throwable is LinkageError -> true
            throwable is UnsatisfiedLinkError -> true
            
            // Check message for plugin/source related errors
            throwable.message?.let { msg ->
                val lower = msg.lowercase()
                lower.contains("source") ||
                lower.contains("plugin") ||
                lower.contains("extension") ||
                lower.contains("catalog")
            } == true -> true
            
            // Default: not recoverable
            else -> false
        }
    }
    
    /**
     * Execute a block safely, catching and handling any exceptions.
     * Returns null if an exception occurs.
     */
    inline fun <T> runSafely(
        context: String = "Unknown",
        block: () -> T
    ): T? {
        return try {
            block()
        } catch (e: Exception) {
            handleException(e, context)
            null
        }
    }
    
    /**
     * Execute a block safely for source operations.
     * Returns null if an exception occurs.
     */
    inline fun <T> runSourceSafely(
        sourceId: Long,
        context: String = "Source",
        block: () -> T
    ): T? {
        return try {
            block()
        } catch (e: Exception) {
            handleSourceError(sourceId, e, context)
            null
        }
    }
    
    /**
     * Execute a block safely for plugin operations.
     * Returns null if an exception occurs.
     */
    inline fun <T> runPluginSafely(
        pluginId: String,
        context: String = "Plugin",
        block: () -> T
    ): T? {
        return try {
            block()
        } catch (e: Exception) {
            handlePluginError(pluginId, e, context)
            null
        }
    }
}

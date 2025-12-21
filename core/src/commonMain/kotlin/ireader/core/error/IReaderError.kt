package ireader.core.error

/**
 * Base sealed class for all IReader errors
 * Provides structured error handling with specific error types
 */
sealed class IReaderError : Exception() {
    
    /**
     * Network-related errors (connection, timeout, etc.)
     */
    data class NetworkError(
        override val message: String,
        val statusCode: Int? = null,
        val url: String? = null,
        override val cause: Throwable? = null
    ) : IReaderError()
    
    /**
     * Database operation errors
     */
    data class DatabaseError(
        override val message: String,
        val operation: String? = null,
        override val cause: Throwable? = null
    ) : IReaderError()
    
    /**
     * Source/extension related errors
     */
    data class SourceError(
        override val message: String,
        val sourceId: Long? = null,
        val sourceName: String? = null,
        override val cause: Throwable? = null
    ) : IReaderError()
    
    /**
     * Plugin related errors
     */
    data class PluginError(
        override val message: String,
        val pluginId: String? = null,
        val pluginName: String? = null,
        override val cause: Throwable? = null
    ) : IReaderError()
    
    /**
     * File system and storage errors
     */
    data class StorageError(
        override val message: String,
        val path: String? = null,
        override val cause: Throwable? = null
    ) : IReaderError()
    
    /**
     * Authentication and authorization errors
     */
    data class AuthError(
        override val message: String,
        val service: String? = null,
        override val cause: Throwable? = null
    ) : IReaderError()
    
    /**
     * Parsing and data format errors
     */
    data class ParseError(
        override val message: String,
        val dataType: String? = null,
        override val cause: Throwable? = null
    ) : IReaderError()
    
    /**
     * Unknown or unclassified errors
     */
    data class UnknownError(
        override val message: String,
        override val cause: Throwable? = null
    ) : IReaderError()
}

/**
 * Check if the throwable is a class loading error (JVM-specific exceptions).
 * Uses class name matching for multiplatform compatibility.
 */
private fun Throwable.isClassLoadingError(): Boolean {
    val className = this::class.simpleName ?: return false
    return className in setOf("ClassNotFoundException", "NoClassDefFoundError")
}

/**
 * Check if the throwable is a linkage error (JVM-specific exceptions).
 * Uses class name matching for multiplatform compatibility.
 */
private fun Throwable.isLinkageError(): Boolean {
    val className = this::class.simpleName ?: return false
    return className in setOf("LinkageError", "UnsatisfiedLinkError")
}

/**
 * Extension function to convert generic exceptions to IReaderError
 */
fun Throwable.toIReaderError(): IReaderError {
    val errorMessage = this.message?.lowercase() ?: ""
    return when {
        this is IReaderError -> this
        
        // Network errors
        errorMessage.contains("unknown host") || errorMessage.contains("unable to resolve") -> 
            IReaderError.NetworkError(
                message = "No internet connection",
                cause = this
            )
        errorMessage.contains("timeout") || errorMessage.contains("timed out") -> 
            IReaderError.NetworkError(
                message = "Connection timeout",
                cause = this
            )
        errorMessage.contains("connection refused") ->
            IReaderError.NetworkError(
                message = "Connection refused",
                cause = this
            )
        
        // Plugin errors (JVM-specific exceptions, check by class name for multiplatform)
        isClassLoadingError() ->
            IReaderError.PluginError(
                message = "Plugin class not found: ${this.message}",
                cause = this
            )
        isLinkageError() ->
            IReaderError.PluginError(
                message = "Plugin native library error: ${this.message}",
                cause = this
            )
        errorMessage.contains("plugin") ->
            IReaderError.PluginError(
                message = this.message ?: "Plugin error",
                cause = this
            )
        
        // Source errors
        errorMessage.contains("source") || errorMessage.contains("extension") ||
        errorMessage.contains("catalog") ->
            IReaderError.SourceError(
                message = this.message ?: "Source error",
                cause = this
            )
        
        // Parse errors
        errorMessage.contains("parse") || errorMessage.contains("json") ||
        errorMessage.contains("xml") || errorMessage.contains("html") ->
            IReaderError.ParseError(
                message = "Failed to parse content: ${this.message}",
                cause = this
            )
        
        // Storage errors
        this is okio.IOException || errorMessage.contains("io") || errorMessage.contains("file") -> 
            IReaderError.StorageError(
                message = "File operation failed: ${this.message}",
                cause = this
            )
        
        // Auth errors
        errorMessage.contains("401") || errorMessage.contains("403") ||
        errorMessage.contains("unauthorized") || errorMessage.contains("forbidden") ->
            IReaderError.AuthError(
                message = "Authentication required",
                cause = this
            )
        
        else -> IReaderError.UnknownError(
            message = this.message ?: "An unknown error occurred",
            cause = this
        )
    }
}

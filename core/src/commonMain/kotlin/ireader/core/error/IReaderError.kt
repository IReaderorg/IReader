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
     * Source/plugin related errors
     */
    data class SourceError(
        override val message: String,
        val sourceId: Long? = null,
        val sourceName: String? = null,
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
 * Extension function to convert generic exceptions to IReaderError
 */
fun Throwable.toIReaderError(): IReaderError {
    val errorMessage = this.message?.lowercase() ?: ""
    return when {
        this is IReaderError -> this
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
        this is okio.IOException || errorMessage.contains("io") || errorMessage.contains("file") -> 
            IReaderError.StorageError(
                message = "File operation failed: ${this.message}",
                cause = this
            )
        else -> IReaderError.UnknownError(
            message = this.message ?: "An unknown error occurred",
            cause = this
        )
    }
}

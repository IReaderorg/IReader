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
    return when (this) {
        is IReaderError -> this
        is java.net.UnknownHostException -> IReaderError.NetworkError(
            message = "No internet connection",
            cause = this
        )
        is java.net.SocketTimeoutException -> IReaderError.NetworkError(
            message = "Connection timeout",
            cause = this
        )
        is java.io.IOException -> IReaderError.StorageError(
            message = "File operation failed: ${this.message}",
            cause = this
        )
        else -> IReaderError.UnknownError(
            message = this.message ?: "An unknown error occurred",
            cause = this
        )
    }
}

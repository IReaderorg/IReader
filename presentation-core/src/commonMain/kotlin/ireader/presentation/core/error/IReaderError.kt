package ireader.presentation.core.error

/**
 * Base class for all IReader-specific errors following Mihon's error handling pattern.
 * Provides structured error types with proper exception handling.
 */
sealed class IReaderError : Exception() {
    
    /**
     * Network-related errors (connection issues, timeouts, HTTP errors)
     */
    data class NetworkError(
        override val message: String,
        override val cause: Throwable? = null,
        val httpCode: Int? = null
    ) : IReaderError()
    
    /**
     * Database-related errors (query failures, constraint violations, corruption)
     */
    data class DatabaseError(
        override val message: String,
        override val cause: Throwable? = null,
        val operation: String? = null
    ) : IReaderError()
    
    /**
     * Source-related errors (parsing failures, source unavailable, invalid content)
     */
    data class SourceError(
        override val message: String,
        override val cause: Throwable? = null,
        val sourceId: Long? = null,
        val sourceName: String? = null
    ) : IReaderError()
    
    /**
     * File system errors (read/write failures, permissions, storage full)
     */
    data class FileSystemError(
        override val message: String,
        override val cause: Throwable? = null,
        val filePath: String? = null
    ) : IReaderError()
    
    /**
     * Authentication/authorization errors
     */
    data class AuthError(
        override val message: String,
        override val cause: Throwable? = null,
        val authType: String? = null
    ) : IReaderError()
    
    /**
     * Parsing errors (JSON, HTML, XML parsing failures)
     */
    data class ParseError(
        override val message: String,
        override val cause: Throwable? = null,
        val contentType: String? = null
    ) : IReaderError()
    
    /**
     * Unknown or unexpected errors
     */
    data class UnknownError(
        override val message: String,
        override val cause: Throwable? = null
    ) : IReaderError()
    
    companion object {
        /**
         * Creates appropriate error type from generic exception
         */
        fun fromException(exception: Throwable, context: String? = null): IReaderError {
            val errorMessage = exception.message?.lowercase() ?: ""
            return when {
                exception is IReaderError -> exception
                errorMessage.contains("unknown host") || 
                errorMessage.contains("timeout") || 
                errorMessage.contains("connect") -> NetworkError(
                    message = context?.let { "$it: ${exception.message}" } ?: exception.message ?: "Network error",
                    cause = exception
                )
                errorMessage.contains("sql") || errorMessage.contains("database") -> DatabaseError(
                    message = context?.let { "$it: ${exception.message}" } ?: exception.message ?: "Database error",
                    cause = exception,
                    operation = context
                )
                exception is okio.IOException || errorMessage.contains("io") || errorMessage.contains("file") -> FileSystemError(
                    message = context?.let { "$it: ${exception.message}" } ?: exception.message ?: "File system error",
                    cause = exception
                )
                exception is kotlinx.serialization.SerializationException -> ParseError(
                    message = context?.let { "$it: ${exception.message}" } ?: exception.message ?: "Parsing error",
                    cause = exception,
                    contentType = "JSON"
                )
                else -> UnknownError(
                    message = context?.let { "$it: ${exception.message}" } ?: exception.message ?: "Unknown error",
                    cause = exception
                )
            }
        }
    }
}
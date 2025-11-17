package ireader.domain.models.errors

/**
 * Base class for all IReader-specific errors following Mihon's error handling patterns.
 */
sealed class IReaderError : Exception() {
    
    /**
     * Network-related errors (connection issues, timeouts, etc.)
     */
    data class NetworkError(override val message: String, override val cause: Throwable? = null) : IReaderError()
    
    /**
     * Database-related errors (query failures, constraint violations, etc.)
     */
    data class DatabaseError(override val message: String, override val cause: Throwable? = null) : IReaderError()
    
    /**
     * Source-related errors (parsing failures, source unavailable, etc.)
     */
    data class SourceError(override val message: String, override val cause: Throwable? = null) : IReaderError()
    
    /**
     * File system errors (file not found, permission denied, etc.)
     */
    data class FileSystemError(override val message: String, override val cause: Throwable? = null) : IReaderError()
    
    /**
     * Authentication/authorization errors
     */
    data class AuthError(override val message: String, override val cause: Throwable? = null) : IReaderError()
    
    /**
     * Unknown or unexpected errors
     */
    data class UnknownError(override val message: String, override val cause: Throwable? = null) : IReaderError()
}
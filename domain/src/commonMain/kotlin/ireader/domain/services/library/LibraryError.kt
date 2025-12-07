package ireader.domain.services.library

/**
 * Sealed class representing all possible errors in library operations.
 * Used for type-safe error handling across the Library Controller.
 * 
 * Requirements: 5.4
 */
sealed class LibraryError {
    /**
     * Failed to load library data.
     */
    data class LoadFailed(val message: String) : LibraryError()
    
    /**
     * Failed to refresh library.
     */
    data class RefreshFailed(val message: String) : LibraryError()
    
    /**
     * Database operation failed.
     */
    data class DatabaseError(val message: String) : LibraryError()
    
    /**
     * Returns a user-friendly error message.
     */
    fun toUserMessage(): String = when (this) {
        is LoadFailed -> "Failed to load library: $message"
        is RefreshFailed -> "Failed to refresh library: $message"
        is DatabaseError -> "Database error: $message"
    }
}

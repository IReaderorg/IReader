package ireader.domain.services.book

/**
 * Sealed class representing all possible errors in book operations.
 * Used for type-safe error handling across the Book Controller.
 * 
 * Requirements: 5.4
 */
sealed class BookError {
    /**
     * The requested book was not found in the database.
     */
    data class BookNotFound(val bookId: Long) : BookError()
    
    /**
     * Failed to update book data.
     */
    data class UpdateFailed(val message: String) : BookError()
    
    /**
     * Failed to refresh book from source.
     */
    data class RefreshFailed(val message: String) : BookError()
    
    /**
     * Database operation failed.
     */
    data class DatabaseError(val message: String) : BookError()
    
    /**
     * Returns a user-friendly error message.
     */
    fun toUserMessage(): String = when (this) {
        is BookNotFound -> "Book not found (ID: $bookId)"
        is UpdateFailed -> "Failed to update book: $message"
        is RefreshFailed -> "Failed to refresh book: $message"
        is DatabaseError -> "Database error: $message"
    }
}

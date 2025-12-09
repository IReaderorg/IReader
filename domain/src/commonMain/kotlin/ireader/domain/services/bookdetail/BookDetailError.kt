package ireader.domain.services.bookdetail

/**
 * Sealed class representing all possible errors in book detail operations.
 * Used for type-safe error handling across the BookDetail Controller.
 * 
 * Requirements: 4.1, 4.2, 4.3, 4.4, 4.5
 */
sealed class BookDetailError {
    /**
     * Failed to load book or chapters.
     */
    data class LoadFailed(val message: String) : BookDetailError()
    
    /**
     * Network error occurred during book detail operations.
     */
    data class NetworkError(val message: String) : BookDetailError()
    
    /**
     * The requested book was not found.
     */
    data class NotFound(val bookId: Long) : BookDetailError()
    
    /**
     * Failed to refresh book or chapters from source.
     */
    data class RefreshFailed(val message: String) : BookDetailError()
    
    /**
     * Source/extension not available for this book.
     */
    data class SourceNotAvailable(val sourceId: Long) : BookDetailError()
    
    /**
     * Database operation failed.
     */
    data class DatabaseError(val message: String) : BookDetailError()
    
    /**
     * Returns a user-friendly error message.
     * Requirements: 4.4
     */
    fun toUserMessage(): String = when (this) {
        is LoadFailed -> "Failed to load book: $message"
        is NetworkError -> "Network error: $message"
        is NotFound -> "Book not found (ID: $bookId)"
        is RefreshFailed -> "Failed to refresh: $message"
        is SourceNotAvailable -> "Source not available (ID: $sourceId)"
        is DatabaseError -> "Database error: $message"
    }
}

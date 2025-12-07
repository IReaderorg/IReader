package ireader.domain.services.chapter

/**
 * Sealed class representing all possible errors in chapter operations.
 * Used for type-safe error handling across the Chapter Controller.
 */
sealed class ChapterError {
    /**
     * The requested book was not found in the database.
     */
    data class BookNotFound(val bookId: Long) : ChapterError()
    
    /**
     * The requested chapter was not found in the database.
     */
    data class ChapterNotFound(val chapterId: Long) : ChapterError()
    
    /**
     * Failed to load chapter content from remote source.
     */
    data class ContentLoadFailed(val message: String) : ChapterError()
    
    /**
     * Network-related error occurred during remote operations.
     */
    data class NetworkError(val message: String) : ChapterError()
    
    /**
     * Database operation failed.
     */
    data class DatabaseError(val message: String) : ChapterError()
    
    /**
     * Returns a user-friendly error message.
     */
    fun toUserMessage(): String = when (this) {
        is BookNotFound -> "Book not found (ID: $bookId)"
        is ChapterNotFound -> "Chapter not found (ID: $chapterId)"
        is ContentLoadFailed -> "Failed to load content: $message"
        is NetworkError -> "Network error: $message"
        is DatabaseError -> "Database error: $message"
    }
}

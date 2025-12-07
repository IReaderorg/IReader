package ireader.domain.services.book

import androidx.compose.runtime.Immutable
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Category

/**
 * Immutable data class representing the complete state of book operations.
 * This is the single source of truth for all book-related data across screens.
 * 
 * Requirements: 2.1, 2.2, 2.3, 2.4, 2.5
 */
@Immutable
data class BookState(
    // ========== Book Data ==========
    
    /**
     * The currently loaded book, or null if no book is loaded.
     */
    val book: Book? = null,
    
    /**
     * Categories assigned to the current book.
     */
    val categories: List<Category> = emptyList(),
    
    // ========== Reading Progress ==========
    
    /**
     * Current reading progress as a percentage (0.0 to 1.0).
     */
    val readingProgress: Float = 0f,
    
    /**
     * ID of the last read chapter for this book.
     */
    val lastReadChapterId: Long? = null,
    
    /**
     * Total number of chapters in the book.
     */
    val totalChapters: Int = 0,
    
    /**
     * Number of chapters that have been read.
     */
    val readChapters: Int = 0,
    
    // ========== Loading States ==========
    
    /**
     * True when loading book data from the database.
     */
    val isLoading: Boolean = false,
    
    /**
     * True when refreshing book data from the source.
     */
    val isRefreshing: Boolean = false,
    
    // ========== Error State ==========
    
    /**
     * Current error, or null if no error.
     */
    val error: BookError? = null
) {
    // ========== Computed Properties ==========
    
    /**
     * Progress percentage based on read chapters vs total chapters.
     */
    val progressPercentage: Float
        get() = if (totalChapters > 0) readChapters.toFloat() / totalChapters else 0f
    
    /**
     * True if a book is currently loaded.
     */
    val hasBook: Boolean
        get() = book != null
    
    /**
     * True if the book is marked as favorite.
     */
    val isFavorite: Boolean
        get() = book?.favorite == true
    
    /**
     * True if any loading operation is in progress.
     */
    val isAnyLoading: Boolean
        get() = isLoading || isRefreshing
    
    /**
     * Number of unread chapters.
     */
    val unreadChapters: Int
        get() = (totalChapters - readChapters).coerceAtLeast(0)
    
    /**
     * True if the book has been started (at least one chapter read).
     */
    val hasStarted: Boolean
        get() = readChapters > 0
    
    /**
     * True if all chapters have been read.
     */
    val isCompleted: Boolean
        get() = totalChapters > 0 && readChapters >= totalChapters
}

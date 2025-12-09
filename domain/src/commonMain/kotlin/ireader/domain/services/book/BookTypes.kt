package ireader.domain.services.book

import androidx.compose.runtime.Immutable
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Category

/**
 * Consolidated types for the Book Controller.
 * Contains Command, State, Event, and Error definitions.
 * 
 * Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 5.1, 5.4
 */

// ============================================================================
// COMMANDS
// ============================================================================

/**
 * Sealed class representing all commands that can be dispatched to the Book Controller.
 * All book operations are expressed as commands for predictable state management.
 */
sealed class BookCommand {
    // Lifecycle Commands
    /** Load a book and subscribe to reactive updates from the database. */
    data class LoadBook(val bookId: Long) : BookCommand()
    /** Clean up resources, cancel subscriptions, and reset state. */
    object Cleanup : BookCommand()
    
    // Progress Commands
    /** Update reading progress for the current book. */
    data class UpdateReadingProgress(val chapterId: Long, val progress: Float = 0f) : BookCommand()
    
    // Book Operations
    /** Toggle the favorite status of the current book. */
    object ToggleFavorite : BookCommand()
    /** Set the category for the current book. */
    data class SetCategory(val categoryId: Long) : BookCommand()
    /** Update book metadata (title, author, description). */
    data class UpdateMetadata(
        val title: String? = null,
        val author: String? = null,
        val description: String? = null
    ) : BookCommand()
    /** Refresh book data from the source. */
    object RefreshFromSource : BookCommand()
}

// ============================================================================
// STATE
// ============================================================================

/**
 * Immutable data class representing the complete state of book operations.
 * This is the single source of truth for all book-related data across screens.
 */
@Immutable
data class BookState(
    // Book Data
    val book: Book? = null,
    val categories: List<Category> = emptyList(),
    
    // Reading Progress
    val readingProgress: Float = 0f,
    val lastReadChapterId: Long? = null,
    val totalChapters: Int = 0,
    val readChapters: Int = 0,
    
    // Loading States
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    
    // Error State
    val error: BookError? = null
) {
    // Computed Properties
    val progressPercentage: Float get() = if (totalChapters > 0) readChapters.toFloat() / totalChapters else 0f
    val hasBook: Boolean get() = book != null
    val isFavorite: Boolean get() = book?.favorite == true
    val isAnyLoading: Boolean get() = isLoading || isRefreshing
    val unreadChapters: Int get() = (totalChapters - readChapters).coerceAtLeast(0)
    val hasStarted: Boolean get() = readChapters > 0
    val isCompleted: Boolean get() = totalChapters > 0 && readChapters >= totalChapters
}

// ============================================================================
// EVENTS
// ============================================================================

/**
 * Sealed class representing one-time events emitted by the Book Controller.
 * These events are used for UI feedback and should be consumed once.
 */
sealed class BookEvent {
    /** An error occurred during a book operation. */
    data class Error(val error: BookError) : BookEvent()
    /** A book was successfully loaded. */
    data class BookLoaded(val book: Book) : BookEvent()
    /** Book metadata was successfully updated. */
    object MetadataUpdated : BookEvent()
    /** Reading progress was successfully saved to the database. */
    object ProgressSaved : BookEvent()
    /** Book favorite status was toggled. */
    data class FavoriteToggled(val isFavorite: Boolean) : BookEvent()
    /** Book category was updated. */
    data class CategoryUpdated(val categoryId: Long) : BookEvent()
    /** Book was refreshed from source. */
    object RefreshCompleted : BookEvent()
}

// ============================================================================
// ERRORS
// ============================================================================

/**
 * Sealed class representing all possible errors in book operations.
 * Used for type-safe error handling across the Book Controller.
 */
sealed class BookError {
    /** The requested book was not found in the database. */
    data class BookNotFound(val bookId: Long) : BookError()
    /** Failed to update book data. */
    data class UpdateFailed(val message: String) : BookError()
    /** Failed to refresh book from source. */
    data class RefreshFailed(val message: String) : BookError()
    /** Database operation failed. */
    data class DatabaseError(val message: String) : BookError()
    
    /** Returns a user-friendly error message. */
    fun toUserMessage(): String = when (this) {
        is BookNotFound -> "Book not found (ID: $bookId)"
        is UpdateFailed -> "Failed to update book: $message"
        is RefreshFailed -> "Failed to refresh book: $message"
        is DatabaseError -> "Database error: $message"
    }
}

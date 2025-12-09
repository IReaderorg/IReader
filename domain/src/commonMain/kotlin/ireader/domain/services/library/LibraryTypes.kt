package ireader.domain.services.library

import androidx.compose.runtime.Immutable
import ireader.domain.models.entities.Category
import ireader.domain.models.entities.LibraryBook

/**
 * Consolidated types for the Library Controller.
 * Contains Command, State, Event, and Error definitions.
 * 
 * Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 5.1, 5.4
 */

// ============================================================================
// COMMANDS
// ============================================================================

/**
 * Sealed class representing all commands that can be dispatched to the Library Controller.
 * All library operations are expressed as commands for predictable state management.
 */
sealed class LibraryCommand {
    // Lifecycle Commands
    /** Load the library and subscribe to reactive updates from the database. */
    object LoadLibrary : LibraryCommand()
    /** Clean up resources, cancel subscriptions, and reset state. */
    object Cleanup : LibraryCommand()
    
    // Filter/Sort Commands
    /** Set the current filter for the library. */
    data class SetFilter(val filter: LibraryFilter) : LibraryCommand()
    /** Set the current sort for the library. */
    data class SetSort(val sort: LibrarySort) : LibraryCommand()
    /** Set the current category to filter by. */
    data class SetCategory(val categoryId: Long?) : LibraryCommand()
    
    // Selection Commands
    /** Select a book by ID (idempotent). */
    data class SelectBook(val bookId: Long) : LibraryCommand()
    /** Deselect a book by ID. */
    data class DeselectBook(val bookId: Long) : LibraryCommand()
    /** Select all books in the current filtered view. */
    object SelectAll : LibraryCommand()
    /** Clear all selections. */
    object ClearSelection : LibraryCommand()
    /** Invert the current selection. */
    object InvertSelection : LibraryCommand()
    
    // Refresh Commands
    /** Refresh the library from the database. */
    object RefreshLibrary : LibraryCommand()
}

// ============================================================================
// STATE
// ============================================================================

/**
 * Immutable data class representing the complete state of library operations.
 * This is the single source of truth for all library-related data across screens.
 */
@Immutable
data class LibraryState(
    // Book Data
    val books: List<LibraryBook> = emptyList(),
    val filteredBooks: List<LibraryBook> = emptyList(),
    val categories: List<Category> = emptyList(),
    
    // Selection State
    val selectedBookIds: Set<Long> = emptySet(),
    
    // Filter/Sort State
    val currentCategoryId: Long? = null,
    val filter: LibraryFilter = LibraryFilter.None,
    val sort: LibrarySort = LibrarySort.default,
    
    // Loading States
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    
    // Error State
    val error: LibraryError? = null
) {
    // Computed Properties
    val hasSelection: Boolean get() = selectedBookIds.isNotEmpty()
    val selectionCount: Int get() = selectedBookIds.size
    val allSelected: Boolean get() = filteredBooks.isNotEmpty() && filteredBooks.all { it.id in selectedBookIds }
    val totalBooks: Int get() = books.size
    val filteredCount: Int get() = filteredBooks.size
    val isAnyLoading: Boolean get() = isLoading || isRefreshing
    val selectedBooks: List<LibraryBook> get() = filteredBooks.filter { it.id in selectedBookIds }
}

// ============================================================================
// EVENTS
// ============================================================================

/**
 * Sealed class representing one-time events emitted by the Library Controller.
 * These events are used for UI feedback and should be consumed once.
 */
sealed class LibraryEvent {
    /** An error occurred during a library operation. */
    data class Error(val error: LibraryError) : LibraryEvent()
    /** Library was successfully loaded. */
    object LibraryLoaded : LibraryEvent()
    /** Library refresh completed successfully. */
    object RefreshCompleted : LibraryEvent()
    /** Selection was changed. */
    data class SelectionChanged(val count: Int) : LibraryEvent()
    /** Filter was changed. */
    data class FilterChanged(val filter: LibraryFilter) : LibraryEvent()
    /** Sort was changed. */
    data class SortChanged(val sort: LibrarySort) : LibraryEvent()
}

// ============================================================================
// ERRORS
// ============================================================================

/**
 * Sealed class representing all possible errors in library operations.
 * Used for type-safe error handling across the Library Controller.
 */
sealed class LibraryError {
    /** Failed to load library data. */
    data class LoadFailed(val message: String) : LibraryError()
    /** Failed to refresh library. */
    data class RefreshFailed(val message: String) : LibraryError()
    /** Database operation failed. */
    data class DatabaseError(val message: String) : LibraryError()
    
    /** Returns a user-friendly error message. */
    fun toUserMessage(): String = when (this) {
        is LoadFailed -> "Failed to load library: $message"
        is RefreshFailed -> "Failed to refresh library: $message"
        is DatabaseError -> "Database error: $message"
    }
}

package ireader.domain.services.library

/**
 * Sealed class representing all commands that can be dispatched to the Library Controller.
 * All library operations are expressed as commands for predictable state management.
 * 
 * Requirements: 3.1, 3.2, 3.3, 3.4, 3.5
 */
sealed class LibraryCommand {
    // ========== Lifecycle Commands ==========
    
    /**
     * Load the library and subscribe to reactive updates from the database.
     * Requirements: 3.1
     */
    object LoadLibrary : LibraryCommand()
    
    /**
     * Clean up resources, cancel subscriptions, and reset state.
     */
    object Cleanup : LibraryCommand()
    
    // ========== Filter/Sort Commands ==========
    
    /**
     * Set the current filter for the library.
     * Requirements: 3.2
     */
    data class SetFilter(val filter: LibraryFilter) : LibraryCommand()
    
    /**
     * Set the current sort for the library.
     * Requirements: 3.3
     */
    data class SetSort(val sort: LibrarySort) : LibraryCommand()
    
    /**
     * Set the current category to filter by.
     */
    data class SetCategory(val categoryId: Long?) : LibraryCommand()
    
    // ========== Selection Commands ==========
    
    /**
     * Select a book by ID (idempotent - selecting twice has same effect as once).
     * Requirements: 3.4
     */
    data class SelectBook(val bookId: Long) : LibraryCommand()
    
    /**
     * Deselect a book by ID.
     */
    data class DeselectBook(val bookId: Long) : LibraryCommand()
    
    /**
     * Select all books in the current filtered view.
     */
    object SelectAll : LibraryCommand()
    
    /**
     * Clear all selections.
     * Requirements: 3.5
     */
    object ClearSelection : LibraryCommand()
    
    /**
     * Invert the current selection.
     */
    object InvertSelection : LibraryCommand()
    
    // ========== Refresh Commands ==========
    
    /**
     * Refresh the library from the database.
     */
    object RefreshLibrary : LibraryCommand()
}

package ireader.domain.services.library

import androidx.compose.runtime.Immutable
import ireader.domain.models.entities.Category
import ireader.domain.models.entities.LibraryBook

/**
 * Immutable data class representing the complete state of library operations.
 * This is the single source of truth for all library-related data across screens.
 * 
 * Requirements: 3.1, 3.2, 3.3, 3.4, 3.5
 */
@Immutable
data class LibraryState(
    // ========== Book Data ==========
    
    /**
     * All books in the library (unfiltered).
     */
    val books: List<LibraryBook> = emptyList(),
    
    /**
     * Books after applying current filter and sort.
     */
    val filteredBooks: List<LibraryBook> = emptyList(),
    
    /**
     * All available categories.
     */
    val categories: List<Category> = emptyList(),
    
    // ========== Selection State ==========
    
    /**
     * Set of selected book IDs.
     */
    val selectedBookIds: Set<Long> = emptySet(),
    
    // ========== Filter/Sort State ==========
    
    /**
     * Currently selected category ID, or null for all categories.
     */
    val currentCategoryId: Long? = null,
    
    /**
     * Current filter applied to the library.
     */
    val filter: LibraryFilter = LibraryFilter.None,
    
    /**
     * Current sort applied to the library.
     */
    val sort: LibrarySort = LibrarySort.default,
    
    // ========== Loading States ==========
    
    /**
     * True when loading library data from the database.
     */
    val isLoading: Boolean = false,
    
    /**
     * True when refreshing library data.
     */
    val isRefreshing: Boolean = false,
    
    // ========== Error State ==========
    
    /**
     * Current error, or null if no error.
     */
    val error: LibraryError? = null
) {
    // ========== Computed Properties ==========
    
    /**
     * True if any books are selected.
     */
    val hasSelection: Boolean
        get() = selectedBookIds.isNotEmpty()
    
    /**
     * Number of selected books.
     */
    val selectionCount: Int
        get() = selectedBookIds.size
    
    /**
     * True if all filtered books are selected.
     */
    val allSelected: Boolean
        get() = filteredBooks.isNotEmpty() && filteredBooks.all { it.id in selectedBookIds }
    
    /**
     * Total number of books in the library.
     */
    val totalBooks: Int
        get() = books.size
    
    /**
     * Number of books after filtering.
     */
    val filteredCount: Int
        get() = filteredBooks.size
    
    /**
     * True if any loading operation is in progress.
     */
    val isAnyLoading: Boolean
        get() = isLoading || isRefreshing
    
    /**
     * Get selected books.
     */
    val selectedBooks: List<LibraryBook>
        get() = filteredBooks.filter { it.id in selectedBookIds }
}

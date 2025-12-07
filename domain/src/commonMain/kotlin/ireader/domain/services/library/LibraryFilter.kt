package ireader.domain.services.library

import ireader.domain.models.entities.LibraryBook

/**
 * Sealed class representing all possible filters for the library.
 * Each filter provides a predicate function to filter books.
 * 
 * Requirements: 3.2
 */
sealed class LibraryFilter {
    /**
     * No filter applied - all books pass.
     */
    object None : LibraryFilter()
    
    /**
     * Filter for books that have downloaded chapters.
     */
    object Downloaded : LibraryFilter()
    
    /**
     * Filter for books that have not been started (no chapters read).
     */
    object Unread : LibraryFilter()
    
    /**
     * Filter for books that have been started but not completed.
     */
    object Started : LibraryFilter()
    
    /**
     * Filter for books that have been completed (all chapters read).
     */
    object Completed : LibraryFilter()
    
    /**
     * Filter for books in a specific category.
     */
    data class Category(val categoryId: Long) : LibraryFilter()
    
    /**
     * Combine multiple filters (all must pass).
     */
    data class Combined(val filters: List<LibraryFilter>) : LibraryFilter()
    
    /**
     * Convert this filter to a predicate function.
     * 
     * @return A function that returns true if the book passes the filter
     */
    fun toPredicate(): (LibraryBook) -> Boolean = when (this) {
        None -> { _ -> true }
        Downloaded -> { book -> book.readCount > 0 } // readCount represents downloaded chapters in LibraryBook
        Unread -> { book -> !book.hasStarted }
        Started -> { book -> book.hasStarted && book.unreadCount > 0 }
        Completed -> { book -> book.totalChapters > 0 && book.unreadCount == 0 }
        is Category -> { book -> book.category.toLong() == categoryId }
        is Combined -> { book -> filters.all { it.toPredicate()(book) } }
    }
    
    companion object {
        /**
         * Create a combined filter from multiple filters.
         * If only one filter is provided, returns that filter directly.
         * If no filters are provided, returns None.
         */
        fun combine(vararg filters: LibraryFilter): LibraryFilter {
            val nonNoneFilters = filters.filter { it != None }
            return when {
                nonNoneFilters.isEmpty() -> None
                nonNoneFilters.size == 1 -> nonNoneFilters.first()
                else -> Combined(nonNoneFilters)
            }
        }
    }
}

package ireader.domain.services.bookdetail

import androidx.compose.runtime.Stable
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter

/**
 * Stable data class representing the complete state of book detail operations.
 * This is the single source of truth for all book detail-related data.
 * 
 * Requirements: 3.1, 3.3, 3.4, 3.5, 5.1
 */
@Stable
data class BookDetailState(
    // ========== Book Data ==========
    
    /**
     * The currently loaded book, or null if no book is loaded.
     */
    val book: Book? = null,
    
    /**
     * All chapters for the current book.
     */
    val chapters: List<Chapter> = emptyList(),
    
    /**
     * Chapters after applying filters and search.
     */
    val filteredChapters: List<Chapter> = emptyList(),
    
    // ========== Selection State ==========
    
    /**
     * Set of selected chapter IDs.
     */
    val selectedChapterIds: Set<Long> = emptySet(),
    
    // ========== Filter State ==========
    
    /**
     * Current chapter filter.
     */
    val filter: ChapterFilter = ChapterFilter.None,
    
    /**
     * Current search query for filtering chapters.
     */
    val searchQuery: String? = null,
    
    // ========== Sort State ==========
    
    /**
     * Current chapter sort order.
     */
    val sort: ChapterSortOrder = ChapterSortOrder.Default,
    
    // ========== Reading Progress ==========
    
    /**
     * ID of the last read chapter.
     */
    val lastReadChapterId: Long? = null,
    
    // ========== Loading States ==========
    
    /**
     * True when loading book data.
     */
    val isLoading: Boolean = false,
    
    /**
     * True when refreshing book from source.
     */
    val isRefreshingBook: Boolean = false,
    
    /**
     * True when refreshing chapters from source.
     */
    val isRefreshingChapters: Boolean = false,
    
    // ========== Error State ==========
    
    /**
     * Current error, or null if no error.
     * Requirements: 4.2, 4.5
     */
    val error: BookDetailError? = null
) {
    // ========== Computed Properties ==========
    
    /**
     * Number of selected chapters.
     */
    val selectionCount: Int
        get() = selectedChapterIds.size
    
    /**
     * True if any chapters are selected.
     */
    val hasSelection: Boolean
        get() = selectedChapterIds.isNotEmpty()
    
    /**
     * True if a book is currently loaded.
     */
    val hasBook: Boolean
        get() = book != null
    
    /**
     * True if there are any chapters.
     */
    val hasChapters: Boolean
        get() = chapters.isNotEmpty()
    
    /**
     * Total number of chapters.
     */
    val totalChapters: Int
        get() = chapters.size
    
    /**
     * Number of read chapters.
     */
    val readChapters: Int
        get() = chapters.count { it.read }
    
    /**
     * Number of unread chapters.
     */
    val unreadChapters: Int
        get() = chapters.count { !it.read }
    
    /**
     * Number of bookmarked chapters.
     */
    val bookmarkedChapters: Int
        get() = chapters.count { it.bookmark }
    
    /**
     * Number of downloaded chapters.
     */
    val downloadedChapters: Int
        get() = chapters.count { it.content.isNotEmpty() }
    
    /**
     * True if any loading operation is in progress.
     */
    val isAnyLoading: Boolean
        get() = isLoading || isRefreshingBook || isRefreshingChapters
    
    /**
     * True if there's an active error.
     */
    val hasError: Boolean
        get() = error != null
    
    /**
     * True if all filtered chapters are selected.
     */
    val allFilteredSelected: Boolean
        get() = filteredChapters.isNotEmpty() && 
                filteredChapters.all { it.id in selectedChapterIds }
    
    /**
     * Get selected chapters.
     */
    fun getSelectedChapters(): List<Chapter> {
        return chapters.filter { it.id in selectedChapterIds }
    }
    
    /**
     * Get the chapter to continue reading from.
     * Returns the last read chapter if available, otherwise the first unread chapter,
     * or the first chapter if all are read.
     */
    fun getContinueReadingChapter(): Chapter? {
        if (chapters.isEmpty()) return null
        
        // If we have a last read chapter, return it
        lastReadChapterId?.let { lastId ->
            chapters.find { it.id == lastId }?.let { return it }
        }
        
        // Otherwise, find the first unread chapter
        val sortedChapters = if (sort.ascending) chapters else chapters.reversed()
        return sortedChapters.firstOrNull { !it.read } ?: sortedChapters.firstOrNull()
    }
}

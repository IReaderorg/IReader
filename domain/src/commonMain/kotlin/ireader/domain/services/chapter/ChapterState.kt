package ireader.domain.services.chapter

import androidx.compose.runtime.Immutable
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter

/**
 * Immutable data class representing the complete state of chapter operations.
 * This is the single source of truth for all chapter-related data across screens.
 */
@Immutable
data class ChapterState(
    // ========== Book Data ==========
    
    /**
     * The currently loaded book, or null if no book is loaded.
     */
    val book: Book? = null,
    
    /**
     * All chapters belonging to the current book (unfiltered).
     */
    val chapters: List<Chapter> = emptyList(),
    
    /**
     * Chapters after applying filter and sort.
     */
    val filteredChapters: List<Chapter> = emptyList(),
    
    // ========== Current Chapter ==========
    
    /**
     * The currently active chapter, or null if no chapter is selected.
     */
    val currentChapter: Chapter? = null,
    
    /**
     * Index of the current chapter in the chapters list.
     */
    val currentChapterIndex: Int = 0,
    
    /**
     * Current paragraph index within the chapter content.
     */
    val currentParagraphIndex: Int = 0,
    
    // ========== Loading States ==========
    
    /**
     * True when loading book data from the database.
     */
    val isLoadingBook: Boolean = false,
    
    /**
     * True when loading chapters list from the database.
     */
    val isLoadingChapters: Boolean = false,
    
    /**
     * True when loading chapter content (local or remote).
     */
    val isLoadingContent: Boolean = false,
    
    /**
     * True when preloading the next chapter in the background.
     */
    val isPreloading: Boolean = false,
    
    // ========== Selection ==========
    
    /**
     * Set of selected chapter IDs for batch operations.
     */
    val selectedChapterIds: Set<Long> = emptySet(),
    
    // ========== Filter & Sort ==========
    
    /**
     * Current filter applied to chapters.
     */
    val filter: ChapterFilter = ChapterFilter.None,
    
    /**
     * Current sort order applied to chapters.
     */
    val sort: ChapterSort = ChapterSort.Default,
    
    // ========== Navigation Helpers ==========
    
    /**
     * ID of the last read chapter for this book.
     */
    val lastReadChapterId: Long? = null,
    
    // ========== Error State ==========
    
    /**
     * Current error, or null if no error.
     */
    val error: ChapterError? = null
) {
    // ========== Computed Properties ==========
    
    /**
     * True if the current chapter has content loaded.
     */
    val hasContent: Boolean
        get() = currentChapter?.content?.isNotEmpty() == true
    
    /**
     * True if there is a next chapter available.
     */
    val canGoNext: Boolean
        get() = chapters.isNotEmpty() && currentChapterIndex < chapters.lastIndex
    
    /**
     * True if there is a previous chapter available.
     */
    val canGoPrevious: Boolean
        get() = chapters.isNotEmpty() && currentChapterIndex > 0
    
    /**
     * True if any chapters are selected.
     */
    val hasSelection: Boolean
        get() = selectedChapterIds.isNotEmpty()
    
    /**
     * Number of selected chapters.
     */
    val selectionCount: Int
        get() = selectedChapterIds.size
    
    /**
     * Total number of chapters.
     */
    val totalChapters: Int
        get() = chapters.size
    
    /**
     * Total number of filtered chapters.
     */
    val filteredChapterCount: Int
        get() = filteredChapters.size
    
    /**
     * True if any loading operation is in progress.
     */
    val isLoading: Boolean
        get() = isLoadingBook || isLoadingChapters || isLoadingContent
    
    /**
     * True if a book is currently loaded.
     */
    val hasBook: Boolean
        get() = book != null
    
    /**
     * True if chapters are available.
     */
    val hasChapters: Boolean
        get() = chapters.isNotEmpty()
}

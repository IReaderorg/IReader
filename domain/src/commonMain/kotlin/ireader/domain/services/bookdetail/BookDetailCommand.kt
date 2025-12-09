package ireader.domain.services.bookdetail

import ireader.domain.models.entities.Chapter

/**
 * Sealed class representing all commands that can be dispatched to the BookDetail Controller.
 * All book detail operations are expressed as commands for predictable state management.
 * 
 * Requirements: 3.1, 3.3, 3.4, 3.5
 */
sealed class BookDetailCommand {
    // ========== Lifecycle Commands ==========
    
    /**
     * Load a book and its chapters, subscribe to reactive updates.
     * Requirements: 3.1
     */
    data class LoadBook(val bookId: Long) : BookDetailCommand()
    
    /**
     * Clean up resources, cancel subscriptions, and reset state.
     */
    object Cleanup : BookDetailCommand()
    
    // ========== Selection Commands ==========
    
    /**
     * Select a chapter by ID.
     * Requirements: 3.1
     */
    data class SelectChapter(val chapterId: Long) : BookDetailCommand()
    
    /**
     * Deselect a chapter by ID.
     * Requirements: 3.1
     */
    data class DeselectChapter(val chapterId: Long) : BookDetailCommand()
    
    /**
     * Toggle selection state of a chapter.
     */
    data class ToggleChapterSelection(val chapterId: Long) : BookDetailCommand()
    
    /**
     * Clear all chapter selections.
     * Requirements: 3.1
     */
    object ClearSelection : BookDetailCommand()
    
    /**
     * Select all chapters (optionally filtered).
     * Requirements: 3.1
     */
    data class SelectAll(val onlyFiltered: Boolean = true) : BookDetailCommand()
    
    /**
     * Select chapters in a range (for shift-click selection).
     */
    data class SelectRange(val fromChapterId: Long, val toChapterId: Long) : BookDetailCommand()
    
    // ========== Filter Commands ==========
    
    /**
     * Set the chapter filter.
     * Requirements: 3.1
     */
    data class SetFilter(val filter: ChapterFilter) : BookDetailCommand()
    
    /**
     * Set the search query for filtering chapters.
     */
    data class SetSearchQuery(val query: String?) : BookDetailCommand()
    
    // ========== Sort Commands ==========
    
    /**
     * Set the chapter sort order.
     * Requirements: 3.1
     */
    data class SetSort(val sort: ChapterSortOrder) : BookDetailCommand()
    
    // ========== Refresh Commands ==========
    
    /**
     * Refresh chapters from the source.
     * Requirements: 3.1
     */
    object RefreshChapters : BookDetailCommand()
    
    /**
     * Refresh book details from the source.
     */
    object RefreshBook : BookDetailCommand()
    
    // ========== Navigation Commands ==========
    
    /**
     * Navigate to reader with a specific chapter.
     */
    data class NavigateToReader(val chapterId: Long) : BookDetailCommand()
    
    /**
     * Navigate to reader with the last read chapter or first chapter.
     */
    object ContinueReading : BookDetailCommand()
    
    // ========== Error Commands ==========
    
    /**
     * Clear the current error state.
     * Requirements: 4.5
     */
    object ClearError : BookDetailCommand()
}

/**
 * Filter options for chapters.
 */
sealed class ChapterFilter {
    object None : ChapterFilter()
    object Unread : ChapterFilter()
    object Read : ChapterFilter()
    object Bookmarked : ChapterFilter()
    object Downloaded : ChapterFilter()
    data class Combined(
        val showUnread: Boolean? = null,
        val showRead: Boolean? = null,
        val showBookmarked: Boolean? = null,
        val showDownloaded: Boolean? = null
    ) : ChapterFilter()
}

/**
 * Sort options for chapters.
 */
data class ChapterSortOrder(
    val type: Type = Type.SOURCE,
    val ascending: Boolean = true
) {
    enum class Type {
        SOURCE,      // Sort by source order (chapter number)
        CHAPTER_NUMBER,
        UPLOAD_DATE,
        NAME
    }
    
    companion object {
        val Default = ChapterSortOrder(Type.SOURCE, ascending = true)
    }
}

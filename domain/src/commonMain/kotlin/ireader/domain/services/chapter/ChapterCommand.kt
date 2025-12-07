package ireader.domain.services.chapter

/**
 * Sealed class representing all commands that can be dispatched to the Chapter Controller.
 * All chapter operations are expressed as commands for predictable state management.
 */
sealed class ChapterCommand {
    // ========== Lifecycle Commands ==========
    
    /**
     * Load a book and its chapters. Subscribes to chapter updates from the database.
     */
    data class LoadBook(val bookId: Long) : ChapterCommand()
    
    /**
     * Load a specific chapter's content. If content is not available locally,
     * it will be fetched from the remote source.
     */
    data class LoadChapter(
        val chapterId: Long,
        val startParagraph: Int = 0
    ) : ChapterCommand()
    
    /**
     * Clean up resources, cancel subscriptions, and reset state.
     */
    object Cleanup : ChapterCommand()
    
    // ========== Navigation Commands ==========
    
    /**
     * Navigate to the next chapter in sequence.
     * Returns null at the last chapter boundary.
     */
    object NextChapter : ChapterCommand()
    
    /**
     * Navigate to the previous chapter in sequence.
     * Returns null at the first chapter boundary.
     */
    object PreviousChapter : ChapterCommand()
    
    /**
     * Jump to a specific chapter by ID.
     * Emits an error if the chapter is not found.
     */
    data class JumpToChapter(val chapterId: Long) : ChapterCommand()
    
    // ========== Selection Commands (for batch operations) ==========
    
    /**
     * Add a chapter to the selection set.
     * Idempotent - selecting an already selected chapter has no effect.
     */
    data class SelectChapter(val chapterId: Long) : ChapterCommand()
    
    /**
     * Remove a chapter from the selection set.
     * Idempotent - deselecting an unselected chapter has no effect.
     */
    data class DeselectChapter(val chapterId: Long) : ChapterCommand()
    
    /**
     * Select all chapters in the current filtered list.
     */
    object SelectAll : ChapterCommand()
    
    /**
     * Clear all chapter selections.
     */
    object ClearSelection : ChapterCommand()
    
    /**
     * Invert the current selection - selected becomes unselected and vice versa.
     */
    object InvertSelection : ChapterCommand()
    
    // ========== Filter & Sort Commands ==========
    
    /**
     * Apply a filter to the chapters list.
     * Preserves the current selection.
     */
    data class SetFilter(val filter: ChapterFilter) : ChapterCommand()
    
    /**
     * Apply a sort order to the chapters list.
     * Preserves the current selection.
     */
    data class SetSort(val sort: ChapterSort) : ChapterCommand()
    
    // ========== Progress Commands ==========
    
    /**
     * Update reading progress for a chapter.
     * Persists the progress to the database.
     */
    data class UpdateProgress(
        val chapterId: Long,
        val paragraphIndex: Int
    ) : ChapterCommand()
    
    /**
     * Mark a chapter as read.
     */
    data class MarkAsRead(val chapterId: Long) : ChapterCommand()
    
    /**
     * Mark a chapter as unread.
     */
    data class MarkAsUnread(val chapterId: Long) : ChapterCommand()
    
    // ========== Content Commands ==========
    
    /**
     * Preload the next chapter's content in the background.
     * Does not change the current chapter.
     */
    object PreloadNextChapter : ChapterCommand()
    
    /**
     * Refresh chapters from the database.
     */
    object RefreshChapters : ChapterCommand()
}

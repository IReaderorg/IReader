package ireader.domain.services.chapter

import androidx.compose.runtime.Immutable
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter

/**
 * Consolidated types for the Chapter Controller.
 * Contains Command, State, Event, Error, Filter, and Sort definitions.
 */

// ============================================================================
// COMMANDS
// ============================================================================

/**
 * Sealed class representing all commands that can be dispatched to the Chapter Controller.
 * All chapter operations are expressed as commands for predictable state management.
 */
sealed class ChapterCommand {
    // Lifecycle Commands
    /** Load a book and its chapters. Subscribes to chapter updates from the database. */
    data class LoadBook(val bookId: Long) : ChapterCommand()
    /** Load a specific chapter's content. */
    data class LoadChapter(val chapterId: Long, val startParagraph: Int = 0) : ChapterCommand()
    /** Clean up resources, cancel subscriptions, and reset state. */
    object Cleanup : ChapterCommand()
    
    // Navigation Commands
    /** Navigate to the next chapter in sequence. */
    object NextChapter : ChapterCommand()
    /** Navigate to the previous chapter in sequence. */
    object PreviousChapter : ChapterCommand()
    /** Jump to a specific chapter by ID. */
    data class JumpToChapter(val chapterId: Long) : ChapterCommand()
    
    // Selection Commands
    /** Add a chapter to the selection set. */
    data class SelectChapter(val chapterId: Long) : ChapterCommand()
    /** Remove a chapter from the selection set. */
    data class DeselectChapter(val chapterId: Long) : ChapterCommand()
    /** Select all chapters in the current filtered list. */
    object SelectAll : ChapterCommand()
    /** Clear all chapter selections. */
    object ClearSelection : ChapterCommand()
    /** Invert the current selection. */
    object InvertSelection : ChapterCommand()
    
    // Filter & Sort Commands
    /** Apply a filter to the chapters list. */
    data class SetFilter(val filter: ChapterFilter) : ChapterCommand()
    /** Apply a sort order to the chapters list. */
    data class SetSort(val sort: ChapterSort) : ChapterCommand()
    
    // Progress Commands
    /** Update reading progress for a chapter. */
    data class UpdateProgress(val chapterId: Long, val paragraphIndex: Int) : ChapterCommand()
    /** Mark a chapter as read. */
    data class MarkAsRead(val chapterId: Long) : ChapterCommand()
    /** Mark a chapter as unread. */
    data class MarkAsUnread(val chapterId: Long) : ChapterCommand()
    
    // Content Commands
    /** Preload the next chapter's content in the background. */
    object PreloadNextChapter : ChapterCommand()
    /** Refresh chapters from the database. */
    object RefreshChapters : ChapterCommand()
}

// ============================================================================
// STATE
// ============================================================================

/**
 * Immutable data class representing the complete state of chapter operations.
 * This is the single source of truth for all chapter-related data across screens.
 */
@Immutable
data class ChapterState(
    // Book Data
    val book: Book? = null,
    val chapters: List<Chapter> = emptyList(),
    val filteredChapters: List<Chapter> = emptyList(),
    
    // Current Chapter
    val currentChapter: Chapter? = null,
    val currentChapterIndex: Int = 0,
    val currentParagraphIndex: Int = 0,
    
    // Loading States
    val isLoadingBook: Boolean = false,
    val isLoadingChapters: Boolean = false,
    val isLoadingContent: Boolean = false,
    val isPreloading: Boolean = false,
    
    // Selection
    val selectedChapterIds: Set<Long> = emptySet(),
    
    // Filter & Sort
    val filter: ChapterFilter = ChapterFilter.None,
    val sort: ChapterSort = ChapterSort.Default,
    
    // Navigation Helpers
    val lastReadChapterId: Long? = null,
    
    // Error State
    val error: ChapterError? = null
) {
    // Computed Properties
    val hasContent: Boolean get() = currentChapter?.content?.isNotEmpty() == true
    val canGoNext: Boolean get() = chapters.isNotEmpty() && currentChapterIndex < chapters.lastIndex
    val canGoPrevious: Boolean get() = chapters.isNotEmpty() && currentChapterIndex > 0
    val hasSelection: Boolean get() = selectedChapterIds.isNotEmpty()
    val selectionCount: Int get() = selectedChapterIds.size
    val totalChapters: Int get() = chapters.size
    val filteredChapterCount: Int get() = filteredChapters.size
    val isLoading: Boolean get() = isLoadingBook || isLoadingChapters || isLoadingContent
    val hasBook: Boolean get() = book != null
    val hasChapters: Boolean get() = chapters.isNotEmpty()
}

// ============================================================================
// EVENTS
// ============================================================================

/**
 * Sealed class representing one-time events emitted by the Chapter Controller.
 * These events are used for UI feedback and should be consumed once.
 */
sealed class ChapterEvent {
    /** An error occurred during a chapter operation. */
    data class Error(val error: ChapterError) : ChapterEvent()
    /** A chapter was successfully loaded with content. */
    data class ChapterLoaded(val chapter: Chapter) : ChapterEvent()
    /** The current chapter has been completed (reached the end). */
    object ChapterCompleted : ChapterEvent()
    /** Reading progress was successfully saved to the database. */
    data class ProgressSaved(val chapterId: Long) : ChapterEvent()
    /** Chapter content was successfully fetched from remote source. */
    data class ContentFetched(val chapterId: Long) : ChapterEvent()
}

// ============================================================================
// ERRORS
// ============================================================================

/**
 * Sealed class representing all possible errors in chapter operations.
 * Used for type-safe error handling across the Chapter Controller.
 */
sealed class ChapterError {
    /** The requested book was not found in the database. */
    data class BookNotFound(val bookId: Long) : ChapterError()
    /** The requested chapter was not found in the database. */
    data class ChapterNotFound(val chapterId: Long) : ChapterError()
    /** Failed to load chapter content from remote source. */
    data class ContentLoadFailed(val message: String) : ChapterError()
    /** Network-related error occurred during remote operations. */
    data class NetworkError(val message: String) : ChapterError()
    /** Database operation failed. */
    data class DatabaseError(val message: String) : ChapterError()
    
    /** Returns a user-friendly error message. */
    fun toUserMessage(): String = when (this) {
        is BookNotFound -> "Book not found (ID: $bookId)"
        is ChapterNotFound -> "Chapter not found (ID: $chapterId)"
        is ContentLoadFailed -> "Failed to load content: $message"
        is NetworkError -> "Network error: $message"
        is DatabaseError -> "Database error: $message"
    }
}

// ============================================================================
// FILTER
// ============================================================================

/**
 * Sealed class representing chapter filter options.
 * Supports single filters and combined filters for complex filtering.
 */
sealed class ChapterFilter {
    /** No filter applied - show all chapters. */
    object None : ChapterFilter()
    /** Show only read chapters. */
    object ReadOnly : ChapterFilter()
    /** Show only unread chapters. */
    object UnreadOnly : ChapterFilter()
    /** Show only bookmarked chapters. */
    object BookmarkedOnly : ChapterFilter()
    /** Show only downloaded chapters (chapters with content). */
    object DownloadedOnly : ChapterFilter()
    /** Combine multiple filters (AND logic). */
    data class Combined(val filters: Set<ChapterFilter>) : ChapterFilter()
    
    /** Returns a predicate function to filter chapters. */
    fun toPredicate(): (Chapter) -> Boolean = when (this) {
        is None -> { _ -> true }
        is ReadOnly -> { chapter -> chapter.read }
        is UnreadOnly -> { chapter -> !chapter.read }
        is BookmarkedOnly -> { chapter -> chapter.bookmark }
        is DownloadedOnly -> { chapter -> chapter.content.isNotEmpty() }
        is Combined -> { chapter ->
            filters.all { filter ->
                if (filter is Combined) true else filter.toPredicate()(chapter)
            }
        }
    }
}

// ============================================================================
// SORT
// ============================================================================

/**
 * Data class representing chapter sort options.
 */
data class ChapterSort(
    val type: Type = Type.NUMBER,
    val ascending: Boolean = true
) {
    enum class Type {
        NUMBER,
        NAME,
        DATE_ADDED,
        DATE_READ
    }
    
    companion object {
        val Default = ChapterSort(Type.NUMBER, true)
    }
    
    /** Returns a comparator function to sort chapters. */
    fun toComparator(): Comparator<Chapter> {
        val baseComparator: Comparator<Chapter> = when (type) {
            Type.NUMBER -> compareBy { it.number }
            Type.NAME -> compareBy { it.name }
            Type.DATE_ADDED -> compareBy { it.dateFetch }
            Type.DATE_READ -> compareBy { it.lastPageRead }
        }
        return if (ascending) baseComparator else baseComparator.reversed()
    }
}

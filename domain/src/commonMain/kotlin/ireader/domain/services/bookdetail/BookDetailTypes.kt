package ireader.domain.services.bookdetail

import androidx.compose.runtime.Stable
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter

/**
 * Consolidated types for the BookDetail Controller.
 * Contains Command, State, Event, Error, Filter, and Sort definitions.
 * 
 * Requirements: 3.1, 3.3, 3.4, 3.5, 4.1, 4.2, 4.3, 4.4, 4.5, 5.1
 */

// ============================================================================
// COMMANDS
// ============================================================================

/**
 * Sealed class representing all commands that can be dispatched to the BookDetail Controller.
 * All book detail operations are expressed as commands for predictable state management.
 */
sealed class BookDetailCommand {
    // Lifecycle Commands
    /** Load a book and its chapters, subscribe to reactive updates. */
    data class LoadBook(val bookId: Long) : BookDetailCommand()
    /** Clean up resources, cancel subscriptions, and reset state. */
    object Cleanup : BookDetailCommand()
    
    // Selection Commands
    /** Select a chapter by ID. */
    data class SelectChapter(val chapterId: Long) : BookDetailCommand()
    /** Deselect a chapter by ID. */
    data class DeselectChapter(val chapterId: Long) : BookDetailCommand()
    /** Toggle selection state of a chapter. */
    data class ToggleChapterSelection(val chapterId: Long) : BookDetailCommand()
    /** Clear all chapter selections. */
    object ClearSelection : BookDetailCommand()
    /** Select all chapters (optionally filtered). */
    data class SelectAll(val onlyFiltered: Boolean = true) : BookDetailCommand()
    /** Select chapters in a range (for shift-click selection). */
    data class SelectRange(val fromChapterId: Long, val toChapterId: Long) : BookDetailCommand()
    
    // Filter Commands
    /** Set the chapter filter. */
    data class SetFilter(val filter: ChapterFilter) : BookDetailCommand()
    /** Set the search query for filtering chapters. */
    data class SetSearchQuery(val query: String?) : BookDetailCommand()
    
    // Sort Commands
    /** Set the chapter sort order. */
    data class SetSort(val sort: ChapterSortOrder) : BookDetailCommand()
    
    // Refresh Commands
    /** Refresh chapters from the source. */
    object RefreshChapters : BookDetailCommand()
    /** Refresh book details from the source. */
    object RefreshBook : BookDetailCommand()
    
    // Navigation Commands
    /** Navigate to reader with a specific chapter. */
    data class NavigateToReader(val chapterId: Long) : BookDetailCommand()
    /** Navigate to reader with the last read chapter or first chapter. */
    object ContinueReading : BookDetailCommand()
    
    // Error Commands
    /** Clear the current error state. */
    object ClearError : BookDetailCommand()
}

// ============================================================================
// FILTER
// ============================================================================

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

// ============================================================================
// SORT
// ============================================================================

/**
 * Sort options for chapters.
 */
data class ChapterSortOrder(
    val type: Type = Type.SOURCE,
    val ascending: Boolean = true
) {
    enum class Type {
        SOURCE,
        CHAPTER_NUMBER,
        UPLOAD_DATE,
        NAME
    }
    
    companion object {
        val Default = ChapterSortOrder(Type.SOURCE, ascending = true)
    }
}

// ============================================================================
// STATE
// ============================================================================

/**
 * Stable data class representing the complete state of book detail operations.
 * This is the single source of truth for all book detail-related data.
 */
@Stable
data class BookDetailState(
    // Book Data
    val book: Book? = null,
    val chapters: List<Chapter> = emptyList(),
    val filteredChapters: List<Chapter> = emptyList(),
    
    // Selection State
    val selectedChapterIds: Set<Long> = emptySet(),
    
    // Filter State
    val filter: ChapterFilter = ChapterFilter.None,
    val searchQuery: String? = null,
    
    // Sort State
    val sort: ChapterSortOrder = ChapterSortOrder.Default,
    
    // Reading Progress
    val lastReadChapterId: Long? = null,
    
    // Loading States
    val isLoading: Boolean = false,
    val isRefreshingBook: Boolean = false,
    val isRefreshingChapters: Boolean = false,
    
    // Error State
    val error: BookDetailError? = null
) {
    // Computed Properties
    val selectionCount: Int get() = selectedChapterIds.size
    val hasSelection: Boolean get() = selectedChapterIds.isNotEmpty()
    val hasBook: Boolean get() = book != null
    val hasChapters: Boolean get() = chapters.isNotEmpty()
    val totalChapters: Int get() = chapters.size
    val readChapters: Int get() = chapters.count { it.read }
    val unreadChapters: Int get() = chapters.count { !it.read }
    val bookmarkedChapters: Int get() = chapters.count { it.bookmark }
    val downloadedChapters: Int get() = chapters.count { it.content.isNotEmpty() }
    val isAnyLoading: Boolean get() = isLoading || isRefreshingBook || isRefreshingChapters
    val hasError: Boolean get() = error != null
    val allFilteredSelected: Boolean get() = filteredChapters.isNotEmpty() && filteredChapters.all { it.id in selectedChapterIds }
    
    fun getSelectedChapters(): List<Chapter> = chapters.filter { it.id in selectedChapterIds }
    
    fun getContinueReadingChapter(): Chapter? {
        if (chapters.isEmpty()) return null
        lastReadChapterId?.let { lastId ->
            chapters.find { it.id == lastId }?.let { return it }
        }
        val sortedChapters = if (sort.ascending) chapters else chapters.reversed()
        return sortedChapters.firstOrNull { !it.read } ?: sortedChapters.firstOrNull()
    }
}

// ============================================================================
// EVENTS
// ============================================================================

/**
 * Sealed class representing one-time events emitted by the BookDetail Controller.
 * These events are used for UI feedback and should be consumed once.
 */
sealed class BookDetailEvent {
    /** An error occurred during a book detail operation. */
    data class Error(val error: BookDetailError) : BookDetailEvent()
    /** Book was successfully loaded. */
    data class BookLoaded(val book: Book) : BookDetailEvent()
    /** Chapters were successfully loaded. */
    data class ChaptersLoaded(val count: Int) : BookDetailEvent()
    /** Navigate to reader with specific book and chapter. */
    data class NavigateToReader(val bookId: Long, val chapterId: Long) : BookDetailEvent()
    /** Navigate to web view for a URL. */
    data class NavigateToWebView(val url: String, val sourceId: Long, val bookId: Long) : BookDetailEvent()
    /** Navigate back from book detail screen. */
    object NavigateBack : BookDetailEvent()
    /** Book was refreshed from source. */
    object BookRefreshed : BookDetailEvent()
    /** Chapters were refreshed from source. */
    data class ChaptersRefreshed(val newCount: Int, val totalCount: Int) : BookDetailEvent()
    /** Selection state changed. */
    data class SelectionChanged(val selectedCount: Int) : BookDetailEvent()
    /** Show a snackbar message. */
    data class ShowSnackbar(val message: String) : BookDetailEvent()
}

// ============================================================================
// ERRORS
// ============================================================================

/**
 * Sealed class representing all possible errors in book detail operations.
 * Used for type-safe error handling across the BookDetail Controller.
 */
sealed class BookDetailError {
    /** Failed to load book or chapters. */
    data class LoadFailed(val message: String) : BookDetailError()
    /** Network error occurred during book detail operations. */
    data class NetworkError(val message: String) : BookDetailError()
    /** The requested book was not found. */
    data class NotFound(val bookId: Long) : BookDetailError()
    /** Failed to refresh book or chapters from source. */
    data class RefreshFailed(val message: String) : BookDetailError()
    /** Source/extension not available for this book. */
    data class SourceNotAvailable(val sourceId: Long) : BookDetailError()
    /** Database operation failed. */
    data class DatabaseError(val message: String) : BookDetailError()
    
    /** Returns a user-friendly error message. */
    fun toUserMessage(): String = when (this) {
        is LoadFailed -> "Failed to load book: $message"
        is NetworkError -> "Network error: $message"
        is NotFound -> "Book not found (ID: $bookId)"
        is RefreshFailed -> "Failed to refresh: $message"
        is SourceNotAvailable -> "Source not available (ID: $sourceId)"
        is DatabaseError -> "Database error: $message"
    }
}

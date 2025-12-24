package ireader.domain.services.chapter

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlin.time.Duration.Companion.milliseconds

/**
 * Lightweight change notification system for chapter data.
 * 
 * Similar to LibraryChangeNotifier, this provides reactive signals when
 * chapter data changes, allowing ViewModels to refresh their state.
 * 
 * This solves the problem of ReaderScreenViewModel having stale chapter data:
 * - ChapterController subscribes to database changes
 * - ChapterNotifier emits lightweight signals when changes occur
 * - ReaderScreenViewModel/TTSViewModel observe and refresh as needed
 * 
 * Performance optimizations:
 * - Uses extraBufferCapacity to avoid blocking emitters
 * - Provides filtered flows for specific books to reduce unnecessary processing
 * - changesForBookDebounced() adds debouncing for batch operations
 * - tryNotifyChange is non-blocking for use in hot paths
 * 
 * Usage:
 * 1. ChapterController calls notifyChange() when chapters are modified
 * 2. ViewModels observe changes flow and reload when needed
 */
@OptIn(FlowPreview::class)
class ChapterNotifier {
    
    /**
     * Types of changes that can occur with chapters.
     */
    sealed class ChangeType {
        /** A chapter was added */
        data class ChapterAdded(val chapterId: Long, val bookId: Long) : ChangeType()
        
        /** A chapter was removed */
        data class ChapterRemoved(val chapterId: Long, val bookId: Long) : ChangeType()
        
        /** A chapter's data was updated (content, metadata, etc.) */
        data class ChapterUpdated(val chapterId: Long, val bookId: Long) : ChangeType()
        
        /** Multiple chapters were updated */
        data class ChaptersUpdated(val chapterIds: List<Long>, val bookId: Long) : ChangeType()
        
        /** Chapter content was fetched from remote */
        data class ContentFetched(val chapterId: Long, val bookId: Long) : ChangeType()
        
        /** Chapter read progress changed */
        data class ProgressChanged(val chapterId: Long, val bookId: Long) : ChangeType()
        
        /** All chapters for a book were refreshed */
        data class BookChaptersRefreshed(val bookId: Long) : ChangeType()
        
        /** Current chapter changed (navigation) */
        data class CurrentChapterChanged(val chapterId: Long, val bookId: Long) : ChangeType()
        
        /** Full refresh needed */
        object FullRefresh : ChangeType()
        
        /**
         * Extract bookId from change type, or null for FullRefresh.
         */
        fun getBookId(): Long? = when (this) {
            is ChapterAdded -> bookId
            is ChapterRemoved -> bookId
            is ChapterUpdated -> bookId
            is ChaptersUpdated -> bookId
            is ContentFetched -> bookId
            is ProgressChanged -> bookId
            is BookChaptersRefreshed -> bookId
            is CurrentChapterChanged -> bookId
            is FullRefresh -> null
        }
    }
    
    private val _changes = MutableSharedFlow<ChangeType>(
        replay = 0,
        extraBufferCapacity = 64 // Buffer to avoid blocking emitters
    )
    
    /**
     * Flow of chapter changes. Observe this to know when to reload data.
     */
    val changes: Flow<ChangeType> = _changes.asSharedFlow()
    
    /**
     * Get a filtered flow for changes affecting a specific book.
     * More efficient than filtering in the collector.
     * 
     * @param bookId The book ID to filter for
     * @return Flow of changes for this book only (plus FullRefresh)
     */
    fun changesForBook(bookId: Long): Flow<ChangeType> = changes
        .mapNotNull { change ->
            val changeBookId = change.getBookId()
            if (changeBookId == bookId || change is ChangeType.FullRefresh) {
                change
            } else {
                null
            }
        }
        .distinctUntilChanged() // Skip duplicate consecutive emissions
    
    /**
     * Get a filtered and debounced flow for changes affecting a specific book.
     * Best for UI that doesn't need immediate updates (e.g., chapter drawer).
     * 
     * @param bookId The book ID to filter for
     * @param debounceMs Debounce duration in milliseconds (default 100ms)
     * @return Debounced flow of changes for this book only
     */
    fun changesForBookDebounced(bookId: Long, debounceMs: Long = 100): Flow<ChangeType> = 
        changesForBook(bookId).debounce(debounceMs.milliseconds)
    
    /**
     * Notify that a change occurred.
     */
    suspend fun notifyChange(change: ChangeType) {
        _changes.emit(change)
    }
    
    /**
     * Non-suspending version for use in callbacks.
     * Returns true if emitted, false if buffer is full.
     */
    fun tryNotifyChange(change: ChangeType): Boolean {
        return _changes.tryEmit(change)
    }
}

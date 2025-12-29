package ireader.domain.services.library

import ireader.core.log.IReaderLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Lightweight change notification system for library data.
 * 
 * This solves the problem of knowing when to reload paginated data:
 * - Old approach: subscribe() returned Flow that auto-updated on DB changes
 * - New approach: awaitPaginated() is one-shot, doesn't know when data changed
 * 
 * LibraryChangeNotifier provides a lightweight signal that data has changed,
 * without loading all the data. The ViewModel can observe this and decide
 * whether to reload its paginated data.
 * 
 * Usage:
 * 1. Repository/UseCase calls notifyChange() when books are modified
 * 2. ViewModel observes changes flow and reloads pagination when needed
 * 
 * This is more efficient than subscribing to all books because:
 * - Only emits a signal, doesn't transfer book data
 * - ViewModel can debounce/batch multiple changes
 * - ViewModel can decide which categories need reloading
 */
class LibraryChangeNotifier {
    
    /**
     * Types of changes that can occur in the library.
     * This helps the ViewModel decide what to reload.
     */
    sealed class ChangeType {
        /** A book was added to the library */
        data class BookAdded(val bookId: Long) : ChangeType()
        
        /** A book was removed from the library */
        data class BookRemoved(val bookId: Long) : ChangeType()
        
        /** A book's data was updated (title, cover, chapters, etc.) */
        data class BookUpdated(val bookId: Long) : ChangeType()
        
        /** Multiple books were updated (batch operation) */
        data class BooksUpdated(val bookIds: List<Long>) : ChangeType()
        
        /** A book's category assignment changed */
        data class CategoryChanged(val bookId: Long, val categoryId: Long?) : ChangeType()
        
        /** Multiple books' categories changed */
        data class CategoriesChanged(val bookIds: List<Long>) : ChangeType()
        
        /** Book's read progress changed (last_read_at, unread count) */
        data class ReadProgressChanged(val bookId: Long) : ChangeType()
        
        /** Full library refresh needed (e.g., after sync, import) */
        object FullRefresh : ChangeType()
        
        /** Unknown change - reload everything to be safe */
        object Unknown : ChangeType()
    }
    
    private val _changes = MutableSharedFlow<ChangeType>(
        replay = 1, // Keep last event for late subscribers
        extraBufferCapacity = 64 // Buffer to prevent blocking writers
    )
    
    /** Track number of active subscribers for debugging */
    private var subscriberCount = 0
    
    /**
     * Flow of library changes. Observe this to know when to reload data.
     */
    val changes: Flow<ChangeType> = _changes.asSharedFlow()
    
    /**
     * Notify that a change occurred in the library.
     * Call this from repositories/use cases when data is modified.
     * 
     * This is a suspend function but will not block - it uses a buffer.
     */
    suspend fun notifyChange(change: ChangeType) {
        IReaderLog.info("LibraryChangeNotifier.notifyChange() called: $change, subscribers=${_changes.subscriptionCount.value}", "ChangeNotifier")
        val emitted = _changes.emit(change)
        IReaderLog.info("LibraryChangeNotifier.notifyChange() emitted: $change", "ChangeNotifier")
    }
    
    /**
     * Non-suspending version for use in callbacks.
     * Returns true if the change was emitted, false if buffer was full.
     */
    fun tryNotifyChange(change: ChangeType): Boolean {
        IReaderLog.info("LibraryChangeNotifier.tryNotifyChange() called: $change, subscribers=${_changes.subscriptionCount.value}", "ChangeNotifier")
        val result = _changes.tryEmit(change)
        IReaderLog.info("LibraryChangeNotifier.tryNotifyChange() result=$result for: $change", "ChangeNotifier")
        return result
    }
}

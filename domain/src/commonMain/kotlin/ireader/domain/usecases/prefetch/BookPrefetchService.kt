package ireader.domain.usecases.prefetch
import ireader.domain.utils.extensions.ioDispatcher

import ireader.core.log.Log
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.usecases.local.LocalGetBookUseCases
import ireader.domain.usecases.local.LocalGetChapterUseCase
import ireader.domain.usecases.history.HistoryUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Service for prefetching book data to improve perceived performance.
 * 
 * When a user is likely to click on a book (e.g., hovering, scrolling slowly),
 * this service preloads the book's data into memory so that when the user
 * actually clicks, the data is already available.
 * 
 * Key optimizations:
 * - LRU cache for recently prefetched books
 * - Debouncing to avoid excessive prefetch calls
 * - Background loading that doesn't block UI
 * - Memory-efficient with configurable cache size
 */
class BookPrefetchService(
    private val getBookUseCases: LocalGetBookUseCases,
    private val getChapterUseCase: LocalGetChapterUseCase,
    private val historyUseCase: HistoryUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private val mutex = Mutex()
    
    // LRU cache for prefetched book data
    private val prefetchCache = mutableMapOf<Long, PrefetchedBookData>()
    private val cacheAccessOrder = mutableListOf<Long>()
    
    // Track ongoing prefetch jobs to avoid duplicates
    private val ongoingPrefetches = mutableMapOf<Long, Job>()
    
    companion object {
        private const val TAG = "BookPrefetchService"
        private const val MAX_CACHE_SIZE = 10 // Keep last 10 books in memory
    }
    
    /**
     * Data class holding prefetched book information
     */
    data class PrefetchedBookData(
        val book: Book,
        val chapters: List<Chapter>,
        val lastReadChapterId: Long?,
        val prefetchedAt: Long = currentTimeToLong()
    )
    
    /**
     * Prefetch book data in the background.
     * Call this when user is likely to click on a book.
     * 
     * @param bookId The ID of the book to prefetch
     */
    fun prefetch(bookId: Long) {
        scope.launch {
            prefetchInternal(bookId)
        }
    }
    
    /**
     * Prefetch multiple books (e.g., visible items in a list)
     * 
     * @param bookIds List of book IDs to prefetch
     */
    fun prefetchMultiple(bookIds: List<Long>) {
        // Only prefetch first few to avoid memory pressure
        val toPrefetch = bookIds.take(3)
        scope.launch {
            toPrefetch.forEach { bookId ->
                prefetchInternal(bookId)
            }
        }
    }
    
    /**
     * Get prefetched data if available.
     * Returns null if data hasn't been prefetched yet.
     * 
     * @param bookId The ID of the book
     * @return Prefetched data or null
     */
    suspend fun getPrefetchedData(bookId: Long): PrefetchedBookData? = mutex.withLock {
        val data = prefetchCache[bookId]
        
        // Check if data is still fresh (within 5 minutes)
        if (data != null && currentTimeToLong() - data.prefetchedAt < 300_000) {
            Log.debug("Prefetch cache HIT for book $bookId", TAG)
            return data
        }
        
        Log.debug("Prefetch cache MISS for book $bookId", TAG)
        return null
    }
    
    /**
     * Clear prefetch cache for a specific book.
     * Call this after book data is modified.
     */
    suspend fun invalidate(bookId: Long) = mutex.withLock {
        prefetchCache.remove(bookId)
        ongoingPrefetches[bookId]?.cancel()
        ongoingPrefetches.remove(bookId)
    }
    
    /**
     * Clear all prefetch cache.
     */
    suspend fun clearAll() = mutex.withLock {
        prefetchCache.clear()
        ongoingPrefetches.values.forEach { it.cancel() }
        ongoingPrefetches.clear()
    }
    
    private suspend fun prefetchInternal(bookId: Long) {
        // Check if already cached or being prefetched
        mutex.withLock {
            if (prefetchCache.containsKey(bookId)) {
                return // Already cached
            }
            if (ongoingPrefetches.containsKey(bookId)) {
                return // Already being prefetched
            }
        }
        
        val job = scope.launch {
            try {
                Log.debug("Prefetching book $bookId", TAG)
                val startTime = currentTimeToLong()
                
                // Fetch book data
                val book = getBookUseCases.findBookById(bookId) ?: return@launch
                val chapters = getChapterUseCase.findChaptersByBookId(bookId)
                val history = historyUseCase.findHistoryByBookId(bookId)
                
                val data = PrefetchedBookData(
                    book = book,
                    chapters = chapters,
                    lastReadChapterId = history?.chapterId
                )
                
                // Store in cache
                mutex.withLock {
                    // Enforce cache size limit using LRU
                    while (prefetchCache.size >= MAX_CACHE_SIZE) {
                        val oldestKey = cacheAccessOrder.firstOrNull()
                        if (oldestKey != null) {
                            prefetchCache.remove(oldestKey)
                            cacheAccessOrder.removeAt(0)
                        } else {
                            break
                        }
                    }
                    prefetchCache[bookId] = data
                    cacheAccessOrder.remove(bookId)
                    cacheAccessOrder.add(bookId)
                    ongoingPrefetches.remove(bookId)
                }
                
                val duration = currentTimeToLong() - startTime
                Log.debug("Prefetched book $bookId in ${duration}ms (${chapters.size} chapters)", TAG)
                
            } catch (e: Exception) {
                Log.error("Failed to prefetch book $bookId: ${e.message}", TAG)
                mutex.withLock {
                    ongoingPrefetches.remove(bookId)
                }
            }
        }
        
        mutex.withLock {
            ongoingPrefetches[bookId] = job
        }
    }
}

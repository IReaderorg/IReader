package ireader.domain.services.download

import ireader.core.log.Log
import ireader.domain.data.repository.ChapterRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

/**
 * Desktop implementation of DownloadCache.
 * Uses ConcurrentHashMap for thread-safe in-memory caching with TTL-based refresh.
 */
class DesktopDownloadCache(
    private val chapterRepository: ChapterRepository
) : DownloadCache {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    // Map<BookId, Set<ChapterId>>
    private val cache = ConcurrentHashMap<Long, MutableSet<Long>>()
    
    @Volatile
    private var lastRefresh: Long = 0
    
    @Volatile
    private var initialized = false
    
    private val mutex = Mutex()
    
    override fun isChapterDownloaded(bookId: Long, chapterId: Long): Boolean {
        checkAndRefreshIfNeeded()
        return cache[bookId]?.contains(chapterId) == true
    }
    
    override fun getDownloadedChapterIds(bookId: Long): Set<Long> {
        checkAndRefreshIfNeeded()
        return cache[bookId]?.toSet() ?: emptySet()
    }
    
    override fun addDownloadedChapter(bookId: Long, chapterId: Long) {
        cache.getOrPut(bookId) { ConcurrentHashMap.newKeySet() }.add(chapterId)
        Log.debug { "DownloadCache: Added chapter $chapterId for book $bookId" }
    }
    
    override fun removeDownloadedChapter(bookId: Long, chapterId: Long) {
        cache[bookId]?.remove(chapterId)
        // Clean up empty sets
        if (cache[bookId]?.isEmpty() == true) {
            cache.remove(bookId)
        }
        Log.debug { "DownloadCache: Removed chapter $chapterId for book $bookId" }
    }
    
    override fun invalidate() {
        cache.clear()
        lastRefresh = 0
        initialized = false
        Log.debug { "DownloadCache: Invalidated entire cache" }
    }
    
    override fun invalidateBook(bookId: Long) {
        cache.remove(bookId)
        Log.debug { "DownloadCache: Invalidated cache for book $bookId" }
    }
    
    override suspend fun refresh() {
        mutex.withLock {
            try {
                Log.debug { "DownloadCache: Starting refresh from database" }
                cache.clear()
                
                // Query all chapters that have content (downloaded)
                val downloadedChapters = chapterRepository.findAllDownloadedChapters()
                
                for (chapter in downloadedChapters) {
                    cache.getOrPut(chapter.bookId) { ConcurrentHashMap.newKeySet() }
                        .add(chapter.id)
                }
                
                lastRefresh = System.currentTimeMillis()
                initialized = true
                Log.debug { "DownloadCache: Refreshed with ${getDownloadedCount()} chapters" }
            } catch (e: Exception) {
                Log.error(e) { "DownloadCache: Failed to refresh" }
            }
        }
    }
    
    override fun getDownloadedCount(): Int {
        return cache.values.sumOf { it.size }
    }
    
    override fun getDownloadedCount(bookId: Long): Int {
        return cache[bookId]?.size ?: 0
    }
    
    override fun isInitialized(): Boolean = initialized
    
    private fun checkAndRefreshIfNeeded() {
        val now = System.currentTimeMillis()
        if (!initialized || (now - lastRefresh > DownloadCache.DEFAULT_TTL_MS)) {
            scope.launch {
                refresh()
            }
        }
    }
}

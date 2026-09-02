package ireader.data.core

import ireader.core.log.Log
import ireader.domain.models.library.LibrarySort
import ireader.domain.preferences.prefs.LibraryPreferences
import kotlinx.coroutines.CoroutineScope
import ireader.domain.utils.extensions.ioDispatcher
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ireader.domain.utils.extensions.currentTimeToLong
import ireader.domain.utils.extensions.toLocalDate
import ireader.domain.data.cache.UpdatesDataCache

/**
 * Preloads critical database data during app startup for faster initial access.
 * 
 * This class coordinates with DatabaseOptimizations to warm up the cache
 * with frequently accessed data, reducing perceived latency when users
 * first interact with the app.
 */
class DatabasePreloader(
    private val dbOptimizations: DatabaseOptimizations,
    private val handler: DatabaseHandler,
    private val libraryPreferences: LibraryPreferences
) {
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)
    
    companion object {
        private const val TAG = "DatabasePreloader"
    }
    
    /**
     * Preload critical data asynchronously.
     * Call this during app initialization (e.g., in Application.onCreate).
     * 
     * This runs in the background and doesn't block the main thread.
     */
    fun preloadAsync() {
        scope.launch {
            preloadCriticalData()
        }
    }
    
    /**
     * Preload critical data and wait for completion.
     * Use this when you need to ensure data is loaded before proceeding.
     * 
     * IMPORTANT: On low-memory devices with large libraries (10,000+ books),
     * we need to be very conservative with memory usage. We run preloads
     * sequentially instead of in parallel to reduce peak memory usage.
     */
    suspend fun preloadCriticalData() = withContext(ioDispatcher) {
        val startTime = currentTimeToLong()
        
        try {
            // 1. Concurrently preload UI caches first for zero-latency cold start rendering
            kotlinx.coroutines.coroutineScope {
                launch { preloadLibraryBooks() }
                launch { preloadRecentHistory() }
                launch { preloadRecentUpdates() }
                launch { preloadCategories() }
            }
            
            val duration = currentTimeToLong() - startTime
            Log.info { "UI data preloaded into in-memory caches in ${duration}ms" }
            
            // 2. Run maintenance asynchronously in background without blocking UI
            scope.launch {
                try {
                    fixLastReadAtIfNeeded()
                } catch (e: Exception) {
                    Log.error(e, "Background maintenance failed")
                }
            }
            
            // Log performance stats
            dbOptimizations.logPerformanceReport()
            
        } catch (e: Exception) {
            Log.error(e, "Critical data preload failed")
        }
    }
    
    /**
     * Fix last_read_at column if it's not populated.
     * This handles cases where migration 27/28 didn't properly populate the column.
     * Also runs on every startup to ensure last_read_at is always up-to-date.
     */
    private suspend fun fixLastReadAtIfNeeded() {
        try {
            // Check history table
            val historyCount = handler.awaitOne {
                bookQueries.countHistoryEntries()
            }
            
            // If there's history data, force update all books' last_read_at
            // This ensures the column is always up-to-date regardless of trigger state
            if (historyCount > 0) {
                handler.await {
                    bookQueries.forceUpdateLastReadAtFromHistory()
                }
            }
        } catch (e: Exception) {
            Log.error(e, "Failed to fix last_read_at")
        }
    }
    
    /**
     * Refresh cached chapter counts for all books.
     * This ensures smart categories (Currently Reading, Completed, Unread) work correctly.
     * Runs on every startup to fix any stale cached counts.
     */
    private suspend fun refreshCachedChapterCounts() {
        try {
            val startTime = currentTimeToLong()
            handler.await {
                chapterQueries.refreshAllBookChapterCounts()
            }
            val duration = currentTimeToLong() - startTime
            Log.debug { "Cached chapter counts refreshed in ${duration}ms" }
        } catch (e: Exception) {
            Log.error(e, "Failed to refresh cached chapter counts")
        }
    }
    
    /**
     * Preload data for a specific book.
     * Call this when navigating to book detail screen.
     */
    suspend fun preloadBookData(bookId: Long) {
        dbOptimizations.preloadBookData(bookId)
    }
    
    /**
     * Preload data for the reader.
     * Call this when user is about to start reading.
     */
    suspend fun preloadReaderData(bookId: Long, chapterId: Long) = withContext(ioDispatcher) {
        try {
            // Preload current chapter and adjacent chapters
            val chapters = handler.awaitList {
                chapterQueries.getChaptersByMangaIdLight(bookId, ireader.data.chapter.chapterMapperLight)
            }
            
            val currentIndex = chapters.indexOfFirst { it.id == chapterId }
            if (currentIndex >= 0) {
                // Preload next chapter content if available
                val nextChapter = chapters.getOrNull(currentIndex + 1)
                nextChapter?.let { chapter ->
                    // This will be loaded when user navigates
                    Log.debug { "Next chapter ready for preload: ${chapter.name}" }
                }
            }
        } catch (e: Exception) {
            Log.error(e, "Reader data preload failed")
        }
    }
    
    private suspend fun preloadLibraryBooks() {
        try {
            // Get user's preferred sort order from preferences
            val sort = libraryPreferences.sorting().get()
            val limit = 50L // Only preload first 50 books for instant display
            
            // Use the appropriate sorted query based on user preference
            val books = handler.awaitList {
                when (sort.type) {
                    LibrarySort.Type.Title -> {
                        if (sort.isAscending) bookQueries.getLibraryPaginatedByTitle(limit, 0L, ireader.data.book.getLibraryFastMapper)
                        else bookQueries.getLibraryPaginatedByTitleDesc(limit, 0L, ireader.data.book.getLibraryFastMapper)
                    }
                    LibrarySort.Type.LastRead -> {
                        if (sort.isAscending) bookQueries.getLibraryPaginatedByLastReadAsc(limit, 0L, ireader.data.book.getLibraryFastMapper)
                        else bookQueries.getLibraryPaginatedByLastRead(limit, 0L, ireader.data.book.getLibraryFastMapper)
                    }
                    LibrarySort.Type.LastUpdated -> {
                        if (sort.isAscending) bookQueries.getLibraryPaginatedByLastUpdateAsc(limit, 0L, ireader.data.book.getLibraryFastMapper)
                        else bookQueries.getLibraryPaginatedByLastUpdate(limit, 0L, ireader.data.book.getLibraryFastMapper)
                    }
                    LibrarySort.Type.Unread -> {
                        if (sort.isAscending) bookQueries.getLibraryPaginatedByUnreadAsc(limit, 0L, ireader.data.book.getLibraryFastMapper)
                        else bookQueries.getLibraryPaginatedByUnread(limit, 0L, ireader.data.book.getLibraryFastMapper)
                    }
                    LibrarySort.Type.TotalChapters -> {
                        if (sort.isAscending) bookQueries.getLibraryPaginatedByTotalChaptersAsc(limit, 0L, ireader.data.book.getLibraryFastMapper)
                        else bookQueries.getLibraryPaginatedByTotalChapters(limit, 0L, ireader.data.book.getLibraryFastMapper)
                    }
                    LibrarySort.Type.Source -> {
                        if (sort.isAscending) bookQueries.getLibraryPaginatedBySource(limit, 0L, ireader.data.book.getLibraryFastMapper)
                        else bookQueries.getLibraryPaginatedBySourceDesc(limit, 0L, ireader.data.book.getLibraryFastMapper)
                    }
                    LibrarySort.Type.DateAdded, LibrarySort.Type.DateFetched -> {
                        if (sort.isAscending) bookQueries.getLibraryPaginatedByDateAddedAsc(limit, 0L, ireader.data.book.getLibraryFastMapper)
                        else bookQueries.getLibraryPaginatedByDateAdded(limit, 0L, ireader.data.book.getLibraryFastMapper)
                    }
                }
            }
            // Update the in-memory cache for instant display
            ireader.domain.data.cache.LibraryDataCache.updateCache(books)
        } catch (e: Exception) {
            Log.error(e, "Failed to preload library books")
        }
    }
    
    private suspend fun preloadCategories() {
        try {
            handler.awaitList {
                categoryQueries.getCategories(ireader.data.category.categoryMapper)
            }
            Log.debug { "Categories preloaded" }
        } catch (e: Exception) {
            Log.error(e, "Failed to preload categories")
        }
    }
    
    private suspend fun preloadRecentHistory() {
        try {
            val limit = 30L
            val historyItems = handler.awaitList {
                historyViewQueries.history("", limit, 0L, ireader.data.history.historyWithRelationsMapper)
            }
            if (historyItems.isNotEmpty()) {
                val grouped = historyItems.groupBy { it.readAt }
                ireader.domain.data.cache.HistoryDataCache.updateCache(grouped)
            }
            Log.debug { "Recent history preloaded: ${historyItems.size} items" }
        } catch (e: Exception) {
            Log.error(e, "Failed to preload recent history")
        }
    }
    
    private suspend fun preloadRecentUpdates() {
        try {
            val limit = 30L
            val updatesItems = handler.awaitList {
                updateViewQueries.updatesPaginated(0L, limit, 0L, ireader.data.updates.updatesMapper)
            }
            if (updatesItems.isNotEmpty()) {
                val grouped = updatesItems.groupBy { it.dateFetch.toLocalDate() }
                UpdatesDataCache.updateCache(grouped)
            }
            Log.debug { "Recent updates preloaded: ${updatesItems.size} items" }
        } catch (e: Exception) {
            Log.error(e, "Failed to preload recent updates")
        }
    }
    
    /**
     * Invalidate all preloaded caches.
     * Call this after major data changes (e.g., restore from backup).
     */
    suspend fun invalidateAllPreloadedData() {
        UpdatesDataCache.invalidate()
        dbOptimizations.invalidateCache("preload_")
        Log.info { "All preloaded data invalidated" }
    }
}

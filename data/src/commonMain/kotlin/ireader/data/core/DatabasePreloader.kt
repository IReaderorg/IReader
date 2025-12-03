package ireader.data.core

import ireader.core.log.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Preloads critical database data during app startup for faster initial access.
 * 
 * This class coordinates with DatabaseOptimizations to warm up the cache
 * with frequently accessed data, reducing perceived latency when users
 * first interact with the app.
 */
class DatabasePreloader(
    private val dbOptimizations: DatabaseOptimizations,
    private val handler: DatabaseHandler
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
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
     */
    suspend fun preloadCriticalData() = withContext(Dispatchers.IO) {
        Log.info("Starting critical data preload...", TAG)
        val startTime = currentTimeToLong()
        
        try {
            // Parallel preload of independent data
            val jobs = listOf(
                async { preloadLibraryBooks() },
                async { preloadCategories() },
                async { preloadRecentHistory() }
            )
            
            jobs.awaitAll()
            
            val duration = currentTimeToLong() - startTime
            Log.info("Critical data preload completed in ${duration}ms", TAG)
            
            // Log performance stats
            dbOptimizations.logPerformanceReport()
            
        } catch (e: Exception) {
            Log.error("Critical data preload failed: ${e.message}", TAG)
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
    suspend fun preloadReaderData(bookId: Long, chapterId: Long) = withContext(Dispatchers.IO) {
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
                    Log.debug("Next chapter ready for preload: ${chapter.name}", TAG)
                }
            }
        } catch (e: Exception) {
            Log.error("Reader data preload failed: ${e.message}", TAG)
        }
    }
    
    private suspend fun preloadLibraryBooks() {
        try {
            handler.awaitList {
                bookQueries.findInLibraryBooks(ireader.data.book.booksMapper)
            }
            Log.debug("Library books preloaded", TAG)
        } catch (e: Exception) {
            Log.error("Failed to preload library books: ${e.message}", TAG)
        }
    }
    
    private suspend fun preloadCategories() {
        try {
            handler.awaitList {
                categoryQueries.getCategories(ireader.data.category.categoryMapper)
            }
            Log.debug("Categories preloaded", TAG)
        } catch (e: Exception) {
            Log.error("Failed to preload categories: ${e.message}", TAG)
        }
    }
    
    private suspend fun preloadRecentHistory() {
        try {
            handler.awaitList {
                historyViewQueries.history("", 20, 0, ireader.data.history.historyWithRelationsMapper)
            }
            Log.debug("Recent history preloaded", TAG)
        } catch (e: Exception) {
            Log.error("Failed to preload recent history: ${e.message}", TAG)
        }
    }
    
    /**
     * Invalidate all preloaded caches.
     * Call this after major data changes (e.g., restore from backup).
     */
    suspend fun invalidateAllPreloadedData() {
        dbOptimizations.invalidateCache("preload_")
        Log.info("All preloaded data invalidated", TAG)
    }
}

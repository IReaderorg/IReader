package ireader.domain.services.chapter

import ireader.core.log.Log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * ChapterOperationQueue - Serializes chapter remote fetch and DB insert operations
 * to prevent race conditions.
 *
 * The problem:
 * - Reader's fetchRemoteChapter and ChapterController's loadChapter can both try
 *   to fetch the same chapter from remote simultaneously
 * - Reader's preloadChapter and ChapterController's preloadNextChapter can both
 *   try to fetch the same next chapter simultaneously
 * - Multiple concurrent insertChapter calls for the same chapter cause conflicts
 *
 * The solution:
 * - Per-chapter mutex ensures only one operation runs per chapter at a time
 * - Global concurrency semaphore limits total simultaneous remote fetches
 * - Recent completion cache prevents redundant fetches of recently-loaded chapters
 * - All remote fetch + insert paths should go through this queue
 */
class ChapterOperationQueue {

    companion object {
        private const val TAG = "ChapterOperationQueue"
        
        /** Maximum concurrent remote fetch operations across all chapters */
        private const val MAX_CONCURRENT_FETCHES = 2
        
        /** How long (ms) to remember that a chapter was recently fetched,
         *  to skip redundant fetches within this window */
        private const val RECENT_FETCH_WINDOW_MS = 5000L
        
        /** Delay (ms) between starting a remote fetch and allowing the next
         *  operation for the same chapter. This gives time for the DB insert
         *  to complete before ChapterController tries to load the same chapter. */
        const val FETCH_TO_CONTROLLER_DELAY_MS = 300L
    }

    /** Per-chapter mutex: ensures only one fetch/insert per chapter at a time */
    private val chapterMutexes = mutableMapOf<String, Mutex>()
    private val mutexMapLock = Mutex()

    /** Global semaphore: limits total concurrent remote fetches */
    private val fetchSemaphore = Semaphore(MAX_CONCURRENT_FETCHES)

    /** Tracks recently fetched chapters (chapterKey -> timestamp) to skip redundant fetches */
    private val recentlyFetchedChapters = mutableMapOf<String, Long>()
    private val recentFetchLock = Mutex()

    /**
     * Execute a remote fetch + DB insert operation for a chapter, with serialization.
     *
     * If the same chapter is already being fetched by another caller, this will wait
     * for that fetch to complete rather than starting a duplicate.
     *
     * @param chapterKey Unique key identifying the chapter (e.g., "${bookId}_${chapterUrl}")
     * @param operation The suspend function to execute (fetch from remote + save to DB)
     * @return The result of the operation, or null if it was skipped (recently fetched)
     */
    suspend fun <T> executeChapterFetch(
        chapterKey: String,
        skipIfRecentlyFetched: Boolean = true,
        operation: suspend () -> T
    ): T? {
        // Check if this chapter was recently fetched
        if (skipIfRecentlyFetched && wasRecentlyFetched(chapterKey)) {
            Log.debug { "$TAG: Skipping fetch for $chapterKey - recently fetched" }
            return null
        }

        // Get or create per-chapter mutex
        val chapterMutex = mutexMapLock.withLock {
            chapterMutexes.getOrPut(chapterKey) { Mutex() }
        }

        // Acquire per-chapter mutex (serializes operations for same chapter)
        return chapterMutex.withLock {
            // Double-check: another caller may have fetched while we waited for the mutex
            if (skipIfRecentlyFetched && wasRecentlyFetched(chapterKey)) {
                Log.debug { "$TAG: Skipping fetch for $chapterKey - fetched while waiting" }
                return@withLock null
            }

            Log.debug { "$TAG: Starting operation for $chapterKey" }

            // Acquire global semaphore (limits total concurrent fetches)
            val result = fetchSemaphore.withPermit {
                operation()
            }

            // Mark as recently fetched
            markAsRecentlyFetched(chapterKey)

            Log.debug { "$TAG: Completed operation for $chapterKey" }
            result
        }
    }

    /**
     * Execute a DB-only operation for a chapter (no remote fetch), with per-chapter serialization.
     * This doesn't consume a slot from the global fetch semaphore.
     */
    suspend fun <T> executeChapterInsert(
        chapterKey: String,
        operation: suspend () -> T
    ): T {
        val chapterMutex = mutexMapLock.withLock {
            chapterMutexes.getOrPut(chapterKey) { Mutex() }
        }

        return chapterMutex.withLock {
            operation()
        }
    }

    /**
     * Check if a chapter was recently fetched (within the window).
     */
    private suspend fun wasRecentlyFetched(chapterKey: String): Boolean {
        return recentFetchLock.withLock {
            val fetchTime = recentlyFetchedChapters[chapterKey] ?: return@withLock false
            val elapsed = currentTimeMs() - fetchTime
            if (elapsed > RECENT_FETCH_WINDOW_MS) {
                recentlyFetchedChapters.remove(chapterKey)
                false
            } else {
                true
            }
        }
    }

    /**
     * Mark a chapter as recently fetched.
     */
    private suspend fun markAsRecentlyFetched(chapterKey: String) {
        recentFetchLock.withLock {
            recentlyFetchedChapters[chapterKey] = currentTimeMs()
            // Cleanup old entries
            val now = currentTimeMs()
            recentlyFetchedChapters.entries.removeAll { now - it.value > RECENT_FETCH_WINDOW_MS }
        }
    }

    /**
     * Clear the recent fetch cache. Call when switching books or resetting state.
     */
    suspend fun clearRecentFetchCache() {
        recentFetchLock.withLock {
            recentlyFetchedChapters.clear()
        }
    }

    /**
     * Check if a chapter operation is currently in progress.
     */
    suspend fun isOperationInProgress(chapterKey: String): Boolean {
        return mutexMapLock.withLock {
            chapterMutexes[chapterKey]?.isLocked == true
        }
    }

    /**
     * Create a standard chapter key from bookId and chapter key/url.
     */
    fun createChapterKey(bookId: Long, chapterKeyOrUrl: String): String {
        return "${bookId}_${chapterKeyOrUrl}"
    }

    @OptIn(ExperimentalTime::class)
    private fun currentTimeMs(): Long = Clock.System.now().toEpochMilliseconds()
}

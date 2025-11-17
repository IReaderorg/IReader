package ireader.domain.usecases.reader

import ireader.domain.models.reader.ReadingSession
import ireader.domain.models.reader.ReaderStatistics
import ireader.domain.preferences.prefs.ReaderPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Use case for tracking reading statistics
 * Requirements: 8.2
 */
class TrackReadingStatistics(
    private val readerPreferences: ReaderPreferences,
) {
    /**
     * Start a new reading session
     */
    suspend fun startSession(bookId: Long, chapterId: Long): ReadingSession {
        val startTime = System.currentTimeMillis()
        readerPreferences.currentSessionStartTime().set(startTime)
        
        return ReadingSession(
            bookId = bookId,
            chapterId = chapterId,
            startTime = startTime
        )
    }

    /**
     * End the current reading session and update statistics
     */
    suspend fun endSession(session: ReadingSession, pagesRead: Int) {
        val endTime = System.currentTimeMillis()
        val duration = endTime - session.startTime
        
        // Update total reading time
        val currentTotal = readerPreferences.totalReadingTimeMillis().get()
        readerPreferences.totalReadingTimeMillis().set(currentTotal + duration)
        
        // Update pages read
        val currentPages = readerPreferences.pagesRead().get()
        readerPreferences.pagesRead().set(currentPages + pagesRead)
        
        // Clear session start time
        readerPreferences.currentSessionStartTime().set(0L)
    }

    /**
     * Update chapter completion count
     */
    suspend fun incrementChaptersCompleted() {
        val current = readerPreferences.chaptersCompleted().get()
        readerPreferences.chaptersCompleted().set(current + 1)
    }

    /**
     * Track page read
     */
    suspend fun trackPageRead() {
        val current = readerPreferences.pagesRead().get()
        readerPreferences.pagesRead().set(current + 1)
    }

    /**
     * Get current statistics
     */
    suspend fun getStatistics(): ReaderStatistics {
        val totalTime = readerPreferences.totalReadingTimeMillis().get()
        val pagesRead = readerPreferences.pagesRead().get()
        val chaptersCompleted = readerPreferences.chaptersCompleted().get()
        val sessionStart = readerPreferences.currentSessionStartTime().get()
        
        val currentSessionDuration = if (sessionStart > 0) {
            System.currentTimeMillis() - sessionStart
        } else {
            0L
        }
        
        return ReaderStatistics(
            totalReadingTimeMillis = totalTime,
            pagesRead = pagesRead,
            chaptersCompleted = chaptersCompleted,
            currentSessionStartTime = sessionStart,
            currentSessionDurationMillis = currentSessionDuration,
            lastReadTimestamp = System.currentTimeMillis()
        )
    }

    /**
     * Observe statistics as Flow
     */
    fun observeStatistics(): Flow<ReaderStatistics> {
        return combine(
            readerPreferences.totalReadingTimeMillis().changes(),
            readerPreferences.pagesRead().changes(),
            readerPreferences.chaptersCompleted().changes(),
            readerPreferences.currentSessionStartTime().changes()
        ) { totalTime, pagesRead, chaptersCompleted, sessionStart ->
            val currentSessionDuration = if (sessionStart > 0) {
                System.currentTimeMillis() - sessionStart
            } else {
                0L
            }
            
            ReaderStatistics(
                totalReadingTimeMillis = totalTime,
                pagesRead = pagesRead,
                chaptersCompleted = chaptersCompleted,
                currentSessionStartTime = sessionStart,
                currentSessionDurationMillis = currentSessionDuration,
                lastReadTimestamp = System.currentTimeMillis()
            )
        }
    }

    /**
     * Reset all statistics
     */
    suspend fun resetStatistics() {
        readerPreferences.totalReadingTimeMillis().set(0L)
        readerPreferences.pagesRead().set(0L)
        readerPreferences.chaptersCompleted().set(0L)
        readerPreferences.currentSessionStartTime().set(0L)
    }
}

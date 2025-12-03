package ireader.domain.services

import ireader.domain.data.repository.HistoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

/**
 * Service class that manages reading progress tracking and history updates.
 * This class is responsible for updating the database when reading events occur.
 */
class ReadingProgressManager(
    private val historyRepository: HistoryRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    /**
     * Updates reading progress for a chapter
     *
     * @param chapterId The ID of the chapter being read
     * @param progress The reading progress as a value between 0.0 and 1.0
     * @param readDuration The time spent reading in milliseconds (optional)
     */
    @OptIn(ExperimentalTime::class)
    fun updateReadingProgress(chapterId: Long, progress: Float, readDuration: Long = 0) {
        coroutineScope.launch {
            val currentTimeMillis = kotlin.time.Clock.System.now().toEpochMilliseconds()
            
            // Save history with the current timestamp and progress
            historyRepository.upsert(
                chapterId = chapterId,
                readAt = currentTimeMillis,
                readDuration = readDuration,
                progress = progress
            )
        }
    }
    
    /**
     * Marks a chapter as completed (100% progress)
     *
     * @param chapterId The ID of the chapter to mark as completed
     * @param readDuration The time spent reading in milliseconds (optional)
     */
    fun markChapterCompleted(chapterId: Long, readDuration: Long = 0) {
        updateReadingProgress(chapterId, 1.0f, readDuration)
    }
    
    /**
     * Starts reading a chapter, initializing the progress if it doesn't exist yet.
     * This should be called when a chapter is first opened.
     *
     * @param chapterId The ID of the chapter being started
     */
    fun startReading(chapterId: Long) {
        coroutineScope.launch {
            val history = historyRepository.findHistoryByChapterId(chapterId)
            
            // If no history exists or progress is 0, initialize with a small progress value
            if (history == null || history.progress == 0.0f) {
                updateReadingProgress(chapterId, 0.01f) // Just started reading
            }
        }
    }
    
    /**
     * Updates the reading progress for a specific page in a chapter
     *
     * @param chapterId The ID of the chapter
     * @param currentPage The current page being read
     * @param totalPages Total number of pages in the chapter
     * @param readDuration Time spent reading so far
     */
    fun updatePageProgress(chapterId: Long, currentPage: Int, totalPages: Int, readDuration: Long = 0) {
        if (totalPages <= 0) return
        
        val progress = (currentPage.toFloat() / totalPages).coerceIn(0.0f, 1.0f)
        updateReadingProgress(chapterId, progress, readDuration)
    }
    
    /**
     * Resets reading progress for a book
     *
     * @param bookId The ID of the book to reset
     */
    fun resetBookProgress(bookId: Long) {
        coroutineScope.launch {
            historyRepository.resetHistoryByBookId(bookId)
        }
    }
    
    /**
     * Gets the latest reading progress for a chapter
     *
     * @param chapterId The ID of the chapter
     * @return The reading progress as a value between 0.0 and 1.0, or null if no progress exists
     */
    suspend fun getChapterProgress(chapterId: Long): Float? {
        val history = historyRepository.findHistoryByChapterId(chapterId)
        return history?.progress
    }
    
    /**
     * Gets the latest chapter that was read for a book
     *
     * @param bookId The ID of the book
     * @return The chapter ID of the last read chapter, or null if no chapters have been read
     */
    suspend fun getLastReadChapter(bookId: Long): Long? {
        val history = historyRepository.findHistoryByBookId(bookId)
        return history?.chapterId
    }
} 
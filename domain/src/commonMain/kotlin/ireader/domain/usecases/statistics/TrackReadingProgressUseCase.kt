package ireader.domain.usecases.statistics

import ireader.domain.data.repository.ReadingStatisticsRepository
import ireader.domain.utils.extensions.currentTimeToLong
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class TrackReadingProgressUseCase(
    private val statisticsRepository: ReadingStatisticsRepository
) {
    /**
     * Track reading progress when user reaches 80% of chapter
     */
    suspend fun onChapterProgressUpdate(progress: Float, chapterWordCount: Int) {
        if (progress >= 0.8f) {
            statisticsRepository.incrementChaptersRead()
            statisticsRepository.addWordsRead(chapterWordCount)
        }
    }

    /**
     * Track reading time in minutes.
     * Always saves reading time, rounding up partial minutes.
     */
    suspend fun trackReadingTime(durationMillis: Long) {
        // Convert to minutes, rounding up (ceiling)
        // This ensures even short reading sessions (e.g., 30 seconds) are counted as 1 minute
        val minutes = ((durationMillis + 59999) / 60000).coerceAtLeast(1)
        
        println("[TrackReadingProgress] Tracking reading time: ${durationMillis}ms = $minutes minutes")
        statisticsRepository.addReadingTime(minutes)
        println("[TrackReadingProgress] Successfully saved $minutes minutes")
    }

    /**
     * Update reading streak based on last read date.
     * Uses proper day boundary calculation to determine if user read on consecutive days.
     * Also automatically tracks the longest streak in the database.
     */
    suspend fun updateReadingStreak(currentDateMillis: Long) {
        val lastReadDate = statisticsRepository.getLastReadDate()
        val currentStreak = statisticsRepository.getCurrentStreak()
        
        // If this is the first time reading, set streak to 1
        if (lastReadDate == null || lastReadDate == 0L) {
            statisticsRepository.updateStreakWithLongest(1, currentDateMillis)
            return
        }
        
        val daysSinceLastRead = calculateDaysDifference(lastReadDate, currentDateMillis)
        
        val newStreak = when {
            daysSinceLastRead == 0L -> {
                // Same day - just update the last read date, keep streak
                // But ensure streak is at least 1
                maxOf(currentStreak, 1)
            }
            daysSinceLastRead == 1L -> {
                // Next day - increment streak
                currentStreak + 1
            }
            else -> {
                // Streak broken (more than 1 day gap) - reset to 1
                1
            }
        }
        
        // Use updateStreakWithLongest to automatically track longest streak
        statisticsRepository.updateStreakWithLongest(newStreak, currentDateMillis)
    }

    /**
     * Calculate the difference in days between two timestamps.
     * Uses UTC day boundaries for consistent calculation across timezones.
     */
    private fun calculateDaysDifference(date1Millis: Long, date2Millis: Long): Long {
        val millisInDay = 24 * 60 * 60 * 1000L
        
        // Normalize both dates to start of day (midnight UTC)
        val date1Day = date1Millis / millisInDay
        val date2Day = date2Millis / millisInDay
        
        // Calculate difference in days
        return kotlin.math.abs(date2Day - date1Day)
    }

    /**
     * Estimate word count from chapter content
     */
    fun estimateWordCount(content: String): Int {
        return content.split(Regex("\\s+")).filter { it.isNotBlank() }.size
    }

    /**
     * Track book completion when user finishes the last chapter
     */
    suspend fun trackBookCompletion() {
        statisticsRepository.incrementBooksCompleted()
    }
}

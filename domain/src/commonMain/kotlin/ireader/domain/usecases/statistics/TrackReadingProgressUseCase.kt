package ireader.domain.usecases.statistics

import ireader.domain.data.repository.ReadingStatisticsRepository
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
     * Track reading time in minutes
     */
    suspend fun trackReadingTime(durationMillis: Long) {
        val minutes = durationMillis.milliseconds.inWholeMinutes
        if (minutes > 0) {
            statisticsRepository.addReadingTime(minutes)
        }
    }

    /**
     * Update reading streak based on last read date
     * Checks if user read on consecutive days and updates streak accordingly
     */
    suspend fun updateReadingStreak(currentDateMillis: Long) {
        val lastReadDate = statisticsRepository.getLastReadDate()
        val currentStreak = statisticsRepository.getCurrentStreak()
        
        // If this is the first time reading, set streak to 1
        if (lastReadDate == null || lastReadDate == 0L) {
            statisticsRepository.updateStreak(1, currentDateMillis)
            return
        }
        
        val daysSinceLastRead = calculateDaysDifference(lastReadDate, currentDateMillis)
        
        val newStreak = when {
            daysSinceLastRead == 0L -> currentStreak // Same day, keep current streak
            daysSinceLastRead == 1L -> currentStreak + 1 // Next day, increment streak
            else -> 1 // Streak broken (more than 1 day gap), reset to 1
        }
        
        statisticsRepository.updateStreak(newStreak, currentDateMillis)
    }

    /**
     * Calculate the difference in days between two timestamps
     * Normalizes timestamps to start of day (midnight) for accurate day comparison
     */
    private fun calculateDaysDifference(date1Millis: Long, date2Millis: Long): Long {
        val millisInDay = 24 * 60 * 60 * 1000L
        
        // Normalize both dates to start of day (midnight)
        val date1StartOfDay = (date1Millis / millisInDay) * millisInDay
        val date2StartOfDay = (date2Millis / millisInDay) * millisInDay
        
        // Calculate difference in days
        return kotlin.math.abs(date2StartOfDay - date1StartOfDay) / millisInDay
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

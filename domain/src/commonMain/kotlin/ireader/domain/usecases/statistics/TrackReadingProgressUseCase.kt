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
     */
    suspend fun updateReadingStreak(currentDate: Long) {
        val stats = statisticsRepository.getStatistics()
        val lastReadDate = stats.readingStreak // This should be fetched from DB
        
        val daysSinceLastRead = calculateDaysDifference(lastReadDate.toLong(), currentDate)
        
        val newStreak = when {
            daysSinceLastRead == 0L -> stats.readingStreak // Same day
            daysSinceLastRead == 1L -> stats.readingStreak + 1 // Next day, increment
            else -> 1 // Streak broken, reset to 1
        }
        
        statisticsRepository.updateStreak(newStreak, currentDate)
    }

    private fun calculateDaysDifference(date1: Long, date2: Long): Long {
        val millisInDay = 24 * 60 * 60 * 1000
        return kotlin.math.abs(date2 - date1) / millisInDay
    }

    /**
     * Estimate word count from chapter content
     */
    fun estimateWordCount(content: String): Int {
        return content.split(Regex("\\s+")).filter { it.isNotBlank() }.size
    }
}

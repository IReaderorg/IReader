package ireader.domain.models.reader

/**
 * Reader statistics for tracking reading progress and analytics
 * Requirements: 8.2
 */
data class ReaderStatistics(
    val totalReadingTimeMillis: Long = 0L,
    val pagesRead: Long = 0L,
    val chaptersCompleted: Long = 0L,
    val averageReadingSpeedPagesPerHour: Float = 0f,
    val currentSessionStartTime: Long = 0L,
    val currentSessionDurationMillis: Long = 0L,
    val lastReadTimestamp: Long = 0L,
) {
    /**
     * Calculate average reading speed in pages per hour
     */
    fun calculateReadingSpeed(): Float {
        if (totalReadingTimeMillis == 0L) return 0f
        val hours = totalReadingTimeMillis / (1000f * 60f * 60f)
        return if (hours > 0) pagesRead / hours else 0f
    }

    /**
     * Get formatted reading time as "Xh Ym"
     */
    fun getFormattedReadingTime(): String {
        val hours = totalReadingTimeMillis / (1000 * 60 * 60)
        val minutes = (totalReadingTimeMillis % (1000 * 60 * 60)) / (1000 * 60)
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "< 1m"
        }
    }

    /**
     * Get formatted session time
     */
    fun getFormattedSessionTime(): String {
        val minutes = currentSessionDurationMillis / (1000 * 60)
        val seconds = (currentSessionDurationMillis % (1000 * 60)) / 1000
        return when {
            minutes > 0 -> "${minutes}m ${seconds}s"
            else -> "${seconds}s"
        }
    }
}

/**
 * Session statistics for current reading session
 */
data class ReadingSession(
    val bookId: Long,
    val chapterId: Long,
    val startTime: Long,
    val endTime: Long? = null,
    val pagesRead: Int = 0,
    val startPage: Int = 0,
    val endPage: Int = 0,
) {
    val durationMillis: Long
        get() = (endTime ?: System.currentTimeMillis()) - startTime

    val isActive: Boolean
        get() = endTime == null
}

/**
 * Page progress tracking
 */
data class PageProgress(
    val currentPage: Int,
    val totalPages: Int,
    val chapterId: Long,
    val bookId: Long,
    val timestamp: Long = System.currentTimeMillis(),
) {
    val progressPercentage: Float
        get() = if (totalPages > 0) (currentPage.toFloat() / totalPages) * 100f else 0f

    val isComplete: Boolean
        get() = currentPage >= totalPages
}

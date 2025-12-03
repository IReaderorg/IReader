package ireader.domain.models.library

import kotlinx.serialization.Serializable
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Comprehensive library statistics
 */
@Serializable
data class LibraryStatistics(
    val totalBooks: Int = 0,
    val favoriteBooks: Int = 0,
    val completedBooks: Int = 0,
    val readingBooks: Int = 0,
    val plannedBooks: Int = 0,
    val onHoldBooks: Int = 0,
    val droppedBooks: Int = 0,
    val totalChapters: Int = 0,
    val readChapters: Int = 0,
    val unreadChapters: Int = 0,
    val downloadedChapters: Int = 0,
    val totalReadingTime: Long = 0, // in milliseconds
    val averageReadingSpeed: Float = 0f, // chapters per hour
    val booksPerGenre: Map<String, Int> = emptyMap(),
    val booksPerSource: Map<Long, Int> = emptyMap(),
    val booksPerStatus: Map<Long, Int> = emptyMap(),
    val monthlyReadingStats: List<MonthlyReadingStats> = emptyList(),
    val lastUpdated: Long = currentTimeToLong()
) {
    val completionRate: Float
        get() = if (totalChapters > 0) (readChapters.toFloat() / totalChapters) * 100f else 0f
    
    val averageChaptersPerBook: Float
        get() = if (totalBooks > 0) totalChapters.toFloat() / totalBooks else 0f
}

/**
 * Monthly reading statistics
 */
@Serializable
data class MonthlyReadingStats(
    val year: Int,
    val month: Int,
    val chaptersRead: Int,
    val booksCompleted: Int,
    val readingTime: Long, // in milliseconds
    val newBooksAdded: Int
)

/**
 * Reading progress tracking
 */
@Serializable
data class ReadingProgress(
    val bookId: Long,
    val totalChapters: Int,
    val readChapters: Int,
    val currentChapter: Int,
    val lastReadTime: Long,
    val readingTime: Long, // total time spent reading this book
    val averageReadingSpeed: Float, // chapters per hour for this book
    val estimatedTimeToComplete: Long // estimated time to finish in milliseconds
) {
    val progressPercentage: Float
        get() = if (totalChapters > 0) (readChapters.toFloat() / totalChapters) * 100f else 0f
    
    val isCompleted: Boolean
        get() = readChapters >= totalChapters && totalChapters > 0
}

/**
 * Library analytics data
 */
@Serializable
data class LibraryAnalytics(
    val readingStreak: Int = 0, // days
    val longestReadingStreak: Int = 0,
    val booksCompletedThisMonth: Int = 0,
    val booksCompletedThisYear: Int = 0,
    val chaptersReadThisWeek: Int = 0,
    val chaptersReadThisMonth: Int = 0,
    val favoriteGenres: List<String> = emptyList(),
    val mostActiveReadingHour: Int = 0, // 0-23
    val averageSessionLength: Long = 0, // in milliseconds
    val totalSessions: Int = 0,
    val lastReadingSession: Long = 0
)
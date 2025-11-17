package ireader.domain.data.repository

import ireader.domain.models.library.LibraryAnalytics
import ireader.domain.models.library.LibraryStatistics
import ireader.domain.models.library.MonthlyReadingStats
import ireader.domain.models.library.ReadingProgress
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing library statistics and analytics
 */
interface LibraryStatisticsRepository {
    
    // Library statistics
    suspend fun getLibraryStatistics(): LibraryStatistics
    fun getLibraryStatisticsAsFlow(): Flow<LibraryStatistics>
    suspend fun updateStatistics(): Boolean
    
    // Reading progress
    suspend fun getReadingProgress(bookId: Long): ReadingProgress?
    fun getReadingProgressAsFlow(bookId: Long): Flow<ReadingProgress?>
    suspend fun updateReadingProgress(progress: ReadingProgress): Boolean
    suspend fun getAllReadingProgress(): List<ReadingProgress>
    
    // Analytics
    suspend fun getLibraryAnalytics(): LibraryAnalytics
    fun getLibraryAnalyticsAsFlow(): Flow<LibraryAnalytics>
    suspend fun updateAnalytics(): Boolean
    
    // Monthly statistics
    suspend fun getMonthlyStats(year: Int, month: Int): MonthlyReadingStats?
    suspend fun getYearlyStats(year: Int): List<MonthlyReadingStats>
    suspend fun getAllMonthlyStats(): List<MonthlyReadingStats>
    
    // Reading sessions
    suspend fun recordReadingSession(bookId: Long, chapterId: Long, duration: Long): Boolean
    suspend fun getReadingSessions(bookId: Long): List<ReadingSession>
    suspend fun getTotalReadingTime(): Long
    suspend fun getReadingTimeForPeriod(startTime: Long, endTime: Long): Long
    
    // Genre statistics
    suspend fun getGenreStatistics(): Map<String, GenreStats>
    suspend fun getSourceStatistics(): Map<Long, SourceStats>
    
    // Achievements and milestones
    suspend fun checkAchievements(): List<Achievement>
    suspend fun getUnlockedAchievements(): List<Achievement>
    
    // Export
    suspend fun exportStatistics(): StatisticsExport
}

/**
 * Reading session data
 */
data class ReadingSession(
    val id: Long = 0,
    val bookId: Long,
    val chapterId: Long,
    val startTime: Long,
    val endTime: Long,
    val duration: Long,
    val pagesRead: Int = 0
) {
    val sessionDate: Long
        get() = startTime
}

/**
 * Genre statistics
 */
data class GenreStats(
    val genre: String,
    val bookCount: Int,
    val chaptersRead: Int,
    val totalReadingTime: Long,
    val averageRating: Float,
    val completionRate: Float
)

/**
 * Source statistics
 */
data class SourceStats(
    val sourceId: Long,
    val sourceName: String,
    val bookCount: Int,
    val chaptersRead: Int,
    val totalReadingTime: Long,
    val lastUsed: Long,
    val averageUpdateFrequency: Long
)

/**
 * Achievement system
 */
data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long = 0,
    val progress: Float = 0f,
    val maxProgress: Float = 100f,
    val category: AchievementCategory = AchievementCategory.READING
)

enum class AchievementCategory {
    READING,
    LIBRARY,
    STREAK,
    COMPLETION,
    EXPLORATION
}

/**
 * Statistics export data
 */
data class StatisticsExport(
    val libraryStats: LibraryStatistics,
    val analytics: LibraryAnalytics,
    val monthlyStats: List<MonthlyReadingStats>,
    val readingSessions: List<ReadingSession>,
    val achievements: List<Achievement>,
    val exportTimestamp: Long = System.currentTimeMillis()
)
package ireader.domain.use_cases.library

import ireader.domain.data.repository.LibraryStatisticsRepository
import ireader.domain.models.library.LibraryAnalytics
import ireader.domain.models.library.LibraryStatistics
import ireader.domain.models.library.MonthlyReadingStats
import ireader.domain.models.library.ReadingProgress
import kotlinx.coroutines.flow.Flow

/**
 * Use case for managing library statistics and analytics
 */
class LibraryStatisticsUseCase(
    private val statisticsRepository: LibraryStatisticsRepository
) {
    
    /**
     * Get comprehensive library statistics
     */
    suspend fun getLibraryStatistics(): LibraryStatistics {
        return statisticsRepository.getLibraryStatistics()
    }
    
    /**
     * Get library statistics as Flow for reactive updates
     */
    fun getLibraryStatisticsAsFlow(): Flow<LibraryStatistics> {
        return statisticsRepository.getLibraryStatisticsAsFlow()
    }
    
    /**
     * Update library statistics
     */
    suspend fun updateStatistics(): Boolean {
        return statisticsRepository.updateStatistics()
    }
    
    /**
     * Get reading progress for a specific book
     */
    suspend fun getReadingProgress(bookId: Long): ReadingProgress? {
        return statisticsRepository.getReadingProgress(bookId)
    }
    
    /**
     * Get reading progress as Flow
     */
    fun getReadingProgressAsFlow(bookId: Long): Flow<ReadingProgress?> {
        return statisticsRepository.getReadingProgressAsFlow(bookId)
    }
    
    /**
     * Update reading progress for a book
     */
    suspend fun updateReadingProgress(progress: ReadingProgress): Boolean {
        return statisticsRepository.updateReadingProgress(progress)
    }
    
    /**
     * Get all reading progress data
     */
    suspend fun getAllReadingProgress(): List<ReadingProgress> {
        return statisticsRepository.getAllReadingProgress()
    }
    
    /**
     * Get library analytics
     */
    suspend fun getLibraryAnalytics(): LibraryAnalytics {
        return statisticsRepository.getLibraryAnalytics()
    }
    
    /**
     * Get library analytics as Flow
     */
    fun getLibraryAnalyticsAsFlow(): Flow<LibraryAnalytics> {
        return statisticsRepository.getLibraryAnalyticsAsFlow()
    }
    
    /**
     * Update analytics data
     */
    suspend fun updateAnalytics(): Boolean {
        return statisticsRepository.updateAnalytics()
    }
    
    /**
     * Get monthly reading statistics
     */
    suspend fun getMonthlyStats(year: Int, month: Int): MonthlyReadingStats? {
        return statisticsRepository.getMonthlyStats(year, month)
    }
    
    /**
     * Get yearly reading statistics
     */
    suspend fun getYearlyStats(year: Int): List<MonthlyReadingStats> {
        return statisticsRepository.getYearlyStats(year)
    }
    
    /**
     * Get all monthly statistics
     */
    suspend fun getAllMonthlyStats(): List<MonthlyReadingStats> {
        return statisticsRepository.getAllMonthlyStats()
    }
    
    /**
     * Record a reading session
     */
    suspend fun recordReadingSession(bookId: Long, chapterId: Long, duration: Long): Boolean {
        return statisticsRepository.recordReadingSession(bookId, chapterId, duration)
    }
    
    /**
     * Get reading sessions for a book
     */
    suspend fun getReadingSessions(bookId: Long): List<ireader.domain.data.repository.ReadingSession> {
        return statisticsRepository.getReadingSessions(bookId)
    }
    
    /**
     * Get total reading time
     */
    suspend fun getTotalReadingTime(): Long {
        return statisticsRepository.getTotalReadingTime()
    }
    
    /**
     * Get reading time for a specific period
     */
    suspend fun getReadingTimeForPeriod(startTime: Long, endTime: Long): Long {
        return statisticsRepository.getReadingTimeForPeriod(startTime, endTime)
    }
    
    /**
     * Get genre statistics
     */
    suspend fun getGenreStatistics(): Map<String, ireader.domain.data.repository.GenreStats> {
        return statisticsRepository.getGenreStatistics()
    }
    
    /**
     * Get source statistics
     */
    suspend fun getSourceStatistics(): Map<Long, ireader.domain.data.repository.SourceStats> {
        return statisticsRepository.getSourceStatistics()
    }
    
    /**
     * Check for new achievements
     */
    suspend fun checkAchievements(): List<ireader.domain.data.repository.Achievement> {
        return statisticsRepository.checkAchievements()
    }
    
    /**
     * Get unlocked achievements
     */
    suspend fun getUnlockedAchievements(): List<ireader.domain.data.repository.Achievement> {
        return statisticsRepository.getUnlockedAchievements()
    }
    
    /**
     * Export statistics data
     */
    suspend fun exportStatistics(): ireader.domain.data.repository.StatisticsExport {
        return statisticsRepository.exportStatistics()
    }
}
package ireader.plugin.api

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

/**
 * API for plugins to access reading analytics.
 * 
 * This API allows plugins to:
 * - Access reading statistics
 * - Track reading sessions
 * - Monitor goals and achievements
 * - Get reading patterns
 */
interface ReadingAnalyticsApi {
    
    // Current session
    
    /**
     * Get the current reading session if active.
     */
    suspend fun getCurrentSession(): ReadingSessionInfo?
    
    /**
     * Get the current session duration in milliseconds.
     */
    fun getCurrentSessionDuration(): Long
    
    // Statistics
    
    /**
     * Get today's reading statistics.
     */
    suspend fun getTodayStats(): DailyStatsInfo
    
    /**
     * Get reading statistics for a specific date.
     */
    suspend fun getStatsForDate(date: String): DailyStatsInfo?
    
    /**
     * Get this week's reading statistics.
     */
    suspend fun getWeekStats(): List<DailyStatsInfo>
    
    /**
     * Get overall reading statistics.
     */
    suspend fun getOverallStats(): OverallStatsInfo
    
    /**
     * Get reading statistics for a specific book.
     */
    suspend fun getBookStats(bookId: Long): BookStatsInfo?
    
    // Speed
    
    /**
     * Get current reading speed (words per minute).
     */
    fun getCurrentReadingSpeed(): Float
    
    /**
     * Get average reading speed.
     */
    suspend fun getAverageReadingSpeed(): Float
    
    // Streaks
    
    /**
     * Get current reading streak in days.
     */
    suspend fun getCurrentStreak(): Int
    
    /**
     * Get longest reading streak in days.
     */
    suspend fun getLongestStreak(): Int
    
    // Goals
    
    /**
     * Get active reading goals.
     */
    suspend fun getActiveGoals(): List<GoalInfo>
    
    /**
     * Get progress for a specific goal.
     */
    suspend fun getGoalProgress(goalId: String): GoalProgressInfo?
    
    // Achievements
    
    /**
     * Get unlocked achievements.
     */
    suspend fun getUnlockedAchievements(): List<AchievementInfo>
    
    /**
     * Get total achievement points.
     */
    suspend fun getTotalAchievementPoints(): Int
    
    // Events
    
    /**
     * Subscribe to reading analytics events.
     */
    fun subscribeToEvents(): Flow<ReadingAnalyticsEventInfo>
}

/**
 * Reading session information for plugins.
 */
@Serializable
data class ReadingSessionInfo(
    val sessionId: String,
    val bookId: Long,
    val bookTitle: String,
    val startTime: Long,
    val durationMs: Long,
    val wordsRead: Int,
    val pagesRead: Int,
    val currentChapterId: Long,
    val wordsPerMinute: Float
)

/**
 * Daily statistics for plugins.
 */
@Serializable
data class DailyStatsInfo(
    val date: String,
    val totalReadingTimeMs: Long,
    val sessionsCount: Int,
    val wordsRead: Int,
    val pagesRead: Int,
    val averageWpm: Float,
    val goalProgress: Float?
)

/**
 * Overall statistics for plugins.
 */
@Serializable
data class OverallStatsInfo(
    val totalReadingTimeMs: Long,
    val totalSessions: Int,
    val totalBooksCompleted: Int,
    val totalWordsRead: Long,
    val averageWpm: Float,
    val currentStreak: Int,
    val longestStreak: Int
)

/**
 * Book statistics for plugins.
 */
@Serializable
data class BookStatsInfo(
    val bookId: Long,
    val bookTitle: String,
    val totalReadingTimeMs: Long,
    val sessionsCount: Int,
    val progressPercent: Float,
    val averageWpm: Float,
    val estimatedTimeToFinishMs: Long?
)

/**
 * Goal information for plugins.
 */
@Serializable
data class GoalInfo(
    val id: String,
    val type: String,
    val target: Int,
    val currentProgress: Int,
    val progressPercent: Float,
    val isCompleted: Boolean
)

/**
 * Goal progress information.
 */
@Serializable
data class GoalProgressInfo(
    val goalId: String,
    val currentProgress: Int,
    val target: Int,
    val progressPercent: Float,
    val remainingToTarget: Int,
    val estimatedCompletionDate: Long?
)

/**
 * Achievement information for plugins.
 */
@Serializable
data class AchievementInfo(
    val id: String,
    val name: String,
    val description: String,
    val iconUrl: String?,
    val tier: String,
    val points: Int,
    val unlockedDate: Long?
)

/**
 * Reading analytics event for plugins.
 */
@Serializable
sealed class ReadingAnalyticsEventInfo {
    @Serializable
    data class SessionStarted(val sessionId: String, val bookId: Long) : ReadingAnalyticsEventInfo()
    
    @Serializable
    data class SessionEnded(val sessionId: String, val durationMs: Long, val wordsRead: Int) : ReadingAnalyticsEventInfo()
    
    @Serializable
    data class GoalProgress(val goalId: String, val progress: Int, val target: Int) : ReadingAnalyticsEventInfo()
    
    @Serializable
    data class GoalCompleted(val goalId: String, val goalType: String) : ReadingAnalyticsEventInfo()
    
    @Serializable
    data class AchievementUnlocked(val achievementId: String, val name: String, val points: Int) : ReadingAnalyticsEventInfo()
    
    @Serializable
    data class StreakUpdated(val currentStreak: Int, val isNewRecord: Boolean) : ReadingAnalyticsEventInfo()
    
    @Serializable
    data class MilestoneReached(val type: String, val value: Long) : ReadingAnalyticsEventInfo()
}

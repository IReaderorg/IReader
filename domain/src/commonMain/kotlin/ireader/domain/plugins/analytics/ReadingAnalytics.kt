package ireader.domain.plugins.analytics

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Reading Analytics Plugin API
 * 
 * Exposes reading statistics to plugins for features like:
 * - Reading speed tracking
 * - Time spent reading
 * - Progress tracking
 * - Reading patterns analysis
 * - Goal tracking
 * - Achievements
 */

/**
 * Reading session data.
 */
@Serializable
data class ReadingSession(
    val id: String,
    val bookId: Long,
    val bookTitle: String,
    val startTime: Long,
    val endTime: Long?,
    val startChapterId: Long,
    val endChapterId: Long?,
    val startPosition: Int,
    val endPosition: Int?,
    val pagesRead: Int = 0,
    val wordsRead: Int = 0,
    val charactersRead: Int = 0,
    val pauseDurationMs: Long = 0,
    val deviceType: String,
    val isCompleted: Boolean = false
) {
    val durationMs: Long
        get() = (endTime ?: currentTimeToLong()) - startTime - pauseDurationMs
    
    val activeReadingTimeMs: Long
        get() = durationMs - pauseDurationMs
    
    val wordsPerMinute: Float
        get() = if (activeReadingTimeMs > 0) {
            (wordsRead.toFloat() / activeReadingTimeMs) * 60_000
        } else 0f
}

/**
 * Daily reading statistics.
 */
@Serializable
data class DailyReadingStats(
    val date: String, // YYYY-MM-DD format
    val totalReadingTimeMs: Long,
    val sessionsCount: Int,
    val booksRead: Int,
    val chaptersRead: Int,
    val pagesRead: Int,
    val wordsRead: Int,
    val averageWordsPerMinute: Float,
    val longestSessionMs: Long,
    val peakReadingHour: Int,
    val goalProgress: Float?
)

/**
 * Book reading statistics.
 */
@Serializable
data class BookReadingStats(
    val bookId: Long,
    val bookTitle: String,
    val totalReadingTimeMs: Long,
    val sessionsCount: Int,
    val chaptersRead: Int,
    val totalChapters: Int,
    val progressPercent: Float,
    val averageSessionLengthMs: Long,
    val averageWordsPerMinute: Float,
    val firstReadDate: Long,
    val lastReadDate: Long,
    val estimatedTimeToFinishMs: Long?,
    val readingStreak: Int
)

/**
 * Overall reading statistics.
 */
@Serializable
data class OverallReadingStats(
    val totalReadingTimeMs: Long,
    val totalSessions: Int,
    val totalBooksStarted: Int,
    val totalBooksCompleted: Int,
    val totalChaptersRead: Int,
    val totalPagesRead: Int,
    val totalWordsRead: Long,
    val averageWordsPerMinute: Float,
    val averageSessionLengthMs: Long,
    val longestStreak: Int,
    val currentStreak: Int,
    val favoriteReadingHour: Int,
    val favoriteReadingDay: String,
    val mostReadGenre: String?,
    val fastestBook: BookSpeedRecord?,
    val longestSession: SessionRecord?
)

@Serializable
data class BookSpeedRecord(
    val bookId: Long,
    val bookTitle: String,
    val wordsPerMinute: Float
)

@Serializable
data class SessionRecord(
    val sessionId: String,
    val bookTitle: String,
    val durationMs: Long,
    val date: Long
)

/**
 * Reading pattern analysis.
 */
@Serializable
data class ReadingPattern(
    val hourlyDistribution: Map<Int, Long>, // Hour (0-23) -> Reading time in ms
    val dailyDistribution: Map<String, Long>, // Day name -> Reading time in ms
    val weeklyTrend: List<WeeklyData>,
    val monthlyTrend: List<MonthlyData>,
    val preferredSessionLength: SessionLengthPreference,
    val readingConsistency: Float, // 0-1, how consistent reading habits are
    val peakProductivityHours: List<Int>
)

@Serializable
data class WeeklyData(
    val weekStart: String, // YYYY-MM-DD
    val totalTimeMs: Long,
    val sessionsCount: Int,
    val booksRead: Int
)

@Serializable
data class MonthlyData(
    val month: String, // YYYY-MM
    val totalTimeMs: Long,
    val sessionsCount: Int,
    val booksCompleted: Int,
    val averageDaily: Long
)

@Serializable
enum class SessionLengthPreference {
    SHORT,      // < 15 min
    MEDIUM,     // 15-45 min
    LONG,       // 45-90 min
    MARATHON    // > 90 min
}

/**
 * Reading goal.
 */
@Serializable
data class ReadingGoal(
    val id: String,
    val type: GoalType,
    val target: Int,
    val period: GoalPeriod,
    val startDate: Long,
    val endDate: Long?,
    val currentProgress: Int,
    val isActive: Boolean,
    val isCompleted: Boolean,
    val completedDate: Long?,
    val streakDays: Int
)

@Serializable
enum class GoalType {
    BOOKS,
    CHAPTERS,
    PAGES,
    WORDS,
    TIME_MINUTES,
    SESSIONS
}

@Serializable
enum class GoalPeriod {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY,
    CUSTOM
}

/**
 * Reading achievement.
 */
@Serializable
data class ReadingAchievement(
    val id: String,
    val name: String,
    val description: String,
    val iconUrl: String?,
    val category: AchievementCategory,
    val tier: AchievementTier,
    val requirement: AchievementRequirement,
    val progress: Int,
    val isUnlocked: Boolean,
    val unlockedDate: Long?,
    val points: Int
)

@Serializable
enum class AchievementCategory {
    READING_TIME,
    BOOKS_COMPLETED,
    STREAK,
    SPEED,
    EXPLORATION,
    SOCIAL,
    SPECIAL
}

@Serializable
enum class AchievementTier {
    BRONZE,
    SILVER,
    GOLD,
    PLATINUM,
    DIAMOND
}

@Serializable
data class AchievementRequirement(
    val type: String,
    val value: Int,
    val description: String
)

/**
 * Reading milestone.
 */
@Serializable
data class ReadingMilestone(
    val id: String,
    val type: MilestoneType,
    val value: Long,
    val reachedDate: Long,
    val bookId: Long?,
    val bookTitle: String?
)

@Serializable
enum class MilestoneType {
    FIRST_BOOK_COMPLETED,
    BOOKS_10,
    BOOKS_50,
    BOOKS_100,
    HOURS_10,
    HOURS_100,
    HOURS_1000,
    WORDS_100K,
    WORDS_1M,
    STREAK_7,
    STREAK_30,
    STREAK_100,
    STREAK_365
}

/**
 * Reading speed analysis.
 */
@Serializable
data class ReadingSpeedAnalysis(
    val overallWpm: Float,
    val byGenre: Map<String, Float>,
    val byTimeOfDay: Map<Int, Float>,
    val trend: SpeedTrend,
    val percentile: Int, // Compared to other users
    val improvementPercent: Float // Compared to first month
)

@Serializable
enum class SpeedTrend {
    IMPROVING,
    STABLE,
    DECLINING
}

/**
 * Reading heatmap data.
 */
@Serializable
data class ReadingHeatmap(
    val year: Int,
    val data: Map<String, HeatmapDay> // YYYY-MM-DD -> data
)

@Serializable
data class HeatmapDay(
    val date: String,
    val readingTimeMs: Long,
    val intensity: Int, // 0-4 for visualization
    val booksRead: List<String>
)

/**
 * Comparison with previous period.
 */
@Serializable
data class PeriodComparison(
    val currentPeriod: PeriodStats,
    val previousPeriod: PeriodStats,
    val readingTimeChange: Float,
    val sessionsChange: Float,
    val speedChange: Float,
    val booksChange: Int
)

@Serializable
data class PeriodStats(
    val startDate: Long,
    val endDate: Long,
    val totalTimeMs: Long,
    val sessions: Int,
    val averageWpm: Float,
    val booksCompleted: Int
)

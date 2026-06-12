package ireader.domain.models.gamification

/**
 * Reading challenge models for daily/weekly/monthly goals.
 */

data class ReadingChallenge(
    val id: String,
    val type: ChallengeType,
    val goalMinutes: Long,
    val currentMinutes: Long,
    val rewardStones: Int,
    val isCompleted: Boolean,
    val completedAt: Long? = null,
    val startDate: Long,
    val endDate: Long,
) {
    val progress: Float
        get() = if (goalMinutes > 0) (currentMinutes.toFloat() / goalMinutes.toFloat()).coerceIn(0f, 1f) else 0f
}

enum class ChallengeType(val label: String, val emoji: String) {
    DAILY("Daily Goal", "📅"),
    WEEKLY("Weekly Goal", "📆"),
    MONTHLY("Monthly Goal", "🗓️")
}

data class ReadingChallengeState(
    val dailyChallenge: ReadingChallenge? = null,
    val weeklyChallenge: ReadingChallenge? = null,
    val monthlyChallenge: ReadingChallenge? = null,
    val totalStonesEarnedFromChallenges: Int = 0,
    val challengesCompletedToday: Int = 0,
)

/** Milestone definitions for celebration triggers. */
data class Milestone(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val rewardStones: Int,
    val threshold: Long,
    val metric: MilestoneMetric,
)

enum class MilestoneMetric {
    BOOKS_READ,
    CHAPTERS_READ,
    READING_MINUTES,
    STREAK_DAYS
}

object Milestones {
    val ALL = listOf(
        // Books milestones
        Milestone("books_10", "Bookworm Initiate", "Read 10 books", "📚", 50, 10, MilestoneMetric.BOOKS_READ),
        Milestone("books_50", "Literary Explorer", "Read 50 books", "📖", 200, 50, MilestoneMetric.BOOKS_READ),
        Milestone("books_100", "Century Reader", "Read 100 books", "🏆", 500, 100, MilestoneMetric.BOOKS_READ),
        Milestone("books_500", "Reading Master", "Read 500 books", "👑", 1000, 500, MilestoneMetric.BOOKS_READ),

        // Chapters milestones
        Milestone("chapters_100", "Chapter Hunter", "Read 100 chapters", "📖", 50, 100, MilestoneMetric.CHAPTERS_READ),
        Milestone("chapters_1000", "Chapter Devourer", "Read 1,000 chapters", "🔥", 200, 1000, MilestoneMetric.CHAPTERS_READ),
        Milestone("chapters_5000", "Chapter Legend", "Read 5,000 chapters", "⚡", 500, 5000, MilestoneMetric.CHAPTERS_READ),
        Milestone("chapters_10000", "Chapter Deity", "Read 10,000 chapters", "💎", 1000, 10000, MilestoneMetric.CHAPTERS_READ),

        // Streak milestones
        Milestone("streak_7", "Week Warrior", "7-day reading streak", "🔥", 20, 7, MilestoneMetric.STREAK_DAYS),
        Milestone("streak_30", "Monthly Master", "30-day reading streak", "🌟", 100, 30, MilestoneMetric.STREAK_DAYS),
        Milestone("streak_100", "Century Streak", "100-day reading streak", "💫", 300, 100, MilestoneMetric.STREAK_DAYS),
        Milestone("streak_365", "Year Legend", "365-day reading streak", "👑", 1000, 365, MilestoneMetric.STREAK_DAYS),

        // Reading time milestones
        Milestone("time_1000", "Dedicated Reader", "Read for 1,000 minutes", "⏱️", 50, 1000, MilestoneMetric.READING_MINUTES),
        Milestone("time_5000", "Time Lord", "Read for 5,000 minutes", "⏰", 200, 5000, MilestoneMetric.READING_MINUTES),
        Milestone("time_10000", "Eternal Reader", "Read for 10,000 minutes", "🌌", 500, 10000, MilestoneMetric.READING_MINUTES),
        Milestone("time_50000", "Reading Immortal", "Read for 50,000 minutes", "🏆", 2000, 50000, MilestoneMetric.READING_MINUTES),
    )
}

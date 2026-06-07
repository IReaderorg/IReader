package ireader.domain.models.gamification

/**
 * Gamification + community domain models (No-Money edition).
 * All currency here is earned-only and cosmetic; nothing is purchasable with money.
 */

/** The user's economy/level snapshot, sourced from public.users. */
data class GamificationProfile(
    val userId: String,
    val level: Int = 1,
    val xp: Long = 0,
    val levelTitle: String = "Novice Reader",
    val spiritStones: Long = 0,
    val checkinStreak: Int = 0,
    val activeTitleId: String? = null,
    val discordLinked: Boolean = false,
    val discordUsername: String? = null,
    val avatarUrl: String? = null,
    val coverUrl: String? = null,
    val bio: String = "",
    val displayName: String? = null,
    val joinedAt: String? = null,
) {
    /** Cumulative XP required to reach a given level: 50 * L * (L-1). */
    fun xpForLevel(level: Int): Long = (50L * level * (level - 1))
    val xpIntoLevel: Long get() = (xp - xpForLevel(level)).coerceAtLeast(0)
    val xpSpanThisLevel: Long get() = (xpForLevel(level + 1) - xpForLevel(level)).coerceAtLeast(1)
    val levelProgress: Float get() = (xpIntoLevel.toFloat() / xpSpanThisLevel.toFloat()).coerceIn(0f, 1f)
}

/** A catalog achievement definition (public.achievement_definitions). */
data class AchievementDef(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val imageUrl: String?,
    val category: String,
    val tier: String,
    val metric: String,
    val threshold: Long,
    val rewardXp: Int,
    val rewardStones: Int,
    val isSecret: Boolean = false,
)

/** Per-user progress merged with its definition, for the showcase UI. */
data class AchievementView(
    val def: AchievementDef,
    val progress: Long,
    val isCompleted: Boolean,
    val earnedAt: Long?,
) {
    val fraction: Float
        get() = if (def.threshold <= 0) 1f else (progress.toFloat() / def.threshold.toFloat()).coerceIn(0f, 1f)
}

/** Returned by sync/checkin RPCs when an achievement is freshly earned. */
data class UnlockedAchievement(
    val achievementId: String,
    val name: String,
    val icon: String,
    val imageUrl: String?,
    val tier: String,
    val rewardXp: Int,
    val rewardStones: Int,
)

data class OwnedTitle(
    val titleId: String,
    val titleName: String,
    val rarity: String,
    val isActive: Boolean,
    val acquiredAt: Long,
)

data class SpiritStoneTxn(
    val amount: Long,
    val type: String,
    val description: String,
    val createdAt: Long,
)

data class CheckinResult(
    val already: Boolean,
    val streakDay: Int,
    val reward: Int,
)

/** Stats the client feeds to sync_reading_stats (local-first source of truth pre-sign-in). */
data class ReadingStatsSnapshot(
    val minutes: Long,
    val chapters: Long,
    val books: Long,
    val streak: Long,
    val longestStreak: Long,
    val avgWpm: Long,
    val genresExplored: Long,
)

// ---- Social ----

data class FollowUser(
    val userId: String,
    val username: String,
    val avatarUrl: String?,
    val level: Int = 1,
)

data class ProfileComment(
    val id: String,
    val profileUserId: String,
    val commenterId: String,
    val commenterName: String,
    val commenterAvatar: String?,
    val text: String,
    val likes: Int,
    val createdAt: Long,
)

data class ReadingActivityItem(
    val id: String,
    val type: String,            // READING, REVIEW, VOTE, ACHIEVEMENT
    val bookId: String?,
    val bookTitle: String?,
    val chapterNumber: Int?,
    val description: String,
    val createdAt: Long,
)

data class CommunityAnnouncement(
    val id: String,
    val title: String?,
    val body: String?,
    val discordUrl: String?,
    val postedAt: Long,
)

// ---- Leaderboard filters (Webnovel "Hall of Readers") ----

enum class LeaderboardMetric(val column: String, val label: String, val unit: String) {
    READING_MINUTES("total_reading_time_minutes", "Reading Time", "min"),
    CHAPTERS("total_chapters_read", "Chapters", "ch"),
    STREAK("reading_streak", "Streak", "days"),
    BOOKS("books_completed", "Books", "books"),
}

enum class LeaderboardPeriod(val label: String) { WEEKLY("Weekly"), MONTHLY("Monthly"), ALL_TIME("All-Time") }

/** Rank-ladder tier derived from percentile, for the tier band on the leaderboard. */
enum class ReaderTier(val display: String, val emblem: String) {
    BRONZE("Bronze", "🥉"),
    SILVER("Silver", "🥈"),
    GOLD("Gold", "🥇"),
    PLATINUM("Platinum", "💠"),
    DIAMOND("Diamond", "💎"),
    LEGEND("Legend", "👑");

    companion object {
        fun fromPercentile(p: Float): ReaderTier = when {
            p <= 0.01f -> LEGEND
            p <= 0.05f -> DIAMOND
            p <= 0.15f -> PLATINUM
            p <= 0.35f -> GOLD
            p <= 0.65f -> SILVER
            else -> BRONZE
        }
    }
}

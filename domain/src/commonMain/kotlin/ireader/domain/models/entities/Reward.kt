package ireader.domain.models.entities

/**
 * Represents a reward earned by the user.
 */
data class Reward(
    val id: String,
    val name: String,
    val description: String,
    val type: RewardType,
    val icon: String,
    val earnedAt: Long,
    val xpValue: Int
)

enum class RewardType {
    BADGE,
    ACHIEVEMENT,
    LEVEL_UP,
    STREAK,
    MILESTONE
}

/**
 * Represents an XP event from user activity.
 */
data class XpEvent(
    val source: XpSource,
    val amount: Int,
    val timestamp: Long
)

enum class XpSource {
    READING_TIME,      // 1 XP per minute
    CHAPTER_READ,      // 5 XP per chapter
    BOOK_COMPLETED,    // 50 XP per book
    STREAK_MILESTONE,  // 10 XP per streak day
    DAILY_LOGIN        // 5 XP per day
}

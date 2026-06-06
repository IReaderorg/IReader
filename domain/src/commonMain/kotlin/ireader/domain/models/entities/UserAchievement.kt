package ireader.domain.models.entities

/**
 * Represents an achievement earned by a user.
 */
data class UserAchievement(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val earnedAt: Long,
    val category: AchievementCategory
)

enum class AchievementCategory {
    READING_TIME,
    CHAPTERS,
    BOOKS,
    STREAK,
    SPECIAL
}

package ireader.domain.models.entities

import kotlinx.serialization.Serializable

@Serializable
data class LeaderboardEntry(
    val id: String? = null,
    val userId: String,
    val username: String,
    val totalReadingTimeMinutes: Long,
    val rank: Int = 0,
    val avatarUrl: String? = null,
    val hasBadge: Boolean = false,
    val badgeType: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class UserLeaderboardStats(
    val userId: String,
    val username: String,
    val totalReadingTimeMinutes: Long,
    val totalChaptersRead: Int = 0,
    val booksCompleted: Int = 0,
    val readingStreak: Int = 0,
    val hasBadge: Boolean = false,
    val badgeType: String? = null,
    val lastSyncedAt: Long = 0
)

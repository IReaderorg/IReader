package ireader.domain.models.entities

import kotlinx.serialization.Serializable
import ireader.domain.utils.extensions.currentTimeToLong

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
    val updatedAt: Long = currentTimeToLong(),
    val level: Int = 1,
    val levelTitle: String = "Novice Reader",
    val xp: Long = 0,
    val xpToNextLevel: Long = 60,
    val totalChaptersRead: Int = 0,
    val booksCompleted: Int = 0,
    val readingStreak: Int = 0,
    val syncedBooks: List<SyncedBookSummary> = emptyList(),
)

@Serializable
data class SyncedBookSummary(
    val title: String,
    val coverUrl: String,
    val sourceName: String,
    val sourceId: Long,
    val bookUrl: String,
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
    val lastSyncedAt: Long = 0,
    val level: Int = 1,
    val levelTitle: String = "Novice Reader",
    val xp: Long = 0,
    val xpToNextLevel: Long = 60
)

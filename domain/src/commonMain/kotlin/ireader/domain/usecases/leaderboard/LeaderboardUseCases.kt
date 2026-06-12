package ireader.domain.usecases.leaderboard

import ireader.domain.data.repository.LeaderboardRepository
import ireader.domain.data.repository.ReadingStatisticsRepository
import ireader.domain.data.repository.RemoteRepository
import ireader.domain.models.entities.LeaderboardEntry
import ireader.domain.models.entities.ReaderLevel
import ireader.domain.models.entities.UserLeaderboardStats
import ireader.domain.preferences.prefs.UiPreferences
import kotlinx.coroutines.flow.Flow
import ireader.domain.utils.extensions.currentTimeToLong

class LeaderboardUseCases(
    private val leaderboardRepository: LeaderboardRepository,
    private val statisticsRepository: ReadingStatisticsRepository,
    private val remoteRepository: RemoteRepository,
    private val uiPreferences: UiPreferences
) {
    
    suspend fun getLeaderboard(limit: Int = 100): Result<List<LeaderboardEntry>> {
        return leaderboardRepository.getLeaderboard(limit = limit)
    }
    
    suspend fun getUserRank(): Result<LeaderboardEntry?> {
        val user = remoteRepository.getCurrentUser().getOrNull() ?: return Result.success(null)
        return leaderboardRepository.getUserRank(user.id)
    }
    
    suspend fun syncCurrentUserStats(): Result<Unit> {
        val user = remoteRepository.getCurrentUser().getOrNull()
            ?: return Result.failure(Exception("User not logged in"))

        val stats = statisticsRepository.getStatistics()

        // Fetch remote leaderboard entry to merge (prevents data loss across devices)
        val remoteEntry = leaderboardRepository.getUserLeaderboardEntry(user.id).getOrNull()

        // Merge: take maximum values so reading time is never lost
        val totalMinutes = if (remoteEntry != null) {
            maxOf(stats.totalReadingTimeMinutes, remoteEntry.totalReadingTimeMinutes)
        } else {
            stats.totalReadingTimeMinutes
        }
        val readerLevel = ReaderLevel.fromMinutes(totalMinutes)

        val mergedStats = if (remoteEntry != null) {
            UserLeaderboardStats(
                userId = user.id,
                username = user.username ?: user.email.substringBefore("@"),
                totalReadingTimeMinutes = totalMinutes,
                totalChaptersRead = maxOf(stats.totalChaptersRead, remoteEntry.totalChaptersRead),
                booksCompleted = maxOf(stats.booksCompleted, remoteEntry.booksCompleted),
                readingStreak = maxOf(stats.readingStreak, remoteEntry.readingStreak),
                hasBadge = remoteEntry.hasBadge || user.isSupporter,
                badgeType = remoteEntry.badgeType ?: if (user.isSupporter) "supporter" else null,
                lastSyncedAt = currentTimeToLong(),
                level = readerLevel.level,
                levelTitle = readerLevel.title,
                xp = readerLevel.currentXp,
                xpToNextLevel = readerLevel.xpToNextLevel
            )
        } else {
            // No remote entry yet, use local data
            UserLeaderboardStats(
                userId = user.id,
                username = user.username ?: user.email.substringBefore("@"),
                totalReadingTimeMinutes = totalMinutes,
                totalChaptersRead = stats.totalChaptersRead,
                booksCompleted = stats.booksCompleted,
                readingStreak = stats.readingStreak,
                hasBadge = user.isSupporter,
                badgeType = if (user.isSupporter) "supporter" else null,
                lastSyncedAt = currentTimeToLong(),
                level = readerLevel.level,
                levelTitle = readerLevel.title,
                xp = readerLevel.currentXp,
                xpToNextLevel = readerLevel.xpToNextLevel
            )
        }

        // Upsert merged stats to remote
        val result = leaderboardRepository.syncUserStats(mergedStats)

        // If remote had higher values, update local statistics to match
        if (remoteEntry != null && result.isSuccess) {
            updateLocalStatisticsIfNeeded(stats, remoteEntry)
        }

        return result
    }

    /**
     * Update local statistics if remote leaderboard had higher values.
     * This ensures data is never lost when syncing from another device.
     */
    private suspend fun updateLocalStatisticsIfNeeded(
        local: ireader.domain.models.entities.ReadingStatisticsType1,
        remote: UserLeaderboardStats
    ) {
        if (remote.totalReadingTimeMinutes > local.totalReadingTimeMinutes) {
            val diff = remote.totalReadingTimeMinutes - local.totalReadingTimeMinutes
            statisticsRepository.addReadingTime(diff)
        }
        if (remote.totalChaptersRead > local.totalChaptersRead) {
            val diff = remote.totalChaptersRead - local.totalChaptersRead
            repeat(diff) { statisticsRepository.incrementChaptersRead() }
        }
        if (remote.booksCompleted > local.booksCompleted) {
            val diff = remote.booksCompleted - local.booksCompleted
            repeat(diff) { statisticsRepository.incrementBooksCompleted() }
        }
        if (remote.readingStreak > local.readingStreak) {
            val lastReadDate = statisticsRepository.getLastReadDate() ?: currentTimeToLong()
            statisticsRepository.updateStreak(remote.readingStreak, lastReadDate)
        }
    }
    
    fun observeLeaderboard(limit: Int = 100): Flow<List<LeaderboardEntry>> {
        return leaderboardRepository.observeLeaderboard(limit)
    }
    
    suspend fun getTopUsers(limit: Int = 10): Result<List<LeaderboardEntry>> {
        return leaderboardRepository.getTopUsers(limit)
    }
    
    fun isRealtimeEnabled(): Boolean {
        return uiPreferences.leaderboardRealtimeEnabled().get()
    }
    
    fun setRealtimeEnabled(enabled: Boolean) {
        uiPreferences.leaderboardRealtimeEnabled().set(enabled)
    }

    /**
     * Calculate the reader's current level from their total reading time.
     */
    fun getReaderLevel(totalMinutes: Long): ReaderLevel {
        return ReaderLevel.fromMinutes(totalMinutes)
    }

    suspend fun getUserSyncedBooks(userId: String): Result<List<ireader.domain.models.entities.SyncedBookSummary>> {
        return leaderboardRepository.getUserSyncedBooks(userId)
    }
}

package ireader.domain.usecases.leaderboard

import ireader.domain.data.repository.LeaderboardRepository
import ireader.domain.data.repository.ReadingStatisticsRepository
import ireader.domain.data.repository.RemoteRepository
import ireader.domain.models.entities.LeaderboardEntry
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
        
        val leaderboardStats = UserLeaderboardStats(
            userId = user.id,
            username = user.username ?: user.email.substringBefore("@"),
            totalReadingTimeMinutes = stats.totalReadingTimeMinutes,
            totalChaptersRead = stats.totalChaptersRead,
            booksCompleted = stats.booksCompleted,
            readingStreak = stats.readingStreak,
            hasBadge = user.isSupporter, // Or check badge ownership
            badgeType = if (user.isSupporter) "supporter" else null,
            lastSyncedAt = currentTimeToLong()
        )
        
        return leaderboardRepository.syncUserStats(leaderboardStats)
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
}

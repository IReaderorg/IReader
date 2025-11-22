package ireader.domain.data.repository

import ireader.domain.models.entities.LeaderboardEntry
import ireader.domain.models.entities.UserLeaderboardStats
import kotlinx.coroutines.flow.Flow

interface LeaderboardRepository {
    /**
     * Get leaderboard entries with pagination
     */
    suspend fun getLeaderboard(limit: Int = 100, offset: Int = 0): Result<List<LeaderboardEntry>>
    
    /**
     * Get user's current rank and stats
     */
    suspend fun getUserRank(userId: String): Result<LeaderboardEntry?>
    
    /**
     * Sync user's reading stats to leaderboard
     */
    suspend fun syncUserStats(stats: UserLeaderboardStats): Result<Unit>
    
    /**
     * Observe leaderboard changes in realtime
     */
    fun observeLeaderboard(limit: Int = 100): Flow<List<LeaderboardEntry>>
    
    /**
     * Get top N users
     */
    suspend fun getTopUsers(limit: Int = 10): Result<List<LeaderboardEntry>>
    
    /**
     * Get users around a specific rank
     */
    suspend fun getUsersAroundRank(rank: Int, range: Int = 5): Result<List<LeaderboardEntry>>
}

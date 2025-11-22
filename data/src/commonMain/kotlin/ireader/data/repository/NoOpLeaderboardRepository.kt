package ireader.data.repository

import ireader.domain.data.repository.LeaderboardRepository
import ireader.domain.models.entities.LeaderboardEntry
import ireader.domain.models.entities.UserLeaderboardStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * No-op implementation of LeaderboardRepository for when Supabase is not configured.
 * Returns empty results and success for all operations.
 */
class NoOpLeaderboardRepository : LeaderboardRepository {
    
    override suspend fun getLeaderboard(limit: Int, offset: Int): Result<List<LeaderboardEntry>> {
        return Result.success(emptyList())
    }
    
    override suspend fun getUserRank(userId: String): Result<LeaderboardEntry?> {
        return Result.success(null)
    }
    
    override suspend fun syncUserStats(stats: UserLeaderboardStats): Result<Unit> {
        return Result.success(Unit)
    }
    
    override fun observeLeaderboard(limit: Int): Flow<List<LeaderboardEntry>> {
        return flowOf(emptyList())
    }
    
    override suspend fun getTopUsers(limit: Int): Result<List<LeaderboardEntry>> {
        return Result.success(emptyList())
    }
    
    override suspend fun getUsersAroundRank(rank: Int, range: Int): Result<List<LeaderboardEntry>> {
        return Result.success(emptyList())
    }
}

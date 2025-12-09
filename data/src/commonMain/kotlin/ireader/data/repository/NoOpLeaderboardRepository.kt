package ireader.data.repository

import ireader.data.repository.base.NoOpRepositoryBase
import ireader.domain.data.repository.LeaderboardRepository
import ireader.domain.models.entities.LeaderboardEntry
import ireader.domain.models.entities.UserLeaderboardStats
import kotlinx.coroutines.flow.Flow

/**
 * No-op implementation of LeaderboardRepository for when Supabase is not configured.
 * Returns empty results and success for all operations.
 * 
 * Implemented as a singleton object since it is stateless.
 * @see Requirements 2.1, 2.2, 2.3, 2.4
 */
object NoOpLeaderboardRepository : NoOpRepositoryBase(), LeaderboardRepository {
    
    override suspend fun getLeaderboard(limit: Int, offset: Int): Result<List<LeaderboardEntry>> =
        emptyListResult()
    
    override suspend fun getUserRank(userId: String): Result<LeaderboardEntry?> =
        emptyResult()
    
    override suspend fun syncUserStats(stats: UserLeaderboardStats): Result<Unit> =
        unitResult()
    
    override fun observeLeaderboard(limit: Int): Flow<List<LeaderboardEntry>> =
        emptyListFlow()
    
    override suspend fun getTopUsers(limit: Int): Result<List<LeaderboardEntry>> =
        emptyListResult()
    
    override suspend fun getUsersAroundRank(rank: Int, range: Int): Result<List<LeaderboardEntry>> =
        emptyListResult()
}

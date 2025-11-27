package ireader.data.repository

import ireader.domain.data.repository.DonationLeaderboardRepository
import ireader.domain.models.entities.DonationLeaderboardEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * No-op implementation of DonationLeaderboardRepository for when Supabase is not configured.
 * Returns empty results and success for all operations.
 */
class NoOpDonationLeaderboardRepository : DonationLeaderboardRepository {
    
    override suspend fun getDonationLeaderboard(limit: Int, offset: Int): Result<List<DonationLeaderboardEntry>> {
        return Result.success(emptyList())
    }
    
    override suspend fun getUserDonationRank(userId: String): Result<DonationLeaderboardEntry?> {
        return Result.success(null)
    }
    
    override fun observeDonationLeaderboard(limit: Int): Flow<List<DonationLeaderboardEntry>> {
        return flowOf(emptyList())
    }
    
    override suspend fun getTopDonors(limit: Int): Result<List<DonationLeaderboardEntry>> {
        return Result.success(emptyList())
    }
}

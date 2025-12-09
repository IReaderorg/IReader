package ireader.data.repository

import ireader.data.repository.base.NoOpRepositoryBase
import ireader.domain.data.repository.DonationLeaderboardRepository
import ireader.domain.models.entities.DonationLeaderboardEntry
import kotlinx.coroutines.flow.Flow

/**
 * No-op implementation of DonationLeaderboardRepository for when Supabase is not configured.
 * Returns empty results and success for all operations.
 * 
 * Implemented as a singleton object since it is stateless.
 * @see Requirements 2.1, 2.2, 2.3, 2.4
 */
object NoOpDonationLeaderboardRepository : NoOpRepositoryBase(), DonationLeaderboardRepository {
    
    override suspend fun getDonationLeaderboard(limit: Int, offset: Int): Result<List<DonationLeaderboardEntry>> =
        emptyListResult()
    
    override suspend fun getUserDonationRank(userId: String): Result<DonationLeaderboardEntry?> =
        emptyResult()
    
    override fun observeDonationLeaderboard(limit: Int): Flow<List<DonationLeaderboardEntry>> =
        emptyListFlow()
    
    override suspend fun getTopDonors(limit: Int): Result<List<DonationLeaderboardEntry>> =
        emptyListResult()
}

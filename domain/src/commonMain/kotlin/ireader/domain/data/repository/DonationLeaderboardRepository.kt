package ireader.domain.data.repository

import ireader.domain.models.entities.DonationLeaderboardEntry
import kotlinx.coroutines.flow.Flow

interface DonationLeaderboardRepository {
    /**
     * Get donation leaderboard entries with pagination
     */
    suspend fun getDonationLeaderboard(limit: Int = 100, offset: Int = 0): Result<List<DonationLeaderboardEntry>>
    
    /**
     * Get user's current rank and donation stats
     */
    suspend fun getUserDonationRank(userId: String): Result<DonationLeaderboardEntry?>
    
    /**
     * Observe donation leaderboard changes in realtime
     */
    fun observeDonationLeaderboard(limit: Int = 100): Flow<List<DonationLeaderboardEntry>>
    
    /**
     * Get top N donors
     */
    suspend fun getTopDonors(limit: Int = 10): Result<List<DonationLeaderboardEntry>>
}

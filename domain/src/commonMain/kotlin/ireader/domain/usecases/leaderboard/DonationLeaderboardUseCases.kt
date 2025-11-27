package ireader.domain.usecases.leaderboard

import ireader.domain.data.repository.DonationLeaderboardRepository
import ireader.domain.data.repository.RemoteRepository
import ireader.domain.models.entities.DonationLeaderboardEntry
import ireader.domain.preferences.prefs.UiPreferences
import kotlinx.coroutines.flow.Flow

class DonationLeaderboardUseCases(
    private val donationLeaderboardRepository: DonationLeaderboardRepository,
    private val remoteRepository: RemoteRepository,
    private val uiPreferences: UiPreferences
) {
    
    suspend fun getDonationLeaderboard(limit: Int = 100): Result<List<DonationLeaderboardEntry>> {
        return donationLeaderboardRepository.getDonationLeaderboard(limit = limit)
    }
    
    suspend fun getUserDonationRank(): Result<DonationLeaderboardEntry?> {
        val user = remoteRepository.getCurrentUser().getOrNull() ?: return Result.success(null)
        return donationLeaderboardRepository.getUserDonationRank(user.id)
    }
    
    fun observeDonationLeaderboard(limit: Int = 100): Flow<List<DonationLeaderboardEntry>> {
        return donationLeaderboardRepository.observeDonationLeaderboard(limit)
    }
    
    suspend fun getTopDonors(limit: Int = 10): Result<List<DonationLeaderboardEntry>> {
        return donationLeaderboardRepository.getTopDonors(limit)
    }
    
    fun isRealtimeEnabled(): Boolean {
        return uiPreferences.donationLeaderboardRealtimeEnabled().get()
    }
    
    fun setRealtimeEnabled(enabled: Boolean) {
        uiPreferences.donationLeaderboardRealtimeEnabled().set(enabled)
    }
}

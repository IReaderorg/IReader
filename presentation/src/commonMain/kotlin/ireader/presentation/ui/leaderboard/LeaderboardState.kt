package ireader.presentation.ui.leaderboard

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import ireader.domain.models.entities.DonationLeaderboardEntry
import ireader.domain.models.entities.LeaderboardEntry

/**
 * Immutable state for the Reading Leaderboard screen following Mihon's StateScreenModel pattern.
 */
@Immutable
data class LeaderboardScreenState(
    val leaderboard: List<LeaderboardEntry> = emptyList(),
    val userRank: LeaderboardEntry? = null,
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val error: String? = null,
    val syncError: String? = null,
    val lastSyncTime: Long = 0,
    val isRealtimeEnabled: Boolean = false
) {
    @Stable
    val isEmpty: Boolean get() = leaderboard.isEmpty() && !isLoading
    
    @Stable
    val isInitialLoading: Boolean get() = isLoading && leaderboard.isEmpty()
    
    @Stable
    val hasContent: Boolean get() = leaderboard.isNotEmpty()
    
    @Stable
    val hasUserRank: Boolean get() = userRank != null
}

/**
 * Immutable state for the Donation Leaderboard screen following Mihon's StateScreenModel pattern.
 */
@Immutable
data class DonationLeaderboardScreenState(
    val leaderboard: List<DonationLeaderboardEntry> = emptyList(),
    val userRank: DonationLeaderboardEntry? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRealtimeEnabled: Boolean = false
) {
    @Stable
    val isEmpty: Boolean get() = leaderboard.isEmpty() && !isLoading
    
    @Stable
    val isInitialLoading: Boolean get() = isLoading && leaderboard.isEmpty()
    
    @Stable
    val hasContent: Boolean get() = leaderboard.isNotEmpty()
    
    @Stable
    val hasUserRank: Boolean get() = userRank != null
}

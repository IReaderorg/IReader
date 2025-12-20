package ireader.presentation.core.ui

import ireader.presentation.core.LocalNavigator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import ireader.presentation.ui.leaderboard.CombinedLeaderboardScreen
import ireader.presentation.ui.leaderboard.DonationLeaderboardViewModel
import ireader.presentation.ui.leaderboard.LeaderboardViewModel
import ireader.presentation.core.safePopBackStack
/**
 * Screen specification for the Leaderboard (Reading + Donations tabs)
 */
class LeaderboardScreenSpec {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val readingViewModel: LeaderboardViewModel = getIViewModel()
        val donationViewModel: DonationLeaderboardViewModel = getIViewModel()
        
        CombinedLeaderboardScreen(
            readingVm = readingViewModel,
            donationVm = donationViewModel,
            onBack = {
                navController.safePopBackStack()
            }
        )
    }
}

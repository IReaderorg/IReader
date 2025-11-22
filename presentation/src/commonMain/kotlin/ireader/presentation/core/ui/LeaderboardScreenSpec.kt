package ireader.presentation.core.ui

import ireader.presentation.core.LocalNavigator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import ireader.presentation.ui.leaderboard.LeaderboardScreen
import ireader.presentation.ui.leaderboard.LeaderboardViewModel

/**
 * Screen specification for the Leaderboard
 */
class LeaderboardScreenSpec {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val viewModel: LeaderboardViewModel = getIViewModel()
        
        LeaderboardScreen(
            vm = viewModel,
            onBack = {
                navController.popBackStack()
            }
        )
    }
}

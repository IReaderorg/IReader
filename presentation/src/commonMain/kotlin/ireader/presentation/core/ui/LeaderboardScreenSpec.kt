package ireader.presentation.core.ui

import ireader.presentation.core.LocalNavigator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import ireader.presentation.ui.leaderboard.LeaderboardScreen
import ireader.presentation.ui.leaderboard.LeaderboardViewModel
import ireader.presentation.core.safePopBackStack
/**
 * Screen specification for the Leaderboard with level system
 */
class LeaderboardScreenSpec {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val readingViewModel: LeaderboardViewModel = getIViewModel()
        
        LeaderboardScreen(
            vm = readingViewModel,
            onBack = {
                navController.safePopBackStack()
            },
            onNavigateToProfile = { userId: String ->
                navController.navigate("userProfile/$userId")
            }
        )
    }
}

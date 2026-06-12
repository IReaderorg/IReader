package ireader.presentation.core.ui

import androidx.compose.runtime.Composable
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.safePopBackStack
import ireader.presentation.ui.leaderboard.LeaderboardViewModel
import ireader.presentation.ui.leaderboard.UserProfileScreen

class UserProfileScreenSpec {

    @Composable
    fun Content(userId: String) {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val leaderboardViewModel: LeaderboardViewModel = getIViewModel()

        UserProfileScreen(
            vm = leaderboardViewModel,
            userId = userId,
            onBack = { navController.safePopBackStack() },
            onBookClick = { bookUrl, sourceId ->
                navController.navigate("explore/$sourceId")
            }
        )
    }
}

package ireader.presentation.core.ui

import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.NavigationRoutes
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import ireader.presentation.ui.settings.badges.manage.BadgeManagementScreen
import ireader.presentation.ui.settings.badges.manage.BadgeManagementViewModel

/**
 * Screen specification for Badge Management
 */
class BadgeManagementScreenSpec {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val viewModel: BadgeManagementViewModel = getIViewModel()
        
        BadgeManagementScreen(
            viewModel = viewModel,
            onNavigateBack = {
                navController.popBackStack()
            },
            onNavigateToBadgeStore = {
                navController.navigate(NavigationRoutes.badgeStore)
            }
        )
    }
}

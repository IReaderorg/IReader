package ireader.presentation.core.ui

import androidx.compose.runtime.Composable
import ireader.presentation.core.LocalNavigator
import ireader.presentation.ui.developerportal.DeveloperPortalScreen
import ireader.presentation.ui.developerportal.DeveloperPortalViewModel

/**
 * Screen specification for Developer Portal
 */
class DeveloperPortalScreenSpec {

    @Composable
    fun Content() {
        val navController = LocalNavigator.current
        val viewModel: DeveloperPortalViewModel = getIViewModel()

        DeveloperPortalScreen(
            viewModel = viewModel,
            onNavigateBack = {
                navController?.popBackStack()
            }
        )
    }
}

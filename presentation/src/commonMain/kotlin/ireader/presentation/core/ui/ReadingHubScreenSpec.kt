package ireader.presentation.core.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.safePopBackStack
import ireader.presentation.ui.readinghub.ReadingHubScreen
import ireader.presentation.ui.readinghub.ReadingHubViewModel

/**
 * Screen specification for the unified Reading Hub screen.
 * Combines statistics, reading buddy, and quotes preview into a single screen.
 */
class ReadingHubScreenSpec {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val viewModel: ReadingHubViewModel = getIViewModel()
        
        ReadingHubScreen(
            vm = viewModel,
            onBack = { navController.safePopBackStack() }
        )
    }
}

package ireader.presentation.core.ui

import androidx.compose.runtime.Composable
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.safePopBackStack
import ireader.presentation.ui.settings.tracking.SettingsTrackingScreen
import ireader.presentation.ui.settings.tracking.SettingsTrackingViewModel
import org.koin.compose.koinInject

/**
 * Screen spec for tracking settings.
 * Provides integration with external tracking services like AniList, MAL, Kitsu, etc.
 */
class TrackingSettingsScreenSpec {
    
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val viewModel: SettingsTrackingViewModel = koinInject()
        
        SettingsTrackingScreen(
            onNavigateUp = { navController.safePopBackStack() },
            viewModel = viewModel
        )
    }
}

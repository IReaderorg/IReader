package ireader.presentation.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.safePopBackStack
import ireader.presentation.ui.settings.cloudflare.CloudflareBypassSettingsScreen
import ireader.presentation.ui.settings.cloudflare.CloudflareBypassSettingsViewModel
import org.koin.compose.koinInject

/**
 * Screen spec for Cloudflare bypass settings.
 * Allows configuring FlareSolverr and other bypass providers.
 */
class CloudflareBypassSettingsScreenSpec {
    
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val viewModel: CloudflareBypassSettingsViewModel = koinInject()
        
        val flareSolverrUrl by viewModel.flareSolverrUrl.collectAsState()
        
        CloudflareBypassSettingsScreen(
            onNavigateUp = { navController.safePopBackStack() },
            bypassManager = viewModel.bypassManager,
            flareSolverrUrl = flareSolverrUrl,
            onFlareSolverrUrlChange = viewModel::updateFlareSolverrUrl
        )
    }
}

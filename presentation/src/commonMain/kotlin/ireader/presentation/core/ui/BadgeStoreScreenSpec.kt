package ireader.presentation.core.ui

import ireader.presentation.core.LocalNavigator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import ireader.presentation.ui.settings.badges.store.BadgeStoreScreen
import ireader.presentation.ui.settings.badges.store.BadgeStoreViewModel

/**
 * Screen specification for the Badge Store
 */
class BadgeStoreScreenSpec {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val uriHandler = LocalUriHandler.current
        val viewModel: BadgeStoreViewModel = getIViewModel()
        
        BadgeStoreScreen(
            viewModel = viewModel,
            onNavigateBack = {
                navController.popBackStack()
            },
            onOpenDonationLink = { url ->
                uriHandler.openUri(url)
            }
        )
    }
}

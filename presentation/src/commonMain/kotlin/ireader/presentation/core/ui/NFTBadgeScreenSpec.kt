package ireader.presentation.core.ui

import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.NavigationRoutes
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import ireader.presentation.ui.settings.badges.nft.NFTBadgeScreen
import ireader.presentation.ui.settings.badges.nft.NFTBadgeViewModel
import ireader.presentation.core.safePopBackStack
/**
 * Screen specification for the NFT Badge verification
 */
class NFTBadgeScreenSpec {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val viewModel: NFTBadgeViewModel = getIViewModel()
        
        NFTBadgeScreen(
            viewModel = viewModel,
            onNavigateBack = {
                navController.safePopBackStack()
            },
            onOpenUrl = { url ->
                // Open URL in external browser - platform specific
                // For now, navigate to webView
                navController.navigate(NavigationRoutes.webView(url))
            }
        )
    }
}

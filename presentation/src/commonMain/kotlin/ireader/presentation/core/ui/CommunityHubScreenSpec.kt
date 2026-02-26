package ireader.presentation.core.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.NavigationRoutes
import ireader.presentation.core.safePopBackStack
import ireader.presentation.ui.settings.CommunityHubScreen
import kotlinx.coroutines.launch

/**
 * Screen specification for the Community Hub - a parent screen that groups
 * all community-related features including leaderboards, popular books,
 * reviews, and badge customization.
 */
class CommunityHubScreenSpec {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        
        // Get admin status from the ViewModel
        val vm: ireader.presentation.ui.settings.MainSettingScreenViewModel = getIViewModel()
        
        CommunityHubScreen(
            onBack = {
                navController.safePopBackStack()
            },
            onLeaderboard = {
                navController.navigate(NavigationRoutes.leaderboard)
            },
            onPopularBooks = {
                navController.navigate(NavigationRoutes.popularBooks)
            },
            onAllReviews = {
                navController.navigate(NavigationRoutes.allReviews)
            },
            onCharacterArtGallery = {
                navController.navigate(NavigationRoutes.characterArtUpload)
            },
            onReadingBuddy = {
                navController.navigate(NavigationRoutes.readingHub)
            },
            onMyQuotes = {
                navController.navigate(NavigationRoutes.myQuotes)
            },
            onGlossary = {
                navController.navigate(NavigationRoutes.glossary)
            },
            onCommunitySource = {
                navController.navigate(NavigationRoutes.communitySourceConfig)
            },
            onUserSources = {
                navController.navigate(NavigationRoutes.userSources)
            },
            onLegadoSources = {
                navController.navigate(NavigationRoutes.legadoSourceImport)
            },
            onFeatureStore = {
                navController.navigate(NavigationRoutes.featureStore)
            },
            onPluginRepository = {
                navController.navigate(NavigationRoutes.pluginRepository)
            },
            onDeveloperPortal = {
                navController.navigate(NavigationRoutes.developerPortal)
            },
            onBadgeStore = {
                navController.navigate(NavigationRoutes.badgeStore)
            },
            onNFTBadge = {
                navController.navigate(NavigationRoutes.nftBadge)
            },
            onBadgeManagement = {
                navController.navigate(NavigationRoutes.badgeManagement)
            },
            isAdmin = vm.isAdmin.value,
            onAdminBadgeVerification = {
                navController.navigate(NavigationRoutes.adminBadgeVerification)
            },
            onAdminUserPanel = {
                navController.navigate(NavigationRoutes.adminUserPanel)
            }
        )
    }
}

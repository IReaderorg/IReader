package ireader.presentation.core.ui

import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.NavigationRoutes

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalUriHandler
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import ireader.i18n.discord
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.settings.MainSettingScreenViewModel
import ireader.presentation.ui.settings.MoreScreen

object MoreScreenSpec : Tab {

    override val options: TabOptions
        @Composable
        get()  {
            val title = localize(Res.string.more)
            val icon = rememberVectorPainter(Icons.Filled.MoreHoriz)
            return remember {
                TabOptions(
                    index = 4u,
                    title = title,
                    icon = icon,
                )
            }

        }
    @OptIn(
        ExperimentalMaterial3Api::class
    )
    @Composable
    override fun Content() {
        val uriHandler = LocalUriHandler.current
        val vm: MainSettingScreenViewModel = getIViewModel()
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        IScaffold(
            topBar = { scrollBehavior ->
                TitleToolbar(
                    title = localize(Res.string.more),
                    scrollBehavior = scrollBehavior,
                    popBackStack = null,
                )
            }
        ) { padding ->
            MoreScreen(
                modifier = Modifier.padding(padding),
                vm = vm,
                onAbout = {
                    navController.navigate(NavigationRoutes.about)
                },
                onSettings = {
                    navController.navigate(NavigationRoutes.settings)
                },
                onBackupScreen = {
                    navController.navigate(NavigationRoutes.backupRestore)
                },
                onDownloadScreen = {
                    navController.navigate(NavigationRoutes.downloader)
                },
                onHelp = {
                    uriHandler.openUri(discord)
                },
                onCategory = {
                    navController.navigate(NavigationRoutes.category)
                },
                onDonation = {
                    navController.navigate(NavigationRoutes.donation)
                },
                onTTSEngineManager = {
                    navController.navigate(NavigationRoutes.ttsEngineManager)
                },
                onWeb3Profile = {
                    navController.navigate(NavigationRoutes.profile)
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
                onAdminBadgeVerification = {
                    navController.navigate(NavigationRoutes.adminBadgeVerification)
                },
                onLeaderboard = {
                    navController.navigate(NavigationRoutes.leaderboard)
                },
            )
        }

    }
}

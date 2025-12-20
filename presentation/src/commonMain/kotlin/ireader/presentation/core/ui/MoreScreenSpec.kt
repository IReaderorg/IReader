package ireader.presentation.core.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalUriHandler
import ireader.i18n.discord
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.more
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.NavigationRoutes
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.settings.MainSettingScreenViewModel
import ireader.presentation.ui.settings.MoreScreen

/**
 * More screen specification - provides tab metadata and content
 */
object MoreScreenSpec {

    @Composable
    fun getTitle(): String = localize(Res.string.more)

    @Composable
    fun getIcon(): Painter = rememberVectorPainter(Icons.Filled.MoreHoriz)

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TabContent() {
        val uriHandler = LocalUriHandler.current
        val vm: MainSettingScreenViewModel = getIViewModel()
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        
        IScaffold { padding ->
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
                onWeb3Profile = {
                    navController.navigate(NavigationRoutes.profile)
                },
                onCommunityHub = {
                    navController.navigate(NavigationRoutes.communityHub)
                },
                onReadingBuddy = {
                    navController.navigate(NavigationRoutes.readingHub)
                },
            )
        }
    }
}

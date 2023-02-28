package ireader.presentation.core.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource

import androidx.navigation.NamedNavArgument
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import ireader.presentation.ui.component.Controller
import ireader.i18n.discord
import ireader.i18n.localize
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.core.ui.util.NavigationArgs

import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.settings.MainSettingScreenViewModel
import ireader.presentation.ui.settings.MoreScreen
import ireader.i18n.resources.MR

object MoreScreenSpec : Tab {

    override val options: TabOptions
        @Composable
        get()  {
            val title = localize(MR.strings.more)
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
        ExperimentalMaterialApi::class,
        ExperimentalMaterial3Api::class
    )
    @Composable
    override fun Content() {
        val uriHandler = LocalUriHandler.current
        val vm: MainSettingScreenViewModel = getIViewModel()
        val navigator = LocalNavigator.currentOrThrow
        IScaffold(
            topBar = { scrollBehavior ->
                TitleToolbar(
                    title = localize(MR.strings.more),
                    scrollBehavior = scrollBehavior,
                    popBackStack  = {
                        popBackStack(navigator)
                    },
                )
            }
        ) { padding ->
            MoreScreen(
                modifier = Modifier.padding(padding),
                vm = vm,
                onAbout = {
                    navigator.push(AboutSettingSpec())
                },
                onSettings = {
                    navigator.push(SettingScreenSpec())
                },
                onBackupScreen = {
                    navigator.push(BackupAndRestoreScreenSpec())
                },
                onDownloadScreen = {
                    navigator.push(DownloaderScreenSpec())
                },
                onHelp = {
                    uriHandler.openUri(discord)
                },
                onCategory = {
                    navigator.push(CategoryScreenSpec())
                }
            )
        }

    }
}

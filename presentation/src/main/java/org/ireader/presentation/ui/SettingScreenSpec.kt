package org.ireader.presentation.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import org.ireader.domain.ui.NavigationArgs
import org.ireader.presentation.R
import org.ireader.settings.setting.MainSettingScreenViewModel
import org.ireader.settings.setting.SettingScreen

object SettingScreenSpec : BottomNavScreenSpec {
    override val icon: ImageVector = Icons.Default.Settings
    override val label: Int = R.string.setting_screen_label
    override val navHostRoute: String = "setting"

    override val arguments: List<NamedNavArgument> = listOf(
        NavigationArgs.showBottomNav
    )

    @OptIn(
        ExperimentalPagerApi::class, androidx.compose.animation.ExperimentalAnimationApi::class,
        androidx.compose.material.ExperimentalMaterialApi::class
    )
    @Composable
    override fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
    ) {
        val uriHandler = LocalUriHandler.current
        val vm: MainSettingScreenViewModel = hiltViewModel()


        SettingScreen(
            vm = vm,
            onAbout = {
                navController.navigate(AboutInfoScreenSpec.navHostRoute)
            },
            onAdvance = {
                navController.navigate(AdvanceSettingSpec.navHostRoute)
            },
            onAppearanceScreen = {
                navController.navigate(AppearanceScreenSpec.navHostRoute)
            },
            onBackupScreen = {
                navController.navigate(BackupAndRestoreScreenSpec.navHostRoute)
            },
            onDownloadScreen = {
                navController.navigate(DownloaderScreenSpec.navHostRoute)
            },
            onHelp = {
                uriHandler.openUri("https://discord.gg/HBU6zD8c5v")
            }
        )
    }
}
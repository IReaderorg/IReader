package org.ireader.presentation.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import org.ireader.common_resources.discord
import org.ireader.components.components.TitleToolbar
import org.ireader.domain.ui.NavigationArgs
import org.ireader.presentation.R
import org.ireader.settings.setting.MainSettingScreenViewModel
import org.ireader.settings.setting.MoreScreen

object MoreScreenSpec : BottomNavScreenSpec {
    override val icon: ImageVector = Icons.Filled.MoreHoriz
    override val label: Int = R.string.more
    override val navHostRoute: String = "more"

    override val arguments: List<NamedNavArgument> = listOf(
        NavigationArgs.showBottomNav
    )
    @ExperimentalMaterial3Api
    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun TopBar(
                controller: ScreenSpec.Controller
    ) {
        TitleToolbar(
            title = stringResource(org.ireader.ui_settings.R.string.more),
            navController = null
        )
    }

    @OptIn(
        ExperimentalMaterialApi::class,
        ExperimentalMaterial3Api::class
    )
    @Composable
    override fun Content(
        controller: ScreenSpec.Controller
    ) {
        val uriHandler = LocalUriHandler.current
        val vm: MainSettingScreenViewModel = hiltViewModel   (controller.navBackStackEntry)


        MoreScreen(
            modifier = Modifier.padding(controller.scaffoldPadding),
            vm = vm,
            onAbout = {
                controller.navController.navigate(AboutSettingSpec.navHostRoute)
            },
            onSettings = {
                controller.navController.navigate(SettingScreenSpec.navHostRoute)
            },
            onAppearanceScreen = {
                controller.navController.navigate(AppearanceScreenSpec.navHostRoute)
            },
            onBackupScreen = {
                controller.navController.navigate(BackupAndRestoreScreenSpec.navHostRoute)
            },
            onDownloadScreen = {
                controller.navController.navigate(DownloaderScreenSpec.navHostRoute)
            },
            onHelp = {
                uriHandler.openUri(discord)
            },
            onCategory = {
                controller.navController.navigate(CategoryScreenSpec.navHostRoute)
            }
        )
    }
}
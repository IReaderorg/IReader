package org.ireader.presentation.ui

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import org.ireader.components.Controller
import org.ireader.components.components.TitleToolbar
import org.ireader.presentation.R
import org.ireader.settings.setting.general.GeneralSettingScreen
import org.ireader.settings.setting.general.GeneralSettingScreenViewModel

@ExperimentalMaterial3Api
@OptIn(ExperimentalMaterialApi::class)
object GeneralScreenSpec : ScreenSpec {
    override val navHostRoute: String = "general_screen_spec"

    @Composable
    override fun TopBar(
        controller: Controller
    ) {
        TitleToolbar(
            title = stringResource(id = R.string.general),
            navController = controller.navController,
            scrollBehavior = controller.scrollBehavior
        )
    }

    @Composable
    override fun Content(
        controller: Controller
    ) {
        val vm: GeneralSettingScreenViewModel = hiltViewModel(controller.navBackStackEntry)

        val context = LocalContext.current
        GeneralSettingScreen(
            scaffoldPadding = controller.scaffoldPadding,
            vm = vm,
        )
    }
}

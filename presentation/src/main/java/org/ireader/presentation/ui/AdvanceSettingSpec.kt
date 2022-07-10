package org.ireader.presentation.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import org.ireader.Controller
import org.ireader.components.components.TitleToolbar
import org.ireader.core_ui.ui.SnackBarListener
import org.ireader.settings.setting.AdvanceSettingViewModel
import org.ireader.settings.setting.advance.AdvanceSettings
import org.ireader.ui_settings.R

object AdvanceSettingSpec : ScreenSpec {

    override val navHostRoute: String = "advance_setting_route"

    @ExperimentalMaterial3Api
    @Composable
    override fun TopBar(
        controller: Controller
    ) {
        TitleToolbar(
            title = stringResource(R.string.advance_setting),
            navController = controller.navController,
            scrollBehavior = controller.scrollBehavior
        )
    }

    @Composable
    override fun Content(
        controller: Controller
    ) {
        val vm: AdvanceSettingViewModel = hiltViewModel(controller.navBackStackEntry)
        SnackBarListener(vm = vm, host = controller.snackBarHostState)
        AdvanceSettings(vm = vm, controller = controller)
    }
}

package org.ireader.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import org.ireader.components.components.TitleToolbar
import org.ireader.settings.setting.SettingViewModel
import org.ireader.settings.setting.advance_setting.AdvanceSettings
import org.ireader.ui_settings.R

object AdvanceSettingSpec : ScreenSpec {

    override val navHostRoute: String = "advance_setting_route"
    @ExperimentalMaterial3Api
    @Composable
    override fun TopBar(
        controller: ScreenSpec.Controller
    ) {
        TitleToolbar(
            title = stringResource(R.string.advance_setting),
            navController =controller. navController
        )
    }

    @Composable
    override fun Content(
        controller: ScreenSpec.Controller
    ) {
        val vm: SettingViewModel = hiltViewModel(   controller.navBackStackEntry)
        Box(modifier = Modifier.padding(controller.scaffoldPadding)) {
            AdvanceSettings(
                vm = vm,
                onBackStack = {
                    controller.navController.popBackStack()
                },
                snackBarHostState = controller.snackBarHostState
            )
        }

    }
}

package org.ireader.presentation.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import org.ireader.components.Controller
import org.ireader.appearance.AppearanceSettingScreen
import org.ireader.appearance.AppearanceToolbar
import org.ireader.appearance.AppearanceViewModel
import org.ireader.core_ui.ui.SnackBarListener

object AppearanceScreenSpec : ScreenSpec {

    override val navHostRoute: String = "appearance_setting_route"

    @ExperimentalMaterial3Api
    @Composable
    override fun TopBar(
        controller: Controller
    ) {
        val vm: AppearanceViewModel = hiltViewModel(controller.navBackStackEntry)
        AppearanceToolbar(
            vm = vm,
            onPopBackStack = {
                popBackStack(controller.navController)
            },
            scrollBehavior = controller.scrollBehavior
        )
    }

    @Composable
    override fun Content(
        controller: Controller
    ) {
        val viewModel: AppearanceViewModel = hiltViewModel(controller.navBackStackEntry)
        SnackBarListener(viewModel, controller.snackBarHostState)
        AppearanceSettingScreen(
            modifier = Modifier.padding(controller.scaffoldPadding),
            saveDarkModePreference = { theme ->
                viewModel.saveNightModePreferences(theme)
            },
            onPopBackStack = {
                controller.navController.popBackStack()
            },
            vm = viewModel,
            scaffoldPaddingValues = controller.scaffoldPadding

        )
    }
}

fun popBackStack(navController: NavController) {
    navController.popBackStack()
}

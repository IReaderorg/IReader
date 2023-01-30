package ireader.presentation.core.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import androidx.navigation.NavController
import ireader.presentation.ui.component.Controller
import ireader.presentation.ui.settings.appearance.AppearanceSettingScreen
import ireader.presentation.ui.core.ui.SnackBarListener
import ireader.presentation.ui.settings.appearance.AppearanceToolbar
import ireader.presentation.ui.settings.appearance.AppearanceViewModel
import org.koin.androidx.compose.getViewModel

object AppearanceScreenSpec : ScreenSpec {

    override val navHostRoute: String = "appearance_setting_route"

    @ExperimentalMaterial3Api
    @Composable
    override fun TopBar(
        controller: Controller
    ) {
        val vm: AppearanceViewModel = getViewModel()
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
        val viewModel: AppearanceViewModel = getViewModel(viewModelStoreOwner = controller.navBackStackEntry)
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
            scaffoldPaddingValues = controller.scaffoldPadding,
            onColorChange = {
                viewModel.isSavable = true
            },
            onColorReset = {
                viewModel.isSavable = false
            }

        )
    }
}

fun popBackStack(navController: NavController) {
    navController.popBackStack()
}

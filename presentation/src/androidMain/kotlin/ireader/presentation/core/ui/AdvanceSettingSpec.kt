package ireader.presentation.core.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

import ireader.presentation.ui.component.Controller
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.core.ui.SnackBarListener
import ireader.presentation.ui.settings.AdvanceSettingViewModel
import ireader.presentation.ui.settings.advance.AdvanceSettings
import ireader.presentation.R
import org.koin.androidx.compose.getViewModel

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

        val vm: AdvanceSettingViewModel = getViewModel()
        SnackBarListener(vm = vm, host = controller.snackBarHostState)
        AdvanceSettings(vm = vm, controller = controller)
    }
}

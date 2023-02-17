package ireader.presentation.core.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource

import ireader.presentation.ui.component.Controller
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.core.ui.SnackBarListener
import ireader.presentation.ui.settings.AdvanceSettingViewModel
import ireader.presentation.ui.settings.advance.AdvanceSettings
import ireader.presentation.R
import ireader.presentation.ui.component.IScaffold
import org.koin.androidx.compose.getViewModel

object AdvanceSettingSpec : ScreenSpec {

    override val navHostRoute: String = "advance_setting_route"

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(
        controller: Controller
    ) {
        val vm: AdvanceSettingViewModel = getViewModel()
        val host = SnackBarListener(vm = vm)
        IScaffold(
            topBar = {
                TitleToolbar(
                    title = stringResource(R.string.advance_setting),
                    navController = controller.navController,
                    scrollBehavior = controller.scrollBehavior
                )
            }, snackbarHostState = host
        ) {padding ->
            AdvanceSettings(vm = vm, controller = controller, padding = padding)
        }
    }
}

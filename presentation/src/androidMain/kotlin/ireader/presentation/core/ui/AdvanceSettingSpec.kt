package ireader.presentation.core.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.i18n.localize

import ireader.presentation.ui.component.Controller
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.core.ui.SnackBarListener
import ireader.presentation.ui.settings.AdvanceSettingViewModel
import ireader.presentation.ui.settings.advance.AdvanceSettings
import ireader.i18n.resources.MR
import ireader.presentation.core.VoyagerScreen
import ireader.presentation.ui.component.IScaffold
class AdvanceSettingSpec : VoyagerScreen() {


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val vm: AdvanceSettingViewModel = getIViewModel()
        val host = SnackBarListener(vm = vm)
        val navigator = LocalNavigator.currentOrThrow
        IScaffold(
            topBar = {scrollBehavior ->
                TitleToolbar(
                    title = localize(MR.strings.advance_setting),
                    scrollBehavior = scrollBehavior,
                    popBackStack = {
                        navigator.pop()
                    }
                )
            }, snackbarHostState = host
        ) {padding ->
            AdvanceSettings(vm = vm, padding = padding)
        }
    }
}

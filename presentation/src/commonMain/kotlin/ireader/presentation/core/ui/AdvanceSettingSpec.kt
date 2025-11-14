package ireader.presentation.core.ui

import ireader.presentation.core.LocalNavigator


import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.core.ui.SnackBarListener
import ireader.presentation.ui.settings.advance.AdvanceSettingViewModel
import ireader.presentation.ui.settings.advance.AdvanceSettings
class AdvanceSettingSpec {


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val vm: AdvanceSettingViewModel = getIViewModel()
        val host = SnackBarListener(vm = vm)
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        IScaffold(
            topBar = {scrollBehavior ->
                TitleToolbar(
                    title = localize(Res.string.advance_setting),
                    scrollBehavior = scrollBehavior,
                    popBackStack = {
                        navController.popBackStack()
                    }
                )
            }, snackbarHostState = host
        ) {padding ->
            AdvanceSettings(vm = vm, padding = padding)
        }
    }
}

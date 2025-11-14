package ireader.presentation.core.ui


import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.core.VoyagerScreen
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.core.ui.SnackBarListener
import ireader.presentation.ui.settings.advance.AdvanceSettingViewModel
import ireader.presentation.ui.settings.advance.AdvanceSettings
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
                    title = localize(Res.string.advance_setting),
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

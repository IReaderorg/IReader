package ireader.presentation.core.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.i18n.resources.MR
import ireader.presentation.core.VoyagerScreen
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.settings.general.GeneralSettingScreen
import ireader.presentation.ui.settings.general.GeneralSettingScreenViewModel


@ExperimentalMaterial3Api
class GeneralScreenSpec : VoyagerScreen() {

    @Composable
    override fun Content() {
        val vm: GeneralSettingScreenViewModel = getIViewModel()
        val navigator = LocalNavigator.currentOrThrow
        val localizeHelper = LocalLocalizeHelper.currentOrThrow
        IScaffold(
            topBar = { scrollBehavior ->
                TitleToolbar(
                    title = localizeHelper.localize(MR.strings.general),
                    scrollBehavior = scrollBehavior,
                    popBackStack = {
                        popBackStack(navigator)
                    }
                )
            }
        ) {scaffoldPadding ->
            GeneralSettingScreen(
                scaffoldPadding = scaffoldPadding,
                vm = vm,
                onTranslationSettingsClick = {
                    navigator.push(TranslationScreenSpec())
                },
            )
        }

    }
}

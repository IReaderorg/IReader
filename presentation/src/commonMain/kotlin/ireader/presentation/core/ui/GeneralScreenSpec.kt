package ireader.presentation.core.ui

import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.NavigationRoutes

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.settings.general.GeneralSettingScreen
import ireader.presentation.ui.settings.general.GeneralSettingScreenViewModel
import ireader.presentation.core.safePopBackStack

@ExperimentalMaterial3Api
class GeneralScreenSpec {

    @Composable
    fun Content() {
        val vm: GeneralSettingScreenViewModel = getIViewModel()
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
        IScaffold(
            topBar = { scrollBehavior ->
                TitleToolbar(
                    title = localizeHelper.localize(Res.string.general),
                    scrollBehavior = scrollBehavior,
                    popBackStack = {
                        navController.safePopBackStack()
                    }
                )
            }
        ) {scaffoldPadding ->
            GeneralSettingScreen(
                scaffoldPadding = scaffoldPadding,
                vm = vm,
                onTranslationSettingsClick = {
                    navController.navigate(NavigationRoutes.translationSettings)
                },
                onJSPluginSettingsClick = {
                    navController.navigate(NavigationRoutes.jsPluginSettings)
                },
            )
        }

    }
}

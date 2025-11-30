package ireader.presentation.core.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ireader.presentation.core.LocalNavigator
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.settings.JSPluginSettingsScreen
import ireader.domain.preferences.prefs.UiPreferences
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

@ExperimentalMaterial3Api
class JSPluginSettingsScreenSpec {

    @Composable
    fun Content() {
        val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val uiPreferences: UiPreferences = getIViewModel()
        
        IScaffold(
            topBar = { scrollBehavior ->
                TitleToolbar(
                    title = localizeHelper.localize(Res.string.javascript_plugin_settings),
                    scrollBehavior = scrollBehavior,
                    popBackStack = {
                        navController.popBackStack()
                    }
                )
            }
        ) { padding ->
            JSPluginSettingsScreen(
                uiPreferences = uiPreferences,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

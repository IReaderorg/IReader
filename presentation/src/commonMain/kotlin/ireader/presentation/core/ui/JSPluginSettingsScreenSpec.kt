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

@ExperimentalMaterial3Api
class JSPluginSettingsScreenSpec {

    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val uiPreferences: UiPreferences = getIViewModel()
        
        IScaffold(
            topBar = { scrollBehavior ->
                TitleToolbar(
                    title = "JavaScript Plugin Settings",
                    scrollBehavior = scrollBehavior,
                    popBackStack = {
                        navController.popBackStack()
                    }
                )
            }
        ) { padding ->
            JSPluginSettingsScreen(
                uiPreferences = uiPreferences,
                repositories = emptyList(), // TODO: Implement repository management
                onRepositoryAdd = { /* TODO */ },
                onRepositoryRemove = { /* TODO */ },
                onRepositoryToggle = { _, _ -> /* TODO */ },
                modifier = Modifier.padding(padding)
            )
        }
    }
}

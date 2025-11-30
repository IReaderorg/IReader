package ireader.presentation.core.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import ireader.presentation.ui.home.sources.settings.BrowseSettingsScreen
import ireader.presentation.ui.home.sources.settings.BrowseSettingsViewModel
import org.koin.compose.koinInject

/**
 * Browse Settings Screen Spec
 * - Multiple language selection for sources
 * - Shows only languages with available sources
 * - Persists selections to BrowsePreferences
 */
class BrowseSettingsScreenSpec {
    
    @Composable
    fun Content() {
        val vm: BrowseSettingsViewModel = koinInject()
        val navigator = ireader.presentation.core.LocalNavigator.current
        val snackbarHostState = remember { SnackbarHostState() }
        
        BrowseSettingsScreen(
            onBackPressed = { navigator?.popBackStack() },
            availableLanguages = vm.state.availableLanguages,
            selectedLanguages = vm.state.selectedLanguages,
            onLanguageToggled = { languageCode ->
                vm.toggleLanguage(languageCode)
            },
            onSelectAll = {
                vm.selectAll()
            },
            onDeselectAll = {
                vm.deselectAll()
            }
        )
    }
}

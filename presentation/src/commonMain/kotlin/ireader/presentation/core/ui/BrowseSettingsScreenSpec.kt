package ireader.presentation.core.ui

import androidx.compose.runtime.*
import cafe.adriel.voyager.core.screen.Screen
import ireader.presentation.ui.home.sources.extension.ExtensionViewModel
import ireader.presentation.ui.home.sources.extension.LanguageChoice
import ireader.presentation.ui.home.sources.settings.BrowseSettingsScreen
import org.koin.compose.koinInject

/**
 * Browse Settings Screen Spec
 * - Multiple language selection for sources
 * - Shows only languages with available sources
 */
class BrowseSettingsScreenSpec : Screen {
    
    @Composable
    override fun Content() {
        val vm: ExtensionViewModel = koinInject()
        val navigator = ireader.presentation.core.LocalNavigator.current
        
        // Get available languages from sources
        val availableLanguages = remember(vm.pinnedCatalogs, vm.unpinnedCatalogs, vm.remoteCatalogs) {
            val allCatalogs = vm.pinnedCatalogs + vm.unpinnedCatalogs + vm.remoteCatalogs
            allCatalogs.mapNotNull { catalog ->
                when (catalog) {
                    is ireader.domain.models.entities.CatalogInstalled -> catalog.source?.lang
                    is ireader.domain.models.entities.CatalogRemote -> catalog.lang
                    else -> null
                }
            }.distinct().sorted()
        }
        
        // Track selected languages
        var selectedLanguages by remember {
            mutableStateOf(
                when (val current = vm.selectedUserSourceLanguage) {
                    is LanguageChoice.All -> availableLanguages.toSet()
                    is LanguageChoice.One -> setOf(current.language.code)
                    is LanguageChoice.Others -> current.languages.map { it.code }.toSet()
                }
            )
        }
        
        // Update VM when selection changes
        LaunchedEffect(selectedLanguages) {
            val choice = when {
                selectedLanguages.isEmpty() -> LanguageChoice.All
                selectedLanguages.size == availableLanguages.size -> LanguageChoice.All
                selectedLanguages.size == 1 -> {
                    val lang = selectedLanguages.first()
                    LanguageChoice.One(ireader.presentation.ui.home.sources.extension.Language(lang))
                }
                else -> {
                    // Multiple languages selected - use Others
                    val languages = selectedLanguages.map { 
                        ireader.presentation.ui.home.sources.extension.Language(it) 
                    }
                    LanguageChoice.Others(languages)
                }
            }
            vm.selectedUserSourceLanguage = choice
            vm.selectedLanguage = choice
        }
        
        BrowseSettingsScreen(
            onBackPressed = { navigator?.popBackStack() },
            availableLanguages = availableLanguages,
            selectedLanguages = selectedLanguages,
            onLanguageToggled = { languageCode ->
                selectedLanguages = if (selectedLanguages.contains(languageCode)) {
                    selectedLanguages - languageCode
                } else {
                    selectedLanguages + languageCode
                }
            },
            onSelectAll = {
                selectedLanguages = availableLanguages.toSet()
            },
            onDeselectAll = {
                selectedLanguages = emptySet()
            }
        )
    }
}

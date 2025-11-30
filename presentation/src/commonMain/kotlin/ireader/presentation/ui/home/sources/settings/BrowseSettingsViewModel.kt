package ireader.presentation.ui.home.sources.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.domain.catalogs.interactor.GetCatalogsByType
import ireader.domain.models.entities.CatalogRemote
import ireader.domain.preferences.prefs.BrowsePreferences
import ireader.i18n.UiText
import ireader.i18n.resources.language_added
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import ireader.presentation.ui.home.sources.extension.ExtensionViewModel
import ireader.presentation.ui.home.sources.extension.Language
import ireader.presentation.ui.home.sources.extension.LanguageChoice
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

class BrowseSettingsViewModel(
    private val browsePreferences: BrowsePreferences,
    private val getCatalogsByType: GetCatalogsByType,
    private val extensionViewModel: ExtensionViewModel
) : BaseViewModel() {

    var state by mutableStateOf(BrowseSettingsState())
        private set

    init {
        // Load initial selected languages from preferences synchronously
        val initialSelectedLanguages = browsePreferences.selectedLanguages().get()
        state = state.copy(selectedLanguages = initialSelectedLanguages)
        
        // Then observe changes
        browsePreferences.selectedLanguages().changes()
            .onEach { languages ->
                state = state.copy(selectedLanguages = languages)
            }
            .launchIn(scope)
        
        // Load available languages from installed extensions
        scope.launch {
            getCatalogsByType.subscribe(excludeRemoteInstalled = false)
                .collect { (pinned, unpinned, remote) ->
                    val allLanguages = buildSet {
                        // Get languages from installed sources
                        pinned.forEach { catalog ->
                            catalog.source?.lang?.let { 
                                add(it)
                                println("BrowseSettings: Added installed language: $it from ${catalog.name}")
                            }
                        }
                        unpinned.forEach { catalog ->
                            catalog.source?.lang?.let { 
                                add(it)
                                println("BrowseSettings: Added installed language: $it from ${catalog.name}")
                            }
                        }
                        // Get languages from remote sources
                        remote.forEach { catalog ->
                            add(catalog.lang)
                            println("BrowseSettings: Added remote language: ${catalog.lang} from ${catalog.name}")
                        }
                    }.sorted()
                    
                    println("BrowseSettings: Total available languages: ${allLanguages.size} - $allLanguages")
                    println("BrowseSettings: Selected languages: ${state.selectedLanguages.size} - ${state.selectedLanguages}")
                    
                    state = state.copy(availableLanguages = allLanguages)
                }
        }
    }

    private fun updateExtensionViewModelLanguages(selectedLanguages: Set<String>) {
        val choice = when {
            selectedLanguages.isEmpty() -> LanguageChoice.All
            selectedLanguages.size == 1 -> {
                LanguageChoice.One(Language(selectedLanguages.first()))
            }
            else -> {
                val languages = selectedLanguages.map { Language(it) }
                LanguageChoice.Others(languages)
            }
        }
        extensionViewModel.selectedUserSourceLanguage = choice
        extensionViewModel.selectedLanguage = choice
    }

    fun toggleLanguage(languageCode: String) {
        scope.launch {
            val current = state.selectedLanguages.toMutableSet()
            if (languageCode in current) {
                current.remove(languageCode)
                // If empty after removal, it means "all languages"
                browsePreferences.selectedLanguages().set(current)
                updateExtensionViewModelLanguages(current)
                showSnackBar(UiText.DynamicString(if (current.isEmpty()) "All languages selected" else "Language removed"))
            } else {
                current.add(languageCode)
                browsePreferences.selectedLanguages().set(current)
                updateExtensionViewModelLanguages(current)
                showSnackBar(UiText.MStringResource(ireader.i18n.resources.Res.string.language_added))
            }
        }
    }

    fun selectAll() {
        scope.launch {
            val allLanguages = state.availableLanguages.toSet()
            browsePreferences.selectedLanguages().set(allLanguages)
            updateExtensionViewModelLanguages(allLanguages)
            showSnackBar(UiText.DynamicString("All languages selected"))
        }
    }

    fun deselectAll() {
        scope.launch {
            // Empty set means "all languages"
            val allLanguages = emptySet<String>()
            browsePreferences.selectedLanguages().set(allLanguages)
            updateExtensionViewModelLanguages(allLanguages)
            showSnackBar(UiText.DynamicString("All languages selected"))
        }
    }
}

data class BrowseSettingsState(
    val availableLanguages: List<String> = emptyList(),
    val selectedLanguages: Set<String> = emptySet() // Empty = all languages
)

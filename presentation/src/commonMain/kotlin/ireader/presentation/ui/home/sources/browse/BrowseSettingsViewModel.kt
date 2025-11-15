package ireader.presentation.ui.home.sources.browse

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.domain.preferences.prefs.BrowsePreferences
import ireader.i18n.UiEvent
import ireader.i18n.UiText
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class BrowseSettingsViewModel(
    private val browsePreferences: BrowsePreferences
) : BaseViewModel() {

    var state by mutableStateOf(BrowseSettingsState())
        private set

    init {
        // Collect preferences
        browsePreferences.concurrentGlobalSearches().changes()
            .onEach { concurrent ->
                state = state.copy(concurrentSearches = concurrent)
            }
            .launchIn(scope)

        browsePreferences.selectedLanguages().changes()
            .onEach { languages ->
                state = state.copy(selectedLanguages = languages)
            }
            .launchIn(scope)

        browsePreferences.searchTimeout().changes()
            .onEach { timeout ->
                state = state.copy(searchTimeout = timeout)
            }
            .launchIn(scope)

        browsePreferences.maxResultsPerSource().changes()
            .onEach { maxResults ->
                state = state.copy(maxResultsPerSource = maxResults)
            }
            .launchIn(scope)
    }

    fun setConcurrentSearches(value: Int) {
        scope.launch {
            browsePreferences.concurrentGlobalSearches().set(value)
            showSettingsUpdatedMessage()
        }
    }

    fun toggleLanguage(languageCode: String) {
        scope.launch {
            val currentLanguages = state.selectedLanguages.toMutableSet()
            if (languageCode in currentLanguages) {
                // Don't allow removing the last language
                if (currentLanguages.size > 1) {
                    currentLanguages.remove(languageCode)
                }
            } else {
                currentLanguages.add(languageCode)
            }
            browsePreferences.selectedLanguages().set(currentLanguages)
            showSettingsUpdatedMessage()
        }
    }

    fun setSearchTimeout(timeout: Long) {
        scope.launch {
            browsePreferences.searchTimeout().set(timeout)
            showSettingsUpdatedMessage()
        }
    }

    fun setMaxResultsPerSource(maxResults: Int) {
        scope.launch {
            browsePreferences.maxResultsPerSource().set(maxResults)
            showSettingsUpdatedMessage()
        }
    }

    fun resetToDefaults() {
        scope.launch {
            browsePreferences.concurrentGlobalSearches().set(3)
            browsePreferences.selectedLanguages().set(setOf("en"))
            browsePreferences.searchTimeout().set(30000L)
            browsePreferences.maxResultsPerSource().set(25)
            showSnackBar(UiText.DynamicString("Settings reset to defaults"))
        }
    }

    private fun showSettingsUpdatedMessage() {
        showSnackBar(UiText.DynamicString("Browse settings updated"))
    }
}

data class BrowseSettingsState(
    val concurrentSearches: Int = 3,
    val selectedLanguages: Set<String> = setOf("en"),
    val searchTimeout: Long = 30000L,
    val maxResultsPerSource: Int = 25
)

package ireader.presentation.ui.settings.recommendations

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.core.prefs.PreferenceStore
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.preferences.prefs.UiPreferences
import ireader.presentation.ui.core.viewmodel.BaseViewModel

class SimilarTitlesSettingsViewModel(
    private val uiPreferences: UiPreferences
) : BaseViewModel() {

    val showSimilarTitles = uiPreferences.showSimilarTitles().stateIn(scope)
    val similarTitlesSource = uiPreferences.similarTitlesSource().stateIn(scope)
    val similarTitlesMatchMode = uiPreferences.similarTitlesMatchMode().stateIn(scope)
    val similarTitlesMaxCount = uiPreferences.similarTitlesMaxCount().stateIn(scope)

    var showSourceDialog by mutableStateOf(false)
        private set
    var showMatchModeDialog by mutableStateOf(false)
        private set
    var showMaxCountDialog by mutableStateOf(false)
        private set

    fun setShowSimilarTitles(enabled: Boolean) {
        uiPreferences.showSimilarTitles().set(enabled)
    }

    fun showSourceSelectionDialog() {
        showSourceDialog = true
    }

    fun dismissSourceDialog() {
        showSourceDialog = false
    }

    fun setSimilarTitlesSource(source: PreferenceValues.SimilarTitlesSource) {
        uiPreferences.similarTitlesSource().set(source)
    }

    fun showMatchModeSelectionDialog() {
        showMatchModeDialog = true
    }

    fun dismissMatchModeDialog() {
        showMatchModeDialog = false
    }

    fun setSimilarTitlesMatchMode(mode: PreferenceValues.SimilarTitlesMatchMode) {
        uiPreferences.similarTitlesMatchMode().set(mode)
    }

    fun showMaxCountDialog() {
        showMaxCountDialog = true
    }

    fun dismissMaxCountDialog() {
        showMaxCountDialog = false
    }

    fun setSimilarTitlesMaxCount(count: Int) {
        uiPreferences.similarTitlesMaxCount().set(count)
    }
}

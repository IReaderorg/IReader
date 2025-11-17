package ireader.presentation.ui.settings.library

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.core.prefs.PreferenceStore
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for the enhanced library settings screen.
 * Manages comprehensive library preferences following Mihon's LibrarySettingsScreenModel.
 */
class SettingsLibraryViewModel(
    private val preferenceStore: PreferenceStore
) : BaseViewModel() {
    
    // Display preferences
    val defaultSort: StateFlow<String> = preferenceStore.getString("library_default_sort", "alphabetical").asStateFlow()
    val defaultSortDirection: StateFlow<Boolean> = preferenceStore.getBoolean("library_sort_ascending", true).asStateFlow()
    val showContinueReadingButton: StateFlow<Boolean> = preferenceStore.getBoolean("show_continue_reading_button", true).asStateFlow()
    
    // Badge preferences
    val showUnreadBadge: StateFlow<Boolean> = preferenceStore.getBoolean("show_unread_badge", true).asStateFlow()
    val showDownloadBadge: StateFlow<Boolean> = preferenceStore.getBoolean("show_download_badge", true).asStateFlow()
    val showLanguageBadge: StateFlow<Boolean> = preferenceStore.getBoolean("show_language_badge", false).asStateFlow()
    val showLocalBadge: StateFlow<Boolean> = preferenceStore.getBoolean("show_local_badge", true).asStateFlow()
    
    // Auto update preferences
    val autoUpdateLibrary: StateFlow<Boolean> = preferenceStore.getBoolean("auto_update_library", false).asStateFlow()
    val autoUpdateInterval: StateFlow<Int> = preferenceStore.getInt("auto_update_interval", 12).asStateFlow()
    val autoUpdateRestrictions: StateFlow<Set<String>> = preferenceStore.getStringSet("auto_update_restrictions", emptySet()).asStateFlow()
    
    // Update filter preferences
    val updateOnlyCompleted: StateFlow<Boolean> = preferenceStore.getBoolean("update_only_completed", false).asStateFlow()
    val updateOnlyNonCompleted: StateFlow<Boolean> = preferenceStore.getBoolean("update_only_non_completed", false).asStateFlow()
    val skipTitlesWithoutChapters: StateFlow<Boolean> = preferenceStore.getBoolean("skip_titles_without_chapters", false).asStateFlow()
    val refreshCoversToo: StateFlow<Boolean> = preferenceStore.getBoolean("refresh_covers_too", false).asStateFlow()
    
    // Dialog states
    var showDefaultSortDialog by mutableStateOf(false)
        private set
    var showUpdateIntervalDialog by mutableStateOf(false)
        private set
    var showUpdateRestrictionsDialog by mutableStateOf(false)
        private set
    
    // Display functions
    fun showDefaultSortDialog() {
        showDefaultSortDialog = true
    }
    
    fun dismissDefaultSortDialog() {
        showDefaultSortDialog = false
    }
    
    fun setDefaultSort(sort: String) {
        preferenceStore.getString("library_default_sort", "alphabetical").set(sort)
    }
    
    fun toggleSortDirection() {
        val current = preferenceStore.getBoolean("library_sort_ascending", true).get()
        preferenceStore.getBoolean("library_sort_ascending", true).set(!current)
    }
    
    fun setShowContinueReadingButton(enabled: Boolean) {
        preferenceStore.getBoolean("show_continue_reading_button", true).set(enabled)
    }
    
    // Badge functions
    fun setShowUnreadBadge(enabled: Boolean) {
        preferenceStore.getBoolean("show_unread_badge", true).set(enabled)
    }
    
    fun setShowDownloadBadge(enabled: Boolean) {
        preferenceStore.getBoolean("show_download_badge", true).set(enabled)
    }
    
    fun setShowLanguageBadge(enabled: Boolean) {
        preferenceStore.getBoolean("show_language_badge", false).set(enabled)
    }
    
    fun setShowLocalBadge(enabled: Boolean) {
        preferenceStore.getBoolean("show_local_badge", true).set(enabled)
    }
    
    // Auto update functions
    fun setAutoUpdateLibrary(enabled: Boolean) {
        preferenceStore.getBoolean("auto_update_library", false).set(enabled)
    }
    
    fun showUpdateIntervalDialog() {
        showUpdateIntervalDialog = true
    }
    
    fun dismissUpdateIntervalDialog() {
        showUpdateIntervalDialog = false
    }
    
    fun setAutoUpdateInterval(interval: Int) {
        preferenceStore.getInt("auto_update_interval", 12).set(interval)
    }
    
    fun showUpdateRestrictionsDialog() {
        showUpdateRestrictionsDialog = true
    }
    
    fun dismissUpdateRestrictionsDialog() {
        showUpdateRestrictionsDialog = false
    }
    
    fun toggleUpdateRestriction(restriction: String) {
        val current = preferenceStore.getStringSet("auto_update_restrictions", emptySet()).get().toMutableSet()
        if (current.contains(restriction)) {
            current.remove(restriction)
        } else {
            current.add(restriction)
        }
        preferenceStore.getStringSet("auto_update_restrictions", emptySet()).set(current)
    }
    
    // Update filter functions
    fun setUpdateOnlyCompleted(enabled: Boolean) {
        preferenceStore.getBoolean("update_only_completed", false).set(enabled)
        if (enabled) {
            // Disable conflicting option
            preferenceStore.getBoolean("update_only_non_completed", false).set(false)
        }
    }
    
    fun setUpdateOnlyNonCompleted(enabled: Boolean) {
        preferenceStore.getBoolean("update_only_non_completed", false).set(enabled)
    }
    
    fun setSkipTitlesWithoutChapters(enabled: Boolean) {
        preferenceStore.getBoolean("skip_titles_without_chapters", false).set(enabled)
    }
    
    fun setRefreshCoversToo(enabled: Boolean) {
        preferenceStore.getBoolean("refresh_covers_too", false).set(enabled)
    }
    
    // Navigation functions
    fun navigateToCategoryManagement() {
        // TODO: Implement navigation to category management screen
    }
    
    fun navigateToLibraryStatistics() {
        // TODO: Implement navigation to library statistics screen
    }
}
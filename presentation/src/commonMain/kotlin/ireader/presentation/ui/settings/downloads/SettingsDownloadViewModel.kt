package ireader.presentation.ui.settings.downloads

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.core.prefs.PreferenceStore
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for the enhanced download settings screen.
 * Manages comprehensive download preferences with category exclusions and storage management.
 */
class SettingsDownloadViewModel(
    private val preferenceStore: PreferenceStore
) : BaseViewModel() {
    
    // Storage preferences
    val downloadLocation: StateFlow<String> = preferenceStore.getString("download_location", "").stateIn(scope)
    
    // Download behavior preferences
    val downloadOnlyOverWifi: StateFlow<Boolean> = preferenceStore.getBoolean("download_only_over_wifi", true).stateIn(scope)

    // Automatic download preferences
    val downloadNewChapters: StateFlow<Boolean> = preferenceStore.getBoolean("download_new_chapters", false).stateIn(scope)
    val downloadNewChaptersCategories: StateFlow<Set<String>> = preferenceStore.getStringSet("download_new_chapters_categories", emptySet()).stateIn(scope)
    
    // Auto delete preferences
    val autoDeleteChapters: StateFlow<Boolean> = preferenceStore.getBoolean("auto_delete_chapters", false).stateIn(scope)
    val removeAfterReading: StateFlow<Boolean> = preferenceStore.getBoolean("remove_after_reading", false).stateIn(scope)
    val removeAfterMarkedAsRead: StateFlow<Boolean> = preferenceStore.getBoolean("remove_after_marked_as_read", false).stateIn(scope)
    val removeExcludeCategories: StateFlow<Set<String>> = preferenceStore.getStringSet("remove_exclude_categories", emptySet()).stateIn(scope)
    
    // File format preferences
    val saveChaptersAsCBZ: StateFlow<Boolean> = preferenceStore.getBoolean("save_chapters_as_cbz", false).stateIn(scope)
    val splitTallImages: StateFlow<Boolean> = preferenceStore.getBoolean("split_tall_images", false).stateIn(scope)
    
    // Dialog states
    var showDownloadLocationDialog by mutableStateOf(false)
        private set

    var showDownloadCategoriesDialog by mutableStateOf(false)
        private set
    var showRemoveExcludeCategoriesDialog by mutableStateOf(false)
        private set
    var showClearCacheDialog by mutableStateOf(false)
        private set
    
    // Storage functions
    fun showDownloadLocationDialog() {
        showDownloadLocationDialog = true
    }
    
    fun dismissDownloadLocationDialog() {
        showDownloadLocationDialog = false
    }
    
    fun selectDownloadLocation() {
        // TODO: Implement folder picker
        // For now, just dismiss the dialog
        dismissDownloadLocationDialog()
    }
    
    fun setDownloadLocation(location: String) {
        preferenceStore.getString("download_location", "").set(location)
    }
    
    // Download behavior functions
    fun setDownloadOnlyOverWifi(enabled: Boolean) {
        preferenceStore.getBoolean("download_only_over_wifi", true).set(enabled)
    }


    // Automatic download functions
    fun setDownloadNewChapters(enabled: Boolean) {
        preferenceStore.getBoolean("download_new_chapters", false).set(enabled)
    }
    
    fun showDownloadCategoriesDialog() {
        showDownloadCategoriesDialog = true
    }
    
    fun dismissDownloadCategoriesDialog() {
        showDownloadCategoriesDialog = false
    }
    
    fun toggleDownloadCategory(category: String) {
        val current = preferenceStore.getStringSet("download_new_chapters_categories", emptySet()).get().toMutableSet()
        if (current.contains(category)) {
            current.remove(category)
        } else {
            current.add(category)
        }
        preferenceStore.getStringSet("download_new_chapters_categories", emptySet()).set(current)
    }
    
    // Auto delete functions
    fun setAutoDeleteChapters(enabled: Boolean) {
        preferenceStore.getBoolean("auto_delete_chapters", false).set(enabled)
    }
    
    fun setRemoveAfterReading(enabled: Boolean) {
        preferenceStore.getBoolean("remove_after_reading", false).set(enabled)
    }
    
    fun setRemoveAfterMarkedAsRead(enabled: Boolean) {
        preferenceStore.getBoolean("remove_after_marked_as_read", false).set(enabled)
    }
    
    fun showRemoveExcludeCategoriesDialog() {
        showRemoveExcludeCategoriesDialog = true
    }
    
    fun dismissRemoveExcludeCategoriesDialog() {
        showRemoveExcludeCategoriesDialog = false
    }
    
    fun toggleRemoveExcludeCategory(category: String) {
        val current = preferenceStore.getStringSet("remove_exclude_categories", emptySet()).get().toMutableSet()
        if (current.contains(category)) {
            current.remove(category)
        } else {
            current.add(category)
        }
        preferenceStore.getStringSet("remove_exclude_categories", emptySet()).set(current)
    }
    
    // File format functions
    fun setSaveChaptersAsCBZ(enabled: Boolean) {
        preferenceStore.getBoolean("save_chapters_as_cbz", false).set(enabled)
    }
    
    fun setSplitTallImages(enabled: Boolean) {
        preferenceStore.getBoolean("split_tall_images", false).set(enabled)
    }
    
    // Advanced functions
    fun showClearCacheDialog() {
        showClearCacheDialog = true
    }
    
    fun dismissClearCacheDialog() {
        showClearCacheDialog = false
    }
    
    fun clearDownloadCache() {
        // TODO: Implement cache clearing logic
    }
    
    // Navigation functions
    fun navigateToStorageUsage() {
        // TODO: Implement navigation to storage usage screen
    }
    
    fun navigateToDownloadQueue() {
        // TODO: Implement navigation to download queue screen
    }
}

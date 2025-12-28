package ireader.presentation.ui.settings.downloads

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.core.log.Log
import ireader.core.prefs.PreferenceStore
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the enhanced download settings screen.
 * Manages comprehensive download preferences with category exclusions and storage management.
 */
class SettingsDownloadViewModel(
    private val preferenceStore: PreferenceStore,
    private val downloadHelper: DownloadHelper = DefaultDownloadHelper()
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
    
    // Download cache size
    private val _downloadCacheSize = MutableStateFlow(0L)
    val downloadCacheSize: StateFlow<Long> = _downloadCacheSize.asStateFlow()
    
    // Snackbar message
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()
    
    // Dialog states
    var showDownloadLocationDialog by mutableStateOf(false)
        private set
    var showDownloadCategoriesDialog by mutableStateOf(false)
        private set
    var showRemoveExcludeCategoriesDialog by mutableStateOf(false)
        private set
    var showClearCacheDialog by mutableStateOf(false)
        private set
    
    // Navigation events
    private val _navigationEvent = MutableStateFlow<DownloadNavigationEvent?>(null)
    val navigationEvent: StateFlow<DownloadNavigationEvent?> = _navigationEvent.asStateFlow()
    
    init {
        refreshDownloadCacheSize()
    }
    
    fun refreshDownloadCacheSize() {
        scope.launch {
            _downloadCacheSize.value = downloadHelper.getDownloadCacheSize()
        }
    }
    
    // Storage functions
    fun showDownloadLocationDialog() {
        showDownloadLocationDialog = true
    }
    
    fun dismissDownloadLocationDialog() {
        showDownloadLocationDialog = false
    }
    
    fun selectDownloadLocation() {
        // Trigger folder picker via navigation event
        _navigationEvent.value = DownloadNavigationEvent.SelectFolder
    }
    
    fun setDownloadLocation(location: String) {
        preferenceStore.getString("download_location", "").set(location)
        showSnackbar("Download location updated")
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
        scope.launch {
            try {
                downloadHelper.clearDownloadCache()
                _downloadCacheSize.value = 0L
                showSnackbar("Download cache cleared")
                Log.info { "Download cache cleared" }
            } catch (e: Exception) {
                Log.error(e, "Failed to clear download cache")
                showSnackbar("Failed to clear download cache: ${e.message}")
            }
        }
    }
    
    // Navigation functions
    fun navigateToStorageUsage() {
        _navigationEvent.value = DownloadNavigationEvent.StorageUsage
    }
    
    fun navigateToDownloadQueue() {
        _navigationEvent.value = DownloadNavigationEvent.DownloadQueue
    }
    
    fun clearNavigationEvent() {
        _navigationEvent.value = null
    }
    
    private fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }
    
    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}

/**
 * Navigation events for download settings
 */
sealed class DownloadNavigationEvent {
    object SelectFolder : DownloadNavigationEvent()
    object StorageUsage : DownloadNavigationEvent()
    object DownloadQueue : DownloadNavigationEvent()
}

/**
 * Interface for platform-specific download operations
 */
interface DownloadHelper {
    fun getDownloadCacheSize(): Long
    fun clearDownloadCache()
}

/**
 * Default implementation for commonMain
 */
class DefaultDownloadHelper : DownloadHelper {
    override fun getDownloadCacheSize(): Long = 0L
    override fun clearDownloadCache() {}
}

package ireader.presentation.ui.settings.library

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.core.prefs.PreferenceStore
import ireader.domain.data.repository.CategoryRepository
import ireader.domain.models.entities.Category
import ireader.domain.preferences.prefs.LibraryPreferences
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the enhanced library settings screen.
 * Manages comprehensive library preferences following Mihon's LibrarySettingsScreenModel.
 */
class SettingsLibraryViewModel(
    private val preferenceStore: PreferenceStore,
    private val libraryPreferences: LibraryPreferences? = null,
    private val categoryRepository: CategoryRepository? = null
) : BaseViewModel() {
    
    // Display preferences
    val defaultSort: StateFlow<String> = preferenceStore.getString("library_default_sort", "alphabetical").stateIn(scope)
    val defaultSortDirection: StateFlow<Boolean> = preferenceStore.getBoolean("library_sort_ascending", true).stateIn(scope)
    val showContinueReadingButton: StateFlow<Boolean> = preferenceStore.getBoolean("show_continue_reading_button", true).stateIn(scope)
    
    // Badge preferences
    val showUnreadBadge: StateFlow<Boolean> = preferenceStore.getBoolean("show_unread_badge", true).stateIn(scope)
    val showDownloadBadge: StateFlow<Boolean> = preferenceStore.getBoolean("show_download_badge", true).stateIn(scope)
    val showLanguageBadge: StateFlow<Boolean> = preferenceStore.getBoolean("show_language_badge", false).stateIn(scope)
    val showLocalBadge: StateFlow<Boolean> = preferenceStore.getBoolean("show_local_badge", true).stateIn(scope)
    
    // Auto update preferences
    val autoUpdateLibrary: StateFlow<Boolean> = preferenceStore.getBoolean("auto_update_library", false).stateIn(scope)
    val autoUpdateInterval: StateFlow<Int> = preferenceStore.getInt("auto_update_interval", 12).stateIn(scope)
    val autoUpdateRestrictions: StateFlow<Set<String>> = preferenceStore.getStringSet("auto_update_restrictions", emptySet()).stateIn(scope)
    
    // Update filter preferences
    val updateOnlyCompleted: StateFlow<Boolean> = preferenceStore.getBoolean("update_only_completed", false).stateIn(scope)
    val updateOnlyNonCompleted: StateFlow<Boolean> = preferenceStore.getBoolean("update_only_non_completed", false).stateIn(scope)
    val skipTitlesWithoutChapters: StateFlow<Boolean> = preferenceStore.getBoolean("skip_titles_without_chapters", false).stateIn(scope)
    val refreshCoversToo: StateFlow<Boolean> = preferenceStore.getBoolean("refresh_covers_too", false).stateIn(scope)
    
    // Default category preference
    private val _defaultCategory = MutableStateFlow<Long>(Category.UNCATEGORIZED_ID)
    val defaultCategory: StateFlow<Long> = _defaultCategory.asStateFlow()
    
    // Available categories for selection
    private val _availableCategories = MutableStateFlow<List<Category>>(emptyList())
    val availableCategories: StateFlow<List<Category>> = _availableCategories.asStateFlow()
    
    // Dialog states
    var showDefaultSortDialog by mutableStateOf(false)
        private set
    var showUpdateIntervalDialog by mutableStateOf(false)
        private set
    var showUpdateRestrictionsDialog by mutableStateOf(false)
        private set
    var showDefaultCategoryDialog by mutableStateOf(false)
        private set
    
    init {
        loadDefaultCategory()
        loadCategories()
    }
    
    private fun loadDefaultCategory() {
        scope.launch {
            val categoryId = libraryPreferences?.defaultCategory()?.get() ?: Category.UNCATEGORIZED_ID
            _defaultCategory.value = categoryId
        }
    }
    
    private fun loadCategories() {
        scope.launch {
            val categories = categoryRepository?.getAll()?.filter { it.id != Category.ALL_ID } ?: emptyList()
            _availableCategories.value = categories
        }
    }
    
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
    
    // Default category functions
    fun showDefaultCategoryDialog() {
        loadCategories() // Refresh categories list
        showDefaultCategoryDialog = true
    }
    
    fun dismissDefaultCategoryDialog() {
        showDefaultCategoryDialog = false
    }
    
    fun setDefaultCategory(categoryId: Long) {
        scope.launch {
            libraryPreferences?.defaultCategory()?.set(categoryId)
            _defaultCategory.value = categoryId
        }
    }
    
    /**
     * Get display name for a category ID.
     * Special handling for system categories.
     */
    fun getCategoryDisplayName(categoryId: Long): String {
        return when (categoryId) {
            Category.UNCATEGORIZED_ID -> "Always ask"
            Category.ALL_ID -> "Default (no category)"
            else -> _availableCategories.value.find { it.id == categoryId }?.name ?: "Unknown"
        }
    }
    
    // Navigation functions
    private val _navigationEvent = MutableStateFlow<LibraryNavigationEvent?>(null)
    val navigationEvent: StateFlow<LibraryNavigationEvent?> = _navigationEvent.asStateFlow()
    
    fun navigateToCategoryManagement() {
        _navigationEvent.value = LibraryNavigationEvent.CategoryManagement
    }
    
    fun navigateToLibraryStatistics() {
        _navigationEvent.value = LibraryNavigationEvent.LibraryStatistics
    }
    
    fun clearNavigationEvent() {
        _navigationEvent.value = null
    }
}

/**
 * Navigation events for library settings
 */
sealed class LibraryNavigationEvent {
    object CategoryManagement : LibraryNavigationEvent()
    object LibraryStatistics : LibraryNavigationEvent()
}

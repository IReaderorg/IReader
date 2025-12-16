package ireader.presentation.ui.sourcecreator

import ireader.domain.catalogs.CatalogStore
import ireader.domain.usersource.interactor.*
import ireader.domain.usersource.model.UserSource
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * ViewModel for the Source Creator screen.
 */
class SourceCreatorViewModel(
    private val getUserSource: GetUserSource,
    private val saveUserSource: SaveUserSource,
    private val validateUserSource: ValidateUserSource,
    private val importExportUserSources: ImportExportUserSources,
    private val catalogStore: CatalogStore
) : BaseViewModel() {
    
    private val _state = MutableStateFlow(SourceCreatorState())
    val state: StateFlow<SourceCreatorState> = _state.asStateFlow()
    
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }
    
    /**
     * Load existing source for editing.
     */
    fun loadSource(sourceUrl: String) {
        scope.launch {
            _state.update { it.copy(isLoading = true) }
            
            val source = getUserSource.byUrl(sourceUrl)
            if (source != null) {
                _state.value = SourceCreatorState.fromUserSource(source)
            }
            
            _state.update { it.copy(isLoading = false) }
        }
    }
    
    /**
     * Load source from JSON string.
     */
    fun loadFromJson(jsonString: String) {
        try {
            val source = json.decodeFromString<UserSource>(jsonString)
            _state.value = SourceCreatorState.fromUserSource(source)
            _state.update { it.copy(snackbarMessage = "Source loaded from JSON") }
        } catch (e: Exception) {
            _state.update { it.copy(snackbarMessage = "Invalid JSON: ${e.message}") }
        }
    }
    
    /**
     * Save the current source.
     */
    fun save() {
        val source = _state.value.toUserSource()
        
        // Validate
        val validation = validateUserSource.validate(source)
        if (!validation.isValid) {
            _state.update { it.copy(validationErrors = validation.errors) }
            return
        }
        
        scope.launch {
            _state.update { it.copy(isSaving = true, validationErrors = emptyList()) }
            
            try {
                saveUserSource.await(source)
                // Refresh the catalog store so sources appear immediately
                catalogStore.refreshUserSources()
                _state.update { it.copy(isSaving = false, snackbarMessage = "Source saved successfully") }
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, snackbarMessage = "Error saving: ${e.message}") }
            }
        }
    }
    
    /**
     * Export current source to JSON.
     */
    fun exportToJson(): String {
        val source = _state.value.toUserSource()
        return json.encodeToString(source)
    }
    
    /**
     * Show JSON dialog with current source.
     */
    fun showJsonDialog() {
        val jsonContent = exportToJson()
        _state.update { it.copy(showJsonDialog = true, jsonContent = jsonContent) }
    }
    
    /**
     * Hide JSON dialog.
     */
    fun hideJsonDialog() {
        _state.update { it.copy(showJsonDialog = false) }
    }
    
    /**
     * Import from JSON in dialog.
     */
    fun importFromJsonDialog(jsonString: String) {
        loadFromJson(jsonString)
        hideJsonDialog()
    }
    
    /**
     * Clear snackbar message.
     */
    fun clearSnackbar() {
        _state.update { it.copy(snackbarMessage = null) }
    }
    
    /**
     * Clear validation errors.
     */
    fun clearErrors() {
        _state.update { it.copy(validationErrors = emptyList()) }
    }
    
    /**
     * Set current tab.
     */
    fun setTab(tab: Int) {
        _state.update { it.copy(currentTab = tab) }
    }
    
    // ==================== Field Updates ====================
    
    fun updateSourceName(value: String) = _state.update { it.copy(sourceName = value) }
    fun updateSourceUrl(value: String) = _state.update { it.copy(sourceUrl = value) }
    fun updateSourceGroup(value: String) = _state.update { it.copy(sourceGroup = value) }
    fun updateLang(value: String) = _state.update { it.copy(lang = value) }
    fun updateComment(value: String) = _state.update { it.copy(comment = value) }
    fun updateEnabled(value: Boolean) = _state.update { it.copy(enabled = value) }
    fun updateHeader(value: String) = _state.update { it.copy(header = value) }
    
    fun updateSearchUrl(value: String) = _state.update { it.copy(searchUrl = value) }
    fun updateExploreUrl(value: String) = _state.update { it.copy(exploreUrl = value) }
    
    fun updateSearchBookList(value: String) = _state.update { it.copy(searchBookList = value) }
    fun updateSearchName(value: String) = _state.update { it.copy(searchName = value) }
    fun updateSearchAuthor(value: String) = _state.update { it.copy(searchAuthor = value) }
    fun updateSearchIntro(value: String) = _state.update { it.copy(searchIntro = value) }
    fun updateSearchBookUrl(value: String) = _state.update { it.copy(searchBookUrl = value) }
    fun updateSearchCoverUrl(value: String) = _state.update { it.copy(searchCoverUrl = value) }
    fun updateSearchKind(value: String) = _state.update { it.copy(searchKind = value) }
    
    fun updateBookInfoName(value: String) = _state.update { it.copy(bookInfoName = value) }
    fun updateBookInfoAuthor(value: String) = _state.update { it.copy(bookInfoAuthor = value) }
    fun updateBookInfoIntro(value: String) = _state.update { it.copy(bookInfoIntro = value) }
    fun updateBookInfoCoverUrl(value: String) = _state.update { it.copy(bookInfoCoverUrl = value) }
    fun updateBookInfoKind(value: String) = _state.update { it.copy(bookInfoKind = value) }
    fun updateBookInfoTocUrl(value: String) = _state.update { it.copy(bookInfoTocUrl = value) }
    
    fun updateTocChapterList(value: String) = _state.update { it.copy(tocChapterList = value) }
    fun updateTocChapterName(value: String) = _state.update { it.copy(tocChapterName = value) }
    fun updateTocChapterUrl(value: String) = _state.update { it.copy(tocChapterUrl = value) }
    fun updateTocNextUrl(value: String) = _state.update { it.copy(tocNextUrl = value) }
    fun updateTocIsReverse(value: Boolean) = _state.update { it.copy(tocIsReverse = value) }
    
    fun updateContentSelector(value: String) = _state.update { it.copy(contentSelector = value) }
    fun updateContentNextUrl(value: String) = _state.update { it.copy(contentNextUrl = value) }
    fun updateContentPurify(value: String) = _state.update { it.copy(contentPurify = value) }
    fun updateContentReplaceRegex(value: String) = _state.update { it.copy(contentReplaceRegex = value) }
    
    fun updateExploreBookList(value: String) = _state.update { it.copy(exploreBookList = value) }
    fun updateExploreName(value: String) = _state.update { it.copy(exploreName = value) }
    fun updateExploreAuthor(value: String) = _state.update { it.copy(exploreAuthor = value) }
    fun updateExploreBookUrl(value: String) = _state.update { it.copy(exploreBookUrl = value) }
    fun updateExploreCoverUrl(value: String) = _state.update { it.copy(exploreCoverUrl = value) }
}

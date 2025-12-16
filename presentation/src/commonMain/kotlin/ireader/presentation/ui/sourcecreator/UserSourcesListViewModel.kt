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
 * ViewModel for the user sources list screen.
 */
class UserSourcesListViewModel(
    private val getUserSources: GetUserSources,
    private val deleteUserSource: DeleteUserSource,
    private val toggleUserSourceEnabled: ToggleUserSourceEnabled,
    private val importExportUserSources: ImportExportUserSources,
    private val catalogStore: CatalogStore
) : BaseViewModel() {
    
    private val _state = MutableStateFlow(UserSourcesListState())
    val state: StateFlow<UserSourcesListState> = _state.asStateFlow()
    
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }
    
    init {
        loadSources()
    }
    
    private fun loadSources() {
        scope.launch {
            _state.update { it.copy(isLoading = true) }
            
            getUserSources.subscribe().collect { sources ->
                _state.update { it.copy(sources = sources, isLoading = false) }
            }
        }
    }
    
    fun toggleEnabled(sourceUrl: String, enabled: Boolean) {
        scope.launch {
            toggleUserSourceEnabled.await(sourceUrl, enabled)
            // Refresh the catalog store so changes appear immediately
            catalogStore.refreshUserSources()
        }
    }
    
    fun showDeleteConfirm(source: UserSource) {
        _state.update { it.copy(showDeleteConfirmDialog = true, sourceToDelete = source) }
    }
    
    fun cancelDelete() {
        _state.update { it.copy(showDeleteConfirmDialog = false, sourceToDelete = null) }
    }
    
    fun confirmDelete() {
        val source = _state.value.sourceToDelete ?: return
        
        scope.launch {
            deleteUserSource.byUrl(source.sourceUrl)
            // Refresh the catalog store so changes appear immediately
            catalogStore.refreshUserSources()
            _state.update { 
                it.copy(
                    showDeleteConfirmDialog = false, 
                    sourceToDelete = null,
                    snackbarMessage = "Source deleted"
                ) 
            }
        }
    }
    
    fun importFromJson(jsonString: String) {
        scope.launch {
            val result = importExportUserSources.importFromJson(jsonString)
            result.fold(
                onSuccess = { count ->
                    // Refresh the catalog store so sources appear immediately
                    catalogStore.refreshUserSources()
                    _state.update { it.copy(snackbarMessage = "Imported $count source(s)") }
                },
                onFailure = { error ->
                    _state.update { it.copy(snackbarMessage = "Import failed: ${error.message}") }
                }
            )
        }
    }
    
    fun exportAll() {
        scope.launch {
            val jsonString = importExportUserSources.exportToJson()
            _state.update { it.copy(shareJson = jsonString) }
        }
    }
    
    fun shareSource(source: UserSource) {
        val jsonString = json.encodeToString(source)
        _state.update { it.copy(shareJson = jsonString) }
    }
    
    fun clearShareJson() {
        _state.update { it.copy(shareJson = null) }
    }
    
    fun showImportDialog() {
        _state.update { it.copy(showImportDialog = true) }
    }
    
    fun hideImportDialog() {
        _state.update { it.copy(showImportDialog = false) }
    }
    
    fun showHelpDialog() {
        _state.update { it.copy(showHelpDialog = true) }
    }
    
    fun hideHelpDialog() {
        _state.update { it.copy(showHelpDialog = false) }
    }
    
    fun clearSnackbar() {
        _state.update { it.copy(snackbarMessage = null) }
    }
    
    fun toggleCreateOptions() {
        _state.update { it.copy(showCreateOptions = !it.showCreateOptions) }
    }
    
    fun showDeleteAllConfirm() {
        _state.update { it.copy(showDeleteAllConfirmDialog = true) }
    }
    
    fun cancelDeleteAll() {
        _state.update { it.copy(showDeleteAllConfirmDialog = false) }
    }
    
    fun confirmDeleteAll() {
        scope.launch {
            val count = _state.value.sources.size
            deleteUserSource.all()
            // Refresh the catalog store so changes appear immediately
            catalogStore.refreshUserSources()
            _state.update { 
                it.copy(
                    showDeleteAllConfirmDialog = false,
                    snackbarMessage = "Deleted $count source(s)"
                ) 
            }
        }
    }
}

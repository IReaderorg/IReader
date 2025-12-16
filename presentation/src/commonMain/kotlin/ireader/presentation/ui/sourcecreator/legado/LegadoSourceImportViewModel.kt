package ireader.presentation.ui.sourcecreator.legado

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import ireader.domain.catalogs.CatalogStore
import ireader.domain.usersource.importer.SourceImporter
import ireader.domain.usersource.interactor.SaveUserSource
import ireader.domain.usersource.model.UserSource
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * State for the Legado source import screen.
 */
data class LegadoSourceImportState(
    val sourceUrl: String = "",
    val jsonContent: String = "",
    val isLoading: Boolean = false,
    val parsedSources: List<UserSource> = emptyList(),
    val selectedSources: Set<String> = emptySet(),
    val snackbarMessage: String? = null,
    val importSuccess: Boolean = false,
    val showJsonInput: Boolean = false,
    val errorMessage: String? = null,
    // Popular Legado source repositories
    val popularRepositories: List<LegadoRepository> = defaultRepositories
)

/**
 * Represents a popular Legado source repository.
 */
data class LegadoRepository(
    val name: String,
    val description: String,
    val url: String,
    val language: String = "zh"
)

private val defaultRepositories = listOf(
    LegadoRepository(
        name = "源仓库",
        description = "Popular Chinese novel sources",
        url = "https://raw.githubusercontent.com/shidahuilang/shuyuan/shuyuan/good.json",
        language = "zh"
    ),
    LegadoRepository(
        name = "阅读书源",
        description = "Community maintained sources",
        url = "https://raw.githubusercontent.com/XIU2/Yuedu/master/shuyuan",
        language = "zh"
    )
)

/**
 * ViewModel for importing Legado/阅读 format sources.
 */
class LegadoSourceImportViewModel(
    private val httpClient: HttpClient,
    private val saveUserSource: SaveUserSource,
    private val catalogStore: CatalogStore,
    private val sourceImporter: SourceImporter = SourceImporter()
) : BaseViewModel() {
    
    private val _state = MutableStateFlow(LegadoSourceImportState())
    val state: StateFlow<LegadoSourceImportState> = _state.asStateFlow()

    
    fun updateSourceUrl(url: String) {
        _state.update { it.copy(sourceUrl = url, errorMessage = null) }
    }
    
    fun updateJsonContent(json: String) {
        _state.update { it.copy(jsonContent = json, errorMessage = null) }
    }
    
    fun toggleJsonInput() {
        _state.update { it.copy(showJsonInput = !it.showJsonInput) }
    }
    
    fun fetchFromUrl() {
        val url = _state.value.sourceUrl.trim()
        if (url.isBlank()) {
            _state.update { it.copy(errorMessage = "Please enter a URL") }
            return
        }
        
        scope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val resolvedUrl = sourceImporter.parseImportUrl(url) ?: url
                val response = httpClient.get(resolvedUrl)
                val json = response.bodyAsText()
                parseJson(json)
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = "Failed to fetch: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    fun fetchFromRepository(repository: LegadoRepository) {
        scope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null, sourceUrl = repository.url) }
            
            try {
                val response = httpClient.get(repository.url)
                val json = response.bodyAsText()
                parseJson(json)
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = "Failed to fetch from ${repository.name}: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    fun parseFromJson() {
        val json = _state.value.jsonContent.trim()
        if (json.isBlank()) {
            _state.update { it.copy(errorMessage = "Please enter JSON content") }
            return
        }
        parseJson(json)
    }
    
    private fun parseJson(json: String) {
        scope.launch {
            _state.update { it.copy(isLoading = true) }
            
            when (val result = sourceImporter.importFromJson(json)) {
                is SourceImporter.ImportResult.Success -> {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            parsedSources = result.sources,
                            selectedSources = result.sources.map { s -> s.sourceUrl }.toSet(),
                            errorMessage = null
                        ) 
                    }
                }
                is SourceImporter.ImportResult.Error -> {
                    _state.update { 
                        it.copy(
                            isLoading = false, 
                            errorMessage = "${result.message}${result.details?.let { ": $it" } ?: ""}"
                        ) 
                    }
                }
            }
        }
    }
    
    fun toggleSourceSelection(sourceUrl: String) {
        _state.update { state ->
            val newSelection = if (sourceUrl in state.selectedSources) {
                state.selectedSources - sourceUrl
            } else {
                state.selectedSources + sourceUrl
            }
            state.copy(selectedSources = newSelection)
        }
    }
    
    fun selectAll() {
        _state.update { state ->
            state.copy(selectedSources = state.parsedSources.map { it.sourceUrl }.toSet())
        }
    }
    
    fun deselectAll() {
        _state.update { it.copy(selectedSources = emptySet()) }
    }
    
    fun importSelected() {
        val selectedUrls = _state.value.selectedSources
        val sourcesToImport = _state.value.parsedSources.filter { it.sourceUrl in selectedUrls }
        
        if (sourcesToImport.isEmpty()) {
            _state.update { it.copy(snackbarMessage = "No sources selected") }
            return
        }
        
        scope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                saveUserSource.awaitAll(sourcesToImport)
                // Refresh the catalog store so sources appear immediately
                catalogStore.refreshUserSources()
                _state.update { 
                    it.copy(
                        isLoading = false,
                        importSuccess = true,
                        snackbarMessage = "Imported ${sourcesToImport.size} source(s)"
                    ) 
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false, 
                        snackbarMessage = "Import failed: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    fun clearParsedSources() {
        _state.update { 
            it.copy(
                parsedSources = emptyList(), 
                selectedSources = emptySet(),
                importSuccess = false
            ) 
        }
    }
    
    fun clearSnackbar() {
        _state.update { it.copy(snackbarMessage = null) }
    }
}

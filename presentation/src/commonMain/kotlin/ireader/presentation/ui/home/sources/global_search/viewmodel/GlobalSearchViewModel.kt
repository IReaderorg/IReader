package ireader.presentation.ui.home.sources.global_search.viewmodel

import androidx.compose.runtime.Stable
import ireader.core.log.Log
import ireader.core.source.CatalogSource
import ireader.domain.catalogs.interactor.GetInstalledCatalog
import ireader.domain.catalogs.interactor.GetLocalCatalogs
import ireader.domain.models.entities.Book
import ireader.domain.usecases.remote.RemoteUseCases
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Global Search screen following Mihon's StateScreenModel pattern.
 * 
 * Uses a single immutable StateFlow<GlobalSearchScreenState> instead of multiple mutableStateOf.
 * This provides:
 * - Single source of truth for UI state
 * - Atomic state updates
 * - Better Compose performance with @Immutable state
 */
@Stable
class GlobalSearchViewModel(
    private val catalogStore: GetLocalCatalogs,
    val insertUseCases: ireader.domain.usecases.local.LocalInsertUseCases,
    val getInstalledCatalog: GetInstalledCatalog,
    private val remoteUseCases: RemoteUseCases,
    val param: Param
) : ireader.presentation.ui.core.viewmodel.BaseViewModel() {
    
    data class Param(val query: String?)

    private val _screenState = MutableStateFlow(GlobalSearchScreenState())
    val screenState: StateFlow<GlobalSearchScreenState> = _screenState.asStateFlow()

    private var searchJob: Job? = null

    // Expose query for external access
    var query: String
        get() = _screenState.value.query
        set(value) { _screenState.update { it.copy(query = value) } }

    init {
        val initialQuery = param.query
        if (!initialQuery.isNullOrBlank()) {
            _screenState.update { it.copy(query = initialQuery) }
            searchBooks(initialQuery)
        }
    }

    fun searchBooks(query: String) {
        if (query.isBlank()) {
            return
        }

        // Cancel previous search
        searchJob?.cancel()

        _screenState.update { current ->
            current.copy(
                query = query,
                numberOfTries = current.numberOfTries + 1,
                inProgress = emptyList(),
                noResult = emptyList(),
                withResult = emptyList(),
                isLoading = true,
                error = null
            )
        }

        searchJob = scope.launch {
            try {
                val catalogs = getInstalledCatalog.get()
                _screenState.update { it.copy(installedCatalogs = catalogs) }
                
                val sources = catalogs.mapNotNull { it.source }.filterIsInstance<CatalogSource>()

                if (sources.isEmpty()) {
                    Log.warn { "No catalogs available for search" }
                    _screenState.update { it.copy(isLoading = false) }
                    return@launch
                }

                // Mark all sources as loading initially
                sources.forEach { source ->
                    handleSearchItems(SearchItem(source), loading = true)
                }

                // Use the new GlobalSearchUseCase with Flow
                remoteUseCases.globalSearch.asFlow(query)
                    .onStart {
                        Log.debug { "Starting global search for query: $query with ${sources.size} sources" }
                    }
                    .catch { e ->
                        if (e is CancellationException) {
                            Log.debug { "Global search cancelled for query: $query" }
                            throw e // Re-throw to properly cancel the coroutine
                        } else {
                            Log.error(e, "Error during global search flow")
                            // Mark all in-progress sources as having no results on error
                            _screenState.value.inProgress.forEach { item ->
                                handleSearchItems(item, loading = false)
                            }
                            _screenState.update { it.copy(error = e.message) }
                        }
                    }
                    .collect { globalResult ->
                        Log.debug { "Received global search result with ${globalResult.sourceResults.size} source results" }
                        
                        // Set loading to false when first result comes in
                        if (_screenState.value.isLoading) {
                            _screenState.update { it.copy(isLoading = false) }
                            Log.debug { "First search result received, setting isLoading = false" }
                        }
                        
                        // Process each source result
                        globalResult.sourceResults.forEach { sourceResult ->
                            val source = sources.find { it.id == sourceResult.sourceId }
                            if (source != null) {
                                // Check if this source is still loading - if so, keep it in inProgress
                                if (sourceResult.isLoading) {
                                    // Source is still loading, ensure it's in inProgress list
                                    val searchItem = SearchItem(source = source, items = emptyList())
                                    handleSearchItems(searchItem, loading = true)
                                    return@forEach
                                }
                                
                                try {
                                    // Convert SearchResultItem to Book
                                    val books = sourceResult.results.map { item ->
                                        Book(
                                            id = item.bookId ?: 0,
                                            sourceId = sourceResult.sourceId,
                                            title = item.title,
                                            key = item.key,
                                            author = item.author,
                                            description = item.description,
                                            genres = item.genres,
                                            cover = item.cover,
                                            favorite = item.inLibrary
                                        )
                                    }
                                    
                                    val searchItem = SearchItem(
                                        source = source,
                                        items = books
                                    )
                                    handleSearchItems(searchItem, loading = false)
                                    
                                    Log.debug { "Processed ${books.size} results from ${source.name}" }
                                } catch (e: Exception) {
                                    if (e !is CancellationException) {
                                        Log.error(e, "Error processing results from source ${source.name}")
                                        // Mark this source as having no results
                                        handleSearchItems(SearchItem(source, emptyList()), loading = false)
                                    } else {
                                        throw e
                                    }
                                }
                            } else {
                                Log.warn { "Source not found for ID: ${sourceResult.sourceId}" }
                            }
                        }
                    }
            } catch (e: CancellationException) {
                Log.debug { "Global search cancelled" }
                _screenState.update { it.copy(isLoading = false) }
                // Don't log cancellation as error, it's expected behavior
                throw e
            } catch (e: Exception) {
                Log.error(e, "Error during global search initialization")
                _screenState.update { it.copy(isLoading = false, error = e.message) }
                // Clear loading states on error
                _screenState.value.inProgress.forEach { item ->
                    handleSearchItems(item, loading = false)
                }
            }
        }
    }

    private fun handleSearchItems(searchItem: SearchItem, loading: Boolean = false) {
        _screenState.update { current ->
            if (loading) {
                // Remove from other lists first to avoid duplicates
                current.copy(
                    withResult = current.withResult.filter { it.source.id != searchItem.source.id },
                    noResult = current.noResult.filter { it.source.id != searchItem.source.id },
                    inProgress = current.inProgress.filter { it.source.id != searchItem.source.id } + searchItem
                )
            } else {
                // Remove from all lists first
                val filteredInProgress = current.inProgress.filter { it.source.id != searchItem.source.id }
                val filteredWithResult = current.withResult.filter { it.source.id != searchItem.source.id }
                val filteredNoResult = current.noResult.filter { it.source.id != searchItem.source.id }
                
                // Add to appropriate list
                if (searchItem.items.isEmpty()) {
                    current.copy(
                        inProgress = filteredInProgress,
                        withResult = filteredWithResult,
                        noResult = filteredNoResult + searchItem
                    )
                } else {
                    current.copy(
                        inProgress = filteredInProgress,
                        withResult = filteredWithResult + searchItem,
                        noResult = filteredNoResult
                    )
                }
            }
        }
    }
    
    fun clearError() {
        _screenState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
    }
}

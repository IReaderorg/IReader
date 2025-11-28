package ireader.presentation.ui.home.sources.global_search.viewmodel
import ireader.domain.models.entities.Book

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.core.log.Log
import ireader.core.source.CatalogSource
import ireader.domain.catalogs.interactor.GetInstalledCatalog
import ireader.domain.catalogs.interactor.GetLocalCatalogs
import ireader.domain.models.entities.CatalogInstalled
import ireader.domain.usecases.local.LocalInsertUseCases
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.presentation.ui.home.explore.global_search.SearchResult
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart



class GlobalSearchViewModel(
        private val state: GlobalSearchStateImpl,
        private val catalogStore: GetLocalCatalogs,
        val insertUseCases: ireader.domain.usecases.local.LocalInsertUseCases,
        val getInstalledCatalog: GetInstalledCatalog,
        private val remoteUseCases: RemoteUseCases,
        val param: Param
) : ireader.presentation.ui.core.viewmodel.BaseViewModel(), GlobalSearchState by state {
    data class Param(val query: String?)

    var installedCatalogs by mutableStateOf(emptyList<CatalogInstalled>())

    var inProgress by mutableStateOf(emptyList<SearchItem>())
    var noResult by mutableStateOf(emptyList<SearchItem>())
    var withResult by mutableStateOf(emptyList<SearchItem>())
    var numberOfTries by mutableStateOf(0)

    private var searchJob: Job? = null

    init {
        val query = param.query
        if (!query.isNullOrBlank()) {
            this.query = query
            searchBooks(query)
        }
    }

    fun searchBooks(query: String) {
        if (query.isBlank()) {
            return
        }

        // Cancel previous search
        searchJob?.cancel()

        numberOfTries++
        inProgress = emptyList()
        noResult = emptyList()
        withResult = emptyList()
        
        // Set loading to true when search starts
        isLoading = true

        searchJob = scope.launch {
            try {
                installedCatalogs = getInstalledCatalog.get()
                val catalogs = installedCatalogs.mapNotNull { it.source }.filterIsInstance<CatalogSource>()

                if (catalogs.isEmpty()) {
                    Log.warn { "No catalogs available for search" }
                    isLoading = false
                    return@launch
                }

                // Mark all sources as loading initially
                catalogs.forEach { source ->
                    SearchItem(source).handleSearchItems(true)
                }

                // Use the new GlobalSearchUseCase with Flow
                remoteUseCases.globalSearch.asFlow(query)
                    .onStart {
                        Log.debug { "Starting global search for query: $query with ${catalogs.size} sources" }
                    }
                    .catch { e ->
                        if (e is CancellationException) {
                            Log.debug { "Global search cancelled for query: $query" }
                            throw e // Re-throw to properly cancel the coroutine
                        } else {
                            Log.error(e, "Error during global search flow")
                            // Mark all in-progress sources as having no results on error
                            inProgress.forEach { item ->
                                item.handleSearchItems(loading = false)
                            }
                        }
                    }
                    .collect { globalResult ->
                        Log.debug { "Received global search result with ${globalResult.sourceResults.size} source results" }
                        
                        // Set loading to false when first result comes in
                        if (isLoading) {
                            isLoading = false
                            Log.debug { "First search result received, setting isLoading = false" }
                        }
                        
                        // Process each source result
                        globalResult.sourceResults.forEach { sourceResult ->
                            val source = catalogs.find { it.id == sourceResult.sourceId }
                            if (source != null) {
                                // Check if this source is still loading - if so, keep it in inProgress
                                if (sourceResult.isLoading) {
                                    // Source is still loading, ensure it's in inProgress list
                                    val searchItem = SearchItem(source = source, items = emptyList())
                                    searchItem.handleSearchItems(loading = true)
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
                                    searchItem.handleSearchItems(loading = false)
                                    
                                    Log.debug { "Processed ${books.size} results from ${source.name}" }
                                } catch (e: Exception) {
                                    if (e !is CancellationException) {
                                        Log.error(e, "Error processing results from source ${source.name}")
                                        // Mark this source as having no results
                                        SearchItem(source, emptyList()).handleSearchItems(loading = false)
                                    } else {
                                        throw e
                                    }
                                }
                            } else {
                                Log.warn { "Source not found for ID: ${sourceResult.sourceId}" }
                            }
                        }
                        
                        // Note: Don't mark remaining in-progress sources as complete here
                        // The flow will emit results for each source as they complete
                        // Sources that haven't returned yet should stay in inProgress
                    }
            } catch (e: CancellationException) {
                Log.debug { "Global search cancelled" }
                isLoading = false
                // Don't log cancellation as error, it's expected behavior
                throw e
            } catch (e: Exception) {
                Log.error(e, "Error during global search initialization")
                isLoading = false
                // Clear loading states on error
                inProgress.forEach { item ->
                    item.handleSearchItems(loading = false)
                }
            }
        }
    }

    private fun SearchItem.handleSearchItems(loading: Boolean = false) {
        if (loading) {
            // Remove from other lists first to avoid duplicates
            withResult = withResult.filter { it.source.id != this.source.id }
            noResult = noResult.filter { it.source.id != this.source.id }
            inProgress = inProgress.filter { it.source.id != this.source.id } + this
            return
        }
        
        // Remove from all lists first
        inProgress = inProgress.filter { it.source.id != this.source.id }
        withResult = withResult.filter { it.source.id != this.source.id }
        noResult = noResult.filter { it.source.id != this.source.id }
        
        // Add to appropriate list
        when {
            this.items.isEmpty() -> {
                noResult = noResult + this
            }
            this.items.isNotEmpty() -> {
                withResult = withResult + this
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
    }
}

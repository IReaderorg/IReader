package ireader.presentation.ui.home.sources.global_search.viewmodel

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
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
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

        searchJob = scope.launch {
            try {
                installedCatalogs = getInstalledCatalog.get()
                val catalogs = installedCatalogs.mapNotNull { it.source }.filterIsInstance<CatalogSource>()

                // Mark all sources as loading initially
                catalogs.forEach { source ->
                    SearchItem(source).handleSearchItems(true)
                }

                // Use the new GlobalSearchUseCase
                remoteUseCases.globalSearch(query)
                    .onStart {
                        Log.debug { "Starting global search for query: $query" }
                    }
                    .catch { e ->
                        Log.error(e, "Error during global search")
                    }
                    .collect { result ->
                        // Find the source for this result
                        val source = catalogs.find { it.id == result.sourceId }
                        if (source != null) {
                            val searchItem = SearchItem(
                                source = source,
                                items = result.books
                            )
                            searchItem.handleSearchItems(loading = false)
                        }
                    }
            } catch (e: Exception) {
                Log.error(e, "Error during global search")
            }
        }
    }

    private fun SearchItem.handleSearchItems(loading: Boolean = false) {
        if (loading) {
            inProgress = inProgress + this
            return
        }
        inProgress = inProgress - inProgress.filter { it.source.id == this.source.id }.toSet()
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

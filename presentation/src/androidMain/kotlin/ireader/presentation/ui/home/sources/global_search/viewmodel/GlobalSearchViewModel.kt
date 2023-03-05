package ireader.presentation.ui.home.sources.global_search.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.core.source.CatalogSource
import ireader.core.source.model.Filter
import ireader.domain.catalogs.interactor.GetInstalledCatalog
import ireader.domain.catalogs.interactor.GetLocalCatalogs
import ireader.domain.models.entities.CatalogInstalled
import ireader.domain.models.entities.toBook
import ireader.domain.usecases.local.LocalInsertUseCases
import ireader.domain.utils.extensions.replace

import kotlinx.coroutines.*



class GlobalSearchViewModel(
    private val state: GlobalSearchStateImpl,
    private val catalogStore: GetLocalCatalogs,
    val insertUseCases: LocalInsertUseCases,
    val getInstalledCatalog: GetInstalledCatalog,
    val param: Param
) : ireader.presentation.ui.core.viewmodel.BaseViewModel(), GlobalSearchState by state {
    data class Param(val query: String?)

    var installedCatalogs by mutableStateOf(emptyList<CatalogInstalled>())

    var inProgress by mutableStateOf(emptyList<SearchItem>())
    var noResult by mutableStateOf(emptyList<SearchItem>())
    var withResult by mutableStateOf(emptyList<SearchItem>())
    var numberOfTries by mutableStateOf(0)



    init {
        val query = param.query
        if (!query.isNullOrBlank()) {
            this.query = query
            searchBooks(query)
        }
    }

    fun searchBooks(query: String) {
        numberOfTries++
        inProgress = emptyList()
        noResult = emptyList()
        withResult = emptyList()
        scope.launch {
            installedCatalogs = getInstalledCatalog.get()
            val catalogs =
                installedCatalogs.mapNotNull { it.source }.filterIsInstance<CatalogSource>()
            var availableThreads = 5
            catalogs.forEach { source ->
                scope.launch {
                    SearchItem(source).handleSearchItems(true)
                    while (availableThreads <= 0) {
                        delay(500)
                    }
                    availableThreads--
                    kotlin.runCatching {

                        var items = withTimeout(60000) {
                            source.getMangaList(
                                filters = listOf(
                                    Filter.Title()
                                        .apply { this.value = query }
                                ),
                                1
                            ).mangas.map { it.toBook(source.id) }
                        }

                        withContext(Dispatchers.IO) {
                            items.forEachIndexed { index, book ->
                                items = items.replace(index, book)
                            }
                        }
                        val searchedItems = SearchItem(source, items = items)
                        searchedItems.handleSearchItems()
                    }.getOrElse {
                        SearchItem(source, items = emptyList()).handleSearchItems(false)
                    }
                    availableThreads++
                }
            }
        }
    }

    private fun SearchItem.handleSearchItems(loading:Boolean = false) {
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
}

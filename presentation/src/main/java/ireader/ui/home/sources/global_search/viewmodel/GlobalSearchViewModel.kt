package ireader.ui.home.sources.global_search.viewmodel

import androidx.lifecycle.viewModelScope
import ireader.common.models.entities.toBook
import ireader.core.source.CatalogSource
import ireader.core.source.model.Filter
import ireader.domain.catalogs.interactor.GetInstalledCatalog
import ireader.domain.catalogs.interactor.GetLocalCatalogs
import ireader.domain.usecases.local.LocalInsertUseCases
import ireader.domain.utils.extensions.replace
import ireader.ui.component.Controller
import ireader.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class GlobalSearchViewModel (
    private val state: GlobalSearchStateImpl,
    private val catalogStore: GetLocalCatalogs,
    val insertUseCases: LocalInsertUseCases,
    val getInstalledCatalog: GetInstalledCatalog,
    val param: Param
) : BaseViewModel(), GlobalSearchState by state {
    data class Param(val query:String?)

    companion object  {
        fun createParam(controller: Controller) : Param {
            return Param(controller.navBackStackEntry.arguments?.getString("query"))
        }
    }
    init {
        val query = param.query
        if (!query.isNullOrBlank()) {
            this.query = query
            searchBooks(query)
        }
    }

    fun searchBooks(query: String) {
        viewModelScope.launch {
            val installedCatalogs = getInstalledCatalog.get().mapNotNull { it.source }
            val catalogs = installedCatalogs.filterIsInstance<CatalogSource>()
            searchItems = emptyList()
            catalogs.forEach { source ->
                viewModelScope.launch {

                     kotlin.runCatching {
                         insertSearchItem(SearchItem(source, loading = true))
                         var items = source.getMangaList(
                             filters = listOf(
                                 Filter.Title()
                                     .apply { this.value = query }
                             ),
                             1
                         ).mangas.map { it.toBook(source.id) }
                         withContext(Dispatchers.IO) {
                             items.forEachIndexed { index, book ->
                                 items = items.replace(index, book)
                             }
                         }
                         insertSearchItem(SearchItem(source, items = items, loading = false))
                     }
                }
            }
        }
    }

    fun insertSearchItem(searchItem: SearchItem) {
        val item = searchItems.find { it.source.id == searchItem.source.id }
        searchItems = if (item != null) {
            val index = searchItems.indexOf(item)
            searchItems.replace(index, searchItem)
        } else {
            searchItems + searchItem
        }
    }
}

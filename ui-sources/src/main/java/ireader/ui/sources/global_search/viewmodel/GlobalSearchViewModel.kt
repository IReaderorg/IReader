package ireader.ui.sources.global_search.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ireader.common.extensions.replace
import ireader.common.models.entities.toBook
import ireader.core.api.source.model.Filter
import ireader.core.catalogs.CatalogStore
import ireader.core.ui.viewmodel.BaseViewModel
import ireader.domain.use_cases.local.LocalInsertUseCases
import ireader.ui.component.Controller
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class GlobalSearchViewModel (
    private val state: GlobalSearchStateImpl,
    private val catalogStore: CatalogStore,
    val insertUseCases: LocalInsertUseCases,
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
            searchItems = emptyList()

            catalogStore.catalogs.map { it.source }.forEachIndexed { index, source ->
                viewModelScope.launch {
                    try {
                        if (source is ireader.core.api.source.CatalogSource) {
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
                                    items = items.replace(index,book)
                                }
                            }
                            insertSearchItem(SearchItem(source, items = items, loading = false))
                        }
                    } catch (e: Throwable) {
                    }
                }
            }
        }
    }

    fun insertSearchItem(searchItem: SearchItem) {
        val item = searchItems.find { it.source.id == searchItem.source.id }
        if (item != null) {
            val index = searchItems.indexOf(item)
            searchItems = searchItems.replace(index, searchItem)
        } else {
            searchItems = searchItems + searchItem
        }
    }
}

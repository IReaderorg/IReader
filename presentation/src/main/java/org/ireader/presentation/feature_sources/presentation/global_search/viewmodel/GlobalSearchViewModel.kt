package org.ireader.presentation.feature_sources.presentation.global_search.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ireader.core.utils.replace
import org.ireader.core_api.source.CatalogSource
import org.ireader.core_api.source.model.Filter
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.core.catalog.service.CatalogStore
import org.ireader.common_models.entities.toBook
import org.ireader.domain.use_cases.remote.key.DeleteAllSearchedBook
import org.ireader.domain.use_cases.remote.key.RemoteKeyUseCase
import javax.inject.Inject

@HiltViewModel
class GlobalSearchViewModel @Inject constructor(
    private val state: GlobalSearchStateImpl,
    private val catalogStore: CatalogStore,
    private val insertUseCases: RemoteKeyUseCase,
    private val deleteAllSearchedBooks: DeleteAllSearchedBook,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel(), GlobalSearchState by state {

    init {
        val query = savedStateHandle.get<String>("query")
        if (!query.isNullOrBlank()) {
            this.query = query
            searchBooks(query)
        }
    }

    fun searchBooks(query: String) {
        viewModelScope.launch {
            searchItems = emptyList()
            withContext(Dispatchers.IO) {
                deleteAllSearchedBooks()
            }

            catalogStore.catalogs.map { it.source }.forEachIndexed { index, source ->
                viewModelScope.launch {
                    try {
                        if (source is CatalogSource) {
                            insertSearchItem(SearchItem(source, loading = true))
                            var items = source.getMangaList(filters = listOf(Filter.Title()
                                .apply { this.value = query }), 1).mangas.map { it.toBook(source.id) }
                            withContext(Dispatchers.IO) {
                                val ids =
                                    insertUseCases.insertAllExploredBook(items.map { it.copy(tableId = 2) })
                                items.forEachIndexed { index, book ->
                                    items = items.replace(index, book.copy(id = ids[index]))
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
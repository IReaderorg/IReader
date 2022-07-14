package org.ireader.explore.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.ireader.common_extensions.DefaultPaginator
import org.ireader.common_extensions.SourceNotFoundException
import org.ireader.common_models.DisplayMode
import org.ireader.common_models.entities.BookItem
import org.ireader.common_models.entities.RemoteKeys
import org.ireader.common_models.entities.toBook
import org.ireader.common_resources.UiText
import org.ireader.core_api.log.Log
import org.ireader.core_api.source.model.Filter
import org.ireader.core_api.source.model.MangasPageInfo
import org.ireader.core_catalogs.CatalogStore
import org.ireader.core_ui.exceptionHandler
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.use_cases.local.LocalGetBookUseCases
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import org.ireader.domain.use_cases.preferences.reader_preferences.BrowseScreenPrefUseCase
import org.ireader.domain.use_cases.remote.RemoteUseCases
import org.ireader.domain.use_cases.remote.key.RemoteKeyUseCase
import org.ireader.ui_explore.R
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val state: ExploreStateImpl,
    private val remoteUseCases: RemoteUseCases,
    private val catalogStore: CatalogStore,
    private val browseScreenPrefUseCase: BrowseScreenPrefUseCase,
    private val remoteKeyUseCase: RemoteKeyUseCase,
    private val getBookUseCases: LocalGetBookUseCases,
    private val insertUseCases: LocalInsertUseCases,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel(), ExploreState by state {

    init {
        val sourceId = savedStateHandle.get<Long>("sourceId")
        val query = savedStateHandle.get<String>("query")
        val catalog =
            catalogStore.catalogs.find { it.source?.id == sourceId }
        loadBooks()

        state.catalog = catalog
        val source = state.source
        if (sourceId != null && source != null) {
            if (!query.isNullOrBlank()) {
                toggleSearchMode(true)
                searchQuery = query
                loadItems()
            } else {
                val listings = source.getListings()
                if (listings.isNotEmpty()) {
                    state.stateListing = source.getListings().first()
                    loadItems()
                    // getBooks(source = source, listing = source.getListings().first())
                    viewModelScope.launch {
                        readLayoutType()
                    }
                } else {
                    viewModelScope.launch {
                        showSnackBar(UiText.StringResource(R.string.the_source_is_not_found))
                    }
                }
            }
        } else {
            viewModelScope.launch {
                showSnackBar(UiText.StringResource(R.string.the_source_is_not_found))
            }
        }
    }

    var initExploreJob: Job? = null

    fun loadBooks() {
        initExploreJob?.cancel()
        initExploreJob = viewModelScope.launch {
            remoteKeyUseCase.subScribeAllPagedExploreBooks().distinctUntilChanged().collect() {
                kotlin.runCatching {
                    stateItems = it
                }
            }
        }
    }

    fun loadItems(reset: Boolean = false) {
        getBooksJob?.cancel()
        if (reset) {
            page = 1
        }
        getBooksJob = viewModelScope.launch {
            kotlin.runCatching {
                DefaultPaginator<Int, MangasPageInfo>(
                    initialKey = state.page,
                    onLoadUpdated = {
                        isLoading = it
                    },
                    onRequest = { nextPage ->
                        try {
                            error = null
                            Log.debug { "Explore Request was made - current page:$nextPage" }

                            val query = searchQuery
                            val filters = stateFilters
                            val listing = stateListing
                            val source = catalog
                            if (source != null) {
                                var result = MangasPageInfo(emptyList(), false)
                                remoteUseCases.getRemoteBooks(
                                    searchQuery,
                                    listing,
                                    filters,
                                    source,
                                    page,
                                    onError = {
                                        throw it
                                    },
                                    onSuccess = { res ->
                                        result = res
                                    }
                                )
                                Result.success(result)
                            } else {
                                throw SourceNotFoundException()
                            }
                        } catch (e: Throwable) {
                            Result.failure(e)
                        }
                    },
                    getNextKey = {
                        state.page + 1
                    },
                    onError = { e ->
                        remoteKeyUseCase.prepareExploreMode(page == 1, emptyList(), emptyList())
                        endReached = true
                        e?.let {
                            error = exceptionHandler(it)
                        }
                    },
                    onSuccess = { items, newKey ->
                        val keys = items.mangas.map { book ->
                            RemoteKeys(
                                title = book.title,
                                prevPage = newKey - 1,
                                nextPage = newKey + 1,
                                sourceId = source?.id ?: 0
                            )
                        }
                        val books = items.mangas.map {
                            it.toBook(
                                source?.id ?: 0,
                                tableId = 1
                            )
                        }

                        remoteKeyUseCase.prepareExploreMode(page == 1, books, keys)

                        page = newKey
                        endReached = !items.hasNextPage
                        Log.debug { "Request was finished" }
                    },
                ).loadNextItems()
            }
        }
    }

    private var getBooksJob: Job? = null

    suspend fun addToFavorite(bookItem: BookItem) {
        getBookUseCases.findBookById(bookItem.id)?.let { book ->

            insertUseCases.insertBook(book.copy(favorite = !book.favorite))
        }
    }

    fun toggleSearchMode(inSearchMode: Boolean) {
        state.isSearchModeEnable = inSearchMode

        if (!inSearchMode && source != null) {
            exitSearchedMode()
            searchQuery?.let { query ->
                stateFilters = listOf(Filter.Title().apply { this.value = query })
                loadItems()
            }
        }
    }

    private fun exitSearchedMode() {
        state.searchQuery = ""
        state.apply {
            searchQuery = ""
            isLoading = false
            error = null
        }
    }

    fun saveLayoutType(layoutType: DisplayMode) {
        state.layout = layoutType
        browseScreenPrefUseCase.browseLayoutTypeUseCase.save(layoutType)
    }

    private suspend fun readLayoutType() {
        state.layout = browseScreenPrefUseCase.browseLayoutTypeUseCase.read()
    }

    fun toggleFilterMode(enable: Boolean? = null) {
        state.isFilterEnable = enable ?: !state.isFilterEnable
    }
}

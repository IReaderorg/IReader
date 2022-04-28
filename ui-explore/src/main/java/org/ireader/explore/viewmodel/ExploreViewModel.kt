package org.ireader.explore.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.ireader.common_models.DisplayMode
import org.ireader.common_models.entities.RemoteKeys
import org.ireader.common_models.entities.toBook
import org.ireader.core.DefaultPaginator
import org.ireader.core.exceptions.SourceNotFoundException
import org.ireader.core_api.log.Log
import org.ireader.core_api.source.CatalogSource
import org.ireader.core_api.source.model.Filter
import org.ireader.core_api.source.model.MangasPageInfo
import org.ireader.core_catalogs.CatalogStore
import org.ireader.core_ui.exceptionHandler
import org.ireader.domain.use_cases.local.DeleteUseCase
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import org.ireader.domain.use_cases.preferences.reader_preferences.BrowseLayoutTypeUseCase
import org.ireader.domain.use_cases.remote.RemoteUseCases
import org.ireader.domain.use_cases.remote.key.RemoteKeyUseCase
import javax.inject.Inject


@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val state: ExploreStateImpl,
    private val remoteUseCases: RemoteUseCases,
    private val deleteUseCase: DeleteUseCase,
    private val catalogStore: CatalogStore,
    private val browseLayoutTypeUseCase: BrowseLayoutTypeUseCase,
    private val remoteKeyUseCase: RemoteKeyUseCase,
    private val insertUseCases: LocalInsertUseCases,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), ExploreState by state {

    private val _eventFlow = MutableSharedFlow<org.ireader.common_extensions.UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()


    init {
        val sourceId = savedStateHandle.get<Long>("sourceId")
        val query = savedStateHandle.get<String>("query")
        val source =
            catalogStore.catalogs.find { it.source.id == sourceId }?.source
        loadBooks()

        if (sourceId != null && source is CatalogSource) {
            state.source = source
            if (!query.isNullOrBlank()) {
                toggleSearchMode(true)
                searchQuery = query
                loadItems()
//                getBooks(filters = listOf(Filter.Title().apply { this.value = query }),
//                    source = source)
            } else {
                val listings = source.getListings()
                if (listings.isNotEmpty()) {
                    state.stateListing = source.getListings().first()
                    loadItems()
                    //getBooks(source = source, listing = source.getListings().first())
                    readLayoutType()
                } else {
                    viewModelScope.launch {
                        showSnackBar(org.ireader.common_extensions.UiText.StringResource(org.ireader.core.R.string.the_source_is_not_found))
                    }
                }
            }
        } else {
            viewModelScope.launch {
                showSnackBar(org.ireader.common_extensions.UiText.StringResource(org.ireader.core.R.string.the_source_is_not_found))
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
                            val source = source
                            if (source != null) {
                                var result = MangasPageInfo(emptyList(), false)
                                remoteUseCases.getRemoteBooks(searchQuery,
                                    listing,
                                    filters,
                                    source,
                                    page,
                                    onError = {
                                        throw it
                                    },
                                    onSuccess = { res ->
                                        result = res
                                    })
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
//                        error = when (it) {
//
//                            is EmptyQuery -> UiText.StringResource(R.string.query_must_not_be_empty)
//                            is SourceNotFoundException -> UiText.StringResource(R.string.the_source_is_not_found)
//                            else -> it?.let { it1 -> UiText.ExceptionString(it1) }
//                        }
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
                            it.toBook(source?.id ?: 0,
                                tableId = 1)
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


    private fun onQueryChange(query: String) {
        state.searchQuery = query
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
        state.layout = layoutType.layout
        browseLayoutTypeUseCase.save(layoutType.layoutIndex)
    }

    private fun readLayoutType() {
        state.layout = browseLayoutTypeUseCase.read().layout
    }


    suspend fun showSnackBar(message: org.ireader.common_extensions.UiText?) {
        _eventFlow.emit(
            org.ireader.common_extensions.UiEvent.ShowSnackbar(
                uiText = message ?: org.ireader.common_extensions.UiText.StringResource(org.ireader.core.R.string.error_unknown)
            )
        )
    }

    fun removeExploreBooks() {
        viewModelScope.launch(Dispatchers.IO) {
            deleteUseCase.deleteAllExploreBook()
            deleteUseCase.deleteAllRemoteKeys()
        }
    }

    fun toggleFilterMode(enable: Boolean? = null) {
        state.isFilterEnable = enable ?: !state.isFilterEnable
    }

}
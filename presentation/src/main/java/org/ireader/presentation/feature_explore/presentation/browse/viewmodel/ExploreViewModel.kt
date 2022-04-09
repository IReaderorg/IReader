package org.ireader.presentation.feature_explore.presentation.browse.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.ireader.core.DefaultPaginator
import org.ireader.core.exceptions.EmptyQuery
import org.ireader.core.exceptions.SourceNotFoundException
import org.ireader.core.utils.UiEvent
import org.ireader.core.utils.UiText
import org.ireader.domain.R
import org.ireader.domain.catalog.service.CatalogStore
import org.ireader.domain.models.DisplayMode
import org.ireader.domain.models.RemoteKeys
import org.ireader.domain.models.entities.toBook
import org.ireader.domain.use_cases.local.DeleteUseCase
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import org.ireader.domain.use_cases.preferences.reader_preferences.BrowseLayoutTypeUseCase
import org.ireader.domain.use_cases.remote.RemoteUseCases
import org.ireader.domain.use_cases.remote.key.RemoteKeyUseCase
import org.ireader.domain.utils.withIOContext
import tachiyomi.source.CatalogSource
import tachiyomi.source.model.Filter
import tachiyomi.source.model.MangasPageInfo
import timber.log.Timber
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
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), ExploreState by state {

    private val _eventFlow = MutableSharedFlow<UiEvent>()
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
                        showSnackBar(UiText.StringResource(org.ireader.core.R.string.the_source_is_not_found))
                    }
                }
            }
        } else {
            viewModelScope.launch {
                showSnackBar(UiText.StringResource(org.ireader.core.R.string.the_source_is_not_found))
            }
        }

    }


    fun onEvent(event: ExploreScreenEvents) {
        when (event) {
            is ExploreScreenEvents.OnLayoutTypeChnage -> {
                saveLayoutType(event.layoutType)
            }
            is ExploreScreenEvents.ToggleSearchMode -> {
                toggleSearchMode(event.inSearchMode)
            }
            is ExploreScreenEvents.OnQueryChange -> {
                onQueryChange(event.query)
            }
        }
    }

    fun loadBooks() {
        viewModelScope.launch {
            remoteKeyUseCase.subScribeAllPagedExploreBooks().distinctUntilChanged().onEach {
                stateItems = it
            }.launchIn(viewModelScope)

        }
    }

    fun loadItems(reset: Boolean = false) {
        getBooksJob?.cancel()
        if (reset) {
            page = 1
        }
        getBooksJob = viewModelScope.launch {
            DefaultPaginator<Int, MangasPageInfo>(
                initialKey = state.page,
                onLoadUpdated = {
                    isLoading = it
                },
                onRequest = { nextPage ->
                    try {
                        error = null
                        Timber.e("Request was made; $nextPage")
                        val query = searchQuery
                        val filters = stateFilters
                        val listing = stateListing
                        val source = source
                        if (source != null) {
                            val result = if (searchQuery != null) {
                                if (query != null && query.isNotBlank()) {
                                    source.getMangaList(filters = listOf(Filter.Title()
                                        .apply { this.value = query }), page = page)
                                } else {
                                    throw EmptyQuery()
                                }
                            } else if (filters != null) {
                                source.getMangaList(filters = filters, page)
                            } else {
                                source.getMangaList(sort = listing, page)
                            }
                            Result.success(result)
                        } else {
                            throw SourceNotFoundException()
                        }
                    } catch (e: Exception) {
                        Result.failure(e)
                    }

                },
                getNextKey = {
                    state.page + 1
                },
                onError = {
                    endReached = true
                    error = when (it) {
                        is EmptyQuery -> UiText.StringResource(R.string.query_must_not_be_empty)
                        is SourceNotFoundException -> UiText.StringResource(R.string.the_source_is_not_found)
                        else -> it?.let { it1 -> UiText.ExceptionString(it1) }
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
                        it.toBook(source?.id ?: 0,
                            tableId = 1)
                    }
                    withIOContext {
                        remoteKeyUseCase.prepareExploreMode(page == 1, books, keys)
                    }
                    page = newKey
                    endReached = !items.hasNextPage
                    Timber.e("Request was finished")
                },
            ).loadNextItems()

        }
    }

    private var getBooksJob: Job? = null


    private fun onQueryChange(query: String) {
        state.searchQuery = query
    }

    private fun toggleSearchMode(inSearchMode: Boolean) {
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

    private fun saveLayoutType(layoutType: DisplayMode) {
        state.layout = layoutType.layout
        browseLayoutTypeUseCase.save(layoutType.layoutIndex)
    }

    private fun readLayoutType() {
        state.layout = browseLayoutTypeUseCase.read().layout
    }


    suspend fun showSnackBar(message: UiText?) {
        _eventFlow.emit(
            UiEvent.ShowSnackbar(
                uiText = message ?: UiText.StringResource(org.ireader.core.R.string.error_unknown)
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
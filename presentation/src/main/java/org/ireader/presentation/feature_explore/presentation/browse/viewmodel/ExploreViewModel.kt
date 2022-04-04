package org.ireader.presentation.feature_explore.presentation.browse.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.ireader.core.utils.Constants
import org.ireader.core.utils.UiEvent
import org.ireader.core.utils.UiText
import org.ireader.domain.R
import org.ireader.domain.catalog.service.CatalogStore
import org.ireader.domain.models.DisplayMode
import org.ireader.domain.models.LayoutType
import org.ireader.domain.models.entities.Book
import org.ireader.domain.use_cases.local.DeleteUseCase
import org.ireader.domain.use_cases.preferences.reader_preferences.BrowseLayoutTypeUseCase
import org.ireader.domain.use_cases.remote.RemoteUseCases
import tachiyomi.source.CatalogSource
import tachiyomi.source.model.Filter
import tachiyomi.source.model.Listing
import javax.inject.Inject


@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val state: ExploreStateImpl,
    private val remoteUseCases: RemoteUseCases,
    private val deleteUseCase: DeleteUseCase,
    private val catalogStore: CatalogStore,
    private val browseLayoutTypeUseCase: BrowseLayoutTypeUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), ExploreState by state {

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        val sourceId = savedStateHandle.get<Long>("sourceId")
        val query = savedStateHandle.get<String>("query")
        val source =
            catalogStore.catalogs.find { it.source.id == sourceId }?.source
        if (sourceId != null && source is CatalogSource) {
            state.source = source
            if (!query.isNullOrBlank()) {
                toggleSearchMode(true)
                searchQuery = query
                getBooks(filters = listOf(Filter.Title().apply { this.value = query }),
                    source = source)
            } else {
                val listings = source.getListings()
                if (listings.isNotEmpty()) {
                    state.exploreType = source.getListings().first()
                    getBooks(source = source, listing = source.getListings().first())
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


    private val _books = MutableStateFlow<PagingData<Book>>(PagingData.empty())
    val books = _books


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

    private var getBooksJob: Job? = null

    @OptIn(ExperimentalPagingApi::class)
    fun getBooks(
        query: String? = null, listing: Listing? = null,
        filters: List<Filter<*>>? = null, source: CatalogSource,
    ) {
        getBooksJob?.cancel()
        getBooksJob = viewModelScope.launch(Dispatchers.Main) {
            remoteUseCases.getRemoteBookByPaginationUseCase(
                source,
                listing,
                query = query,
                filters = filters,
                pageSize = if (layout == LayoutType.GridLayout) 15 else 6,
                maxSize = Constants.MAX_PAGE_SIZE
            ).cachedIn(viewModelScope)
                .collect { snapshot ->
                    _books.value = snapshot
                }
        }
    }

    private fun onQueryChange(query: String) {
        state.searchQuery = query
    }

    private fun toggleSearchMode(inSearchMode: Boolean) {
        state.isSearchModeEnable = inSearchMode

        if (!inSearchMode && source != null) {
            exitSearchedMode()
            getBooks(source = source,
                filters = state.modifiedFilter,
                listing = source.getListings().first())
        }
    }

    private fun exitSearchedMode() {
        state.searchQuery = ""
        state.apply {
            searchQuery = ""
            isLoading = false
            error = UiText.StringResource(R.string.no_error)
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
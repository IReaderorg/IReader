package org.ireader.domain.view_models.explore

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
import org.ireader.core.utils.UiEvent
import org.ireader.core.utils.UiText
import org.ireader.domain.R
import org.ireader.domain.catalog.service.CatalogStore
import org.ireader.domain.models.DisplayMode
import org.ireader.domain.models.entities.Book
import org.ireader.domain.use_cases.local.DeleteUseCase
import org.ireader.domain.use_cases.remote.RemoteUseCases
import tachiyomi.source.CatalogSource
import tachiyomi.source.model.Filter
import tachiyomi.source.model.Listing
import javax.inject.Inject


@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val state: ExploreStateImpl,
    private val preferencesUseCase: org.ireader.domain.use_cases.preferences.reader_preferences.PreferencesUseCase,
    private val remoteUseCases: RemoteUseCases,
    private val deleteUseCase: DeleteUseCase,
    private val catalogStore: CatalogStore,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), ExploreState by state {

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        val sourceId = savedStateHandle.get<Long>("sourceId")
        val source =
            catalogStore.catalogs.find { it.source.id == sourceId }?.source
        if (sourceId != null && source is CatalogSource) {
            state.source = source
            state.exploreType = source.getListings().first()
            getBooks(source = source)
            readLayoutType()
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
                listing ?: state.exploreType ?: source.getListings().first(),
                query = query, filters = filters).cachedIn(viewModelScope)
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
            getBooks(source = source)
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
        preferencesUseCase.saveBrowseLayoutUseCase(layoutType.layoutIndex)
    }

    private fun readLayoutType() {
        state.layout = preferencesUseCase.readBrowseLayoutUseCase().layout
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
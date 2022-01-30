package ir.kazemcodes.infinity.feature_explore.presentation.browse

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.kazemcodes.infinity.core.data.network.models.BooksPage
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.use_cases.local.DeleteUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.preferences.reader_preferences.PreferencesUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.remote.RemoteUseCases
import ir.kazemcodes.infinity.core.presentation.layouts.DisplayMode
import ir.kazemcodes.infinity.feature_activity.presentation.NavigationArgs
import ir.kazemcodes.infinity.feature_sources.sources.Extensions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed class ExploreType(val mode: Int) {
    object Latest : ExploreType(0)
    object Popular : ExploreType(1)
    object Search : ExploreType(1)
}

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val preferencesUseCase: PreferencesUseCase,
    private val remoteUseCases: RemoteUseCases,
    private val deleteUseCase: DeleteUseCase,
    private val extensions: Extensions,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state : MutableState<BrowseScreenState> = mutableStateOf<BrowseScreenState>(BrowseScreenState())
    val state: State<BrowseScreenState> = _state

    init {
        val exploreId = savedStateHandle.get<Int>(NavigationArgs.exploreType.name)
        val sourceId = savedStateHandle.get<Long>(NavigationArgs.sourceId.name)
        val source = sourceId?.let { extensions.mappingSourceNameToSource(it) }!!
        _state.value = state.value.copy(source = source)
        val exploreType = when (exploreId) {
            0 -> ExploreType.Latest
            else -> ExploreType.Popular
        }
        _state.value = state.value.copy(exploreType = exploreType)
        getBooks()
        readLayoutType()
    }

    private val _books = MutableStateFlow<PagingData<Book>>(PagingData.empty())
    val books = _books


    private var getBooksJob: Job? = null


    fun onEvent(event: BrowseScreenEvents) {
        when (event) {
            is BrowseScreenEvents.UpdatePage -> {
                updatePage(event.page)
            }
            is BrowseScreenEvents.UpdateLayoutType -> {
                updateLayoutType(event.layoutType)
            }
            is BrowseScreenEvents.ToggleMenuDropDown -> {
                toggleMenuDropDown(isShown = event.isShown)
            }
            is BrowseScreenEvents.ToggleSearchMode -> {
                toggleSearchMode(event.inSearchMode)
            }
            is BrowseScreenEvents.UpdateSearchInput -> {
                updateSearchInput(event.query)
            }
        }
    }


    @OptIn(ExperimentalPagingApi::class)
    fun getBooks(query: String? = null, type: ExploreType? = null) {
        getBooksJob?.cancel()
        getBooksJob = viewModelScope.launch(Dispatchers.Main) {
            remoteUseCases.getRemoteBooksByRemoteMediator(state.value.source,
                type ?: state.value.exploreType,
                query = query).cachedIn(viewModelScope)
                .collect { snapshot ->
                    _books.value = snapshot
                }
        }
    }


    private fun updatePage(page: Int) {
        if (!state.value.isSearchModeEnable) {
            _state.value = state.value.copy(page = page)
        } else {
            _state.value = state.value.copy(searchPage = page)
        }
    }

    private fun updateSearchInput(query: String) {
        _state.value = state.value.copy(searchQuery = query)
    }

    private fun toggleSearchMode(inSearchMode: Boolean? = null) {
        _state.value =
            state.value.copy(isSearchModeEnable = inSearchMode ?: !state.value.isSearchModeEnable)
        if (inSearchMode == false) {
            exitSearchedMode()
            getBooks()
        }
    }

    private fun exitSearchedMode() {
        _state.value = state.value.copy(searchedBook = BooksPage(),
            searchQuery = "",
            page = 1,
            isLoading = false,
            error = "")
    }

    private fun updateLayoutType(layoutType: DisplayMode) {
        _state.value = state.value.copy(layout = layoutType.layout)

        preferencesUseCase.saveBrowseLayoutUseCase(layoutType.layoutIndex)

    }

    private fun readLayoutType() {
        _state.value =
            state.value.copy(layout = preferencesUseCase.readBrowseLayoutUseCase().layout)


    }

    private fun toggleMenuDropDown(isShown: Boolean) {
        _state.value = state.value.copy(isMenuDropDownShown = isShown)
    }



    private fun setExploreModeOffForInLibraryBooks() {
        viewModelScope.launch(Dispatchers.IO) {
            deleteUseCase.setExploreModeOffForInLibraryBooks()
        }
    }


}
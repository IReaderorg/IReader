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
import ir.kazemcodes.infinity.core.ui.NavigationArgs
import ir.kazemcodes.infinity.feature_sources.sources.Extensions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed class ExploreType(val id: Int) {
    object Latest : ExploreType(0)
    object Popular : ExploreType(1)
    object Search : ExploreType(2)
}

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val preferencesUseCase: PreferencesUseCase,
    private val remoteUseCases: RemoteUseCases,
    private val deleteUseCase: DeleteUseCase,
    private val extensions: Extensions,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state: MutableState<ExploreScreenState> = mutableStateOf<ExploreScreenState>(ExploreScreenState())
    val state: State<ExploreScreenState> = _state

    init {
        val exploreId = savedStateHandle.get<Int>(NavigationArgs.exploreType.name)
        val sourceId = savedStateHandle.get<Long>(NavigationArgs.sourceId.name)
        val source = sourceId?.let { extensions.mappingSourceNameToSource(it) }!!
        _state.value = state.value.copy(source = source)
        if (exploreId != null) {
            _state.value = state.value.copy(exploreType = exploreTypeMapper(exploreId))
        }
        getBooks()
        readLayoutType()
    }

    private val _books = MutableStateFlow<PagingData<Book>>(PagingData.empty())
    val books = _books


    fun onEvent(event: ExploreScreenEvents) {
        when (event) {
            is ExploreScreenEvents.OnLayoutTypeChnage -> {
                saveLayoutType(event.layoutType)
            }
            is ExploreScreenEvents.ToggleMenuDropDown -> {
                toggleMenuDropDown(isShown = event.isShown)
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

    private fun onQueryChange(query: String) {
        _state.value = state.value.copy(searchQuery = query)
    }

    private fun toggleSearchMode(inSearchMode: Boolean) {
        _state.value =
            state.value.copy(isSearchModeEnable = inSearchMode)
        if (!inSearchMode) {
            exitSearchedMode()
            getBooks()
        }
    }

    private fun exitSearchedMode() {
        _state.value = state.value.copy(
            searchedBook = BooksPage(),
            searchQuery = "",
            isLoading = false,
            error = "")
    }

    private fun saveLayoutType(layoutType: DisplayMode) {
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

}
package org.ireader.domain.view_models.explore

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.ireader.core.utils.UiText
import org.ireader.domain.R
import org.ireader.domain.models.DisplayMode
import org.ireader.domain.models.ExploreType
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.source.BooksPage
import org.ireader.domain.source.Extensions
import org.ireader.infinity.core.domain.use_cases.local.DeleteUseCase
import org.ireader.use_cases.remote.RemoteUseCases
import javax.inject.Inject


@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val preferencesUseCase: org.ireader.domain.use_cases.preferences.reader_preferences.PreferencesUseCase,
    private val remoteUseCases: RemoteUseCases,
    private val deleteUseCase: DeleteUseCase,
    private val extensions: Extensions,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state: MutableState<ExploreScreenState> =
        mutableStateOf<ExploreScreenState>(ExploreScreenState())
    val state: State<ExploreScreenState> = _state

    init {
        val exploreId = savedStateHandle.get<Int>("exploreType")
        val sourceId = savedStateHandle.get<Long>("sourceId")
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
            error = UiText.StringResource(R.string.no_error))
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
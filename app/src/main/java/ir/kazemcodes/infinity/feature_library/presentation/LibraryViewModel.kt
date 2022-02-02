package ir.kazemcodes.infinity.feature_library.presentation

import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.use_cases.local.DeleteUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalGetBookUseCases
import ir.kazemcodes.infinity.core.domain.use_cases.preferences.reader_preferences.PreferencesUseCase
import ir.kazemcodes.infinity.core.presentation.layouts.DisplayMode
import ir.kazemcodes.infinity.feature_library.presentation.components.FilterType
import ir.kazemcodes.infinity.feature_library.presentation.components.LibraryEvents
import ir.kazemcodes.infinity.feature_library.presentation.components.SortType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterialApi::class)
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val localGetBookUseCases: LocalGetBookUseCases,
    private val deleteUseCase: DeleteUseCase,
    private val preferencesUseCase: PreferencesUseCase,
) : ViewModel() {


    private val _bottomSheetState = mutableStateOf(BottomSheetValue.Collapsed)
    val bottomSheetState = _bottomSheetState

    private val _state = mutableStateOf(LibraryState())
    val state: State<LibraryState> = _state


    private val _books = MutableStateFlow<PagingData<Book>>(PagingData.empty())
    val book = _books

    /**
     * Get All Books By Paging
     */
    private fun getBooks() {
        viewModelScope.launch {
                localGetBookUseCases.GetInLibraryBooksPagingData(
                    sortType = state.value.sortType,
                    isAsc = state.value.isSortAcs,
                    unreadFilter = state.value.unreadFilter).cachedIn(viewModelScope)
                    .collect { snapshot ->
                        _books.value = snapshot
                    }
        }
    }

    init {
        deleteNotInLibraryChapters()
        getBooks()
        readLayoutType()
    }


    fun searchBook(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            localGetBookUseCases.getBooksByQueryByPagination(query).cachedIn(viewModelScope)
                .collect { snapshot ->
                    _books.value = snapshot
                }
        }
    }


    override fun onCleared() {
        viewModelScope.cancel()
        super.onCleared()
    }

    fun onEvent(event: LibraryEvents) {
        when (event) {
            is LibraryEvents.UpdateLayoutType -> {
                updateLayoutType(event.layoutType)
            }
            is LibraryEvents.ToggleSearchMode -> {
                toggleSearchMode(event.inSearchMode)
                getLibraryDataIfSearchModeIsOff(event.inSearchMode ?: false)

            }
            is LibraryEvents.UpdateSearchInput -> {
                updateSearchInput(event.query)
            }
        }

    }


    private fun updateSearchInput(query: String) {
        _state.value = state.value.copy(searchQuery = query)
    }

    private fun toggleSearchMode(inSearchMode: Boolean? = null) {
        _state.value = state.value.copy(inSearchMode = inSearchMode ?: !state.value.inSearchMode)
    }


    private fun updateLayoutType(layoutType: DisplayMode) {
        preferencesUseCase.saveLibraryLayoutUseCase(layoutType.layoutIndex)
        _state.value = state.value.copy(layout = layoutType.layout)

    }
    fun updateBottomSheetState(bottomSheetValue: BottomSheetValue) {
        _bottomSheetState.value = bottomSheetValue
    }

    private fun readLayoutType() {


        val sortType = when (preferencesUseCase.readSortersUseCase()) {
            0 -> {
                SortType.DateAdded
            }
            1 -> {
                SortType.Alphabetically
            }
            2 -> {
                SortType.LastRead
            }
            else -> {
                SortType.TotalChapter
            }
        }
        val filterType = when (preferencesUseCase.readFilterUseCase()) {
            0 -> {
                FilterType.Disable
            }
            else -> {
                FilterType.Unread
            }
        }
        _state.value =
            state.value.copy(layout = preferencesUseCase.readLibraryLayoutUseCase().layout,
                sortType = sortType,
                unreadFilter = filterType)

    }


    private fun deleteNotInLibraryChapters() {
        viewModelScope.launch(Dispatchers.IO) {
            deleteUseCase.deleteInLibraryBook()
        }
    }

    fun changeSortIndex(sortType: SortType) {
        _state.value = state.value.copy(sortType = sortType)
        if (state.value.sortType == sortType) {
            _state.value = _state.value.copy(isSortAcs = !state.value.isSortAcs)
        }
        preferencesUseCase.saveSortersUseCase(state.value.sortType.index)
        getBooks()
    }

    fun enableUnreadFilter(filterType: FilterType) {
        _state.value = state.value.copy(unreadFilter = filterType)
        preferencesUseCase.saveFiltersUseCase(state.value.unreadFilter.index)
        getBooks()

    }

    private fun getLibraryDataIfSearchModeIsOff(inSearchMode: Boolean) {
        if (!inSearchMode) {
            _state.value = state.value.copy(searchedBook = emptyList(), searchQuery = "")
            getBooks()
        }
    }


}
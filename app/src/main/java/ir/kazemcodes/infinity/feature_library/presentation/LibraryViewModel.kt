package ir.kazemcodes.infinity.feature_library.presentation

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
) : ViewModel(), LibraryScreenActions {


    private val _state = mutableStateOf(LibraryState())
    val state: State<LibraryState> = _state


    private val _books = MutableStateFlow<PagingData<Book>>(PagingData.empty())
    val book = _books

    init {
        setExploreModeOffForInLibraryBooks()
        deleteNotInLibraryChapters()
        getLibraryBooks()
        readLayoutType()
    }

    /**
     * Get All Books By Paging
     */
    override fun getLibraryBooks() {
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

    override fun searchBook(query: String) {
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


    override fun updateSearchInput(query: String) {
        _state.value = state.value.copy(searchQuery = query)
    }

    override fun toggleSearchMode(inSearchMode: Boolean?) {
        _state.value = state.value.copy(inSearchMode = inSearchMode ?: !state.value.inSearchMode)
    }


    override fun updateLayoutType(layoutType: DisplayMode) {
        preferencesUseCase.saveLibraryLayoutUseCase(layoutType.layoutIndex)
        _state.value = state.value.copy(layout = layoutType.layout)
    }


    override fun readLayoutType() {
        val sortType = preferencesUseCase.readSortersUseCase()
        val filterType = preferencesUseCase.readFilterUseCase()
        _state.value =
            state.value.copy(layout = preferencesUseCase.readLibraryLayoutUseCase().layout,
                sortType = sortType,
                unreadFilter = filterType)

    }


    override fun deleteNotInLibraryChapters() {
        viewModelScope.launch(Dispatchers.IO) {
            deleteUseCase.deleteNotInLibraryBook()
        }
    }

    override fun changeSortIndex(sortType: SortType) {
        _state.value = state.value.copy(sortType = sortType)
        if (state.value.sortType == sortType) {
            _state.value = _state.value.copy(isSortAcs = !state.value.isSortAcs)
        }
        preferencesUseCase.saveSortersUseCase(state.value.sortType.index)
        getLibraryBooks()
    }

    override fun enableUnreadFilter(filterType: FilterType) {
        _state.value = state.value.copy(unreadFilter = filterType)
        preferencesUseCase.saveFiltersUseCase(state.value.unreadFilter.index)
        getLibraryBooks()
    }

    override fun getLibraryDataIfSearchModeIsOff(inSearchMode: Boolean) {
        if (inSearchMode) return
        _state.value = state.value.copy(searchedBook = emptyList(), searchQuery = "")
        getLibraryBooks()
    }

    override fun setExploreModeOffForInLibraryBooks() {
        viewModelScope.launch(Dispatchers.IO) {
            deleteUseCase.setExploreModeOffForInLibraryBooks()
        }
    }


}
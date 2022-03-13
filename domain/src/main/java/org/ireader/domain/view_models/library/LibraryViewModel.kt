package org.ireader.domain.view_models.library


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.models.DisplayMode
import org.ireader.domain.models.FilterType
import org.ireader.domain.models.SortType
import org.ireader.domain.models.entities.Book
import org.ireader.domain.use_cases.local.LocalGetBookUseCases
import org.ireader.domain.use_cases.preferences.reader_preferences.FiltersUseCase
import org.ireader.domain.use_cases.preferences.reader_preferences.LibraryLayoutTypeUseCase
import org.ireader.domain.use_cases.preferences.reader_preferences.SortersUseCase
import javax.inject.Inject


@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val localGetBookUseCases: LocalGetBookUseCases,
    private val libraryLayoutUseCase: LibraryLayoutTypeUseCase,
    private val sortersUseCase: SortersUseCase,
    private val filtersUseCase: FiltersUseCase,
) : BaseViewModel() {


    var state by mutableStateOf(LibraryScreenState())
        private set

    private val _books = MutableStateFlow<PagingData<Book>>(PagingData.empty())
    val book = _books

    init {
        getLibraryBooks()
        readLayoutTypeAndFilterTypeAndSortType()
    }


    fun onEvent(event: LibraryEvents) {
        when (event) {
            is LibraryEvents.OnLayoutTypeChange -> {
                onLayoutTypeChange(event.layoutType)
            }
            is LibraryEvents.ToggleSearchMode -> {
                toggleSearchMode(event.inSearchMode)
                getLibraryDataIfSearchModeIsOff()
            }
            is LibraryEvents.UpdateSearchInput -> {
                onQueryChange(event.query)
            }
            is LibraryEvents.SearchBook -> {
                searchBook(event.query)
            }
            is LibraryEvents.EnableFilter -> {
                when (event.filterType) {
                    is FilterType.Unread -> {
                        enableUnreadFilter(event.filterType)
                    }
                    else -> {

                    }
                }
            }
        }

    }


    private fun getLibraryBooks() {
        viewModelScope.launch {
            localGetBookUseCases.SubscribeInLibraryBooksPagingData(
                sortType = state.sortType,
                isAsc = state.isSortAcs,
                unreadFilter = state.unreadFilter)
                .cachedIn(viewModelScope)
                .collect { snapshot ->
                    _books.value = snapshot
                }
        }
    }

    private fun searchBook(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            localGetBookUseCases.getBooksByQueryByPagination(query)
                .cachedIn(viewModelScope)
                .collect { snapshot ->
                    _books.value = snapshot
                }
        }
    }

    private fun onQueryChange(query: String) {
        state = state.copy(searchQuery = query)
    }

    private fun toggleSearchMode(inSearchMode: Boolean) {
        state = state.copy(inSearchMode = inSearchMode)
    }


    private fun onLayoutTypeChange(layoutType: DisplayMode) {
        libraryLayoutUseCase.save(layoutType.layoutIndex)
        state = state.copy(layout = layoutType.layout)
    }


    private fun readLayoutTypeAndFilterTypeAndSortType() {
        val sortType = sortersUseCase.read()
        val filterType = filtersUseCase.read()
        val layoutType = libraryLayoutUseCase.read().layout
        state = state.copy(layout = layoutType,
            sortType = sortType,
            unreadFilter = filterType)
    }

    fun changeSortIndex(sortType: SortType) {
        state = state.copy(sortType = sortType)
        if (state.sortType == sortType) {
            state = state.copy(isSortAcs = !state.isSortAcs)
        }
        saveSortType(sortType)
        getLibraryBooks()
    }

    private fun saveSortType(sortType: SortType) {
        sortersUseCase.save(sortType.index)
    }

    fun enableUnreadFilter(filterType: FilterType) {
        state = state.copy(unreadFilter = filterType)
        filtersUseCase.save(state.unreadFilter.index)
        getLibraryBooks()
    }

    private fun getLibraryDataIfSearchModeIsOff() {
        if (state.inSearchMode) return
        state = state.copy(searchedBook = emptyList(), searchQuery = "")
        getLibraryBooks()
    }
}
package ir.kazemcodes.infinity.feature_library.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.zhuinden.simplestack.ScopedServices
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.use_cases.local.DeleteUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalGetBookUseCases
import ir.kazemcodes.infinity.core.domain.use_cases.preferences.reader_preferences.PreferencesUseCase
import ir.kazemcodes.infinity.core.presentation.layouts.DisplayMode
import ir.kazemcodes.infinity.feature_library.presentation.components.FilterType
import ir.kazemcodes.infinity.feature_library.presentation.components.LibraryEvents
import ir.kazemcodes.infinity.feature_library.presentation.components.SortType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect


class LibraryViewModel(
    private val localGetBookUseCases: LocalGetBookUseCases,
    private val deleteUseCase: DeleteUseCase,
    private val preferencesUseCase: PreferencesUseCase,
) : ScopedServices.Registered {

    private val _state = mutableStateOf(LibraryState())
    val state: State<LibraryState> = _state

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _books = MutableStateFlow<PagingData<Book>>(PagingData.empty())
    val book = _books

    /**
     * Get All Books By Paging
     */
    private fun getBooks() {
        coroutineScope.launch(Dispatchers.IO) {
            localGetBookUseCases.GetInLibraryBooksPagingData(
                sortType = state.value.sortType,
                isAsc = state.value.isSortAcs,
                unreadFilter = state.value.unreadFilter).cachedIn(coroutineScope)
                .collect { snapshot ->
                    _books.value = snapshot
                }
        }
    }

    override fun onServiceRegistered() {
        deleteNotInLibraryChapters()
        getBooks()
        readLayoutType()
    }


    fun searchBook(query: String) {
        coroutineScope.launch(Dispatchers.IO) {
            localGetBookUseCases.getBooksByQueryByPagination(query).cachedIn(coroutineScope)
                .collect { snapshot ->
                    _books.value = snapshot
                }
        }
    }

    override fun onServiceUnregistered() {
        coroutineScope.cancel()
    }


    fun onEvent(event: LibraryEvents) {
        when (event) {
            is LibraryEvents.UpdateLayoutType -> {
                updateLayoutType(event.layoutType)
            }
            is LibraryEvents.ToggleSearchMode -> {
                toggleSearchMode(event.inSearchMode)
                getLibraryDataIfSearchModeIsOff(event.inSearchMode?:false)

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
        coroutineScope.launch(Dispatchers.IO) {
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
    private fun getLibraryDataIfSearchModeIsOff(inSearchMode:Boolean) {
        if (!inSearchMode) {
            _state.value = state.value.copy(searchedBook = emptyList(), searchQuery = "")
            getBooks()
        }
    }


}
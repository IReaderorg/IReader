package ir.kazemcodes.infinity.feature_library.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.zhuinden.simplestack.ScopedServices
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.repository.LocalBookRepository
import ir.kazemcodes.infinity.core.domain.use_cases.preferences.PreferencesUseCase
import ir.kazemcodes.infinity.core.presentation.layouts.DisplayMode
import ir.kazemcodes.infinity.feature_library.presentation.components.LibraryEvents
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect


class LibraryViewModel(
    private val localBookRepository: LocalBookRepository,
    private val preferencesUseCase: PreferencesUseCase,
) : ScopedServices.Activated, ScopedServices.Registered {

    private val _state = mutableStateOf(LibraryState())
    val state: State<LibraryState> = _state

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _books = MutableStateFlow<PagingData<Book>>(PagingData.empty())
    val book = _books

    fun getBooks() {
        coroutineScope.launch(Dispatchers.IO) {
            localBookRepository.getBooks().cachedIn(coroutineScope)
                .collect { snapshot ->
                    _books.value = snapshot.map { bookEntity -> bookEntity.toBook() }
                }
        }
    }

    override fun onServiceRegistered() {
        getBooks()

        readLayoutType()
    }
    fun searchBook(query: String) {
        coroutineScope.launch(Dispatchers.IO) {
            localBookRepository.searchInLibraryScreenBooks(query).cachedIn(coroutineScope)
                .collect { snapshot ->
                    _books.value = snapshot.map { bookEntity -> bookEntity.toBook() }
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
        if (inSearchMode == false) {
            _state.value = state.value.copy(searchedBook = emptyList(), searchQuery = "")
            getBooks()
        }
    }


    private fun updateLayoutType(layoutType: DisplayMode) {
        preferencesUseCase.saveLibraryLayoutUseCase(layoutType.layoutIndex)
        _state.value = state.value.copy(layout = layoutType.layout)

    }

    private fun readLayoutType() {
        _state.value =
            state.value.copy(layout = preferencesUseCase.readLibraryLayoutUseCase().layout)
    }


    

    private fun deleteNotInLibraryChapters() {
        coroutineScope.launch(Dispatchers.IO) {
            localBookRepository.deleteChapters()
        }
    }



    override fun onServiceActive() {

        deleteNotInLibraryChapters()
    }

    override fun onServiceInactive() {

    }


}
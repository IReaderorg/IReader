package ir.kazemcodes.infinity.presentation.library

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.zhuinden.simplestack.ScopedServices
import ir.kazemcodes.infinity.domain.models.remote.Book
import ir.kazemcodes.infinity.domain.use_cases.local.LocalUseCase
import ir.kazemcodes.infinity.domain.use_cases.preferences.PreferencesUseCase
import ir.kazemcodes.infinity.presentation.layouts.DisplayMode
import ir.kazemcodes.infinity.presentation.library.components.LibraryEvents
import ir.kazemcodes.infinity.util.Resource
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


class LibraryViewModel(
    private val localUseCase: LocalUseCase,
    private val preferencesUseCase: PreferencesUseCase,
) : ScopedServices.Registered {
    private val _state = mutableStateOf<LibraryState>(LibraryState())
    val state: State<LibraryState> = _state
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)


    override fun onServiceRegistered() {
        deleteNotInLibraryBooks()
        deleteNotInLibraryChapters()
        getLocalBooks()
        readLayoutType()
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
            is LibraryEvents.SearchBooks -> {
                searchBook(event.query)
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

    private fun getLocalBooks() {

        localUseCase.getInLibraryBooksUseCase().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _state.value = state.value.copy(
                        books = result.data ?: emptyList()
                    )
                }
                is Resource.Error -> {
                    _state.value =
                        state.value.copy(error = result.message ?: "Empty")
                }
                is Resource.Loading -> {

                    _state.value = state.value.copy(isLoading = true)
                }
            }
        }.launchIn(coroutineScope)
    }


    private fun searchBook(query: String) {
        val searchBook = mutableListOf<Book>()
        state.value.books.forEach { book ->
            if (book.bookName.contains(query, ignoreCase = true)) {
                searchBook.add(book)
            }
        }
        _state.value = state.value.copy(searchedBook = searchBook)
    }

    private fun deleteNotInLibraryChapters() {
        coroutineScope.launch(Dispatchers.IO) {
            localUseCase.deleteNotInLibraryLocalChaptersUseCase()
        }
    }

    private fun deleteNotInLibraryBooks() {
        coroutineScope.launch(Dispatchers.IO) {
            localUseCase.deleteNotInLibraryBooksUseCase()
        }
    }


}
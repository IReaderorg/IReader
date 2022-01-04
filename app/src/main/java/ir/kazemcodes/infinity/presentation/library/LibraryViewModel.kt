package ir.kazemcodes.infinity.presentation.library

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.zhuinden.simplestack.ScopedServices
import ir.kazemcodes.infinity.domain.models.remote.Book
import ir.kazemcodes.infinity.domain.use_cases.datastore.DataStoreUseCase
import ir.kazemcodes.infinity.domain.use_cases.local.LocalUseCase
import ir.kazemcodes.infinity.presentation.layouts.DisplayMode
import ir.kazemcodes.infinity.presentation.library.components.LibraryEvents
import ir.kazemcodes.infinity.util.Resource
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


class LibraryViewModel(
    private val localUseCase: LocalUseCase,
    private val dataStoreUseCase: DataStoreUseCase,
) : ScopedServices.Registered {
    private val _state = mutableStateOf<LibraryState>(LibraryState())
    val state: State<LibraryState> = _state
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)


    override fun onServiceRegistered() {
        onEvent(LibraryEvents.GetLocalBooks)
        readLayoutType()
    }

    override fun onServiceUnregistered() {
        coroutineScope.cancel()
    }

    fun onEvent(event: LibraryEvents) {
        when (event) {
            is LibraryEvents.GetLocalBooks -> {
                getLocalBooks()
            }
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
        _state.value = state.value.copy(layout = layoutType.layout)
        coroutineScope.launch(Dispatchers.Main) {
            dataStoreUseCase.saveLibraryLayoutUseCase(layoutType.layoutIndex)
        }
    }

    private fun readLayoutType() {
        coroutineScope.launch(Dispatchers.Main) {
            dataStoreUseCase.readLibraryLayoutUseCase().collectLatest { result ->
                if (result.data != null) {
                    _state.value = state.value.copy(layout = result.data.layout)
                }
            }
        }

    }

    private fun getLocalBooks() {

        localUseCase.getLocalBooksUseCase().onEach { result ->

            when (result) {
                is Resource.Success -> {
                    _state.value = LibraryState(
                        books = result.data?.map { it.copy(inLibrary = true) } ?: emptyList()
                    )
                }
                is Resource.Error -> {
                    _state.value =
                        LibraryState(error = result.message ?: "Empty")
                }
                is Resource.Loading -> {

                    _state.value = LibraryState(isLoading = true)
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


}
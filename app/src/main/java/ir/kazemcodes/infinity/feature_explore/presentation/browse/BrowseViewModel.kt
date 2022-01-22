package ir.kazemcodes.infinity.feature_explore.presentation.browse

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.zhuinden.simplestack.ScopedServices
import ir.kazemcodes.infinity.core.data.network.models.BooksPage
import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.repository.LocalBookRepository
import ir.kazemcodes.infinity.core.domain.repository.RemoteRepository
import ir.kazemcodes.infinity.core.domain.use_cases.preferences.PreferencesUseCase
import ir.kazemcodes.infinity.core.presentation.layouts.DisplayMode
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect


sealed class ExploreType(val mode: Int) {
    object Latest : ExploreType(0)
    object Popular : ExploreType(1)
    object Search : ExploreType(1)
}

class BrowseViewModel(
    private val preferencesUseCase: PreferencesUseCase,
    private val source: Source,
    private val exploreType: ExploreType,
    private val localBookRepository: LocalBookRepository,
    private val remoteRepository: RemoteRepository,
) : ScopedServices.Registered {

    private val _state = mutableStateOf<BrowseScreenState>(BrowseScreenState())

    val state: State<BrowseScreenState> = _state


    private val _books = MutableStateFlow<PagingData<Book>>(PagingData.empty())
    val books = _books

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var getBooksJob: Job? = null

    override fun onServiceRegistered() {
        getBooks()
        readLayoutType()
    }

    fun getSource(): Source {

        return source
    }

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

    fun getBooks(query: String?=null,type: ExploreType?=null) {
        getBooksJob?.cancel()
        getBooksJob = coroutineScope.launch(Dispatchers.IO) {
            remoteRepository.getRemoteBooksUseCase(source, type?:exploreType, query = query).cachedIn(coroutineScope)
                .collect { snapshot ->
                    _books.value = snapshot.map { bookEntity -> bookEntity.toBook() }
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
        _state.value = state.value.copy(isSearchModeEnable = inSearchMode ?: !state.value.isSearchModeEnable)
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


    override fun onServiceUnregistered() {
        coroutineScope.cancel()
    }

}
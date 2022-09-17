package ireader.ui.home.explore.viewmodel

import androidx.lifecycle.viewModelScope
import ireader.common.models.entities.Book
import ireader.common.models.entities.BookItem
import ireader.common.models.entities.toBook
import ireader.common.resources.SourceNotFoundException
import ireader.common.resources.UiText
import ireader.core.api.log.Log
import ireader.core.api.source.model.Filter
import ireader.core.api.source.model.MangasPageInfo
import ireader.core.ui.viewmodel.BaseViewModel
import ireader.domain.catalogs.interactor.GetLocalCatalogs
import ireader.domain.models.DisplayMode
import ireader.domain.usecases.local.LocalInsertUseCases
import ireader.domain.usecases.preferences.reader_preferences.BrowseScreenPrefUseCase
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.domain.utils.exceptionHandler
import ireader.domain.utils.extensions.DefaultPaginator
import ireader.presentation.R
import ireader.ui.component.Controller
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class ExploreViewModel(
    private val state: ExploreStateImpl,
    private val remoteUseCases: RemoteUseCases,
    private val catalogStore: GetLocalCatalogs,
    private val browseScreenPrefUseCase: BrowseScreenPrefUseCase,
    val insertUseCases: LocalInsertUseCases,
    private val param: Param,
    val booksState: BooksState
) : BaseViewModel(), ExploreState by state {
    data class Param(val sourceId: Long?, val query: String?)
    companion object {
        fun createParam(controller: Controller): Param {
            return Param(
                controller.navBackStackEntry.arguments?.getLong("sourceId"),
                controller.navBackStackEntry.arguments?.getString("query")
            )
        }
    }

    init {
        booksState.empty()
        val sourceId = param.sourceId
        val query = param.query
        val catalog =
            catalogStore.find(sourceId)

        state.catalog = catalog
        val source = state.source
        if (sourceId != null && source != null) {
            if (!query.isNullOrBlank()) {
                toggleSearchMode(true)
                searchQuery = query
                loadItems()
            } else {
                val listings = source.getListings()
                if (listings.isNotEmpty()) {
                    state.stateListing = source.getListings().first()
                    loadItems()
                    viewModelScope.launch {
                        readLayoutType()
                    }
                } else {
                    viewModelScope.launch {
                        showSnackBar(UiText.StringResource(R.string.the_source_is_not_found))
                    }
                }
            }
        } else {
            viewModelScope.launch {
                showSnackBar(UiText.StringResource(R.string.the_source_is_not_found))
            }
        }
    }

    fun loadItems(reset: Boolean = false) {
        getBooksJob?.cancel()
        if (reset) {
            page = 1
            booksState.books = emptyList()
        }
        getBooksJob = viewModelScope.launch {
            kotlin.runCatching {
                DefaultPaginator<Int, MangasPageInfo>(
                    initialKey = state.page,
                    onLoadUpdated = {
                        isLoading = it
                    },
                    onRequest = { nextPage ->
                        try {
                            error = null
                            Log.debug { "Explore Request was made - current page:$nextPage" }

                            val query = searchQuery
                            val filters = stateFilters
                            val listing = stateListing
                            val source = catalog
                            if (source != null) {
                                var result = MangasPageInfo(emptyList(), false)
                                remoteUseCases.getRemoteBooks(
                                    searchQuery,
                                    listing,
                                    filters,
                                    source,
                                    page,
                                    onError = {
                                        throw it
                                    },
                                    onSuccess = { res ->
                                        result = res
                                    }
                                )
                                Result.success(result)
                            } else {
                                throw SourceNotFoundException()
                            }
                        } catch (e: Throwable) {
                            Result.failure(e)
                        }
                    },
                    getNextKey = {
                        state.page + 1
                    },
                    onError = { e ->
                        endReached = true
                        booksState.books = emptyList()
                        e?.let {
                            error = exceptionHandler(it)
                        }
                    },
                    onSuccess = { items, newKey ->
                        val books = items.mangas.map {
                            it.toBook(
                                sourceId = source?.id ?: -1,
                            )
                        }
                        booksState.books = booksState.books + books

                        page = newKey
                        endReached = !items.hasNextPage
                    },
                ).loadNextItems()
            }
        }
    }

    private var getBooksJob: Job? = null

    suspend fun addToFavorite(bookItem: BookItem, onFavorite: (Book) -> Unit) {
        val favorite = !bookItem.favorite
        val book = bookItem.toBook().copy(favorite = favorite)
        val bookId = insertUseCases.insertBook(book)
        onFavorite(book.copy(id = bookId))
    }


    fun toggleSearchMode(inSearchMode: Boolean) {
        state.isSearchModeEnable = inSearchMode

        if (!inSearchMode && source != null) {
            exitSearchedMode()
            searchQuery?.let { query ->
                stateFilters = listOf(Filter.Title().apply { this.value = query })
                loadItems()
            }
        }
    }

    private fun exitSearchedMode() {
        state.searchQuery = ""
        state.apply {
            searchQuery = ""
            isLoading = false
            error = null
        }
    }

    fun saveLayoutType(layoutType: DisplayMode) {
        state.layout = layoutType
        browseScreenPrefUseCase.browseLayoutTypeUseCase.save(layoutType)
    }

    private suspend fun readLayoutType() {
        state.layout = browseScreenPrefUseCase.browseLayoutTypeUseCase.read()
    }

    fun toggleFilterMode(enable: Boolean? = null) {
        state.isFilterEnable = enable ?: !state.isFilterEnable
    }
}

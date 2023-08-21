package ireader.presentation.ui.home.explore.viewmodel


import ireader.core.log.Log
import ireader.core.source.model.Filter
import ireader.core.source.model.MangasPageInfo
import ireader.domain.catalogs.interactor.GetLocalCatalogs
import ireader.domain.models.DisplayMode
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.BookItem
import ireader.domain.models.entities.toBook
import ireader.domain.preferences.prefs.LibraryPreferences
import ireader.domain.usecases.local.book_usecases.FindDuplicateBook
import ireader.domain.usecases.preferences.reader_preferences.BrowseScreenPrefUseCase
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.domain.utils.exceptionHandler
import ireader.domain.utils.extensions.DefaultPaginator
import ireader.domain.utils.fastMap
import ireader.i18n.SourceNotFoundException
import ireader.i18n.UiText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class ExploreViewModel(
    private val state: ExploreStateImpl,
    private val remoteUseCases: RemoteUseCases,
    private val catalogStore: GetLocalCatalogs,
    private val browseScreenPrefUseCase: BrowseScreenPrefUseCase,
    val insertUseCases: ireader.domain.usecases.local.LocalInsertUseCases,
    private val param: Param,
    private val findDuplicateBook: FindDuplicateBook,
    val booksState: BooksState,
    private val libraryPreferences: LibraryPreferences,
) : ireader.presentation.ui.core.viewmodel.BaseViewModel(), ExploreState by state {
    data class Param(val sourceId: Long?, val query: String?)


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
                    scope.launch {
                        readLayoutType()
                    }
                } else {
                    scope.launch {
                        showSnackBar(UiText.MStringResource { xml -> xml.theSourceIsNotFound })
                    }
                }
            }
        } else {
            scope.launch {
                showSnackBar(UiText.MStringResource { xml -> xml.theSourceIsNotFound })
            }
        }
    }

    fun loadItems(reset: Boolean = false) {
        getBooksJob?.cancel()
        if (reset) {
            page = 1
            booksState.books = emptyList()
        }
        getBooksJob = scope.launch {
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

                            booksState.books = booksState.books.fastMap { book ->
                                findDuplicateBook(book.title, book.sourceId) ?: book
                            }

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
    fun getColumnsForOrientation(isLandscape: Boolean, scope: CoroutineScope): StateFlow<Int> {
        return if (isLandscape) {
            libraryPreferences.columnsInLandscape()
        } else {
            libraryPreferences.columnsInPortrait()
        }.stateIn(scope)
    }
}

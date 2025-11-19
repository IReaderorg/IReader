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
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.min


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
    private val openLocalFolder: ireader.domain.usecases.local.OpenLocalFolder,
    private val syncUseCases: ireader.domain.usecases.sync.SyncUseCases? = null,
    private val filterStateManager: ireader.domain.filters.FilterStateManager? = null
) : ireader.presentation.ui.core.viewmodel.BaseViewModel(), ExploreState by state {
    data class Param(val sourceId: Long?, val query: String?)


    init {
        booksState.empty()
        val sourceId = param.sourceId
        val query = param.query
        
        val catalog = catalogStore.find(sourceId)
        
        if (catalog == null) {
            Log.error { "[ExploreViewModel] Catalog not found for sourceId: $sourceId" }
        }

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
                        showSnackBar(UiText.MStringResource(Res.string.the_source_is_not_found))
                    }
                }
            }
        } else {
            scope.launch {
                showSnackBar(UiText.MStringResource(Res.string.the_source_is_not_found))
            }
        }
    }

    fun loadItems(reset: Boolean = false, jsPluginFilters: Map<String, Any>? = null) {
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

                                val query = searchQuery
                                val filters = stateFilters
                                val listing = stateListing
                                val source = catalog
                                if (source != null) {
                                    var result = MangasPageInfo(emptyList(), false)
                                    
                                    // Check if this is a JS plugin source and we have JS plugin filters
                                    val catalogSource = source.source
                                    if (catalogSource is ireader.domain.js.bridge.JSPluginSource && jsPluginFilters != null) {
                                        // Use JS plugin's custom method with filters
                                        val novels = if (query != null && query.isNotBlank()) {
                                            catalogSource.searchNovels(query, page)
                                        } else {
                                            catalogSource.getPopularNovels(page, jsPluginFilters)
                                        }
                                        result = MangasPageInfo(novels, hasNextPage = novels.isNotEmpty())
                                        Result.success(result)
                                    } else {
                                        // Use standard catalog source method
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
                                    }
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
        val updatedBook = book.copy(id = bookId)
        
        // Sync to remote if book is being favorited
        if (favorite) {
            syncUseCases?.syncBookToRemote?.invoke(updatedBook)
        }
        
        onFavorite(updatedBook)
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
        scope.launch {
            browseScreenPrefUseCase.browseLayoutTypeUseCase.save(layoutType)
        }
    }

    private suspend fun readLayoutType() {
        state.layout = browseScreenPrefUseCase.browseLayoutTypeUseCase.read()
    }

    fun toggleFilterMode(enable: Boolean? = null) {
        state.isFilterEnable = enable ?: !state.isFilterEnable
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getColumnsForOrientation(isLandscape: Boolean, scope: CoroutineScope): StateFlow<Int> {
        // Default columns should always be 2 in portrait for all grid layouts
        val defaultColumns = if (isLandscape) {
            // In landscape, we can allow more columns
            if (layout == DisplayMode.ComfortableGrid) 3 else 2
        } else {
            // In portrait, always use 2 columns for any grid layout
            2
        }
        
        // Use user preferences, but ensure we're not using too many columns for non-ComfortableGrid layouts
        val columns = if (isLandscape) {
            libraryPreferences.columnsInLandscape()
        } else {
            if (layout == DisplayMode.ComfortableGrid) {
                libraryPreferences.columnsInPortrait()
            } else {
                libraryPreferences.columnsInPortraitCompact()
            }
        }.stateIn(scope)
        
        return if (layout != DisplayMode.ComfortableGrid) {
            // For non-comfortable layouts, ensure maximum column count is restricted
            val maxColumns = if (isLandscape) 3 else 2
            columns.mapLatest { min(it, maxColumns) }.stateIn(scope, SharingStarted.Eagerly, defaultColumns)
        } else {
            columns
        }
    }
    
    fun openLocalFolderAction(): Boolean {
        return try {
            openLocalFolder.open()
        } catch (e: Exception) {
            Log.error { "Failed to open local folder" }
            false
        }
    }
    
    fun getLocalFolderPath(): String {
        return openLocalFolder.getPath()
    }
    
    /**
     * Saves JS plugin filter state for the current source.
     */
    fun saveJSPluginFilterState(filters: Map<String, Any>) {
        val sourceId = catalog?.sourceId ?: return
        scope.launch {
            filterStateManager?.saveFilterState(sourceId, filters)
        }
    }
    
    /**
     * Loads JS plugin filter state for the current source.
     */
    suspend fun loadJSPluginFilterState(): Map<String, Any> {
        val sourceId = catalog?.sourceId ?: return emptyMap()
        return filterStateManager?.loadFilterState(sourceId) ?: emptyMap()
    }
    
    /**
     * Clears JS plugin filter state for the current source.
     */
    fun clearJSPluginFilterState() {
        val sourceId = catalog?.sourceId ?: return
        scope.launch {
            filterStateManager?.clearFilterState(sourceId)
        }
    }
}

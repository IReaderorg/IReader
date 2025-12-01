package ireader.presentation.ui.home.explore.viewmodel

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import ireader.core.log.Log
import ireader.core.source.model.Filter
import ireader.core.source.model.Listing
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
import ireader.i18n.SourceNotFoundException
import ireader.i18n.UiText
import ireader.i18n.resources.Res
import ireader.i18n.resources.the_source_is_not_found
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.min

/**
 * ViewModel for the Explore screen following Mihon's StateScreenModel pattern.
 * 
 * Key improvements:
 * - Immutable state with atomic updates via MutableStateFlow
 * - Efficient deduplication using HashSet
 * - Proper job cancellation and lifecycle management
 * - Optimized for low-end devices with minimal allocations
 */
@Stable
class ExploreViewModel(
    private val remoteUseCases: RemoteUseCases,
    private val catalogStore: GetLocalCatalogs,
    private val browseScreenPrefUseCase: BrowseScreenPrefUseCase,
    val insertUseCases: ireader.domain.usecases.local.LocalInsertUseCases,
    private val param: Param,
    private val findDuplicateBook: FindDuplicateBook,
    private val libraryPreferences: LibraryPreferences,
    private val openLocalFolder: ireader.domain.usecases.local.OpenLocalFolder,
    private val syncUseCases: ireader.domain.usecases.sync.SyncUseCases? = null,
    private val filterStateManager: ireader.domain.filters.FilterStateManager? = null
) : BaseViewModel() {
    
    data class Param(val sourceId: Long?, val query: String?)
    
    // Immutable state flow following Mihon's pattern
    private val _state = MutableStateFlow(ExploreScreenState())
    val state: StateFlow<ExploreScreenState> = _state.asStateFlow()
    
    // Track seen books for deduplication (like Mihon's seenManga)
    private val seenBooks = hashSetOf<String>()
    
    // Current loading job for cancellation
    private var loadJob: Job? = null
    
    // Convenience accessors for backward compatibility
    val isLoading: Boolean get() = _state.value.isLoading
    val error: UiText? get() = _state.value.error
    val layout: DisplayMode get() = _state.value.layout
    val isSearchModeEnable: Boolean get() = _state.value.isSearchModeEnabled
    var searchQuery: String?
        get() = _state.value.searchQuery
        set(value) { _state.update { it.copy(searchQuery = value) } }
    val source: ireader.core.source.CatalogSource? get() = _state.value.source
    val catalog: ireader.domain.models.entities.CatalogLocal? get() = _state.value.catalog
    val isFilterEnable: Boolean get() = _state.value.isFilterEnabled
    var modifiedFilter: List<Filter<*>>
        get() = _state.value.modifiedFilters
        set(value) { _state.update { it.copy(modifiedFilters = value) } }
    val page: Int get() = _state.value.page
    var endReached: Boolean
        get() = _state.value.endReached
        set(value) { _state.update { it.copy(endReached = value) } }
    var stateFilters: List<Filter<*>>?
        get() = _state.value.appliedFilters
        set(value) { _state.update { it.copy(appliedFilters = value) } }
    var stateListing: Listing?
        get() = _state.value.currentListing
        set(value) { _state.update { it.copy(currentListing = value) } }
    var savedScrollIndex: Int
        get() = _state.value.savedScrollIndex
        set(value) { _state.update { it.copy(savedScrollIndex = value) } }
    var savedScrollOffset: Int
        get() = _state.value.savedScrollOffset
        set(value) { _state.update { it.copy(savedScrollOffset = value) } }
    
    // Books state for backward compatibility
    val booksState = BooksStateHolder(_state)
    
    init {
        initializeSource()
    }
    
    private fun initializeSource() {
        val sourceId = param.sourceId
        val query = param.query
        
        val catalog = catalogStore.find(sourceId)
        
        if (catalog == null) {
            Log.error { "[ExploreViewModel] Catalog not found for sourceId: $sourceId" }
            scope.launch {
                showSnackBar(UiText.MStringResource(Res.string.the_source_is_not_found))
            }
            return
        }
        
        _state.update { it.copy(catalog = catalog) }
        
        val source = _state.value.source
        if (sourceId != null && source != null) {
            // Initialize filters from source
            _state.update { it.copy(modifiedFilters = source.getFilters()) }
            
            if (!query.isNullOrBlank()) {
                // Start in search mode with query
                _state.update { 
                    it.copy(
                        isSearchModeEnabled = true,
                        searchQuery = query
                    )
                }
                loadItems()
            } else {
                // Start with default listing
                val listings = source.getListings()
                if (listings.isNotEmpty()) {
                    _state.update { it.copy(currentListing = listings.first()) }
                    loadItems()
                    scope.launch { readLayoutType() }
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

    /**
     * Load items with proper pagination and deduplication.
     * Following Mihon's pattern of efficient data loading.
     */
    fun loadItems(reset: Boolean = false) {
        // Cancel any existing load job
        loadJob?.cancel()
        
        if (reset) {
            // Reset pagination state
            seenBooks.clear()
            _state.update { 
                it.copy(
                    page = 1,
                    books = emptyList(),
                    endReached = false,
                    error = null
                )
            }
        }
        
        loadJob = scope.launch {
            val currentState = _state.value
            val catalog = currentState.catalog ?: return@launch
            
            // Set loading state
            _state.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = fetchBooks(
                    catalog = catalog,
                    query = currentState.searchQuery,
                    listing = currentState.currentListing,
                    filters = currentState.appliedFilters,
                    page = currentState.page
                )
                
                result.fold(
                    onSuccess = { pageInfo ->
                        processSuccessResult(pageInfo)
                    },
                    onFailure = { error ->
                        processErrorResult(error)
                    }
                )
            } catch (e: Exception) {
                processErrorResult(e)
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
    
    /**
     * Fetch books from remote source
     */
    private suspend fun fetchBooks(
        catalog: ireader.domain.models.entities.CatalogLocal,
        query: String?,
        listing: Listing?,
        filters: List<Filter<*>>?,
        page: Int
    ): Result<MangasPageInfo> = withContext(Dispatchers.IO) {
        try {
            var result = MangasPageInfo(emptyList(), false)
            
            remoteUseCases.getRemoteBooks(
                query = query,
                listing = listing,
                filters = filters,
                catalog = catalog,
                page = page,
                onError = { throw it },
                onSuccess = { res -> result = res }
            )
            
            Result.success(result)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
    
    /**
     * Process successful fetch result with deduplication
     * 
     * NOTE: We do NOT check for duplicates in the database here.
     * This is intentional - we want to show fresh data from the source.
     * When the user clicks on a book, we'll insert/upsert it then.
     * This fixes the issue where stale database entries caused BookDetail to fail.
     */
    private suspend fun processSuccessResult(pageInfo: MangasPageInfo) {
        val sourceId = source?.id ?: return
        
        // Convert and deduplicate books efficiently (in-memory only)
        val newBooks = withContext(Dispatchers.Default) {
            pageInfo.mangas
                .filter { manga -> 
                    // Deduplicate by URL (like Mihon's seenManga) - in-memory only
                    val key = "${manga.key}_$sourceId"
                    seenBooks.add(key)
                }
                .map { manga -> 
                    // Just convert to Book, don't check database
                    // Database check happens when user clicks on the book
                    manga.toBook(sourceId = sourceId)
                }
        }
        
        _state.update { currentState ->
            currentState.copy(
                books = currentState.books + newBooks,
                page = currentState.page + 1,
                endReached = !pageInfo.hasNextPage,
                error = null
            )
        }
    }
    
    /**
     * Process error result
     */
    private fun processErrorResult(error: Throwable) {
        Log.error { "[ExploreViewModel] Error loading books: ${error.message}" }
        
        val errorText = exceptionHandler(error)
        
        _state.update { currentState ->
            currentState.copy(
                error = errorText,
                endReached = true
            )
        }
    }
    
    /**
     * Add book to favorites
     */
    suspend fun addToFavorite(bookItem: BookItem, onFavorite: (Book) -> Unit) {
        val favorite = !bookItem.favorite
        val book = bookItem.toBook().copy(favorite = favorite)
        val bookId = insertUseCases.insertBook(book)
        val updatedBook = book.copy(id = bookId)
        
        // Update state with new book
        updateBookInState(updatedBook)
        
        // Sync to remote if book is being favorited
        if (favorite) {
            syncUseCases?.syncBookToRemote?.invoke(updatedBook)
        }
        
        onFavorite(updatedBook)
    }
    
    /**
     * Update a book in the current state
     */
    private fun updateBookInState(book: Book) {
        _state.update { currentState ->
            val updatedBooks = currentState.books.map { existingBook ->
                if (existingBook.key == book.key) book else existingBook
            }
            currentState.copy(books = updatedBooks)
        }
    }
    
    /**
     * Toggle search mode
     */
    fun toggleSearchMode(enabled: Boolean) {
        _state.update { it.copy(isSearchModeEnabled = enabled) }
        
        if (!enabled && source != null) {
            exitSearchMode()
        }
    }
    
    private fun exitSearchMode() {
        val query = _state.value.searchQuery
        _state.update { 
            it.copy(
                searchQuery = "",
                isLoading = false,
                error = null
            )
        }
        
        if (!query.isNullOrBlank()) {
            _state.update { 
                it.copy(appliedFilters = listOf(Filter.Title().apply { this.value = query }))
            }
            loadItems()
        }
    }
    
    /**
     * Save layout type preference
     */
    fun saveLayoutType(layoutType: DisplayMode) {
        _state.update { it.copy(layout = layoutType) }
        scope.launch {
            browseScreenPrefUseCase.browseLayoutTypeUseCase.save(layoutType)
        }
    }
    
    private suspend fun readLayoutType() {
        val layout = browseScreenPrefUseCase.browseLayoutTypeUseCase.read()
        _state.update { it.copy(layout = layout) }
    }
    
    /**
     * Toggle filter mode
     */
    fun toggleFilterMode(enable: Boolean? = null) {
        _state.update { 
            it.copy(isFilterEnabled = enable ?: !it.isFilterEnabled)
        }
    }
    
    /**
     * Get columns for orientation with proper flow handling
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getColumnsForOrientation(isLandscape: Boolean, scope: CoroutineScope): StateFlow<Int> {
        val layout = _state.value.layout
        val defaultColumns = if (isLandscape) {
            if (layout == DisplayMode.ComfortableGrid) 3 else 2
        } else {
            2
        }
        
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
            val maxColumns = if (isLandscape) 3 else 2
            columns.mapLatest { min(it, maxColumns) }
                .stateIn(scope, SharingStarted.Eagerly, defaultColumns)
        } else {
            columns
        }
    }
    
    /**
     * Open local folder action
     */
    fun openLocalFolderAction(): Boolean {
        return try {
            openLocalFolder.open()
        } catch (e: Exception) {
            Log.error { "Failed to open local folder" }
            false
        }
    }
    
    fun getLocalFolderPath(): String = openLocalFolder.getPath()
    
    /**
     * Save JS plugin filter state
     */
    fun saveJSPluginFilterState(filters: Map<String, Any>) {
        val sourceId = catalog?.sourceId ?: return
        scope.launch {
            filterStateManager?.saveFilterState(sourceId, filters)
        }
    }
    
    /**
     * Load JS plugin filter state
     */
    suspend fun loadJSPluginFilterState(): Map<String, Any> {
        val sourceId = catalog?.sourceId ?: return emptyMap()
        return filterStateManager?.loadFilterState(sourceId) ?: emptyMap()
    }
    
    /**
     * Clear JS plugin filter state
     */
    fun clearJSPluginFilterState() {
        val sourceId = catalog?.sourceId ?: return
        scope.launch {
            filterStateManager?.clearFilterState(sourceId)
        }
    }
    
    override fun onDestroy() {
        loadJob?.cancel()
        seenBooks.clear()
        super.onDestroy()
    }
}

/**
 * Books state holder for backward compatibility with existing UI code.
 * Provides mutable-like interface over immutable state.
 */
@Stable
class BooksStateHolder(private val stateFlow: MutableStateFlow<ExploreScreenState>) {
    
    var books: List<Book>
        get() = stateFlow.value.books
        set(value) { stateFlow.update { it.copy(books = value) } }
    
    var book: Book? = null
    
    fun replaceBook(book: Book?) {
        if (book != null) {
            stateFlow.update { currentState ->
                val updatedBooks = currentState.books.map { existingBook ->
                    if (existingBook.key == book.key) book else existingBook
                }
                currentState.copy(books = updatedBooks)
            }
            this.book = book
        }
    }
    
    fun empty() {
        stateFlow.update { it.copy(books = emptyList()) }
        book = null
    }
}

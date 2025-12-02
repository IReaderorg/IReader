package ireader.presentation.ui.book.viewmodel

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import ireader.core.log.Log
import ireader.core.source.CatalogSource
import ireader.core.source.Source
import ireader.core.source.model.Command
import ireader.core.source.model.CommandList
import ireader.domain.catalogs.CatalogStore
import ireader.domain.catalogs.interactor.GetLocalCatalog
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.isObsolete
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.services.common.DownloadService
import ireader.domain.services.common.ServiceResult
import ireader.domain.services.platform.ClipboardService
import ireader.domain.services.platform.ShareService
import ireader.domain.usecases.book.BookUseCases
import ireader.domain.usecases.chapter.ChapterUseCases
import ireader.domain.usecases.epub.EpubCreator
import ireader.domain.usecases.epub.ExportBookAsEpubUseCase
import ireader.domain.usecases.epub.ExportNovelAsEpubUseCase
import ireader.domain.usecases.history.HistoryUseCase
import ireader.domain.usecases.local.DeleteUseCase
import ireader.domain.usecases.local.LocalGetBookUseCases
import ireader.domain.usecases.local.LocalGetChapterUseCase
import ireader.domain.usecases.local.LocalInsertUseCases
import ireader.domain.usecases.prefetch.BookPrefetchService
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.domain.usecases.source.CheckSourceAvailabilityUseCase
import ireader.domain.usecases.source.MigrateToSourceUseCase
import ireader.domain.usecases.sync.SyncUseCases
import ireader.domain.utils.extensions.withIOContext
import ireader.domain.utils.extensions.withUIContext
import ireader.i18n.UiText
import ireader.presentation.ui.book.components.ExportOptions
import ireader.presentation.ui.book.helpers.PlatformHelper
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * BookDetailViewModel using sealed state pattern (Mihon architecture).
 * 
 * Key optimizations:
 * - Single StateFlow for all UI state
 * - Immutable collections for Compose optimization
 * - Clear loading/success/error states
 * - Event-driven side effects via SharedFlow
 */
@Stable
class BookDetailViewModel(
    private val localInsertUseCases: LocalInsertUseCases,
    private val getChapterUseCase: LocalGetChapterUseCase,
    private val getBookUseCases: LocalGetBookUseCases,
    private val remoteUseCases: RemoteUseCases,
    private val getLocalCatalog: GetLocalCatalog,
    private val deleteUseCase: DeleteUseCase,
    private val applicationScope: CoroutineScope,
    val createEpub: EpubCreator,
    val exportNovelAsEpub: ExportNovelAsEpubUseCase,
    private val exportBookAsEpubUseCase: ExportBookAsEpubUseCase,
    val historyUseCase: HistoryUseCase,
    val readerPreferences: ReaderPreferences,
    private val param: Param,
    private val checkSourceAvailabilityUseCase: CheckSourceAvailabilityUseCase,
    private val migrateToSourceUseCase: MigrateToSourceUseCase,
    private val catalogStore: CatalogStore,
    private val syncUseCases: SyncUseCases? = null,
    private val downloadService: DownloadService,
    private val bookUseCases: BookUseCases,
    private val chapterUseCases: ChapterUseCases,
    private val clipboardService: ClipboardService,
    private val shareService: ShareService,
    private val platformHelper: PlatformHelper,
    private val bookPrefetchService: BookPrefetchService? = null,
    val insertUseCases: LocalInsertUseCases = localInsertUseCases,
) : BaseViewModel() {

    data class Param(val bookId: Long?)

    // ==================== State Management ====================
    
    private val _state = MutableStateFlow<BookDetailState>(BookDetailState.Loading)
    val state: StateFlow<BookDetailState> = _state.asStateFlow()
    
    private val _events = MutableSharedFlow<BookDetailEvent>()
    val events = _events.asSharedFlow()
    
    // Jobs for cancellation
    private var getBookDetailJob: Job? = null
    private var getChapterDetailJob: Job? = null
    private var subscriptionJob: Job? = null
    
    // Preferences - exposed for UI compatibility
    var filters = mutableStateOf<List<ChaptersFilters>>(ChaptersFilters.getDefault(true))
    var sorting = mutableStateOf<ChapterSort>(loadSortingPreference())
    var layout by readerPreferences.showChapterNumberPreferences().asState()
    
    // Selection state - using SnapshotStateList for compatibility with existing UI
    var selection: SnapshotStateList<Long> = mutableStateListOf()
    val hasSelection: Boolean get() = selection.isNotEmpty()
    
    // Search state
    var searchMode by mutableStateOf(false)
    var query by mutableStateOf<String?>(null)
    
    // Dialog states
    var showDialog by mutableStateOf(false)
    var showMigrationDialog by mutableStateOf(false)
    var showEpubExportDialog by mutableStateOf(false)
    var availableMigrationSources by mutableStateOf<List<CatalogLocal>>(emptyList())
    
    // Source switching state
    val sourceSwitchingState = SourceSwitchingState()
    
    // Scroll state persistence
    var savedScrollIndex by mutableStateOf(0)
        private set
    var savedScrollOffset by mutableStateOf(0)
        private set
    
    // Derived state from sealed state
    val book: Book? get() = (_state.value as? BookDetailState.Success)?.book
    val chapters: List<Chapter> get() = (_state.value as? BookDetailState.Success)?.chapters ?: emptyList()
    val source: Source? get() = (_state.value as? BookDetailState.Success)?.source
    val catalogSource: CatalogLocal? get() = (_state.value as? BookDetailState.Success)?.catalogSource
    val lastRead: Long? get() = (_state.value as? BookDetailState.Success)?.lastReadChapterId
    val detailIsLoading: Boolean get() = _state.value is BookDetailState.Loading || 
        _state.value is BookDetailState.Placeholder ||
        (_state.value as? BookDetailState.Success)?.isRefreshingBook == true
    val chapterIsLoading: Boolean get() = (_state.value as? BookDetailState.Success)?.isRefreshingChapters == true
    val expandedSummary: Boolean get() = (_state.value as? BookDetailState.Success)?.isSummaryExpanded == true
    val modifiedCommands: List<Command<*>> get() = (_state.value as? BookDetailState.Success)?.modifiedCommands ?: emptyList()

    init {
        val bookId = param.bookId
        if (bookId != null) {
            // OPTIMIZATION: Show placeholder immediately - no shimmer needed
            _state.value = BookDetailState.Placeholder(bookId = bookId)
            initializeBook(bookId)
        } else {
            _state.value = BookDetailState.Error("Invalid book ID")
            scope.launch {
                _events.emit(BookDetailEvent.ShowSnackbar("Something is wrong with this book"))
            }
        }
    }

    // ==================== Initialization ====================
    
    private fun initializeBook(bookId: Long) {
        // OPTIMIZATION: Use immediate dispatcher for first frame
        // This ensures the book data is shown as fast as possible
        scope.launch(kotlinx.coroutines.Dispatchers.Main.immediate) {
            try {
                // OPTIMIZATION: Check prefetch cache first for instant display
                val prefetchedData = bookPrefetchService?.getPrefetchedData(bookId)
                
                if (prefetchedData != null) {
                    // Use prefetched data for instant display
                    Log.info { "Using prefetched data for book $bookId" }
                    val book = prefetchedData.book
                    val chapters = prefetchedData.chapters
                    val lastReadChapterId = prefetchedData.lastReadChapterId
                    
                    // Get catalog source (fast, usually cached)
                    val catalogSource = try {
                        getLocalCatalog.get(book.sourceId)
                    } catch (e: Exception) {
                        Log.error("Failed to get catalog for source ${book.sourceId}", e)
                        null
                    }
                    
                    val source = catalogSource?.source
                    val commands = try {
                        if (source is CatalogSource) {
                            source.getCommands().toImmutableList()
                        } else persistentListOf()
                    } catch (e: Exception) {
                        persistentListOf()
                    }
                    
                    // Set initial state immediately from prefetch cache
                    val initialState = BookDetailState.Success(
                        book = book,
                        chapters = chapters.toImmutableList(),
                        source = source,
                        catalogSource = catalogSource,
                        lastReadChapterId = lastReadChapterId,
                        commands = commands,
                        modifiedCommands = commands,
                    )
                    _state.value = initialState
                    Log.info { "BookDetailState.Success (prefetched): book=${book.title}, chapters=${chapters.size}" }
                    
                    // Subscribe for updates in background
                    subscribeToBookAndChapters(bookId, catalogSource)
                    
                    // Trigger remote fetch if book needs updating or has no chapters
                    val needsRemoteFetch = (book.lastUpdate < 1L || chapters.isEmpty()) && catalogSource?.source != null
                    if (needsRemoteFetch) {
                        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                            if (book.lastUpdate < 1L) {
                                getRemoteBookDetailSilent(book, catalogSource)
                            }
                            getRemoteChapterDetailSilent(book, catalogSource)
                        }
                    }
                    
                    // Check source availability in background
                    scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        checkSourceAvailability(bookId)
                    }
                    return@launch
                }
                
                // No prefetch data - load from database
                // First, try to get the book directly for immediate display
                // Use withContext(IO) only for the actual DB call
                val book = withIOContext { getBookUseCases.findBookById(bookId) }
                val sourceId = book?.sourceId
                
                // OPTIMIZATION: Update placeholder with book info immediately
                if (book != null) {
                    _state.value = BookDetailState.Placeholder(
                        bookId = bookId,
                        title = book.title,
                        cover = book.cover,
                        author = book.author,
                        isLoading = true
                    )
                }
                
                // Get catalog source in parallel with chapters if possible
                val catalogSourceDeferred = if (sourceId != null) {
                    scope.async(kotlinx.coroutines.Dispatchers.IO) {
                        try {
                            getLocalCatalog.get(sourceId)
                        } catch (e: Exception) {
                            Log.error("Failed to get catalog for source $sourceId", e)
                            null
                        }
                    }
                } else null
                
                // If book exists, immediately show it (don't wait for subscription)
                if (book != null) {
                    // Parallel fetch of chapters and history for faster initial display
                    val chaptersDeferred = scope.async(kotlinx.coroutines.Dispatchers.IO) {
                        getChapterUseCase.findChaptersByBookId(bookId)
                    }
                    val historyDeferred = scope.async(kotlinx.coroutines.Dispatchers.IO) {
                        historyUseCase.findHistoryByBookId(bookId)
                    }
                    
                    val catalogSource = catalogSourceDeferred?.await()
                    val chapters = chaptersDeferred.await()
                    val history = historyDeferred.await()
                    
                    val source = catalogSource?.source
                    val commands = try {
                        if (source is CatalogSource) {
                            source.getCommands().toImmutableList()
                        } else persistentListOf()
                    } catch (e: Exception) {
                        persistentListOf()
                    }
                    
                    // Set initial state immediately - this triggers UI update
                    val initialState = BookDetailState.Success(
                        book = book,
                        chapters = chapters.toImmutableList(),
                        source = source,
                        catalogSource = catalogSource,
                        lastReadChapterId = history?.chapterId,
                        commands = commands,
                        modifiedCommands = commands,
                    )
                    _state.value = initialState
                    Log.info { "BookDetailState.Success (immediate): book=${book.title}, chapters=${chapters.size}" }
                    
                    // Subscribe for updates AFTER initial state is set
                    // This runs in background and won't block UI
                    subscribeToBookAndChapters(bookId, catalogSource)
                    
                    // Trigger initial fetch if book needs updating (AFTER subscribing)
                    // Fetch remote data if:
                    // 1. Book has never been updated (lastUpdate < 1L), OR
                    // 2. Book has no chapters yet
                    // This runs in background and won't block UI
                    val needsRemoteFetch = (book.lastUpdate < 1L || chapters.isEmpty()) && catalogSource?.source != null
                    if (needsRemoteFetch) {
                        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                            // Fetch book details if never updated
                            if (book.lastUpdate < 1L) {
                                getRemoteBookDetailSilent(book, catalogSource)
                            }
                            // Always fetch chapters if empty or book never updated
                            getRemoteChapterDetailSilent(book, catalogSource)
                        }
                    }
                } else {
                    // Book not found - subscribe and wait
                    val catalogSource = catalogSourceDeferred?.await()
                    subscribeToBookAndChapters(bookId, catalogSource)
                }
                
                // Check source availability in background
                scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    checkSourceAvailability(bookId)
                }
                
            } catch (e: Exception) {
                Log.error("Error initializing book", e)
                _state.value = BookDetailState.Error(e.message ?: "Failed to load book")
            }
        }
    }
    
    // Fetch functions for initial load - now show loading indicator
    private suspend fun getRemoteBookDetailSilent(book: Book, catalog: CatalogLocal?) {
        updateSuccessState { it.copy(isRefreshingBook = true) }
        getBookDetailJob?.cancel()
        getBookDetailJob = scope.launch {
            remoteUseCases.getBookDetail(
                book = book,
                catalog = catalog,
                onError = { message ->
                    withUIContext {
                        updateSuccessState { it.copy(isRefreshingBook = false) }
                    }
                    message?.let { showSnackBar(it) }
                },
                onSuccess = { resultBook ->
                    withUIContext {
                        updateSuccessState { it.copy(isRefreshingBook = false) }
                    }
                    localInsertUseCases.updateBook.update(resultBook)
                }
            )
        }
    }
    
    private suspend fun getRemoteChapterDetailSilent(book: Book, catalog: CatalogLocal?) {
        withUIContext {
            updateSuccessState { it.copy(isRefreshingChapters = true) }
        }
        getChapterDetailJob?.cancel()
        getChapterDetailJob = scope.launch {
            remoteUseCases.getRemoteChapters(
                book = book,
                catalog = catalog,
                onError = { message ->
                    withUIContext {
                        updateSuccessState { it.copy(isRefreshingChapters = false) }
                    }
                    message?.let { showSnackBar(it) }
                },
                onSuccess = { result ->
                    withUIContext {
                        updateSuccessState { it.copy(isRefreshingChapters = false) }
                    }
                    localInsertUseCases.insertChapters(result)
                },
                commands = emptyList(),
                oldChapters = chapters
            )
        }
    }
    
    private fun subscribeToBookAndChapters(bookId: Long, initialCatalog: CatalogLocal?) {
        // Cancel previous subscription if any
        subscriptionJob?.cancel()
        
        // Track if we've ever received a valid book
        var hasReceivedBook = false
        // Track number of null emissions for timeout
        var nullEmissionCount = 0
        val maxNullEmissions = 10 // After ~10 emissions without book, show error
        
        subscriptionJob = combine(
            getBookUseCases.subscribeBookById(bookId),
            getChapterUseCase.subscribeChaptersByBookId(bookId),
            historyUseCase.subscribeHistoryByBookId(bookId),
        ) { book, chapters, history ->
            if (book != null) {
                hasReceivedBook = true
                nullEmissionCount = 0 // Reset counter
                
                // Safely get catalog and source - extension might not be installed
                val (catalog, source) = try {
                    val cat = getLocalCatalog.get(book.sourceId) ?: initialCatalog
                    cat to cat?.source
                } catch (e: Exception) {
                    Log.error("Failed to load catalog for source ${book.sourceId}", e)
                    initialCatalog to initialCatalog?.source
                }
                
                val commands = try {
                    if (source is CatalogSource) {
                        source.getCommands().toImmutableList()
                    } else persistentListOf()
                } catch (e: Exception) {
                    Log.error("Failed to get commands from source", e)
                    persistentListOf()
                }
                
                // Preserve refreshing state from current state
                val currentState = _state.value as? BookDetailState.Success
                
                BookDetailState.Success(
                    book = book,
                    chapters = chapters.toImmutableList(),
                    source = source,
                    catalogSource = catalog,
                    lastReadChapterId = history?.chapterId,
                    commands = commands,
                    modifiedCommands = currentState?.modifiedCommands ?: commands,
                    // Preserve UI state
                    isRefreshingBook = currentState?.isRefreshingBook ?: false,
                    isRefreshingChapters = currentState?.isRefreshingChapters ?: false,
                    isSummaryExpanded = currentState?.isSummaryExpanded ?: false,
                    selectedChapterIds = currentState?.selectedChapterIds ?: persistentSetOf(),
                    searchQuery = currentState?.searchQuery,
                    isSearchMode = currentState?.isSearchMode ?: false,
                )
            } else if (hasReceivedBook) {
                // Book was deleted after we had it - show error
                BookDetailState.Error("Book not found")
            } else {
                // Book not found yet - might be race condition
                nullEmissionCount++
                if (nullEmissionCount >= maxNullEmissions) {
                    // Timeout - book was never found, show error
                    Log.error { "BookDetailViewModel: Timeout waiting for book $bookId after $nullEmissionCount emissions" }
                    BookDetailState.Error("Book not found in database")
                } else {
                    // Still waiting - return null to filter out
                    null
                }
            }
        }
        .filterNotNull() // Filter out null emissions while waiting for book
        .distinctUntilChanged()
        .catch { e ->
            Log.error("Error subscribing to book data", e)
            _state.value = BookDetailState.Error(e.message ?: "Unknown error")
        }
        .onEach { newState ->
            _state.value = newState
            
            // Log state changes for debugging
            when (newState) {
                is BookDetailState.Success -> {
                    Log.info { "BookDetailState.Success: book=${newState.book.title}, chapters=${newState.chapters.size}" }
                }
                is BookDetailState.Loading -> {
                    Log.info { "BookDetailState.Loading" }
                }
                is BookDetailState.Placeholder -> {
                    Log.info { "BookDetailState.Placeholder: bookId=${newState.bookId}" }
                }
                is BookDetailState.Error -> {
                    Log.error { "BookDetailState.Error: ${newState.message}" }
                }
            }
        }
        .launchIn(scope)
    }

    // ==================== State Update Helpers ====================
    
    private inline fun updateSuccessState(
        crossinline update: (BookDetailState.Success) -> BookDetailState.Success
    ) {
        _state.update { current ->
            when (current) {
                is BookDetailState.Loading -> current
                is BookDetailState.Placeholder -> current
                is BookDetailState.Success -> update(current)
                is BookDetailState.Error -> current
            }
        }
    }
    
    private fun emitEvent(event: BookDetailEvent) {
        scope.launch { _events.emit(event) }
    }

    // ==================== Scroll Position ====================
    
    fun saveScrollPosition(index: Int, offset: Int) {
        savedScrollIndex = index
        savedScrollOffset = offset
    }
    
    fun resetScrollPosition() {
        savedScrollIndex = 0
        savedScrollOffset = 0
    }

    // ==================== Preferences ====================
    
    private fun loadSortingPreference(): ChapterSort {
        val sortTypeName = readerPreferences.chapterSortType().get()
        val isAscending = readerPreferences.chapterSortAscending().get()
        return try {
            val type = ChapterSort.Type.valueOf(sortTypeName)
            ChapterSort(type, isAscending)
        } catch (e: Exception) {
            ChapterSort.default
        }
    }

    private fun saveSortingPreference(sort: ChapterSort) {
        readerPreferences.chapterSortType().set(sort.type.name)
        readerPreferences.chapterSortAscending().set(sort.isAscending)
    }

    fun toggleFilter(type: ChaptersFilters.Type) {
        val newFilters = filters.value.map { filterState ->
            if (type == filterState.type) {
                ChaptersFilters(
                    type,
                    when (filterState.value) {
                        ChaptersFilters.Value.Included -> ChaptersFilters.Value.Excluded
                        ChaptersFilters.Value.Excluded -> ChaptersFilters.Value.Missing
                        ChaptersFilters.Value.Missing -> ChaptersFilters.Value.Included
                    }
                )
            } else {
                filterState
            }
        }
        this.filters.value = newFilters
    }

    fun toggleSort(type: ChapterSort.Type) {
        val currentSort = sorting.value
        val newSort = if (type == currentSort.type) {
            currentSort.copy(isAscending = !currentSort.isAscending)
        } else {
            currentSort.copy(type = type)
        }
        sorting.value = newSort
        saveSortingPreference(newSort)
    }
    
    // ==================== Summary ====================
    
    var expandedSummaryState by mutableStateOf(false)
    
    fun toggleSummaryExpansion() {
        expandedSummaryState = !expandedSummaryState
        updateSuccessState { it.copy(isSummaryExpanded = expandedSummaryState) }
    }


    // ==================== Library Actions ====================
    
    fun toggleInLibrary(book: Book) {
        updateSuccessState { it.copy(isInLibraryLoading = true) }
        
        applicationScope.launch {
            try {
                withIOContext {
                    syncUseCases?.toggleBookInLibrary?.invoke(book)
                        ?: run {
                            if (!book.favorite) {
                                localInsertUseCases.updateBook.update(
                                    book.copy(
                                        favorite = true,
                                        dateAdded = Calendar.getInstance().timeInMillis,
                                    )
                                )
                            } else {
                                deleteUseCase.unFavoriteBook(listOf(book.id))
                            }
                        }
                }
                updateSuccessState { it.copy(isInLibraryLoading = false) }
            } catch (e: Exception) {
                updateSuccessState { it.copy(isInLibraryLoading = false) }
                emitEvent(BookDetailEvent.ShowSnackbar("Failed to update library: ${e.message}"))
            }
        }
    }

    // ==================== Remote Data Fetching ====================
    
    suspend fun getRemoteBookDetail(book: Book?, catalog: CatalogLocal?) {
        if (book == null) return
        updateSuccessState { it.copy(isRefreshingBook = true) }
        
        getBookDetailJob?.cancel()
        getBookDetailJob = scope.launch {
            remoteUseCases.getBookDetail(
                book = book,
                catalog = catalog,
                onError = { message ->
                    withUIContext {
                        updateSuccessState { it.copy(isRefreshingBook = false) }
                        message?.let { showSnackBar(it) }
                    }
                },
                onSuccess = { resultBook ->
                    withUIContext {
                        updateSuccessState { it.copy(isRefreshingBook = false) }
                    }
                    localInsertUseCases.updateBook.update(resultBook)
                }
            )
        }
    }
    
    fun refreshBookFromSource(book: Book? = null, catalog: CatalogLocal? = null) {
        val currentState = _state.value as? BookDetailState.Success
        val targetBook = book ?: currentState?.book ?: return
        val targetCatalog = catalog ?: currentState?.catalogSource
        
        scope.launch {
            getRemoteBookDetail(targetBook, targetCatalog)
        }
    }
    
    suspend fun getRemoteChapterDetail(
        book: Book?,
        catalog: CatalogLocal?,
        commands: CommandList = emptyList()
    ) {
        if (book == null) return
        
        // Only update if we're in Success state, otherwise the loading indicator won't show
        if (_state.value is BookDetailState.Success) {
            Log.info { "getRemoteChapterDetail: Setting isRefreshingChapters = true" }
            updateSuccessState { it.copy(isRefreshingChapters = true) }
        }
        
        getChapterDetailJob?.cancel()
        getChapterDetailJob = scope.launch {
            Log.info { "Fetching remote chapters for book: ${book.title}" }
            remoteUseCases.getRemoteChapters(
                book = book,
                catalog = catalog,
                onError = { message ->
                    Log.error { "Error fetching chapters: $message" }
                    message?.let { showSnackBar(it) }
                    withUIContext {
                        updateSuccessState { it.copy(isRefreshingChapters = false) }
                    }
                },
                onSuccess = { result ->
                    Log.info { "Successfully fetched ${result.size} chapters" }
                    localInsertUseCases.insertChapters(result)
                    withUIContext {
                        updateSuccessState { it.copy(isRefreshingChapters = false) }
                    }
                },
                commands = commands,
                oldChapters = chapters
            )
        }
    }
    
    fun refreshChaptersFromSource(commands: CommandList = emptyList()) {
        val currentState = _state.value as? BookDetailState.Success ?: return
        scope.launch {
            getRemoteChapterDetail(currentState.book, currentState.catalogSource, commands)
        }
    }

    // ==================== Chapter Selection ====================
    
    fun toggleSelection(book: Book) {
        // This is for book selection, but we use chapter selection
    }
    
    fun selectBetween() {
        val selectedIds = selection.toSet()
        val chapterIds = chapters.map { it.id }
        val filteredIds = chapterIds.filter { it in selectedIds }.sorted()
        
        if (filteredIds.isNotEmpty()) {
            val min = filteredIds.first()
            val max = filteredIds.last()
            selection.clear()
            selection.addAll((min..max).toList())
        }
    }
    
    fun selectAllChapters() {
        selection.clear()
        selection.addAll(chapters.map { it.id }.distinct())
    }
    
    fun invertSelection() {
        val selectedIds = selection.toSet()
        val invertedIds = chapters.map { it.id }.filterNot { it in selectedIds }.distinct()
        selection.clear()
        selection.addAll(invertedIds)
    }
    
    fun clearSelection() {
        selection.clear()
    }

    // ==================== Chapter Actions ====================
    
    fun deleteChapters(chapters: List<Chapter>) {
        scope.launch(Dispatchers.IO) {
            try {
                chapterUseCases.deleteChapters(chapters)
                selection.clear()
                emitEvent(BookDetailEvent.ShowSnackbar("Chapters deleted"))
            } catch (e: Exception) {
                emitEvent(BookDetailEvent.ShowSnackbar("Failed to delete chapters: ${e.message}"))
            }
        }
    }

    // ==================== Download Actions ====================
    
    fun downloadChapters() {
        val selectedIds = selection.toList()
        if (selectedIds.isEmpty()) {
            emitEvent(BookDetailEvent.ShowSnackbar("No chapters selected"))
            return
        }
        
        scope.launch {
            when (val result = downloadService.queueChapters(selectedIds)) {
                is ServiceResult.Success -> {
                    emitEvent(BookDetailEvent.ShowSnackbar("${selectedIds.size} chapters queued for download"))
                    selection.clear()
                }
                is ServiceResult.Error -> {
                    emitEvent(BookDetailEvent.ShowSnackbar("Download failed: ${result.message ?: "Unknown error"}"))
                }
                else -> {}
            }
        }
    }
    
    fun startDownloadService(book: Book) {
        scope.launch {
            when (val result = downloadService.queueBooks(listOf(book.id))) {
                is ServiceResult.Success -> {
                    showSnackBar(UiText.DynamicString("Book queued for download"))
                }
                is ServiceResult.Error -> {
                    showSnackBar(UiText.DynamicString("Download failed: ${result.message ?: "Unknown error"}"))
                }
                else -> {}
            }
        }
    }
    
    fun downloadUnreadChapters() {
        val unreadChapterIds = chapters.filter { !it.read }.map { it.id }
        if (unreadChapterIds.isEmpty()) {
            emitEvent(BookDetailEvent.ShowSnackbar("No unread chapters to download"))
            return
        }
        
        scope.launch {
            when (val result = downloadService.queueChapters(unreadChapterIds)) {
                is ServiceResult.Success -> {
                    emitEvent(BookDetailEvent.ShowSnackbar("Downloading ${unreadChapterIds.size} unread chapters"))
                }
                is ServiceResult.Error -> {
                    emitEvent(BookDetailEvent.ShowSnackbar("Download failed: ${result.message ?: "Unknown error"}"))
                }
                else -> {}
            }
        }
    }
    
    fun downloadUndownloadedChapters() {
        val undownloadedChapterIds = chapters.filter { it.content.joinToString("").isBlank() }.map { it.id }
        if (undownloadedChapterIds.isEmpty()) {
            emitEvent(BookDetailEvent.ShowSnackbar("All chapters already downloaded"))
            return
        }
        
        scope.launch {
            when (val result = downloadService.queueChapters(undownloadedChapterIds)) {
                is ServiceResult.Success -> {
                    emitEvent(BookDetailEvent.ShowSnackbar("Downloading ${undownloadedChapterIds.size} chapters"))
                }
                is ServiceResult.Error -> {
                    emitEvent(BookDetailEvent.ShowSnackbar("Download failed: ${result.message ?: "Unknown error"}"))
                }
                else -> {}
            }
        }
    }

    // ==================== Source Switching ====================
    
    private fun checkSourceAvailability(bookId: Long) {
        scope.launch {
            try {
                val result = checkSourceAvailabilityUseCase(bookId)
                result.onSuccess { comparison ->
                    if (comparison != null && comparison.betterSourceId != null) {
                        sourceSwitchingState.sourceComparison = comparison
                        sourceSwitchingState.betterSourceName = catalogStore.get(comparison.betterSourceId)?.name
                        sourceSwitchingState.showBanner = true
                    }
                }
            } catch (e: Exception) {
                Log.error { "Error checking source availability: ${e.message}" }
            }
        }
    }
    
    fun loadMigrationSources() {
        scope.launch {
            try {
                val currentSourceId = book?.sourceId
                availableMigrationSources = catalogStore.catalogs.filter { catalog ->
                    val isNotCurrentSource = catalog.sourceId != currentSourceId
                    val hasSource = catalog.source != null
                    val isEnabled = when (catalog) {
                        is ireader.domain.models.entities.CatalogInstalled -> !catalog.isObsolete
                        else -> true
                    }
                    isNotCurrentSource && hasSource && isEnabled
                }
                showMigrationDialog = true
                
                if (availableMigrationSources.isEmpty()) {
                    showSnackBar(UiText.DynamicString("No alternative sources available"))
                }
            } catch (e: Exception) {
                Log.error("Error loading migration sources", e)
                showSnackBar(UiText.DynamicString("Failed to load sources: ${e.message}"))
            }
        }
    }
    
    fun startMigration(targetSourceId: Long) {
        val currentBook = book ?: return
        val targetSourceName = catalogStore.get(targetSourceId)?.name
        
        showMigrationDialog = false
        sourceSwitchingState.showMigrationDialog = true
        
        scope.launch {
            try {
                migrateToSourceUseCase(currentBook.id, targetSourceId).collect { progress ->
                    sourceSwitchingState.migrationProgress = progress
                    
                    if (progress.isComplete) {
                        if (progress.error == null) {
                            delay(1000)
                            sourceSwitchingState.showMigrationDialog = false
                            sourceSwitchingState.reset()
                            showSnackBar(UiText.DynamicString("Successfully migrated to ${targetSourceName ?: "new source"}"))
                        } else {
                            delay(2000)
                            sourceSwitchingState.showMigrationDialog = false
                            showSnackBar(UiText.DynamicString("Migration failed: ${progress.error}"))
                        }
                    }
                }
            } catch (e: Exception) {
                Log.error("Migration error", e)
                sourceSwitchingState.showMigrationDialog = false
                showSnackBar(UiText.DynamicString("Migration failed: ${e.message}"))
            }
        }
    }
    
    fun migrateToSource() {
        val comparison = sourceSwitchingState.sourceComparison ?: return
        val betterSourceId = comparison.betterSourceId ?: return
        
        sourceSwitchingState.showBanner = false
        startMigration(betterSourceId)
    }
    
    fun dismissSourceSwitchingBanner() {
        sourceSwitchingState.showBanner = false
    }

    // ==================== Share & Clipboard ====================
    
    fun shareBook() {
        val currentBook = book ?: return
        
        scope.launch {
            try {
                val shareText = buildString {
                    append(currentBook.title)
                    if (currentBook.author.isNotBlank()) {
                        append(" by ${currentBook.author}")
                    }
                    append("\n\n")
                    if (currentBook.description.isNotBlank()) {
                        append(currentBook.description)
                        append("\n\n")
                    }
                    append("Read on iReader")
                }
                
                when (val result = shareService.shareText(shareText, currentBook.title)) {
                    is ServiceResult.Error -> {
                        emitEvent(BookDetailEvent.ShowSnackbar("Failed to share: ${result.message}"))
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.error("Error sharing book", e)
                emitEvent(BookDetailEvent.ShowSnackbar("Failed to share: ${e.message}"))
            }
        }
    }
    
    fun copyToClipboard(label: String, text: String) {
        scope.launch {
            when (val result = clipboardService.copyText(text, label)) {
                is ServiceResult.Success -> {
                    emitEvent(BookDetailEvent.ShowSnackbar("Copied to clipboard"))
                }
                is ServiceResult.Error -> {
                    emitEvent(BookDetailEvent.ShowSnackbar("Failed to copy: ${result.message ?: "Unknown error"}"))
                }
                else -> {}
            }
        }
    }


    // ==================== EPUB Export ====================
    
    fun exportAsEpub(options: ExportOptions) {
        val currentBook = book ?: return
        
        scope.launch {
            try {
                emitEvent(BookDetailEvent.ShowSnackbar("Preparing EPUB export..."))
                
                val outputUri = try {
                    platformHelper.createEpubExportUri(currentBook.title, currentBook.author)
                } catch (e: Exception) {
                    Log.error("Failed to create export URI", e)
                    emitEvent(BookDetailEvent.ShowSnackbar("Failed to select save location: ${e.message}"))
                    return@launch
                }
                
                if (outputUri == null) {
                    emitEvent(BookDetailEvent.ShowSnackbar("Export cancelled"))
                    return@launch
                }
                
                val domainOptions = ireader.domain.models.epub.ExportOptions(
                    selectedChapters = options.selectedChapters.toSet(),
                    includeCover = options.includeCover,
                    paragraphSpacing = options.formatOptions.paragraphSpacing,
                    chapterHeadingSize = options.formatOptions.chapterHeadingSize,
                    fontFamily = when (options.formatOptions.typography) {
                        ireader.presentation.ui.book.components.Typography.SERIF -> "serif"
                        ireader.presentation.ui.book.components.Typography.SANS_SERIF -> "sans-serif"
                        else -> "serif"
                    },
                    fontSize = 16
                )
                
                val result = exportBookAsEpubUseCase(
                    bookId = currentBook.id,
                    outputUri = ireader.domain.models.common.Uri.parse(outputUri),
                    options = domainOptions
                ) { progress ->
                    emitEvent(BookDetailEvent.ShowSnackbar(progress))
                }
                
                result.onSuccess { filePath ->
                    emitEvent(BookDetailEvent.ShowSnackbar("EPUB exported successfully"))
                    Log.info { "EPUB export successful: $filePath" }
                }.onFailure { error ->
                    emitEvent(BookDetailEvent.ShowSnackbar("Export failed: ${error.message}"))
                    Log.error("EPUB export failed", error)
                }
            } catch (e: Exception) {
                Log.error("Error in EPUB export", e)
                emitEvent(BookDetailEvent.ShowSnackbar("Export failed: ${e.message}"))
            }
        }
    }

    // ==================== Archive ====================
    
    fun archiveBook(book: Book) {
        applicationScope.launch {
            try {
                bookUseCases.updateArchiveStatus(book.id, isArchived = true)
                showSnackBar(UiText.DynamicString("Book archived: ${book.title}"))
            } catch (e: Exception) {
                showSnackBar(UiText.ExceptionString(e))
            }
        }
    }
    
    fun unarchiveBook(book: Book) {
        applicationScope.launch {
            try {
                bookUseCases.updateArchiveStatus(book.id, isArchived = false)
                showSnackBar(UiText.DynamicString("Book unarchived: ${book.title}"))
            } catch (e: Exception) {
                showSnackBar(UiText.ExceptionString(e))
            }
        }
    }

    // ==================== Navigation Helpers ====================
    
    fun getLastChapterIndex(): Int {
        val lastReadId = lastRead ?: return -1
        return chapters.reversed().indexOfFirst { it.id == lastReadId }
    }

    // ==================== Snackbar ====================
    
    suspend fun showSnackBar(message: UiText) {
        _events.emit(BookDetailEvent.ShowSnackbar(message.toString()))
    }
    
    fun showSnackBar(message: String) {
        scope.launch {
            _events.emit(BookDetailEvent.ShowSnackbar(message))
        }
    }
    
    // ==================== Commands ====================
    
    var modifiedCommandsState: List<Command<*>> by mutableStateOf(emptyList())
    
    fun updateModifiedCommands(commands: CommandList) {
        modifiedCommandsState = commands
        updateSuccessState { it.copy(modifiedCommands = commands.toImmutableList()) }
    }
    
    fun resetCommands() {
        val currentSource = source
        val commands = if (currentSource is CatalogSource) {
            currentSource.getCommands()
        } else emptyList()
        modifiedCommandsState = commands
        updateSuccessState { it.copy(modifiedCommands = commands.toImmutableList()) }
    }
    
    // ==================== Cleanup ====================
    
    override fun onCleared() {
        super.onCleared()
        
        // Cancel all jobs when ViewModel is destroyed
        getBookDetailJob?.cancel()
        getChapterDetailJob?.cancel()
        subscriptionJob?.cancel()
        
        Log.info { "BookDetailViewModel cleared - all jobs cancelled" }
    }
}

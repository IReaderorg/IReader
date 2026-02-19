package ireader.presentation.ui.book.viewmodel

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import ireader.core.log.Log
import ireader.core.source.CatalogSource
import ireader.core.startup.ScreenProfiler
import ireader.core.source.Source
import ireader.core.source.model.Command
import ireader.core.source.model.CommandList
import ireader.core.util.IO
import ireader.domain.catalogs.CatalogStore
import ireader.domain.catalogs.interactor.GetLocalCatalog
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.isObsolete
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.services.book.BookCommand
import ireader.domain.services.book.BookController
import ireader.domain.services.bookdetail.BookDetailController
import ireader.domain.services.bookdetail.BookDetailCommand
import ireader.domain.services.bookdetail.BookDetailEvent as ControllerEvent
import ireader.domain.services.chapter.ChapterCommand
import ireader.domain.services.chapter.ChapterController
import ireader.domain.services.common.DownloadService
import ireader.domain.services.common.ServiceResult
import ireader.domain.services.common.TranslationService
import ireader.domain.services.common.TranslationQueueResult
import ireader.domain.services.common.TranslationProgress
import ireader.domain.services.common.TranslationStatus
import ireader.domain.services.common.ServiceState
import ireader.domain.services.platform.ClipboardService
import ireader.domain.services.platform.ShareService
import ireader.domain.usecases.book.BookDetailUseCases
import ireader.domain.usecases.book.BookUseCases
import ireader.domain.usecases.chapter.ChapterUseCases
import ireader.domain.usecases.epub.EpubCreator
import ireader.domain.usecases.prefetch.BookPrefetchService
import ireader.domain.usecases.source.CheckSourceAvailabilityUseCase
import ireader.domain.usecases.source.MigrateToSourceUseCase
import ireader.domain.usecases.sync.SyncUseCases
import ireader.domain.usecases.translate.TranslateBookMetadataUseCase
import ireader.domain.utils.extensions.currentTimeToLong
import ireader.domain.utils.extensions.ioDispatcher
import ireader.domain.utils.extensions.withIOContext
import ireader.domain.utils.extensions.withUIContext
import ireader.i18n.LocalizeHelper
import ireader.i18n.UiText
import ireader.i18n.asString
import ireader.presentation.ui.book.components.ExportOptions
import ireader.presentation.ui.book.components.EpubExportProgress
import ireader.presentation.ui.book.helpers.PlatformHelper
import ireader.presentation.ui.core.utils.formatDecimal
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

/**
 * BookDetailViewModel using sealed state pattern (Mihon architecture).
 * 
 * Refactored to use BookDetailUseCases aggregate to reduce constructor parameters.
 * Target: ≤12 constructor parameters (Requirements: 1.1, 1.4, 1.5)
 * 
 * Key optimizations:
 * - Single StateFlow for all UI state
 * - Immutable collections for Compose optimization
 * - Clear loading/success/error states
 * - Event-driven side effects via SharedFlow
 */
@Stable
class BookDetailViewModel(
    // Use case aggregate - groups 12 related use cases (Requirements: 1.1, 1.4, 1.5)
    private val bookDetailUseCases: BookDetailUseCases,
    private val getLocalCatalog: GetLocalCatalog,
    private val applicationScope: CoroutineScope,
    val createEpub: EpubCreator,
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
    private val translationService: TranslationService? = null,
    private val chapterController: ChapterController,
    private val bookController: BookController,
    private val bookDetailController: BookDetailController,
    private val localizeHelper: LocalizeHelper,
    private val trackingRepository: ireader.domain.data.repository.TrackingRepository? = null,
    private val translateBookMetadataUseCase: TranslateBookMetadataUseCase? = null,
    private val chapterRepository: ireader.domain.data.repository.ChapterRepository,
) : BaseViewModel() {
    
    // Convenience accessors for aggregate use cases (backward compatibility)
    private val localInsertUseCases get() = bookDetailUseCases.insertUseCases
    private val getChapterUseCase get() = bookDetailUseCases.getChapterUseCase
    private val getBookUseCases get() = bookDetailUseCases.getBookUseCases
    private val remoteUseCases get() = bookDetailUseCases.remoteUseCases
    private val deleteUseCase get() = bookDetailUseCases.deleteUseCase
    val historyUseCase get() = bookDetailUseCases.historyUseCase
    val exportNovelAsEpub get() = bookDetailUseCases.exportNovelAsEpub
    private val exportBookAsEpubUseCase get() = bookDetailUseCases.exportBookAsEpub
    private val getTranslatedChaptersByBookIdUseCase get() = bookDetailUseCases.getTranslatedChaptersByBookId
    val insertUseCases get() = bookDetailUseCases.insertUseCases

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
    
    // ==================== Mass Translation State ====================
    var showTranslationOptionsDialog by mutableStateOf(false)
    var showTranslationWarningDialog by mutableStateOf(false)
    var showTranslationProgressDialog by mutableStateOf(false)
    var translationWarningChapterCount by mutableStateOf(0)
    var translationWarningEstimatedTime by mutableStateOf(0L)
    var translationWarningEngineName by mutableStateOf("")
    var selectedTranslationEngineId by mutableStateOf(-1L)
    var translationSourceLanguage by mutableStateOf("en")
    var translationTargetLanguage by mutableStateOf("en")
    
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
    var showImagePickerDialog by mutableStateOf(false)
    var showCategorySelectionDialog by mutableStateOf(false)
    var showChapterRangeDownloadDialog by mutableStateOf(false)
    var availableMigrationSources by mutableStateOf<List<CatalogLocal>>(emptyList())
    
    // Chapter range download state
    var chapterRangeStart by mutableStateOf("")
        private set
    var chapterRangeEnd by mutableStateOf("")
        private set
    
    // Translation export state
    var hasTranslationsForExport by mutableStateOf(false)
        private set
    var translationExportTargetLanguage by mutableStateOf("en")
        private set
    
    // Chapter translations map - maps chapter ID to whether it has translation
    var chapterTranslationsMap by mutableStateOf<Map<Long, Boolean>>(emptyMap())
        private set
    
    // EPUB export progress state
    private val _epubExportProgress = MutableStateFlow<EpubExportProgress>(EpubExportProgress.Idle)
    val epubExportProgress: StateFlow<EpubExportProgress> = _epubExportProgress.asStateFlow()
    
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
        (_state.value as? BookDetailState.Success)?.isRefreshingBook == true
    val chapterIsLoading: Boolean get() = (_state.value as? BookDetailState.Success)?.isRefreshingChapters == true
    val expandedSummary: Boolean get() = (_state.value as? BookDetailState.Success)?.isSummaryExpanded == true
    val modifiedCommands: List<Command<*>> get() = (_state.value as? BookDetailState.Success)?.modifiedCommands ?: emptyList()
    
    // Chapter pagination derived state
    val chapterCurrentPage: Int get() = (_state.value as? BookDetailState.Success)?.chapterCurrentPage ?: 1
    val chapterTotalPages: Int get() = (_state.value as? BookDetailState.Success)?.chapterTotalPages ?: 1
    val isLoadingChapterPage: Boolean get() = (_state.value as? BookDetailState.Success)?.isLoadingChapterPage ?: false
    val supportsPaginatedChapters: Boolean get() = (_state.value as? BookDetailState.Success)?.supportsPaginatedChapters ?: false
    val isPaginated: Boolean get() = (_state.value as? BookDetailState.Success)?.isPaginated ?: false
    val hasNextPage: Boolean get() = (_state.value as? BookDetailState.Success)?.hasNextPage ?: false
    val hasPreviousPage: Boolean get() = (_state.value as? BookDetailState.Success)?.hasPreviousPage ?: false
    
    // Track if ViewModel has been initialized
    private var isInitialized = false

    init {
        val bookId = param.bookId
        if (bookId != null) {
            // Start screen profiling for performance benchmarking
            ScreenProfiler.startScreen("BookDetail_$bookId")
            ScreenProfiler.mark("BookDetail_$bookId", "vm_init_start")
            
            // OPTIMIZATION: Show Success state immediately with empty book - no shimmer needed
            _state.value = BookDetailState.Success.empty(bookId = bookId)
            initializeBook(bookId)
            
            // Load chapter translations in background
            loadChapterTranslations(bookId)
            
            // Subscribe to ChapterController state for selection sync (Requirements: 9.1, 9.4, 9.5)
            subscribeToChapterControllerState()
            
            // Subscribe to BookController state for book data sync (Requirements: 4.3)
            subscribeToBookControllerState()
            
            // Subscribe to BookDetailController state and events (Requirements: 3.1, 3.3, 3.4, 3.5)
            subscribeToBookDetailControllerState()
            subscribeToBookDetailControllerEvents()
            
            // Load book via BookController (SSOT pattern)
            bookController.dispatch(BookCommand.LoadBook(bookId))
            
            // Also load via BookDetailController for SSOT pattern (Requirements: 3.1)
            bookDetailController.dispatch(BookDetailCommand.LoadBook(bookId))
            
            isInitialized = true
        } else {
            _state.value = BookDetailState.Error("Invalid book ID")
            scope.launch {
                _events.emit(BookDetailEvent.ShowSnackbar("Something is wrong with this book"))
            }
        }
    }
    
    /**
     * Called when the screen becomes visible again (e.g., navigating back to it).
     * Ensures the subscription is active and data is fresh.
     * This is needed because the ViewModel is cached and reused across navigation.
     */
    fun onScreenResumed() {
        val bookId = param.bookId ?: return
        
        // Check if subscription is still active by checking if subscriptionJob is active
        val needsResubscription = subscriptionJob?.isActive != true
        val isInErrorState = _state.value is BookDetailState.Error
        val isEmptySuccessState = (_state.value as? BookDetailState.Success)?.let { 
            it.book.title == "Untitled" && it.chapters.isEmpty() 
        } ?: false
        
        if (needsResubscription || isInErrorState || isEmptySuccessState) {
            // Reset to loading state if in error or empty
            if (isInErrorState || isEmptySuccessState) {
                _state.value = BookDetailState.Success.empty(bookId = bookId)
            }
            
            // Re-initialize the book data
            initializeBook(bookId)
            
            // Reload via controllers
            bookController.dispatch(BookCommand.LoadBook(bookId))
            bookDetailController.dispatch(BookDetailCommand.LoadBook(bookId))
        }
    }
    
    /**
     * Subscribe to ChapterController state for selection synchronization.
     * This keeps the local selection state in sync with ChapterController.
     * Requirements: 9.1, 9.4, 9.5
     */
    private fun subscribeToChapterControllerState() {
        chapterController.state
            .onEach { chapterState ->
                // Sync selection state from ChapterController
                val controllerSelection = chapterState.selectedChapterIds
                if (selection.toSet() != controllerSelection) {
                    selection.clear()
                    selection.addAll(controllerSelection)
                }
            }
            .launchIn(scope)
    }
    
    /**
     * Subscribe to BookController state for book data synchronization.
     * This keeps the local state in sync with BookController (SSOT pattern).
     * Requirements: 4.3
     */
    private fun subscribeToBookControllerState() {
        bookController.state
            .onEach { bookState ->
                // Sync book-level data from BookController
                // This provides additional book state like reading progress, categories, etc.
                // The main book data is still loaded via the existing subscription mechanism
                // but BookController provides the SSOT for book operations
            }
            .launchIn(scope)
    }
    
    /**
     * Subscribe to BookDetailController state for book detail-specific state.
     * This provides SSOT for chapter selection, filtering, and sorting.
     * Requirements: 3.1, 3.3, 3.4, 3.5
     */
    private fun subscribeToBookDetailControllerState() {
        bookDetailController.state
            .onEach { controllerState ->
                // Sync filter and sort state from BookDetailController
                // The BookDetailController provides additional state management
                // for chapter filtering, sorting, and selection that complements
                // the existing ChapterController
            }
            .launchIn(scope)
    }
    
    /**
     * Subscribe to BookDetailController events for one-time occurrences.
     * Requirements: 3.4, 4.2, 4.3
     */
    private fun subscribeToBookDetailControllerEvents() {
        bookDetailController.events
            .onEach { event ->
                when (event) {
                    is ControllerEvent.Error -> {
                        Log.error { "BookDetailController error: ${event.error.toUserMessage()}" }
                        emitEvent(BookDetailEvent.ShowSnackbar(event.error.toUserMessage()))
                    }
                    is ControllerEvent.BookLoaded -> {
                        // Book loaded event handled
                    }
                    is ControllerEvent.ChaptersLoaded -> {
                        // Chapters loaded event handled
                    }
                    is ControllerEvent.NavigateToReader -> {
                        emitEvent(BookDetailEvent.NavigateToReader(event.bookId, event.chapterId))
                    }
                    is ControllerEvent.NavigateToWebView -> {
                        // Handle web view navigation if needed
                    }
                    is ControllerEvent.NavigateBack -> {
                        emitEvent(BookDetailEvent.NavigateBack)
                    }
                    is ControllerEvent.BookRefreshed -> {
                        // Book refreshed event handled
                    }
                    is ControllerEvent.ChaptersRefreshed -> {
                        // Chapters refreshed event handled
                    }
                    is ControllerEvent.SelectionChanged -> {
                        // Selection changed event handled
                    }
                    is ControllerEvent.ShowSnackbar -> {
                        emitEvent(BookDetailEvent.ShowSnackbar(event.message))
                    }
                }
            }
            .launchIn(scope)
    }

    // ==================== Initialization ====================
    
    private fun initializeBook(bookId: Long) {
        val screenTag = "BookDetail_$bookId"
        // OPTIMIZATION: Use immediate dispatcher for first frame
        // This ensures the book data is shown as fast as possible
        scope.launch(kotlinx.coroutines.Dispatchers.Main.immediate) {
            try {
                ScreenProfiler.mark(screenTag, "coroutine_started")
                
                // OPTIMIZATION: Check prefetch cache first for instant display
                val prefetchedData = bookPrefetchService?.getPrefetchedData(bookId)
                
                if (prefetchedData != null) {
                    // Use prefetched data for instant display
                    ScreenProfiler.mark(screenTag, "prefetch_cache_hit")
                    // Don't translate here - book should already have translated data from when it was saved
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
                    ScreenProfiler.mark(screenTag, "catalog_source_loaded")
                    
                    val source = catalogSource?.source
                    val commands = try {
                        if (source is CatalogSource) {
                            source.getCommands().toImmutableList()
                        } else persistentListOf()
                    } catch (e: Exception) {
                        persistentListOf()
                    }
                    
                    // Check if source supports pagination (will verify page count in background)
                    val supportsPagination = source?.supportsPaginatedChapters() ?: false
                    
                    // Determine if remote fetch will be needed
                    val needsRemoteFetch = (book.lastUpdate < 1L || chapters.isEmpty()) && catalogSource?.source != null
                    
                    // Set initial state immediately from prefetch cache
                    // Keep refreshing indicators if remote fetch will be triggered
                    // Note: supportsPaginatedChapters stays false until we confirm page count > 1
                    val initialState = BookDetailState.Success(
                        book = book,
                        chapters = chapters.toImmutableList(),
                        source = source,
                        catalogSource = catalogSource,
                        lastReadChapterId = lastReadChapterId,
                        commands = commands,
                        modifiedCommands = commands,
                        isRefreshingBook = needsRemoteFetch && book.lastUpdate < 1L,
                        isRefreshingChapters = needsRemoteFetch,
                    )
                    _state.value = initialState
                    ScreenProfiler.mark(screenTag, "state_set_from_prefetch")
                    ScreenProfiler.finishScreen(screenTag)
                    
                    // Subscribe for updates in background
                    subscribeToBookAndChapters(bookId, catalogSource)
                    
                    // If source supports pagination, check page count in background
                    // This will set supportsPaginatedChapters=true only if pageCount > 1
                    if (supportsPagination) {
                        scope.launch(ioDispatcher) {
                            checkPaginationSupport()
                        }
                    }
                    
                    // Trigger remote fetch if book needs updating or has no chapters
                    if (needsRemoteFetch) {
                        scope.launch(ioDispatcher) {
                            if (book.lastUpdate < 1L) {
                                getRemoteBookDetailSilent(book, catalogSource)
                            }
                            getRemoteChapterDetailSilent(book, catalogSource)
                        }
                    }
                    
                    // Check source availability in background
                    scope.launch(ioDispatcher) {
                        checkSourceAvailability(bookId)
                    }
                    return@launch
                }
                
                // No prefetch data - load from database
                ScreenProfiler.mark(screenTag, "prefetch_cache_miss")
                // First, try to get the book directly for immediate display
                // Use withContext(IO) only for the actual DB call
                // Don't translate here - book should already have translated data from when it was saved
                val book = withIOContext { getBookUseCases.findBookById(bookId) }
                ScreenProfiler.mark(screenTag, "book_loaded_from_db")
                val sourceId = book?.sourceId
                
                // OPTIMIZATION: Update Success state with book info immediately
                if (book != null) {
                    _state.value = BookDetailState.Success(
                        book = book,
                        chapters = persistentListOf(),
                        source = null,
                        catalogSource = null,
                        isRefreshingBook = true,
                        isRefreshingChapters = true,
                    )
                    ScreenProfiler.mark(screenTag, "success_state_with_book")
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
                    ScreenProfiler.mark(screenTag, "catalog_source_loaded")
                    val chapters = chaptersDeferred.await()
                    ScreenProfiler.mark(screenTag, "chapters_loaded_from_db")
                    val history = historyDeferred.await()
                    ScreenProfiler.mark(screenTag, "history_loaded")
                    
                    val source = catalogSource?.source
                    val commands = try {
                        if (source is CatalogSource) {
                            source.getCommands().toImmutableList()
                        } else persistentListOf()
                    } catch (e: Exception) {
                        persistentListOf()
                    }
                    
                    // Check if source supports pagination (will verify page count in background)
                    val supportsPagination = source?.supportsPaginatedChapters() ?: false
                    
                    // Trigger initial fetch if book needs updating (AFTER subscribing)
                    // Fetch remote data if:
                    // 1. Book has never been updated (lastUpdate < 1L), OR
                    // 2. Book has no chapters yet
                    // This runs in background and won't block UI
                    val needsRemoteFetch = (book.lastUpdate < 1L || chapters.isEmpty()) && catalogSource?.source != null
                    
                    // Set initial state immediately - this triggers UI update
                    // Keep refreshing indicators if remote fetch will be triggered
                    // Note: supportsPaginatedChapters stays false until we confirm page count > 1
                    val initialState = BookDetailState.Success(
                        book = book,
                        chapters = chapters.toImmutableList(),
                        source = source,
                        catalogSource = catalogSource,
                        lastReadChapterId = history?.chapterId,
                        commands = commands,
                        modifiedCommands = commands,
                        isRefreshingBook = needsRemoteFetch && book.lastUpdate < 1L,
                        isRefreshingChapters = needsRemoteFetch,
                    )
                    _state.value = initialState
                    ScreenProfiler.mark(screenTag, "state_set_success")
                    ScreenProfiler.finishScreen(screenTag)
                    
                    // Subscribe for updates AFTER initial state is set
                    // This runs in background and won't block UI
                    subscribeToBookAndChapters(bookId, catalogSource)
                    
                    // If source supports pagination, check page count in background
                    // This will set supportsPaginatedChapters=true only if pageCount > 1
                    if (supportsPagination) {
                        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                            checkPaginationSupport()
                        }
                    }
                    
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
                    // Translate book metadata before saving if auto-translate is enabled
                    val translatedBook = translateBookMetadata(resultBook)
                    localInsertUseCases.updateBook.update(translatedBook)
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
                
                // Don't translate here - book should already have translated data from when it was saved
                
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
                    // Preserve pagination state
                    supportsPaginatedChapters = currentState?.supportsPaginatedChapters ?: false,
                    chapterCurrentPage = currentState?.chapterCurrentPage ?: 1,
                    chapterTotalPages = currentState?.chapterTotalPages ?: 1,
                    isLoadingChapterPage = currentState?.isLoadingChapterPage ?: false,
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
                is BookDetailState.Success -> update(current)
                is BookDetailState.Error -> current
            }
        }
    }
    
    private fun emitEvent(event: BookDetailEvent) {
        scope.launch { _events.emit(event) }
    }

    // ==================== Book Metadata Translation ====================

    /**
     * Translate book title and description if auto-translate is enabled.
     * Returns a new Book with translated fields, or the original if translation is disabled/fails.
     */
    private suspend fun translateBookMetadata(book: Book): Book {
        val useCase = translateBookMetadataUseCase ?: return book
        
        val translateNames = useCase.isAutoTranslateNamesEnabled()
        val translateDescriptions = useCase.isAutoTranslateDescriptionsEnabled()
        
        if (!translateNames && !translateDescriptions) return book
        
        var translatedTitle = book.title
        var translatedDescription = book.description
        
        try {
            if (translateNames && book.title.isNotBlank()) {
                translatedTitle = useCase.translateText(book.title)
            }
            if (translateDescriptions && !book.description.isNullOrBlank()) {
                translatedDescription = useCase.translateText(book.description)
            }
        } catch (e: Exception) {
            Log.error { "BookDetailViewModel: Failed to translate book metadata: ${e.message}" }
        }
        
        return book.copy(
            title = translatedTitle,
            description = translatedDescription
        )
    }

    // ==================== Chapter Translations ====================
    
    /**
     * Load chapter translations map to show translation indicators in chapter list.
     * Maps chapter ID to whether it has translation content (translatedContent.size > 0).
     */
    private fun loadChapterTranslations(bookId: Long) {
        scope.launch(ioDispatcher) {
            try {
                val translationsMap = getTranslatedChaptersByBookIdUseCase.getTranslationsMap(bookId)
                // Create a map of chapter ID to whether it has translation content
                chapterTranslationsMap = translationsMap.mapValues { (_, translatedChapter) ->
                    translatedChapter.translatedContent.size > 0
                }
            } catch (e: Exception) {
                Log.error("Failed to load chapter translations", e)
                chapterTranslationsMap = emptyMap()
            }
        }
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
    
    // Categories state for category selection dialog
    private val _availableCategories = MutableStateFlow<List<ireader.domain.models.entities.CategoryWithCount>>(emptyList())
    val availableCategories: StateFlow<List<ireader.domain.models.entities.CategoryWithCount>> = _availableCategories.asStateFlow()
    
    // Pending book to add to library (used when showing category selection dialog)
    private var pendingBookForLibrary: Book? = null
    
    /**
     * Toggle the book's favorite status.
     * If adding to library and categories exist, shows category selection dialog.
     * Requirements: 4.3
     */
    fun toggleInLibrary(book: Book) {
        if (book.favorite) {
            // Removing from library - no dialog needed
            removeFromLibrary(book)
        } else {
            // Adding to library - check if we should show category selection
            scope.launch {
                val categories = withIOContext {
                    bookUseCases.addToLibrary.getAvailableCategories()
                }
                
                if (categories.isNotEmpty()) {
                    // Store pending book and show dialog
                    pendingBookForLibrary = book
                    _availableCategories.value = categories
                    showCategorySelectionDialog = true
                } else {
                    // No categories - add directly
                    addToLibraryWithCategories(book, emptySet())
                }
            }
        }
    }
    
    /**
     * Called when user confirms category selection from dialog.
     */
    fun confirmCategorySelection(selectedCategoryIds: Set<Long>) {
        val book = pendingBookForLibrary ?: return
        showCategorySelectionDialog = false
        pendingBookForLibrary = null
        addToLibraryWithCategories(book, selectedCategoryIds)
    }
    
    /**
     * Called when user dismisses category selection dialog.
     */
    fun dismissCategorySelection() {
        showCategorySelectionDialog = false
        pendingBookForLibrary = null
    }
    
    /**
     * Add book to library with specified categories.
     */
    private fun addToLibraryWithCategories(book: Book, categoryIds: Set<Long>) {
        updateSuccessState { it.copy(isInLibraryLoading = true) }
        
        applicationScope.launch {
            try {
                val bookId = withIOContext {
                    // Use AddToLibrary use case with full book object to ensure book exists in database
                    // This handles the case where book is from explore screen and not yet in database
                    bookUseCases.addToLibrary.awaitWithCategoriesFromBook(book, categoryIds)
                }
                
                if (bookId > 0) {
                    // Sync if available - use syncBookToRemote directly instead of toggleBookInLibrary
                    // because toggleBookInLibrary checks the favorite status and would remove the book
                    // if we pass favorite=true (it thinks the book is already in library and toggles it off)
                    withIOContext {
                        // Get the updated book from database to sync
                        val updatedBook = bookUseCases.getBookById(bookId)
                        if (updatedBook != null && syncUseCases != null) {
                            try {
                                syncUseCases.syncBookToRemote(updatedBook)
                            } catch (e: Exception) {
                                // Sync failure shouldn't prevent local add from succeeding
                                ireader.core.log.Log.warn { "Failed to sync book to remote: ${e.message}" }
                            }
                        }
                    }
                    // Update the book state to reflect the change
                    // Also update the book ID in case it was newly inserted
                    updateSuccessState { state ->
                        state.copy(
                            isInLibraryLoading = false,
                            book = state.book.copy(id = bookId, favorite = true)
                        )
                    }
                    emitEvent(BookDetailEvent.ShowSnackbar("Added to library"))
                } else {
                    updateSuccessState { it.copy(isInLibraryLoading = false) }
                    emitEvent(BookDetailEvent.ShowSnackbar("Failed to add to library"))
                }
            } catch (e: Exception) {
                updateSuccessState { it.copy(isInLibraryLoading = false) }
                emitEvent(BookDetailEvent.ShowSnackbar("Failed to add to library: ${e.message}"))
            }
        }
    }
    
    /**
     * Remove book from library.
     */
    private fun removeFromLibrary(book: Book) {
        updateSuccessState { it.copy(isInLibraryLoading = true) }
        
        bookController.dispatch(BookCommand.ToggleFavorite)
        
        applicationScope.launch {
            try {
                withIOContext {
                    syncUseCases?.toggleBookInLibrary?.invoke(book)
                        ?: deleteUseCase.unFavoriteBook(listOf(book.id))
                }
                updateSuccessState { it.copy(isInLibraryLoading = false) }
                emitEvent(BookDetailEvent.ShowSnackbar("Removed from library"))
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
                    // Translate book metadata before saving if auto-translate is enabled
                    val translatedBook = translateBookMetadata(resultBook)
                    localInsertUseCases.updateBook.update(translatedBook)
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
            updateSuccessState { it.copy(isRefreshingChapters = true) }
        }
        
        getChapterDetailJob?.cancel()
        getChapterDetailJob = scope.launch {
            remoteUseCases.getRemoteChapters(
                book = book,
                catalog = catalog,
                onError = { message ->
                    message?.let { showSnackBar(it) }
                    withUIContext {
                        updateSuccessState { it.copy(isRefreshingChapters = false) }
                    }
                },
                onSuccess = { result ->
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
    
    // ==================== Chapter Pagination ====================
    
    /**
     * Check if the current source supports paginated chapter loading
     * and initialize pagination state if it does.
     * If pagination is supported and no chapters are loaded, automatically loads page 1.
     * Uses the saved chapter page from the database if available.
     */
    fun checkPaginationSupport() {
        val currentState = _state.value as? BookDetailState.Success ?: return
        val catalog = currentState.catalogSource
        
        val supportsPagination = remoteUseCases.getRemoteChapters.supportsPaginatedChapters(catalog)
        
        if (supportsPagination) {
            scope.launch {
                try {
                    val pageCount = remoteUseCases.getRemoteChapters.getChapterPageCount(
                        currentState.book,
                        catalog
                    )
                    
                    // Only enable pagination UI if there are multiple pages
                    if (pageCount > 1) {
                        // Use saved page from book entity, clamped to valid range
                        val savedPage = currentState.book.chapterPage.coerceIn(1, pageCount)
                        
                        updateSuccessState { 
                            it.copy(
                                supportsPaginatedChapters = true,
                                chapterTotalPages = pageCount,
                                chapterCurrentPage = savedPage
                            )
                        }
                        
                        // If no chapters loaded yet, automatically load the saved page
                        val updatedState = _state.value as? BookDetailState.Success
                        if (updatedState != null && updatedState.chapters.isEmpty()) {
                            fetchChapterPage(savedPage)
                        }
                    } else {
                        // Only 1 page, disable pagination UI
                        updateSuccessState { 
                            it.copy(
                                supportsPaginatedChapters = false,
                                chapterTotalPages = 1,
                                chapterCurrentPage = 1
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.error { "Error checking pagination support: ${e.message}" }
                    // On error, disable pagination UI
                    updateSuccessState { 
                        it.copy(supportsPaginatedChapters = false)
                    }
                }
            }
        } else {
            // Source doesn't support pagination, ensure UI is disabled
            updateSuccessState { 
                it.copy(supportsPaginatedChapters = false)
            }
        }
    }
    
    /**
     * Fetch and save a specific page of chapters from the source to the database.
     * Chapters are added to the existing list in the database, not replaced.
     * The UI will update automatically via the database subscription.
     */
    fun fetchChapterPage(page: Int) {
        val currentState = _state.value as? BookDetailState.Success ?: return
        if (!currentState.supportsPaginatedChapters) return
        if (page < 1 || page > currentState.chapterTotalPages) return
        if (currentState.isLoadingChapterPage) return
        
        updateSuccessState { it.copy(isLoadingChapterPage = true, chapterCurrentPage = page) }
        
        scope.launch {
            val result = remoteUseCases.getRemoteChapters.getChapterPage(
                book = currentState.book,
                catalog = currentState.catalogSource,
                page = page
            )
            
            when (result) {
                is ireader.domain.usecases.remote.ChapterPageResult.Success -> {
                    // Filter out chapters that already exist in the database (by key/URL)
                    val existingKeys = chapters.map { it.key }.toSet()
                    val newChapters = result.chapters.filter { it.key !in existingKeys }
                    
                    if (newChapters.isNotEmpty()) {
                        // Save new chapters to database - UI will update via subscription
                        localInsertUseCases.insertChapters(newChapters)
                    }
                    
                    // Save current page to database for persistence
                    bookUseCases.updateChapterPage(currentState.book.id, result.currentPage)
                    
                    updateSuccessState { state ->
                        state.copy(
                            chapterCurrentPage = result.currentPage,
                            chapterTotalPages = result.totalPages,
                            isLoadingChapterPage = false
                        )
                    }
                }
                is ireader.domain.usecases.remote.ChapterPageResult.Error -> {
                    updateSuccessState { it.copy(isLoadingChapterPage = false) }
                    result.error?.let { showSnackBar(it) }
                }
            }
        }
    }
    
    /**
     * Fetch the next page of chapters and add to database.
     */
    fun fetchNextChapterPage() {
        val currentState = _state.value as? BookDetailState.Success ?: return
        if (currentState.hasNextPage) {
            fetchChapterPage(currentState.chapterCurrentPage + 1)
        }
    }
    
    /**
     * Fetch the previous page of chapters and add to database.
     */
    fun fetchPreviousChapterPage() {
        val currentState = _state.value as? BookDetailState.Success ?: return
        if (currentState.hasPreviousPage) {
            fetchChapterPage(currentState.chapterCurrentPage - 1)
        }
    }
    
    // Keep old methods for backward compatibility but redirect to new ones
    fun loadChapterPage(page: Int) = fetchChapterPage(page)
    fun nextChapterPage() = fetchNextChapterPage()
    fun previousChapterPage() = fetchPreviousChapterPage()
    
    /**
     * Fetch all chapters from all pages and save to database.
     * Chapters are added to the existing list, not replaced.
     */
    fun loadAllChapters() {
        val currentState = _state.value as? BookDetailState.Success ?: return
        if (!currentState.supportsPaginatedChapters) return
        if (currentState.isLoadingChapterPage) return
        
        updateSuccessState { it.copy(isLoadingChapterPage = true) }
        
        scope.launch {
            try {
                val allNewChapters = mutableListOf<Chapter>()
                val totalPages = currentState.chapterTotalPages
                val existingKeys = chapters.map { it.key }.toSet()
                
                // Fetch all pages sequentially
                for (page in 1..totalPages) {
                    updateSuccessState { it.copy(chapterCurrentPage = page) }
                    
                    val result = remoteUseCases.getRemoteChapters.getChapterPage(
                        book = currentState.book,
                        catalog = currentState.catalogSource,
                        page = page
                    )
                    
                    when (result) {
                        is ireader.domain.usecases.remote.ChapterPageResult.Success -> {
                            // Filter out chapters that already exist
                            val newChapters = result.chapters.filter { it.key !in existingKeys }
                            allNewChapters.addAll(newChapters)
                        }
                        is ireader.domain.usecases.remote.ChapterPageResult.Error -> {
                            Log.warn { "Failed to load page $page: ${result.error}" }
                        }
                    }
                }
                
                // Save all new chapters to database - UI will update via subscription
                if (allNewChapters.isNotEmpty()) {
                    localInsertUseCases.insertChapters(allNewChapters)
                }
                
                updateSuccessState { state ->
                    state.copy(
                        chapterCurrentPage = totalPages,
                        isLoadingChapterPage = false
                    )
                }
            } catch (e: Exception) {
                Log.error { "Error loading all chapters: ${e.message}" }
                updateSuccessState { it.copy(isLoadingChapterPage = false) }
            }
        }
    }

    // ==================== Chapter Selection ====================
    // Delegated to ChapterController (Requirements: 9.1, 9.4, 9.5)
    
    fun toggleSelection(book: Book) {
        // This is for book selection, but we use chapter selection
    }
    
    /**
     * Select chapters between the first and last selected chapter IDs.
     * Note: This is a UI-specific operation that extends ChapterController's selection.
     */
    fun selectBetween() {
        val selectedIds = selection.toSet()
        val chapterIds = chapters.map { it.id }
        val filteredIds = chapterIds.filter { it in selectedIds }.sorted()
        
        if (filteredIds.isNotEmpty()) {
            val min = filteredIds.first()
            val max = filteredIds.last()
            // Clear and add range - this is a UI-specific operation
            chapterController.dispatch(ChapterCommand.ClearSelection)
            (min..max).forEach { id ->
                chapterController.dispatch(ChapterCommand.SelectChapter(id))
            }
        }
    }
    
    /**
     * Select all chapters - dispatches to ChapterController for SSOT.
     */
    fun selectAllChapters() {
        val allChapterIds = chapters.map { it.id }
        // First clear, then select all via ChapterController
        chapterController.dispatch(ChapterCommand.ClearSelection)
        allChapterIds.forEach { id ->
            chapterController.dispatch(ChapterCommand.SelectChapter(id))
        }
    }
    
    /**
     * Invert selection - dispatches to ChapterController for SSOT.
     */
    fun invertSelection() {
        val allChapterIds = chapters.map { it.id }.toSet()
        val currentSelection = selection.toSet()
        val invertedSelection = allChapterIds - currentSelection
        // Clear and add inverted selection via ChapterController
        chapterController.dispatch(ChapterCommand.ClearSelection)
        invertedSelection.forEach { id ->
            chapterController.dispatch(ChapterCommand.SelectChapter(id))
        }
    }
    
    /**
     * Clear selection - dispatches to ChapterController for SSOT.
     */
    fun clearSelection() {
        // Clear local selection immediately for responsive UI
        selection.clear()
        // Also dispatch to ChapterController to keep it in sync
        chapterController.dispatch(ChapterCommand.ClearSelection)
    }
    
    /**
     * Toggle selection for a single chapter.
     */
    fun toggleChapterSelection(chapterId: Long) {
        if (selection.contains(chapterId)) {
            chapterController.dispatch(ChapterCommand.DeselectChapter(chapterId))
        } else {
            chapterController.dispatch(ChapterCommand.SelectChapter(chapterId))
        }
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
    
    /**
     * Deletes only the content of selected chapters, keeping the chapter records in DB.
     * This clears the downloaded text content while preserving chapter metadata.
     */
    fun deleteChapterContent(chapters: List<Chapter>) {
        scope.launch(Dispatchers.IO) {
            try {
                // Use the dedicated clearChapterContent method which explicitly clears content
                // This is different from insertChapter with empty content, which now preserves existing content
                chapterRepository.clearChapterContent(chapters.map { it.id })
                selection.clear()
                emitEvent(BookDetailEvent.ShowSnackbar("Chapter content deleted"))
            } catch (e: Exception) {
                emitEvent(BookDetailEvent.ShowSnackbar("Failed to delete chapter content: ${e.message}"))
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
        scope.launch {
            // Find the most recently read chapter via History
            val currentBook = book
            if (currentBook == null) {
                emitEvent(BookDetailEvent.ShowSnackbar("No book loaded"))
                return@launch
            }
            
            val allChapters = chapters
            if (allChapters.isEmpty()) {
                emitEvent(BookDetailEvent.ShowSnackbar("No chapters available"))
                return@launch
            }
            
            // Get reading history for this book
            val histories = historyUseCase.findHistoriesByBookId(currentBook.id)
            val lastReadHistory = histories.maxByOrNull { it.readAt ?: 0L }
            
            val chaptersToDownload = if (lastReadHistory != null) {
                // Find the index of the last read chapter
                val lastReadChapterIndex = allChapters.indexOfFirst { it.id == lastReadHistory.chapterId }
                
                if (lastReadChapterIndex >= 0) {
                    // Download chapters AFTER the last read chapter that don't have content
                    allChapters.drop(lastReadChapterIndex + 1)
                        .filter { it.content.joinToString("").isBlank() }
                } else {
                    // Last read chapter not found in current chapter list, download all without content
                    allChapters.filter { it.content.joinToString("").isBlank() }
                }
            } else {
                // No reading history - download all chapters without content
                allChapters.filter { it.content.joinToString("").isBlank() }
            }
            
            val chapterIds = chaptersToDownload.map { it.id }
            if (chapterIds.isEmpty()) {
                emitEvent(BookDetailEvent.ShowSnackbar("No chapters to download"))
                return@launch
            }
            
            when (val result = downloadService.queueChapters(chapterIds)) {
                is ServiceResult.Success -> {
                    emitEvent(BookDetailEvent.ShowSnackbar("Downloading ${chapterIds.size} chapters from last read position"))
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

    // ==================== Chapter Range Download ====================
    
    /**
     * Shows the chapter range download dialog.
     */
    fun showChapterRangeDownloadDialog() {
        chapterRangeStart = ""
        chapterRangeEnd = ""
        showChapterRangeDownloadDialog = true
    }
    
    /**
     * Hides the chapter range download dialog.
     */
    fun hideChapterRangeDownloadDialog() {
        showChapterRangeDownloadDialog = false
        chapterRangeStart = ""
        chapterRangeEnd = ""
    }
    
    /**
     * Update chapter range start value.
     */
    fun updateChapterRangeStart(value: String) {
        chapterRangeStart = value
    }
    
    /**
     * Update chapter range end value.
     */
    fun updateChapterRangeEnd(value: String) {
        chapterRangeEnd = value
    }
    
    /**
     * Download chapters in the specified range (inclusive).
     * Range is 1-based (chapter number 1 = index 0).
     * Uses the existing selection and download mechanism.
     */
    fun downloadChapterRange() {
        val allChapters = chapters
        if (allChapters.isEmpty()) {
            emitEvent(BookDetailEvent.ShowSnackbar("No chapters available"))
            hideChapterRangeDownloadDialog()
            return
        }
        
        val start = chapterRangeStart.toIntOrNull()
        val end = chapterRangeEnd.toIntOrNull()
        
        if (start == null || end == null) {
            emitEvent(BookDetailEvent.ShowSnackbar("Please enter valid chapter numbers"))
            return
        }
        
        if (start < 1 || end < 1) {
            emitEvent(BookDetailEvent.ShowSnackbar("Chapter numbers must be greater than 0"))
            return
        }
        
        if (start > end) {
            emitEvent(BookDetailEvent.ShowSnackbar("Start chapter must be less than or equal to end chapter"))
            return
        }
        
        if (start > allChapters.size || end > allChapters.size) {
            emitEvent(BookDetailEvent.ShowSnackbar("Chapter range exceeds available chapters (${allChapters.size})"))
            return
        }
        
        // Convert 1-based to 0-based indices
        val startIndex = start - 1
        val endIndex = end - 1
        
        // Get chapters in range (inclusive)
        val chaptersInRange = allChapters.subList(startIndex, endIndex + 1)
        
        hideChapterRangeDownloadDialog()
        
        // Clear current selection and add chapters in range
        selection.clear()
        chaptersInRange.forEach { chapter ->
            selection.add(chapter.id)
        }
        
        // Also update ChapterController for UI consistency
        chapterController.dispatch(ChapterCommand.ClearSelection)
        chaptersInRange.forEach { chapter ->
            chapterController.dispatch(ChapterCommand.SelectChapter(chapter.id))
        }
        
        // Use the existing downloadChapters() which works correctly
        downloadChapters()
    }


    // ==================== Mass Translation ====================
    
    /**
     * Shows the mass translation options dialog when user long-presses translate icon
     */
    fun showMassTranslationDialog() {
        val selectedIds = selection.toList()
        if (selectedIds.isEmpty()) {
            emitEvent(BookDetailEvent.ShowSnackbar("No chapters selected"))
            return
        }
        
        if (translationService == null) {
            emitEvent(BookDetailEvent.ShowSnackbar("Translation service not available"))
            return
        }
        
        // Load default translation settings from preferences
        selectedTranslationEngineId = readerPreferences.translatorEngine().get()
        translationSourceLanguage = readerPreferences.translatorOriginLanguage().get()
        translationTargetLanguage = readerPreferences.translatorTargetLanguage().get()
        
        showTranslationOptionsDialog = true
    }
    
    /**
     * Quick translate selected chapters with default settings (single tap)
     */
    fun quickTranslateSelectedChapters() {
        val selectedIds = selection.toList()
        val currentBook = book ?: return
        
        if (selectedIds.isEmpty()) {
            emitEvent(BookDetailEvent.ShowSnackbar("No chapters selected"))
            return
        }
        
        if (translationService == null) {
            emitEvent(BookDetailEvent.ShowSnackbar("Translation service not available"))
            return
        }
        
        // Use saved preferences for quick translation
        val engineId = readerPreferences.translatorEngine().get()
        val sourceLang = readerPreferences.translatorOriginLanguage().get()
        val targetLang = readerPreferences.translatorTargetLanguage().get()
        
        if (engineId < 0) {
            // No engine configured, show options dialog instead
            showMassTranslationDialog()
            return
        }
        
        startMassTranslation(
            engineId = engineId,
            sourceLang = sourceLang,
            targetLang = targetLang,
            bypassWarning = false
        )
    }
    
    /**
     * Start mass translation with specified options
     */
    fun startMassTranslation(
        engineId: Long = selectedTranslationEngineId,
        sourceLang: String = translationSourceLanguage,
        targetLang: String = translationTargetLanguage,
        bypassWarning: Boolean = false
    ) {
        val selectedIds = selection.toList()
        val currentBook = book ?: return
        
        if (translationService == null) {
            emitEvent(BookDetailEvent.ShowSnackbar("Translation service not available"))
            return
        }
        
        showTranslationOptionsDialog = false
        
        // Save preferences
        readerPreferences.translatorEngine().set(engineId)
        readerPreferences.translatorOriginLanguage().set(sourceLang)
        readerPreferences.translatorTargetLanguage().set(targetLang)
        
        scope.launch {
            val result = translationService.queueChapters(
                bookId = currentBook.id,
                chapterIds = selectedIds,
                sourceLanguage = sourceLang,
                targetLanguage = targetLang,
                engineId = engineId,
                bypassWarning = bypassWarning
            )
            
            when (result) {
                is ServiceResult.Success -> {
                    when (val queueResult = result.data) {
                        is TranslationQueueResult.Success -> {
                            showTranslationProgressDialog = true
                            selection.clear()
                            emitEvent(BookDetailEvent.ShowSnackbar("${queueResult.queuedCount} chapters queued for translation"))
                        }
                        is TranslationQueueResult.RateLimitWarning -> {
                            // Show warning dialog
                            translationWarningChapterCount = queueResult.chapterCount
                            translationWarningEstimatedTime = queueResult.estimatedTime / 60000 // Convert to minutes
                            showTranslationWarningDialog = true
                        }
                        is TranslationQueueResult.PreviousTranslationCancelled -> {
                            emitEvent(BookDetailEvent.ShowSnackbar("Previous translation cancelled. Please try again."))
                        }
                    }
                }
                is ServiceResult.Error -> {
                    emitEvent(BookDetailEvent.ShowSnackbar("Translation failed: ${result.message ?: "Unknown error"}"))
                }
                else -> {}
            }
        }
    }
    
    /**
     * Confirm translation after warning dialog
     */
    fun confirmMassTranslation() {
        showTranslationWarningDialog = false
        startMassTranslation(bypassWarning = true)
    }
    
    /**
     * Pause ongoing translation
     */
    fun pauseTranslation() {
        scope.launch {
            translationService?.pause()
        }
    }
    
    /**
     * Resume paused translation
     */
    fun resumeTranslation() {
        scope.launch {
            translationService?.resume()
        }
    }
    
    /**
     * Cancel all ongoing translations
     */
    fun cancelTranslation() {
        scope.launch {
            translationService?.cancelAll()
            showTranslationProgressDialog = false
            emitEvent(BookDetailEvent.ShowSnackbar("Translation cancelled"))
        }
    }
    
    /**
     * Get translation service for UI binding
     */
    fun getTranslationService(): TranslationService? = translationService

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
                
                // Also check if source supports paginated chapters
                checkPaginationSupport()
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
    
    fun startMigration(targetSourceId: Long, flags: ireader.domain.models.migration.MigrationFlags = ireader.domain.models.migration.MigrationFlags()) {
        val currentBook = book ?: return
        val targetSourceName = catalogStore.get(targetSourceId)?.name
        
        showMigrationDialog = false
        sourceSwitchingState.showMigrationDialog = true
        
        // Store migration details for potential retry
        sourceSwitchingState.lastMigrationSourceId = targetSourceId
        sourceSwitchingState.lastMigrationFlags = flags
        
        scope.launch {
            try {
                migrateToSourceUseCase(currentBook.id, targetSourceId, flags).collect { progress ->
                    sourceSwitchingState.migrationProgress = progress
                    
                    if (progress.isComplete) {
                        if (progress.error == null) {
                            // Success - auto-dismiss after short delay
                            delay(1500)
                            sourceSwitchingState.showMigrationDialog = false
                            sourceSwitchingState.resetAll()
                            showSnackBar(UiText.DynamicString("Successfully migrated to ${targetSourceName ?: "new source"}"))
                        }
                        // If there's an error, keep dialog open to show error with retry option
                    }
                }
            } catch (e: Exception) {
                Log.error("Migration error", e)
                // Set error progress for UI to display
                sourceSwitchingState.migrationProgress = MigrateToSourceUseCase.MigrationProgress(
                    currentStep = "Migration failed",
                    progress = 0f,
                    isComplete = true,
                    error = e.message ?: "Unknown error",
                    errorType = MigrateToSourceUseCase.MigrationErrorType.UNKNOWN_ERROR,
                    canRetry = true
                )
            }
        }
    }
    
    fun retryMigration() {
        val sourceId = sourceSwitchingState.lastMigrationSourceId ?: return
        val flags = sourceSwitchingState.lastMigrationFlags ?: ireader.domain.models.migration.MigrationFlags()
        startMigration(sourceId, flags)
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
    
    /**
     * Check if the book has any translated chapters available for export.
     * Should be called before showing the export dialog.
     */
    fun checkTranslationsForExport() {
        val currentBook = book ?: return
        scope.launch {
            try {
                val translations = getTranslatedChaptersByBookIdUseCase(currentBook.id)
                hasTranslationsForExport = translations.isNotEmpty()
                // Get the target language from the first translation if available
                translations.firstOrNull()?.let {
                    translationExportTargetLanguage = it.targetLanguage
                }
            } catch (e: Exception) {
                Log.error("Failed to check translations for export", e)
                hasTranslationsForExport = false
            }
        }
    }
    
    fun exportAsEpub(options: ExportOptions) {
        val currentBook = book ?: return
        val chaptersToExport = chapters
        
        scope.launch {
            try {
                // Show progress dialog
                _epubExportProgress.value = EpubExportProgress.Starting("Preparing export...")
                
                val outputUri = try {
                    platformHelper.createEpubExportUri(currentBook.title, currentBook.author)
                } catch (e: Exception) {
                    Log.error("Failed to create export URI", e)
                    _epubExportProgress.value = EpubExportProgress.Error("Failed to select save location: ${e.message}")
                    return@launch
                }
                
                if (outputUri == null) {
                    _epubExportProgress.value = EpubExportProgress.Idle
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
                    fontSize = 16,
                    useTranslatedContent = options.useTranslatedContent,
                    translationTargetLanguage = options.translationTargetLanguage
                )
                
                val totalChapters = if (options.selectedChapters.isEmpty()) {
                    chaptersToExport.size
                } else {
                    options.selectedChapters.size
                }
                
                val result = exportBookAsEpubUseCase(
                    bookId = currentBook.id,
                    outputUri = ireader.domain.models.common.Uri.parse(outputUri),
                    options = domainOptions
                ) { progressMessage ->
                    // Parse progress message to update UI
                    when {
                        progressMessage.contains("chapter", ignoreCase = true) -> {
                            // Extract chapter number from message like "Exporting chapter 5/20: Chapter Title"
                            val regex = """(\d+)/(\d+)""".toRegex()
                            val match = regex.find(progressMessage)
                            if (match != null) {
                                val current = match.groupValues[1].toIntOrNull() ?: 0
                                val total = match.groupValues[2].toIntOrNull() ?: totalChapters
                                val chapterName = progressMessage.substringAfter(":").trim().ifEmpty { "Processing..." }
                                _epubExportProgress.value = EpubExportProgress.InProgress(
                                    currentChapter = current,
                                    totalChapters = total,
                                    chapterName = chapterName
                                )
                            }
                        }
                        progressMessage.contains("Creating", ignoreCase = true) ||
                        progressMessage.contains("Compressing", ignoreCase = true) -> {
                            _epubExportProgress.value = EpubExportProgress.Compressing(progressMessage)
                        }
                        progressMessage.contains("Saving", ignoreCase = true) ||
                        progressMessage.contains("Writing", ignoreCase = true) -> {
                            _epubExportProgress.value = EpubExportProgress.Writing(progressMessage)
                        }
                    }
                }
                
                result.onSuccess { filePath ->
                    val exportType = if (options.useTranslatedContent) "translated " else ""
                    _epubExportProgress.value = EpubExportProgress.Complete(
                        filePath = filePath,
                        message = "${exportType}EPUB exported successfully"
                    )
                }.onFailure { error ->
                    _epubExportProgress.value = EpubExportProgress.Error(error.message ?: "Export failed")
                    Log.error("EPUB export failed", error)
                }
            } catch (e: Exception) {
                Log.error("Error in EPUB export", e)
                _epubExportProgress.value = EpubExportProgress.Error(e.message ?: "Export failed")
            }
        }
    }
    
    /**
     * Dismiss the EPUB export progress dialog
     */
    fun dismissEpubExportProgress() {
        _epubExportProgress.value = EpubExportProgress.Idle
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
        _events.emit(BookDetailEvent.ShowSnackbar(message.asString(localizeHelper)))
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
    
    // ==================== Custom Cover ====================
    
    /**
     * Handle image selection from the image picker.
     * Copies the image to app storage and updates the book's customCover field.
     * Also updates lastUpdate to invalidate image cache.
     * 
     * @param sourceUri The URI of the selected image (content:// or file://)
     */
    fun handleImageSelected(sourceUri: String) {
        val currentBook = book ?: return
        
        scope.launch {
            try {
                // Copy the image to app's custom cover directory
                val localFileUri = platformHelper.copyImageToCustomCover(sourceUri, currentBook.id)
                
                if (localFileUri != null) {
                    // Update the book's customCover field and lastUpdate in the database
                    // lastUpdate change ensures the image cache key changes
                    val updatedBook = currentBook.copy(
                        customCover = localFileUri,
                        lastUpdate = currentTimeToLong()
                    )
                    localInsertUseCases.updateBook.update(updatedBook)
                    
                    emitEvent(BookDetailEvent.ShowSnackbar("Custom cover set successfully"))
                } else {
                    emitEvent(BookDetailEvent.ShowSnackbar("Failed to copy image"))
                }
            } catch (e: Exception) {
                Log.error("Failed to set custom cover", e)
                emitEvent(BookDetailEvent.ShowSnackbar("Failed to set custom cover: ${e.message}"))
            }
        }
    }
    
    /**
     * Update the book's custom cover from a local file URI.
     * The URI should be a file:// path pointing to the copied image in app storage.
     * This is called after the platform-specific image picker copies the image.
     * 
     * @param localFileUri The file:// URI of the copied image
     */
    fun updateCustomCoverFromLocal(localFileUri: String) {
        val currentBook = book ?: return
        
        scope.launch {
            try {
                // Update the book's customCover field in the database
                val updatedBook = currentBook.copy(customCover = localFileUri)
                localInsertUseCases.updateBook.update(updatedBook)
                
                emitEvent(BookDetailEvent.ShowSnackbar("Custom cover set successfully"))
            } catch (e: Exception) {
                Log.error("Failed to update custom cover", e)
                emitEvent(BookDetailEvent.ShowSnackbar("Failed to set custom cover: ${e.message}"))
            }
        }
    }
    
    /**
     * Reset the custom cover to the original cover from source.
     * Clears the customCover field and updates lastUpdate to invalidate cache.
     */
    fun resetCustomCover() {
        val currentBook = book ?: return
        
        scope.launch {
            try {
                // Reset customCover to empty (will fall back to original cover)
                val updatedBook = currentBook.copy(
                    customCover = "",
                    lastUpdate = currentTimeToLong()
                )
                localInsertUseCases.updateBook.update(updatedBook)
                
                Log.info { "Custom cover reset to original" }
                emitEvent(BookDetailEvent.ShowSnackbar("Cover reset to original"))
            } catch (e: Exception) {
                Log.error("Failed to reset custom cover", e)
                emitEvent(BookDetailEvent.ShowSnackbar("Failed to reset cover: ${e.message}"))
            }
        }
    }
    
    // Cover preview dialog state
    var showCoverPreviewDialog by mutableStateOf(false)
    
    // ==================== Tracking ====================
    
    // Tracking state - per service
    var showTrackingBottomSheet by mutableStateOf(false)
    
    // AniList tracking state
    var isAniListLoggedIn by mutableStateOf(false)
    var isAniListTracked by mutableStateOf(false)
    var aniListStatus by mutableStateOf<String?>(null)
    var aniListProgress by mutableStateOf<Int?>(null)
    var aniListScore by mutableStateOf<Float?>(null)
    private var aniListTrack: ireader.domain.models.entities.Track? = null
    
    // MAL tracking state
    var isMalLoggedIn by mutableStateOf(false)
    var isMalTracked by mutableStateOf(false)
    var malStatus by mutableStateOf<String?>(null)
    var malProgress by mutableStateOf<Int?>(null)
    var malScore by mutableStateOf<Float?>(null)
    private var malTrack: ireader.domain.models.entities.Track? = null
    
    // Kitsu tracking state
    var isKitsuLoggedIn by mutableStateOf(false)
    var isKitsuTracked by mutableStateOf(false)
    var kitsuStatus by mutableStateOf<String?>(null)
    var kitsuProgress by mutableStateOf<Int?>(null)
    var kitsuScore by mutableStateOf<Float?>(null)
    private var kitsuTrack: ireader.domain.models.entities.Track? = null
    
    // MangaUpdates tracking state
    var isMangaUpdatesLoggedIn by mutableStateOf(false)
    var isMangaUpdatesTracked by mutableStateOf(false)
    var mangaUpdatesStatus by mutableStateOf<String?>(null)
    var mangaUpdatesProgress by mutableStateOf<Int?>(null)
    var mangaUpdatesScore by mutableStateOf<Float?>(null)
    private var mangaUpdatesTrack: ireader.domain.models.entities.Track? = null
    
    // MyNovelList tracking state
    var isMyNovelListLoggedIn by mutableStateOf(false)
    var isMyNovelListTracked by mutableStateOf(false)
    var myNovelListStatus by mutableStateOf<String?>(null)
    var myNovelListProgress by mutableStateOf<Int?>(null)
    var myNovelListScore by mutableStateOf<Float?>(null)
    private var myNovelListTrack: ireader.domain.models.entities.Track? = null
    
    // Legacy compatibility properties
    var isTracked: Boolean
        get() = isAniListTracked || isMalTracked || isKitsuTracked || isMangaUpdatesTracked || isMyNovelListTracked
        set(value) { isAniListTracked = value }
    var trackingStatus: String?
        get() = aniListStatus ?: malStatus ?: kitsuStatus ?: mangaUpdatesStatus ?: myNovelListStatus
        set(value) { aniListStatus = value }
    var trackingProgress: Int?
        get() = aniListProgress ?: malProgress ?: kitsuProgress ?: mangaUpdatesProgress ?: myNovelListProgress
        set(value) { aniListProgress = value }
    var trackingScore: Float?
        get() = aniListScore ?: malScore ?: kitsuScore ?: mangaUpdatesScore ?: myNovelListScore
        set(value) { aniListScore = value }
    
    // Search state
    var trackingSearchResults by mutableStateOf<List<ireader.domain.models.entities.TrackSearchResult>>(emptyList())
    var showTrackingSearchDialog by mutableStateOf(false)
    var isSearchingTracking by mutableStateOf(false)
    var currentSearchServiceId by mutableStateOf(ireader.domain.models.entities.TrackerService.ANILIST)
    
    init {
        // Load tracking status when ViewModel is created
        loadInitialTrackingStatus()
    }
    
    /**
     * Load initial tracking status for the current book
     */
    private fun loadInitialTrackingStatus() {
        val bookId = param.bookId ?: return
        if (trackingRepository == null) return
        
        scope.launch {
            try {
                // Check authentication status for all services
                isAniListLoggedIn = trackingRepository.isAuthenticated(ireader.domain.models.entities.TrackerService.ANILIST)
                isMalLoggedIn = trackingRepository.isAuthenticated(ireader.domain.models.entities.TrackerService.MYANIMELIST)
                isKitsuLoggedIn = trackingRepository.isAuthenticated(ireader.domain.models.entities.TrackerService.KITSU)
                isMangaUpdatesLoggedIn = trackingRepository.isAuthenticated(ireader.domain.models.entities.TrackerService.MANGAUPDATES)
                isMyNovelListLoggedIn = trackingRepository.isAuthenticated(ireader.domain.models.entities.TrackerService.MYNOVELLIST)
                
                // Load existing tracks for this book
                val tracks = trackingRepository.getTracksByBook(bookId)
                
                // AniList track
                tracks.find { it.siteId == ireader.domain.models.entities.TrackerService.ANILIST }?.let { track ->
                    aniListTrack = track
                    isAniListTracked = true
                    aniListStatus = track.status.name
                    aniListProgress = track.lastRead.toInt()
                    aniListScore = track.score
                }
                
                // MAL track
                tracks.find { it.siteId == ireader.domain.models.entities.TrackerService.MYANIMELIST }?.let { track ->
                    malTrack = track
                    isMalTracked = true
                    malStatus = track.status.name
                    malProgress = track.lastRead.toInt()
                    malScore = track.score
                }
                
                // Kitsu track
                tracks.find { it.siteId == ireader.domain.models.entities.TrackerService.KITSU }?.let { track ->
                    kitsuTrack = track
                    isKitsuTracked = true
                    kitsuStatus = track.status.name
                    kitsuProgress = track.lastRead.toInt()
                    kitsuScore = track.score
                }
                
                // MangaUpdates track
                tracks.find { it.siteId == ireader.domain.models.entities.TrackerService.MANGAUPDATES }?.let { track ->
                    mangaUpdatesTrack = track
                    isMangaUpdatesTracked = true
                    mangaUpdatesStatus = track.status.name
                    mangaUpdatesProgress = track.lastRead.toInt()
                    mangaUpdatesScore = track.score
                }
                
                // MyNovelList track
                tracks.find { it.siteId == ireader.domain.models.entities.TrackerService.MYNOVELLIST }?.let { track ->
                    myNovelListTrack = track
                    isMyNovelListTracked = true
                    myNovelListStatus = track.status.name
                    myNovelListProgress = track.lastRead.toInt()
                    myNovelListScore = track.score
                }
            } catch (e: Exception) {
                Log.error(e, "Failed to load tracking status")
            }
        }
    }
    
    /**
     * Show the tracking bottom sheet/dialog
     */
    fun showTrackingDialog() {
        showTrackingBottomSheet = true
        // Refresh tracking status when dialog opens
        loadTrackingStatus()
    }
    
    /**
     * Hide the tracking bottom sheet/dialog
     */
    fun hideTrackingDialog() {
        showTrackingBottomSheet = false
    }
    
    /**
     * Load/refresh tracking status for the current book from all services
     */
    private fun loadTrackingStatus() {
        val bookId = param.bookId ?: return
        if (trackingRepository == null) return
        
        scope.launch {
            try {
                // Check authentication status for all services
                isAniListLoggedIn = trackingRepository.isAuthenticated(ireader.domain.models.entities.TrackerService.ANILIST)
                isMalLoggedIn = trackingRepository.isAuthenticated(ireader.domain.models.entities.TrackerService.MYANIMELIST)
                isKitsuLoggedIn = trackingRepository.isAuthenticated(ireader.domain.models.entities.TrackerService.KITSU)
                isMangaUpdatesLoggedIn = trackingRepository.isAuthenticated(ireader.domain.models.entities.TrackerService.MANGAUPDATES)
                isMyNovelListLoggedIn = trackingRepository.isAuthenticated(ireader.domain.models.entities.TrackerService.MYNOVELLIST)
                
                // Sync and reload tracks
                val tracks = trackingRepository.getTracksByBook(bookId)
                
                // AniList
                if (isAniListTracked && aniListTrack != null) {
                    trackingRepository.syncTrack(bookId, ireader.domain.models.entities.TrackerService.ANILIST)
                }
                tracks.find { it.siteId == ireader.domain.models.entities.TrackerService.ANILIST }?.let { track ->
                    aniListTrack = track
                    isAniListTracked = true
                    aniListStatus = track.status.name
                    aniListProgress = track.lastRead.toInt()
                    aniListScore = track.score
                }
                
                // MAL
                if (isMalTracked && malTrack != null) {
                    trackingRepository.syncTrack(bookId, ireader.domain.models.entities.TrackerService.MYANIMELIST)
                }
                tracks.find { it.siteId == ireader.domain.models.entities.TrackerService.MYANIMELIST }?.let { track ->
                    malTrack = track
                    isMalTracked = true
                    malStatus = track.status.name
                    malProgress = track.lastRead.toInt()
                    malScore = track.score
                }
                
                // Kitsu
                if (isKitsuTracked && kitsuTrack != null) {
                    trackingRepository.syncTrack(bookId, ireader.domain.models.entities.TrackerService.KITSU)
                }
                tracks.find { it.siteId == ireader.domain.models.entities.TrackerService.KITSU }?.let { track ->
                    kitsuTrack = track
                    isKitsuTracked = true
                    kitsuStatus = track.status.name
                    kitsuProgress = track.lastRead.toInt()
                    kitsuScore = track.score
                }
                
                // MangaUpdates
                if (isMangaUpdatesTracked && mangaUpdatesTrack != null) {
                    trackingRepository.syncTrack(bookId, ireader.domain.models.entities.TrackerService.MANGAUPDATES)
                }
                tracks.find { it.siteId == ireader.domain.models.entities.TrackerService.MANGAUPDATES }?.let { track ->
                    mangaUpdatesTrack = track
                    isMangaUpdatesTracked = true
                    mangaUpdatesStatus = track.status.name
                    mangaUpdatesProgress = track.lastRead.toInt()
                    mangaUpdatesScore = track.score
                }
                
                // MyNovelList
                if (isMyNovelListTracked && myNovelListTrack != null) {
                    trackingRepository.syncTrack(bookId, ireader.domain.models.entities.TrackerService.MYNOVELLIST)
                }
                tracks.find { it.siteId == ireader.domain.models.entities.TrackerService.MYNOVELLIST }?.let { track ->
                    myNovelListTrack = track
                    isMyNovelListTracked = true
                    myNovelListStatus = track.status.name
                    myNovelListProgress = track.lastRead.toInt()
                    myNovelListScore = track.score
                }
            } catch (e: Exception) {
                Log.error(e, "Failed to load tracking status")
            }
        }
    }
    
    /**
     * Search for this book on a tracking service
     */
    fun searchOnService(serviceId: Int, query: String) {
        if (trackingRepository == null) {
            emitEvent(BookDetailEvent.ShowSnackbar("Tracking not available"))
            return
        }
        
        scope.launch {
            try {
                isSearchingTracking = true
                showTrackingSearchDialog = true
                currentSearchServiceId = serviceId
                
                val results = trackingRepository.searchTracker(serviceId, query)
                
                trackingSearchResults = results
                isSearchingTracking = false
                
                if (results.isEmpty()) {
                    emitEvent(BookDetailEvent.ShowSnackbar("No results found for '$query'"))
                }
            } catch (e: Exception) {
                Log.error(e, "Failed to search on service $serviceId")
                isSearchingTracking = false
                emitEvent(BookDetailEvent.ShowSnackbar("Search failed: ${e.message}"))
            }
        }
    }
    
    /**
     * Search for this book on AniList (legacy compatibility)
     */
    fun searchOnAniList(query: String) {
        searchOnService(ireader.domain.models.entities.TrackerService.ANILIST, query)
    }
    
    /**
     * Link book to a search result from a tracking service
     */
    fun linkToService(serviceId: Int, searchResult: ireader.domain.models.entities.TrackSearchResult) {
        val bookId = param.bookId ?: return
        if (trackingRepository == null) return
        
        scope.launch {
            try {
                val success = trackingRepository.linkBook(bookId, serviceId, searchResult)
                
                if (success) {
                    // Reload tracking status
                    val tracks = trackingRepository.getTracksByBook(bookId)
                    val track = tracks.find { it.siteId == serviceId }
                    
                    if (track != null) {
                        when (serviceId) {
                            ireader.domain.models.entities.TrackerService.ANILIST -> {
                                aniListTrack = track
                                isAniListTracked = true
                                aniListStatus = track.status.name
                                aniListProgress = track.lastRead.toInt()
                                aniListScore = track.score
                            }
                            ireader.domain.models.entities.TrackerService.MYANIMELIST -> {
                                malTrack = track
                                isMalTracked = true
                                malStatus = track.status.name
                                malProgress = track.lastRead.toInt()
                                malScore = track.score
                            }
                            ireader.domain.models.entities.TrackerService.KITSU -> {
                                kitsuTrack = track
                                isKitsuTracked = true
                                kitsuStatus = track.status.name
                                kitsuProgress = track.lastRead.toInt()
                                kitsuScore = track.score
                            }
                            ireader.domain.models.entities.TrackerService.MANGAUPDATES -> {
                                mangaUpdatesTrack = track
                                isMangaUpdatesTracked = true
                                mangaUpdatesStatus = track.status.name
                                mangaUpdatesProgress = track.lastRead.toInt()
                                mangaUpdatesScore = track.score
                            }
                            ireader.domain.models.entities.TrackerService.MYNOVELLIST -> {
                                myNovelListTrack = track
                                isMyNovelListTracked = true
                                myNovelListStatus = track.status.name
                                myNovelListProgress = track.lastRead.toInt()
                                myNovelListScore = track.score
                            }
                        }
                    }
                    
                    showTrackingSearchDialog = false
                    val serviceName = getServiceName(serviceId)
                    emitEvent(BookDetailEvent.ShowSnackbar("Added to $serviceName: ${searchResult.title}"))
                } else {
                    emitEvent(BookDetailEvent.ShowSnackbar("Failed to add tracking"))
                }
            } catch (e: Exception) {
                Log.error(e, "Failed to link to service $serviceId")
                emitEvent(BookDetailEvent.ShowSnackbar("Failed to add: ${e.message}"))
            }
        }
    }
    
    /**
     * Link book to a search result from AniList (legacy compatibility)
     */
    fun linkToAniList(searchResult: ireader.domain.models.entities.TrackSearchResult) {
        linkToService(ireader.domain.models.entities.TrackerService.ANILIST, searchResult)
    }
    
    /**
     * Remove tracking for the current book from a specific service
     */
    fun removeTrackingFromService(serviceId: Int) {
        val bookId = param.bookId ?: return
        if (trackingRepository == null) return
        
        scope.launch {
            try {
                val success = trackingRepository.removeTrack(bookId, serviceId)
                
                if (success) {
                    when (serviceId) {
                        ireader.domain.models.entities.TrackerService.ANILIST -> {
                            aniListTrack = null
                            isAniListTracked = false
                            aniListStatus = null
                            aniListProgress = null
                            aniListScore = null
                        }
                        ireader.domain.models.entities.TrackerService.MYANIMELIST -> {
                            malTrack = null
                            isMalTracked = false
                            malStatus = null
                            malProgress = null
                            malScore = null
                        }
                        ireader.domain.models.entities.TrackerService.KITSU -> {
                            kitsuTrack = null
                            isKitsuTracked = false
                            kitsuStatus = null
                            kitsuProgress = null
                            kitsuScore = null
                        }
                        ireader.domain.models.entities.TrackerService.MANGAUPDATES -> {
                            mangaUpdatesTrack = null
                            isMangaUpdatesTracked = false
                            mangaUpdatesStatus = null
                            mangaUpdatesProgress = null
                            mangaUpdatesScore = null
                        }
                        ireader.domain.models.entities.TrackerService.MYNOVELLIST -> {
                            myNovelListTrack = null
                            isMyNovelListTracked = false
                            myNovelListStatus = null
                            myNovelListProgress = null
                            myNovelListScore = null
                        }
                    }
                    emitEvent(BookDetailEvent.ShowSnackbar("Tracking removed"))
                } else {
                    emitEvent(BookDetailEvent.ShowSnackbar("Failed to remove tracking"))
                }
            } catch (e: Exception) {
                Log.error(e, "Failed to remove tracking")
                emitEvent(BookDetailEvent.ShowSnackbar("Failed to remove tracking: ${e.message}"))
            }
        }
    }
    
    /**
     * Remove tracking for the current book (legacy - removes AniList)
     */
    fun removeTracking() {
        removeTrackingFromService(ireader.domain.models.entities.TrackerService.ANILIST)
    }
    
    /**
     * Update tracking status value for a specific service
     */
    fun updateServiceStatus(serviceId: Int, status: String) {
        val bookId = param.bookId ?: return
        if (trackingRepository == null) return
        
        val trackStatus = try {
            ireader.domain.models.entities.TrackStatus.valueOf(status)
        } catch (e: Exception) {
            Log.error(e, "Invalid track status: $status")
            return
        }
        
        scope.launch {
            try {
                // Get the track for this service
                val tracks = trackingRepository.getTracksByBook(bookId)
                val track = tracks.find { it.siteId == serviceId }
                
                if (track != null) {
                    val update = ireader.domain.models.entities.TrackUpdate(
                        id = track.id,
                        status = trackStatus
                    )
                    val success = trackingRepository.updateTrack(update)
                    
                    if (success) {
                        when (serviceId) {
                            ireader.domain.models.entities.TrackerService.ANILIST -> aniListStatus = status
                            ireader.domain.models.entities.TrackerService.MYANIMELIST -> malStatus = status
                            ireader.domain.models.entities.TrackerService.KITSU -> kitsuStatus = status
                            ireader.domain.models.entities.TrackerService.MANGAUPDATES -> mangaUpdatesStatus = status
                            ireader.domain.models.entities.TrackerService.MYNOVELLIST -> myNovelListStatus = status
                        }
                        emitEvent(BookDetailEvent.ShowSnackbar("Status updated to $status"))
                    } else {
                        emitEvent(BookDetailEvent.ShowSnackbar("Failed to update status"))
                    }
                }
            } catch (e: Exception) {
                Log.error(e, "Failed to update tracking status")
                emitEvent(BookDetailEvent.ShowSnackbar("Failed to update status: ${e.message}"))
            }
        }
    }
    
    /**
     * Update tracking progress for a specific service
     */
    fun updateServiceProgress(serviceId: Int, progress: Int) {
        val bookId = param.bookId ?: return
        if (trackingRepository == null) return
        
        scope.launch {
            try {
                val tracks = trackingRepository.getTracksByBook(bookId)
                val track = tracks.find { it.siteId == serviceId }
                
                if (track != null) {
                    val update = ireader.domain.models.entities.TrackUpdate(
                        id = track.id,
                        lastRead = progress.toFloat()
                    )
                    val success = trackingRepository.updateTrack(update)
                    
                    if (success) {
                        when (serviceId) {
                            ireader.domain.models.entities.TrackerService.ANILIST -> aniListProgress = progress
                            ireader.domain.models.entities.TrackerService.MYANIMELIST -> malProgress = progress
                            ireader.domain.models.entities.TrackerService.KITSU -> kitsuProgress = progress
                            ireader.domain.models.entities.TrackerService.MANGAUPDATES -> mangaUpdatesProgress = progress
                            ireader.domain.models.entities.TrackerService.MYNOVELLIST -> myNovelListProgress = progress
                        }
                        emitEvent(BookDetailEvent.ShowSnackbar("Progress updated to $progress"))
                    } else {
                        emitEvent(BookDetailEvent.ShowSnackbar("Failed to update progress"))
                    }
                }
            } catch (e: Exception) {
                Log.error(e, "Failed to update tracking progress")
                emitEvent(BookDetailEvent.ShowSnackbar("Failed to update progress: ${e.message}"))
            }
        }
    }
    
    /**
     * Update tracking score for a specific service
     */
    fun updateServiceScore(serviceId: Int, score: Float) {
        val bookId = param.bookId ?: return
        if (trackingRepository == null) return
        
        scope.launch {
            try {
                val tracks = trackingRepository.getTracksByBook(bookId)
                val track = tracks.find { it.siteId == serviceId }
                
                if (track != null) {
                    val update = ireader.domain.models.entities.TrackUpdate(
                        id = track.id,
                        score = score
                    )
                    val success = trackingRepository.updateTrack(update)
                    
                    if (success) {
                        when (serviceId) {
                            ireader.domain.models.entities.TrackerService.ANILIST -> aniListScore = score
                            ireader.domain.models.entities.TrackerService.MYANIMELIST -> malScore = score
                            ireader.domain.models.entities.TrackerService.KITSU -> kitsuScore = score
                            ireader.domain.models.entities.TrackerService.MANGAUPDATES -> mangaUpdatesScore = score
                            ireader.domain.models.entities.TrackerService.MYNOVELLIST -> myNovelListScore = score
                        }
                        emitEvent(BookDetailEvent.ShowSnackbar("Score updated to ${score.formatDecimal(1)}"))
                    } else {
                        emitEvent(BookDetailEvent.ShowSnackbar("Failed to update score"))
                    }
                }
            } catch (e: Exception) {
                Log.error(e, "Failed to update tracking score")
                emitEvent(BookDetailEvent.ShowSnackbar("Failed to update score: ${e.message}"))
            }
        }
    }
    
    private fun getServiceName(serviceId: Int): String = when (serviceId) {
        ireader.domain.models.entities.TrackerService.ANILIST -> "AniList"
        ireader.domain.models.entities.TrackerService.MYANIMELIST -> "MyAnimeList"
        ireader.domain.models.entities.TrackerService.KITSU -> "Kitsu"
        ireader.domain.models.entities.TrackerService.MANGAUPDATES -> "MangaUpdates"
        ireader.domain.models.entities.TrackerService.MYNOVELLIST -> "MyNovelList"
        else -> "Unknown"
    }
    
    /**
     * Update tracking status value (Reading, Completed, etc.) - legacy compatibility
     */
    fun updateTrackingStatusValue(status: String) {
        updateServiceStatus(ireader.domain.models.entities.TrackerService.ANILIST, status)
    }
    
    /**
     * Update tracking progress (chapters read) - legacy compatibility
     */
    fun updateTrackingProgress(progress: Int) {
        updateServiceProgress(ireader.domain.models.entities.TrackerService.ANILIST, progress)
    }
    
    /**
     * Update tracking score - legacy compatibility
     */
    fun updateTrackingScore(score: Float) {
        updateServiceScore(ireader.domain.models.entities.TrackerService.ANILIST, score)
    }
    
    /**
     * Update tracking status for the current book.
     * This will be called when tracking data is loaded from AniList or other services.
     */
    fun updateTrackingStatus(tracked: Boolean, status: String?) {
        isTracked = tracked
        trackingStatus = status
    }
    
    /**
     * Share text using platform-specific sharing mechanism.
     */
    fun shareText(text: String, title: String) {
        platformHelper.shareText(text, title)
    }
    
    // ==================== Cleanup ====================
    
    override fun onCleared() {
        super.onCleared()
        
        // Cancel all jobs when ViewModel is destroyed
        getBookDetailJob?.cancel()
        getChapterDetailJob?.cancel()
        subscriptionJob?.cancel()
        
        // Cleanup ChapterController state for this book (Requirements: 9.4, 9.5)
        chapterController.dispatch(ChapterCommand.Cleanup)
        
        // Cleanup BookController state (Requirements: 4.3)
        bookController.dispatch(BookCommand.Cleanup)
        
        // Cleanup BookDetailController state (Requirements: 3.1, 3.3, 3.4, 3.5)
        bookDetailController.dispatch(BookDetailCommand.Cleanup)
        
        // Release BookDetailController resources
        bookDetailController.release()
    }
}

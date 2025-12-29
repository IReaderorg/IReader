package ireader.presentation.ui.home.library.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.state.ToggleableState
import ireader.core.log.Log
import ireader.domain.models.DisplayMode
import ireader.domain.models.entities.BookItem
import ireader.domain.models.entities.Category
import ireader.domain.models.entities.LibraryBook
import ireader.domain.models.library.LibraryFilter
import ireader.domain.models.library.LibrarySort
import ireader.domain.preferences.prefs.LibraryPreferences
import ireader.domain.services.library.LibraryChangeNotifier
import ireader.domain.services.library.LibraryCommand
import ireader.domain.services.library.LibraryController
import ireader.domain.services.platform.PlatformServices
import ireader.domain.usecases.library.LibraryUseCases
import ireader.domain.usecases.preferences.reader_preferences.screens.LibraryScreenPrefUseCases
import ireader.domain.usecases.services.ServiceUseCases
import ireader.i18n.LocalizeHelper
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val TAG = "LibraryViewModel"

/**
 * ViewModel for the Library screen following Mihon's StateScreenModel pattern.
 * 
 * SIMPLIFIED VERSION (Requirements: 3.1, 3.2, 3.3, 3.4, 4.1, 4.4):
 * - Reduced from 26 constructor parameters to 8 using aggregates
 * - State derived from LibraryController (SSOT) - no duplicate state
 * - Selection/filter/sort methods only dispatch to LibraryController
 * 
 * CHANGE DETECTION:
 * - Observes LibraryChangeNotifier to know when books are modified
 * - Reloads only affected categories (not all 10,000+ books)
 * - Debounces rapid changes to avoid excessive reloads
 */
@Stable
@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModel(
    // Aggregate 1: Library use cases (groups 11 use cases)
    private val libraryUseCases: LibraryUseCases,
    // Aggregate 2: Platform services (groups 5 services)
    private val platformServices: PlatformServices,
    // Core dependencies
    private val libraryPreferences: LibraryPreferences,
    private val libraryScreenPrefUseCases: LibraryScreenPrefUseCases,
    private val serviceUseCases: ServiceUseCases,
    private val syncUseCases: ireader.domain.usecases.sync.SyncUseCases? = null,
    private val downloadService: ireader.domain.services.common.DownloadService,
    private val localizeHelper: LocalizeHelper,
    // LibraryController - SSOT for library state (Requirements: 3.1, 3.2, 3.3, 3.4)
    private val libraryController: LibraryController,
    // Change notifier - signals when books are modified (for pagination reload)
    private val changeNotifier: LibraryChangeNotifier? = null
) : BaseViewModel() {

    // ==================== State Derived from LibraryController (SSOT) ====================
    // Requirements: 3.1, 3.2, 3.3, 3.4 - No duplicate state, derive from controller
    
    // Internal UI-specific state (not duplicated from controller)
    private val _uiState = MutableStateFlow(LibraryUiState())
    
    // Search debounce
    private val searchQueryFlow = MutableStateFlow("")
    private var searchJob: Job? = null
    
    // Search results from database
    private val _searchResults = MutableStateFlow<List<LibraryBook>>(emptyList())
    private val _searchTotalCount = MutableStateFlow(0)
    
    // Loaded manga cache
    private val loadedManga = mutableMapOf<Long, List<BookItem>>()
    
    // MutableStateFlow for each category's books - cached to avoid recreation
    private val categoryBooksState = mutableMapOf<Long, MutableStateFlow<List<BookItem>>>()
    
    // Cached category book lists to avoid recomputation
    private val categoryBooksCache = mutableMapOf<Long, List<BookItem>>()
    private var lastBooksCacheKey: Int = 0
    
    // ==================== True DB Pagination State ====================
    // Paginated books loaded directly from database (bypasses LibraryController for initial load)
    private val _paginatedBooks = MutableStateFlow<Map<Long, List<LibraryBook>>>(emptyMap())
    private val _paginatedTotalCounts = MutableStateFlow<Map<Long, Int>>(emptyMap())
    private var paginationLoadJobs = mutableMapOf<Long, Job?>()
    
    // Track categories that are currently loading to prevent duplicate loads
    private val loadingCategories = mutableSetOf<Long>()
    private val loadingCategoriesMutex = Mutex()
    
    // Change notification handling
    private var changeNotificationJob: Job? = null
    private val pendingChanges = MutableStateFlow<Set<Long>>(emptySet()) // Book IDs that changed
    
    // Preferences as StateFlow
    val lastUsedCategory = libraryPreferences.lastUsedCategory().asState()
    val filters = libraryPreferences.filters(true).asState()
    val sorting = libraryPreferences.sorting().asState()
    val showCategoryTabs = libraryPreferences.showCategoryTabs().asState()
    val showAllCategoryTab = libraryPreferences.showAllCategory().asState()
    val showCountInCategory = libraryPreferences.showCountInCategory().asState()
    val readBadge = libraryPreferences.downloadBadges().asState()
    val unreadBadge = libraryPreferences.unreadBadges().asState()
    val goToLastChapterBadge = libraryPreferences.goToLastChapterBadges().asState()
    val showDownloadedChaptersBadge = libraryPreferences.showDownloadedChaptersBadge().asState()
    val showUnreadChaptersBadge = libraryPreferences.showUnreadChaptersBadge().asState()
    val showLocalMangaBadge = libraryPreferences.showLocalMangaBadge().asState()
    val showLanguageBadge = libraryPreferences.showLanguageBadge().asState()
    val perCategorySettings = libraryPreferences.perCategorySettings().asState()
    val layouts = libraryPreferences.categoryFlags().asState()
    val columnInPortrait = libraryPreferences.columnsInPortrait().asState()
    val columnInLandscape = libraryPreferences.columnsInLandscape().asState()
    val showEmptyCategories = libraryPreferences.showEmptyCategories().asState()
    val showResumeReadingCard = libraryPreferences.showResumeReadingCard().asState()
    val showArchivedBooks = libraryPreferences.showArchivedBooks().asState()
    
    val bookCategories = libraryUseCases.categories.subscribeBookCategories().asState(emptyList())

    /**
     * Combined state derived from LibraryController and UI-specific state.
     * Requirements: 3.4 - Use combine/map to derive state from Controller
     */
    val state: StateFlow<LibraryScreenState> = combine(
        libraryController.state,
        _uiState,
        searchQueryFlow,
        libraryPreferences.columnsInPortrait().stateIn(scope),
        libraryPreferences.columnsInLandscape().stateIn(scope),
        _paginatedBooks,
        _searchResults
    ) { values ->
        val controllerState = values[0] as ireader.domain.services.library.LibraryState
        val uiState = values[1] as LibraryUiState
        val searchQuery = values[2] as String
        val columnsPortrait = values[3] as Int
        val columnsLandscape = values[4] as Int
        @Suppress("UNCHECKED_CAST")
        val paginatedBooksMap = values[5] as Map<Long, List<LibraryBook>>
        @Suppress("UNCHECKED_CAST")
        val searchResults = values[6] as List<LibraryBook>
        
        // Get current category's paginated books
        val categoryId = uiState.categories.getOrNull(uiState.selectedCategoryIndex)?.id ?: 0L
        val categoryBooks = paginatedBooksMap[categoryId] ?: emptyList()
        
        // Use search results from database when in search mode, otherwise use category books
        // Apply distinctBy to prevent duplicate key errors in LazyColumn
        val filteredBooks = if (searchQuery.isNotBlank()) {
            searchResults.distinctBy { it.id }
        } else {
            categoryBooks.distinctBy { it.id }
        }
        
        LibraryScreenState(
            isLoading = controllerState.isLoading,
            isRefreshing = controllerState.isRefreshing,
            isUpdatingLibrary = uiState.isUpdatingLibrary,
            books = filteredBooks.toImmutableList(),
            categories = uiState.categories,
            selectedCategoryIndex = uiState.selectedCategoryIndex,
            layout = uiState.layout,
            searchQuery = searchQuery.ifBlank { null },
            inSearchMode = uiState.inSearchMode,
            // Selection from LibraryController (SSOT)
            selectedBookIds = controllerState.selectedBookIds.toImmutableSet(),
            filters = filters.value.toImmutableList(),
            sort = sorting.value,
            activeFilters = filters.value
                .filter { it.value == LibraryFilter.Value.Included }
                .map { it.type }
                .toImmutableSet(),
            error = controllerState.error?.let { ireader.i18n.UiText.DynamicString(it.toUserMessage()) },
            categoryScrollPositions = uiState.categoryScrollPositions,
            showUpdateCategoryDialog = uiState.showUpdateCategoryDialog,
            showImportEpubDialog = uiState.showImportEpubDialog,
            showImportPdfDialog = uiState.showImportPdfDialog,
            batchOperationInProgress = uiState.batchOperationInProgress,
            batchOperationMessage = uiState.batchOperationMessage,
            lastUndoState = uiState.lastUndoState,
            epubImportState = uiState.epubImportState,
            epubExportState = uiState.epubExportState,
            lastReadInfo = uiState.lastReadInfo,
            isResumeCardVisible = uiState.isResumeCardVisible,
            isSyncAvailable = uiState.isSyncAvailable,
            columnsInPortrait = columnsPortrait,
            columnsInLandscape = columnsLandscape
        )
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), LibraryScreenState())
    
    // Convenience accessors
    val isLoading: Boolean get() = state.value.isLoading
    val isEmpty: Boolean get() = state.value.isEmpty
    val selectionMode: Boolean get() = state.value.selectionMode
    val selectedBooks: Set<Long> get() = state.value.selectedBookIds
    val categories get() = state.value.categories
    val selectedCategory get() = state.value.selectedCategory
    val selectedCategoryIndex get() = state.value.selectedCategoryIndex
    val books get() = state.value.books
    val searchQuery get() = state.value.searchQuery
    val inSearchMode get() = state.value.inSearchMode
    val layout get() = state.value.layout
    val isBookRefreshing get() = state.value.isRefreshing
    val showUpdateCategoryDialog get() = state.value.showUpdateCategoryDialog
    val showImportEpubDialog get() = state.value.showImportEpubDialog
    val lastReadInfo get() = state.value.lastReadInfo
    val isResumeCardVisible get() = state.value.isResumeCardVisible
    val isSyncAvailable get() = state.value.isSyncAvailable
    val batchOperationInProgress get() = state.value.batchOperationInProgress
    val batchOperationMessage get() = state.value.batchOperationMessage
    val epubImportState get() = state.value.epubImportState
    val epubExportState get() = state.value.epubExportState
    
    // Public access to deleteUseCase for compatibility
    val deleteUseCase get() = libraryUseCases.delete
    val markBookAsReadOrNotUseCase get() = libraryUseCases.markAsRead
    val getCategory get() = libraryUseCases.categories

    init {
        initializeState()
        libraryController.dispatch(LibraryCommand.LoadLibrary)
    }
    
    private fun initializeState() {
        // Load initial preferences - use libraryPreferences.sorting() which is the SSOT
        // Note: Previously used sortersUseCase which was a different preference key, causing sort to reset on restart
        scope.launch {
            val savedSort = libraryPreferences.sorting().get()
            _uiState.update { it.copy(sort = savedSort) }
        }
        
        // Load layout preference
        scope.launch {
            val layoutFlags = libraryPreferences.categoryFlags().get()
            val displayMode = DisplayMode.getFlag(layoutFlags) ?: DisplayMode.CompactGrid
            _uiState.update { it.copy(layout = displayMode) }
        }
        
        // Check sync availability
        scope.launch {
            val available = syncUseCases?.isSyncAvailable() ?: false
            _uiState.update { it.copy(isSyncAvailable = available) }
        }
        
        // Load last read info
        scope.launch {
            val info = libraryUseCases.getLastRead()
            _uiState.update { 
                it.copy(
                    lastReadInfo = info,
                    isResumeCardVisible = showResumeReadingCard.value
                )
            }
        }
        
        // Subscribe to categories
        // Subscribe to categories reactively for real-time updates
        // This is memory-safe because it only loads category metadata, not books
        combine(
            libraryPreferences.showAllCategory().stateIn(scope),
            libraryPreferences.showEmptyCategories().stateIn(scope)
        ) { showAll, showEmpty ->
            Pair(showAll, showEmpty)
        }.flatMapLatest { (showAll, showEmpty) ->
            libraryUseCases.categories.subscribe(showAll, showEmpty, scope)
        }.onEach { categoriesList ->
            val lastCategoryId = lastUsedCategory.value
            val currentIndex = _uiState.value.selectedCategoryIndex
            // Preserve current selection if possible, otherwise use last used category
            val index = if (currentIndex < categoriesList.size) {
                currentIndex
            } else {
                categoriesList.indexOfFirst { it.id == lastCategoryId }.takeIf { it >= 0 } ?: 0
            }
            
            _uiState.update { current ->
                current.copy(
                    categories = categoriesList.toImmutableList(),
                    selectedCategoryIndex = index
                )
            }
        }.launchIn(scope)
        
        // Load initial books for the default category
        scope.launch {
            try {
                val categoriesList = libraryUseCases.categories.await()
                val lastCategoryId = lastUsedCategory.value
                val index = categoriesList.indexOfFirst { it.category.id == lastCategoryId }.takeIf { it >= 0 } ?: 0
                
                // Check if we have preloaded cache from app startup
                // This provides instant display while DB query runs
                val categoryId = categoriesList.getOrNull(index)?.category?.id ?: 0L
                val cachedBooks = ireader.domain.data.cache.LibraryDataCache.getCachedBooks()
                
                if (cachedBooks.isNotEmpty() && categoryId == 0L) {
                    // Use cached books for instant display (only for "All" category)
                    _paginatedBooks.update { current ->
                        current + (categoryId to cachedBooks)
                    }
                    _paginatedTotalCounts.update { current ->
                        current + (categoryId to cachedBooks.size)
                    }
                    _uiState.update { current ->
                        val newPaginationState = current.categoryPaginationState.toMutableMap()
                        newPaginationState[categoryId] = PaginationState(
                            loadedCount = cachedBooks.size,
                            isLoadingMore = false,
                            hasMoreItems = true, // Assume there are more books
                            totalItems = cachedBooks.size
                        )
                        current.copy(categoryPaginationState = newPaginationState)
                    }
                    // Still load from DB to get accurate count and potentially more books
                    // Use forceRefresh=true since we already have cached data
                    loadInitialBooksForCategory(categoryId, forceRefresh = true)
                } else {
                    // No cache or different category - load from DB
                    loadInitialBooksForCategory(categoryId)
                }
            } catch (e: Exception) {
                // Even if categories fail, try to load books for "All" category
                loadInitialBooksForCategory(0L)
            }
        }
        
        // Debounced search
        searchQueryFlow
            .debounce(300)
            .onEach { query ->
                _uiState.update { it.copy(searchQuery = query.ifBlank { null }) }
            }
            .launchIn(scope)
        
        // ==================== Change Notification Observer ====================
        // Observe LibraryChangeNotifier to know when books are modified.
        // This replaces the old subscribe() approach which loaded ALL books.
        // Now we only get lightweight signals and reload the affected page.
        observeLibraryChanges()
    }
    
    /**
     * Observe library changes and reload pagination when needed.
     * 
     * This is memory-safe because:
     * 1. We only receive lightweight signals (book IDs), not full book data
     * 2. We debounce rapid changes to avoid excessive reloads
     * 3. We only reload the current category's page, not all 10,000+ books
     */
    private fun observeLibraryChanges() {
        if (changeNotifier == null) {
            Log.warn { "$TAG: No changeNotifier available, skipping change observation" }
            return
        }
        
        Log.info { "$TAG: Setting up LibraryChangeNotifier observer" }
        
        changeNotificationJob?.cancel()
        changeNotificationJob = changeNotifier.changes
            .onEach { change ->
                Log.info { "$TAG: Received library change from flow: $change" }
                handleLibraryChange(change)
            }
            .launchIn(scope)
        
        Log.info { "$TAG: LibraryChangeNotifier observer started" }
        
        // Also observe pending changes with debounce to batch rapid updates
        pendingChanges
            .debounce(500) // Wait 500ms for more changes before reloading
            .onEach { changedBookIds ->
                if (changedBookIds.isNotEmpty()) {
                    Log.debug { "$TAG: Processing ${changedBookIds.size} pending book changes" }
                    processPendingChanges(changedBookIds)
                    pendingChanges.value = emptySet() // Clear after processing
                }
            }
            .launchIn(scope)
    }
    
    /**
     * Handle a single library change event.
     * For individual book changes, we batch them and debounce.
     * For full refresh, we reload immediately.
     */
    private fun handleLibraryChange(change: LibraryChangeNotifier.ChangeType) {
        Log.info { "$TAG: handleLibraryChange() received: $change" }
        when (change) {
            is LibraryChangeNotifier.ChangeType.BookAdded,
            is LibraryChangeNotifier.ChangeType.BookRemoved -> {
                // Book added/removed affects counts and potentially visible list
                // Reload current category
                Log.info { "$TAG: BookAdded/Removed - calling refreshCurrentCategoryDebounced()" }
                refreshCurrentCategoryDebounced()
            }
            
            is LibraryChangeNotifier.ChangeType.BookUpdated -> {
                // Single book updated - check if it's in current view
                val bookId = change.bookId
                pendingChanges.update { it + bookId }
            }
            
            is LibraryChangeNotifier.ChangeType.BooksUpdated -> {
                // Multiple books updated - batch them
                pendingChanges.update { it + change.bookIds.toSet() }
            }
            
            is LibraryChangeNotifier.ChangeType.CategoryChanged -> {
                // Book's category changed - may affect current view
                Log.info { "$TAG: CategoryChanged - calling refreshCurrentCategoryDebounced()" }
                refreshCurrentCategoryDebounced()
            }
            
            is LibraryChangeNotifier.ChangeType.CategoriesChanged -> {
                // Multiple category changes - reload current category
                Log.info { "$TAG: CategoriesChanged for ${change.bookIds} - calling refreshCurrentCategoryDebounced()" }
                refreshCurrentCategoryDebounced()
            }
            
            is LibraryChangeNotifier.ChangeType.ReadProgressChanged -> {
                // Read progress changed - update if sorting by last read
                val currentSort = sorting.value
                if (currentSort.type == LibrarySort.Type.LastRead) {
                    refreshCurrentCategoryDebounced()
                } else {
                    // Just update the specific book in the list if visible
                    pendingChanges.update { it + change.bookId }
                }
            }
            
            is LibraryChangeNotifier.ChangeType.FullRefresh,
            is LibraryChangeNotifier.ChangeType.Unknown -> {
                // Full refresh needed - reload all loaded categories
                Log.info { "$TAG: Full library refresh triggered" }
                resetAllPagination()
            }
        }
    }
    
    /**
     * Process batched book changes.
     * Only reloads if any changed book is in the current view.
     */
    private fun processPendingChanges(changedBookIds: Set<Long>) {
        val currentCategoryId = categories.getOrNull(selectedCategoryIndex)?.id ?: 0L
        val currentBooks = _paginatedBooks.value[currentCategoryId] ?: emptyList()
        
        // Check if any changed book is in current view
        val affectsCurrentView = currentBooks.any { it.id in changedBookIds }
        
        if (affectsCurrentView) {
            Log.debug { "$TAG: Changed books affect current view, refreshing" }
            refreshCurrentCategory()
        } else {
            Log.debug { "$TAG: Changed books not in current view, skipping refresh" }
        }
    }
    
    /**
     * Debounced refresh of current category.
     * Prevents rapid reloads when multiple changes occur quickly.
     */
    private var refreshDebounceJob: Job? = null
    private fun refreshCurrentCategoryDebounced() {
        refreshDebounceJob?.cancel()
        refreshDebounceJob = scope.launch {
            kotlinx.coroutines.delay(300) // Debounce 300ms
            refreshCurrentCategory()
        }
    }

    // ==================== Selection Management ====================
    // Requirements: 3.2 - Only dispatch to LibraryController, no duplicate state updates
    
    fun toggleSelection(bookId: Long) {
        // Only dispatch to LibraryController (SSOT pattern)
        if (bookId in state.value.selectedBookIds) {
            libraryController.dispatch(LibraryCommand.DeselectBook(bookId))
        } else {
            libraryController.dispatch(LibraryCommand.SelectBook(bookId))
        }
    }
    
    fun unselectAll() {
        // Only dispatch to LibraryController (SSOT pattern)
        libraryController.dispatch(LibraryCommand.ClearSelection)
    }
    
    fun selectAllInCurrentCategory() {
        // Since LibraryController no longer loads books (to prevent OOM),
        // we handle select all here using the paginated books
        val categoryId = categories.getOrNull(selectedCategoryIndex)?.id ?: 0L
        val paginatedBooks = _paginatedBooks.value[categoryId] ?: emptyList()
        
        // Select all visible books by dispatching individual select commands
        paginatedBooks.forEach { book ->
            if (book.id !in state.value.selectedBookIds) {
                libraryController.dispatch(LibraryCommand.SelectBook(book.id))
            }
        }
    }
    
    fun flipAllInCurrentCategory() {
        // Since LibraryController no longer loads books (to prevent OOM),
        // we handle invert selection here using the paginated books
        val categoryId = categories.getOrNull(selectedCategoryIndex)?.id ?: 0L
        val paginatedBooks = _paginatedBooks.value[categoryId] ?: emptyList()
        val currentSelection = state.value.selectedBookIds
        
        // Invert selection for all visible books
        paginatedBooks.forEach { book ->
            if (book.id in currentSelection) {
                libraryController.dispatch(LibraryCommand.DeselectBook(book.id))
            } else {
                libraryController.dispatch(LibraryCommand.SelectBook(book.id))
            }
        }
    }

    // ==================== Scroll Position ====================
    
    fun saveScrollPosition(categoryId: Long, index: Int, offset: Int) {
        _uiState.update { current ->
            current.copy(
                categoryScrollPositions = current.categoryScrollPositions + (categoryId to (index to offset))
            )
        }
    }
    
    fun getScrollPosition(categoryId: Long): Pair<Int, Int> {
        return _uiState.value.categoryScrollPositions[categoryId] ?: (0 to 0)
    }

    // ==================== Category Management ====================
    
    fun setSelectedPage(index: Int) {
        if (index == selectedCategoryIndex) return
        val category = categories.getOrNull(index) ?: return
        _uiState.update { it.copy(selectedCategoryIndex = index) }
        lastUsedCategory.value = category.id
    }
    
    // ==================== Resume Reading ====================
    
    fun loadLastReadInfo() {
        scope.launch {
            val info = libraryUseCases.getLastRead()
            _uiState.update { 
                it.copy(
                    lastReadInfo = info,
                    isResumeCardVisible = showResumeReadingCard.value
                )
            }
        }
    }
    
    fun dismissResumeCard() {
        _uiState.update { it.copy(isResumeCardVisible = false) }
    }
    
    fun toggleResumeReadingCard(enabled: Boolean) {
        libraryPreferences.showResumeReadingCard().set(enabled)
        _uiState.update { it.copy(isResumeCardVisible = enabled) }
    }
    
    // ==================== Category Update ====================
    
    fun updateCategory(categoryId: Long) {
        scope.launch {
            val selectedIds = state.value.selectedBookIds.toList()
            if (selectedIds.isNotEmpty()) {
                val bookCategories = selectedIds.map { bookId ->
                    ireader.domain.models.entities.BookCategory(bookId = bookId, categoryId = categoryId)
                }
                libraryUseCases.categories.insertBookCategory(bookCategories)
            }
            hideUpdateCategoryDialog()
            unselectAll()
        }
    }
    
    fun showUpdateCategoryDialog() {
        _uiState.update { it.copy(showUpdateCategoryDialog = true) }
    }
    
    fun hideUpdateCategoryDialog() {
        _uiState.update { it.copy(showUpdateCategoryDialog = false) }
    }
    
    // ==================== Layout & Display ====================
    
    fun updateColumnCount(count: Int) {
        libraryPreferences.columnsInPortrait().set(count)
    }
    
    fun onLayoutTypeChange(mode: DisplayMode) {
        _uiState.update { it.copy(layout = mode) }
        libraryPreferences.categoryFlags().set(mode.flag)
    }
    
    fun toggleShowArchivedBooks(enabled: Boolean) {
        libraryPreferences.showArchivedBooks().set(enabled)
    }
    
    /**
     * Get columns for orientation (simple version).
     */
    fun getColumnsForOrientation(isLandscape: Boolean): Int {
        return if (isLandscape) columnInLandscape.value else columnInPortrait.value
    }
    
    /**
     * Get columns for orientation as StateFlow (for use with CoroutineScope).
     * This overload is used by LibraryContent for reactive column updates.
     */
    fun getColumnsForOrientation(isLandscape: Boolean, coroutineScope: CoroutineScope): StateFlow<Int> {
        return if (isLandscape) {
            libraryPreferences.columnsInLandscape().stateIn(coroutineScope)
        } else {
            libraryPreferences.columnsInPortrait().stateIn(coroutineScope)
        }
    }
    
    /**
     * Get the default toggleable state for a category based on selected books.
     * Used by EditCategoriesDialog to determine checkbox state.
     */
    fun getDefaultValue(category: Category): ToggleableState {
        val selectedIds = state.value.selectedBookIds
        if (selectedIds.isEmpty()) return ToggleableState.Off
        
        val bookCategoriesList = bookCategories.value
        val booksInCategory = bookCategoriesList.filter { it.categoryId == category.id }
        val selectedInCategory = booksInCategory.count { it.bookId in selectedIds }
        
        return when {
            selectedInCategory == 0 -> ToggleableState.Off
            selectedInCategory == selectedIds.size -> ToggleableState.On
            else -> ToggleableState.Indeterminate
        }
    }
    
    // ==================== Search ====================
    
    fun searchInLibrary(query: String) {
        searchQueryFlow.value = query
        _uiState.update { it.copy(inSearchMode = query.isNotBlank()) }
        
        // Cancel previous search job
        searchJob?.cancel()
        
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _searchTotalCount.value = 0
            return
        }
        
        // Perform database search with debounce handled by the flow
        searchJob = scope.launch {
            try {
                val sort = sorting.value
                val filtersList = filters.value
                val includeArchived = showArchivedBooks.value
                
                // Search with pagination (load first 100 results)
                val (results, totalCount) = libraryUseCases.getLibraryCategory.searchPaginated(
                    query = query,
                    sort = sort,
                    filters = filtersList,
                    limit = 100,
                    offset = 0,
                    includeArchived = includeArchived
                )
                
                _searchResults.value = results
                _searchTotalCount.value = totalCount
            } catch (e: Exception) {
                _searchResults.value = emptyList()
                _searchTotalCount.value = 0
            }
        }
    }
    
    fun clearSearch() {
        searchJob?.cancel()
        searchQueryFlow.value = ""
        _searchResults.value = emptyList()
        _searchTotalCount.value = 0
        _uiState.update { it.copy(inSearchMode = false, searchQuery = null) }
    }
    
    fun setSearchMode(enabled: Boolean) {
        _uiState.update { it.copy(inSearchMode = enabled) }
        if (!enabled) {
            clearSearch()
        }
    }
    
    // ==================== Library Operations ====================
    
    fun refreshUpdate() {
        libraryController.dispatch(LibraryCommand.RefreshLibrary)
        // Reset pagination to reload fresh data
        resetAllPagination()
    }
    
    fun updateLibrary() {
        scope.launch {
            _uiState.update { it.copy(isUpdatingLibrary = true) }
            serviceUseCases.startLibraryUpdateServicesUseCase.start()
            _uiState.update { it.copy(isUpdatingLibrary = false) }
        }
    }
    
    fun syncWithRemote() {
        scope.launch {
            syncUseCases?.syncLibrary?.invoke()
        }
    }
    
    fun openRandomEntry(): Long? {
        val booksList = state.value.books
        return if (booksList.isNotEmpty()) {
            booksList.random().id
        } else null
    }
    
    // ==================== EPUB Import ====================
    
    fun setShowImportEpubDialog(show: Boolean) {
        _uiState.update { it.copy(showImportEpubDialog = show) }
    }
    
    fun importEpubFiles(uris: List<String>) {
        scope.launch {
            _uiState.update { 
                it.copy(
                    epubImportState = it.epubImportState.copy(
                        showProgress = true,
                        selectedUris = uris
                    )
                )
            }
            
            try {
                val parsedUris = uris.map { ireader.domain.models.common.Uri.parse(it) }
                libraryUseCases.importEpub.parse(parsedUris)
            } catch (e: Exception) {
                // Handle error
            }
            
            _uiState.update { 
                it.copy(
                    epubImportState = it.epubImportState.copy(showProgress = false),
                    showImportEpubDialog = false
                )
            }
        }
    }
    
    // ==================== PDF Import ====================
    
    fun setShowImportPdfDialog(show: Boolean) {
        _uiState.update { it.copy(showImportPdfDialog = show) }
    }
    
    fun importPdfFiles(uris: List<String>) {
        scope.launch {
            _uiState.update { 
                it.copy(
                    epubImportState = it.epubImportState.copy(
                        showProgress = true,
                        selectedUris = uris
                    )
                )
            }
            
            try {
                val parsedUris = uris.map { ireader.domain.models.common.Uri.parse(it) }
                libraryUseCases.importPdf.parse(parsedUris)
            } catch (e: Exception) {
                // Handle error
            }
            
            _uiState.update { 
                it.copy(
                    epubImportState = it.epubImportState.copy(showProgress = false),
                    showImportPdfDialog = false
                )
            }
        }
    }
    
    // ==================== Book Operations ====================
    
    /**
     * Mark selected books as read (uses current selection if no bookIds provided).
     */
    fun markAsReadWithUndo(bookIds: List<Long> = state.value.selectedBookIds.toList()) {
        scope.launch {
            libraryUseCases.markAsRead.markAsRead(bookIds)
        }
    }
    
    /**
     * Mark selected books as unread (uses current selection if no bookIds provided).
     */
    fun markAsUnreadWithUndo(bookIds: List<Long> = state.value.selectedBookIds.toList()) {
        scope.launch {
            libraryUseCases.markAsRead.markAsNotRead(bookIds)
        }
    }
    
    /**
     * Download chapters for selected books (uses current selection if no bookIds provided).
     */
    fun downloadChapters(bookIds: List<Long> = state.value.selectedBookIds.toList()) {
        scope.launch {
            downloadService.queueBooks(bookIds)
        }
    }
    
    /**
     * Download unread chapters for selected books (uses current selection if no bookIds provided).
     */
    fun downloadUnreadChapters(bookIds: List<Long> = state.value.selectedBookIds.toList()) {
        scope.launch {
            libraryUseCases.downloadUnread.downloadUnreadChapters(bookIds)
        }
    }
    
    // ==================== Library Content ====================
    
    /**
     * Get library books for a specific category index.
     * Returns a list of LibraryBook filtered by category.
     * 
     * Uses the BookCategory join table to properly filter books that belong to multiple categories.
     * OPTIMIZED: Uses cached category lookup sets for O(1) membership checks.
     */
    fun getLibraryForCategoryIndex(categoryIndex: Int): List<ireader.domain.models.entities.LibraryBook> {
        val category = categories.getOrNull(categoryIndex) ?: return emptyList()
        val allBooks = state.value.books
        
        // Category ID 0 means "All" - return all books
        if (category.id == 0L) {
            return allBooks
        }
        
        // Get book-category associations - cache the set for O(1) lookups
        val bookCategoriesList = bookCategories.value
        
        // For uncategorized category (ID -1), return books with no category associations
        if (category.id == -1L) {
            val categorizedBookIds = bookCategoriesList.mapTo(HashSet(bookCategoriesList.size)) { it.bookId }
            return allBooks.filter { it.id !in categorizedBookIds }
        }
        
        // For regular categories, filter by BookCategory join table
        // Pre-filter to only relevant associations, then create set
        val bookIdsInCategory = bookCategoriesList
            .asSequence()
            .filter { it.categoryId == category.id }
            .mapTo(HashSet()) { it.bookId }
        
        return allBooks.filter { it.id in bookIdsInCategory }
    }
    
    /**
     * Get library books for a specific category index as a Composable State.
     * Used by LibraryPager which expects @Composable (page: Int) -> State<List<BookItem>>.
     * 
     * PERFORMANCE OPTIMIZED for 800+ books with TRUE DB PAGINATION:
     * - Loads only the requested page from the database
     * - Uses stable cache key to avoid unnecessary recomputation
     * - Automatically loads initial page when category is first accessed
     * - Re-triggers load when pagination state is cleared (e.g., after returning from detail screen)
     */
    @Composable
    fun getLibraryForCategoryIndexAsState(categoryIndex: Int): State<List<BookItem>> {
        val currentState by state.collectAsState()
        val paginatedBooksMap by _paginatedBooks.collectAsState()
        val category = currentState.categories.getOrNull(categoryIndex)
        val categoryId = category?.id ?: 0L
        
        // Get pagination state for this category
        val paginationState = currentState.categoryPaginationState[categoryId]
        
        // Check if books are loaded for this category
        val hasBooks = paginatedBooksMap.containsKey(categoryId)
        
        // Load initial page if not loaded yet OR if pagination state was cleared (e.g., after refresh)
        // Key on both categoryId AND paginationState to re-trigger when state is cleared
        // Also key on hasBooks to handle the case where books were cleared but pagination state wasn't yet
        androidx.compose.runtime.LaunchedEffect(categoryId, paginationState == null, hasBooks) {
            Log.debug { "$TAG: getLibraryForCategoryIndexAsState LaunchedEffect - categoryId=$categoryId, paginationState=${paginationState != null}, hasBooks=$hasBooks" }
            if (category != null && (paginationState == null || !hasBooks)) {
                Log.debug { "$TAG: Triggering loadInitialBooksForCategory for categoryId=$categoryId" }
                loadInitialBooksForCategory(categoryId)
            }
        }
        
        // Create a stable cache key based on paginated data
        val cacheKey = remember(
            paginatedBooksMap[categoryId]?.size ?: 0,
            category?.id,
            paginationState?.loadedCount ?: 0
        ) {
            Triple(
                paginatedBooksMap[categoryId]?.hashCode() ?: 0,
                category?.id,
                paginationState?.loadedCount ?: 0
            )
        }
        
        // Use derivedStateOf for efficient recomputation only when needed
        val booksState = remember(cacheKey) {
            androidx.compose.runtime.derivedStateOf {
                if (category == null) {
                    emptyList()
                } else {
                    // Use paginated books from database
                    // Use distinctBy to ensure no duplicate book IDs (prevents LazyColumn key errors)
                    val paginatedBooks = paginatedBooksMap[categoryId] ?: emptyList()
                    paginatedBooks.distinctBy { it.id }.map { it.toBookItem() }
                }
            }
        }
        
        return booksState
    }
    
    /**
     * Get ALL library books for a specific category (without pagination).
     * Used internally for total count calculations.
     * Note: With true DB pagination, this returns the currently loaded books.
     * Use getTotalBooksForCategory() for the actual total count.
     */
    @Composable
    fun getAllBooksForCategoryAsState(categoryIndex: Int): State<List<BookItem>> {
        val currentState by state.collectAsState()
        val paginatedBooksMap by _paginatedBooks.collectAsState()
        val totalCountsMap by _paginatedTotalCounts.collectAsState()
        val category = currentState.categories.getOrNull(categoryIndex)
        val categoryId = category?.id ?: 0L
        
        val cacheKey = remember(
            paginatedBooksMap[categoryId]?.size ?: 0,
            totalCountsMap[categoryId] ?: 0,
            category?.id
        ) {
            Triple(
                paginatedBooksMap[categoryId]?.hashCode() ?: 0,
                totalCountsMap[categoryId] ?: 0,
                category?.id
            )
        }
        
        val booksState = remember(cacheKey) {
            androidx.compose.runtime.derivedStateOf {
                if (category == null) {
                    emptyList()
                } else {
                    // Return currently loaded paginated books
                    // Use distinctBy to ensure no duplicate book IDs (prevents LazyColumn key errors)
                    val paginatedBooks = paginatedBooksMap[categoryId] ?: emptyList()
                    paginatedBooks.distinctBy { it.id }.map { it.toBookItem() }
                }
            }
        }
        
        return booksState
    }
    
    /**
     * Get cached books for a category with optimized filtering.
     * Uses pre-computed sets for O(1) membership checks.
     */
    private fun getCachedBooksForCategory(
        categoryId: Long,
        allBooks: List<ireader.domain.models.entities.LibraryBook>,
        bookCategories: List<ireader.domain.models.entities.BookCategory>
    ): List<BookItem> {
        // "All" category - return all books
        if (categoryId == 0L) {
            return allBooks.map { it.toBookItem() }
        }
        
        // Build category membership set once (O(n) but only once)
        if (categoryId == -1L) {
            // Uncategorized - books with no category associations
            val categorizedBookIds = bookCategories.mapTo(HashSet(bookCategories.size)) { it.bookId }
            return allBooks
                .asSequence()
                .filter { it.id !in categorizedBookIds }
                .map { it.toBookItem() }
                .toList()
        }
        
        // Regular category - filter by BookCategory join table
        val bookIdsInCategory = bookCategories
            .asSequence()
            .filter { it.categoryId == categoryId }
            .mapTo(HashSet()) { it.bookId }
        
        return allBooks
            .asSequence()
            .filter { it.id in bookIdsInCategory }
            .map { it.toBookItem() }
            .toList()
    }
    
    // ==================== Clipboard ====================
    
    fun copyBookTitle(title: String) {
        scope.launch {
            platformServices.clipboard.copyText(title)
        }
    }
    
    // ==================== EPUB Export ====================
    
    fun exportBookAsEpub(bookId: Long) {
        scope.launch {
            _uiState.update { 
                it.copy(epubExportState = it.epubExportState.copy(showProgress = true))
            }
            // Export logic would go here
            _uiState.update { 
                it.copy(epubExportState = it.epubExportState.copy(showProgress = false))
            }
        }
    }

    // ==================== Filter/Sort Management ====================
    // Requirements: 3.3 - Only dispatch to LibraryController, no duplicate state updates
    
    /**
     * Toggle a filter type (cycles through Included -> Excluded -> Missing).
     * Dispatches to LibraryController and persists to preferences.
     */
    fun toggleFilter(type: LibraryFilter.Type) {
        val currentFilters = filters.value.toMutableList()
        val index = currentFilters.indexOfFirst { it.type == type }
        
        if (index >= 0) {
            val current = currentFilters[index]
            val newValue = when (current.value) {
                LibraryFilter.Value.Missing -> LibraryFilter.Value.Included
                LibraryFilter.Value.Included -> LibraryFilter.Value.Excluded
                LibraryFilter.Value.Excluded -> LibraryFilter.Value.Missing
            }
            currentFilters[index] = LibraryFilter(type, newValue)
        }
        
        // Persist to preferences
        libraryPreferences.filters(true).set(currentFilters)
        
        // Dispatch filter change to LibraryController
        val activeFilter = currentFilters.firstOrNull { it.value == LibraryFilter.Value.Included }
        if (activeFilter != null) {
            val controllerFilter = when (activeFilter.type) {
                LibraryFilter.Type.Unread -> ireader.domain.services.library.LibraryFilter.Unread
                LibraryFilter.Type.Completed -> ireader.domain.services.library.LibraryFilter.Completed
                LibraryFilter.Type.Downloaded -> ireader.domain.services.library.LibraryFilter.Downloaded
                LibraryFilter.Type.InProgress -> ireader.domain.services.library.LibraryFilter.Started // Map InProgress to Started
            }
            libraryController.dispatch(LibraryCommand.SetFilter(controllerFilter))
        } else {
            libraryController.dispatch(LibraryCommand.SetFilter(ireader.domain.services.library.LibraryFilter.None))
        }
        
        // Reset pagination to reload with new filter - pass the new filters directly
        resetAllPagination(newFilters = currentFilters)
    }
    
    /**
     * Toggle a filter immediately (same as toggleFilter but with immediate effect).
     * Requirements: 3.3 - Only dispatch to LibraryController
     */
    fun toggleFilterImmediate(type: LibraryFilter.Type) {
        toggleFilter(type)
    }
    
    /**
     * Toggle sort type.
     * Requirements: 3.3 - Only dispatch to LibraryController
     */
    fun toggleSort(type: LibrarySort.Type) {
        val currentSort = sorting.value
        val newSort = if (currentSort.type == type) {
            // Same type - toggle direction
            LibrarySort(type, !currentSort.isAscending)
        } else {
            // Different type - use ascending by default
            LibrarySort(type, true)
        }
        
        // Persist to preferences
        libraryPreferences.sorting().set(newSort)
        
        // Dispatch to LibraryController - map to controller sort types
        val controllerSortType = when (type) {
            LibrarySort.Type.Title -> ireader.domain.services.library.LibrarySort.Type.Title
            LibrarySort.Type.LastRead -> ireader.domain.services.library.LibrarySort.Type.LastRead
            LibrarySort.Type.DateAdded -> ireader.domain.services.library.LibrarySort.Type.DateAdded
            LibrarySort.Type.TotalChapters -> ireader.domain.services.library.LibrarySort.Type.TotalChapters
            // Map LastUpdated to DateAdded (which sorts by lastUpdate field in controller)
            LibrarySort.Type.LastUpdated -> ireader.domain.services.library.LibrarySort.Type.DateAdded
            LibrarySort.Type.Unread -> ireader.domain.services.library.LibrarySort.Type.UnreadCount
            LibrarySort.Type.DateFetched -> ireader.domain.services.library.LibrarySort.Type.DateAdded
            LibrarySort.Type.Source -> ireader.domain.services.library.LibrarySort.Type.Title
        }
        val controllerSort = ireader.domain.services.library.LibrarySort(
            type = controllerSortType,
            ascending = newSort.isAscending
        )
        libraryController.dispatch(LibraryCommand.SetSort(controllerSort))
        
        // Update UI state
        _uiState.update { it.copy(sort = newSort) }
        
        // Reset pagination to reload with new sort - pass the new sort directly
        resetAllPagination(newSort = newSort)
    }
    
    /**
     * Toggle sort direction.
     * Requirements: 3.3 - Only dispatch to LibraryController
     */
    fun toggleSortDirection() {
        val currentSort = sorting.value
        val newSort = LibrarySort(currentSort.type, !currentSort.isAscending)
        
        // Persist to preferences
        libraryPreferences.sorting().set(newSort)
        
        // Dispatch to LibraryController - map to controller sort types
        val controllerSortType = when (currentSort.type) {
            LibrarySort.Type.Title -> ireader.domain.services.library.LibrarySort.Type.Title
            LibrarySort.Type.LastRead -> ireader.domain.services.library.LibrarySort.Type.LastRead
            LibrarySort.Type.DateAdded -> ireader.domain.services.library.LibrarySort.Type.DateAdded
            LibrarySort.Type.TotalChapters -> ireader.domain.services.library.LibrarySort.Type.TotalChapters
            // Map LastUpdated to DateAdded (which sorts by lastUpdate field in controller)
            LibrarySort.Type.LastUpdated -> ireader.domain.services.library.LibrarySort.Type.DateAdded
            LibrarySort.Type.Unread -> ireader.domain.services.library.LibrarySort.Type.UnreadCount
            LibrarySort.Type.DateFetched -> ireader.domain.services.library.LibrarySort.Type.DateAdded
            LibrarySort.Type.Source -> ireader.domain.services.library.LibrarySort.Type.Title
        }
        val controllerSort = ireader.domain.services.library.LibrarySort(
            type = controllerSortType,
            ascending = newSort.isAscending
        )
        libraryController.dispatch(LibraryCommand.SetSort(controllerSort))
        
        // Update UI state
        _uiState.update { it.copy(sort = newSort) }
        
        // Reset pagination to reload with new sort direction
        resetAllPagination()
    }
    
    // ==================== Pagination ====================
    
    /**
     * Get pagination state for a category.
     */
    fun getPaginationState(categoryId: Long): PaginationState {
        return _uiState.value.categoryPaginationState[categoryId] 
            ?: PaginationState(totalItems = _paginatedTotalCounts.value[categoryId] ?: 0)
    }
    
    /**
     * Load initial page of books for a category from database.
     * This uses true DB pagination - only loads INITIAL_PAGE_SIZE books from the database.
     * 
     * @param categoryId The category to load books for
     * @param forceRefresh If true, will reload even if data is already cached (used after showing preloaded cache)
     */
    fun loadInitialBooksForCategory(categoryId: Long, forceRefresh: Boolean = false) {
        loadInitialBooksForCategoryWithParams(categoryId, forceRefresh, null, null)
    }
    
    /**
     * Load initial page of books for a category from database with optional parameter overrides.
     * This is used when sort/filter changes to ensure the new values are used immediately
     * before the StateFlow updates.
     * 
     * @param categoryId The category to load books for
     * @param forceRefresh If true, will reload even if data is already cached
     * @param sortOverride Optional sort to use instead of the current StateFlow value
     * @param filtersOverride Optional filters to use instead of the current StateFlow value
     */
    private fun loadInitialBooksForCategoryWithParams(
        categoryId: Long,
        forceRefresh: Boolean = false,
        sortOverride: LibrarySort? = null,
        filtersOverride: List<LibraryFilter>? = null
    ) {
        // Cancel any existing load job for this category
        paginationLoadJobs[categoryId]?.cancel()
        
        paginationLoadJobs[categoryId] = scope.launch {
            // Prevent duplicate loads - check if already loading or loaded (inside coroutine for thread safety)
            val shouldLoad = loadingCategoriesMutex.withLock {
                when {
                    categoryId in loadingCategories -> {
                        false
                    }
                    !forceRefresh && _paginatedBooks.value.containsKey(categoryId) -> {
                        false
                    }
                    else -> {
                        loadingCategories.add(categoryId)
                        true
                    }
                }
            }
            
            if (!shouldLoad) return@launch
            
            try {
                // Use override values if provided, otherwise use current StateFlow values
                val sort = sortOverride ?: sorting.value
                val filtersList = filtersOverride ?: filters.value
                val includeArchived = showArchivedBooks.value
                
                // Load first page from database
                val (books, totalCount) = libraryUseCases.getLibraryCategory.awaitPaginated(
                    categoryId = categoryId,
                    sort = sort,
                    filters = filtersList,
                    limit = PaginationState.INITIAL_PAGE_SIZE,
                    offset = 0,
                    includeArchived = includeArchived
                )
                
                // Track when this category was loaded
                lastCategoryLoadTime[categoryId] = ireader.domain.utils.extensions.currentTimeToLong()
                
                // Update paginated books state
                _paginatedBooks.update { current ->
                    current + (categoryId to books)
                }
                
                // Update total counts
                _paginatedTotalCounts.update { current ->
                    current + (categoryId to totalCount)
                }
                
                // Update pagination state
                _uiState.update { current ->
                    val newPaginationState = current.categoryPaginationState.toMutableMap()
                    newPaginationState[categoryId] = PaginationState(
                        loadedCount = books.size,
                        isLoadingMore = false,
                        hasMoreItems = books.size < totalCount,
                        totalItems = totalCount
                    )
                    current.copy(categoryPaginationState = newPaginationState)
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                // Job was cancelled - this is expected when a new load is triggered
                throw e // Re-throw to properly cancel the coroutine
            } catch (e: Exception) {
                Log.error(e, "$TAG: Error loading books for category $categoryId")
                // On error, fall back to empty state
                _uiState.update { current ->
                    val newPaginationState = current.categoryPaginationState.toMutableMap()
                    newPaginationState[categoryId] = PaginationState(
                        loadedCount = 0,
                        isLoadingMore = false,
                        hasMoreItems = false,
                        totalItems = 0
                    )
                    current.copy(categoryPaginationState = newPaginationState)
                }
            } finally {
                loadingCategoriesMutex.withLock {
                    loadingCategories.remove(categoryId)
                }
            }
        }
    }
    
    /**
     * Load more books for a category from database when user scrolls near the end.
     * This uses true DB pagination - loads PAGE_SIZE more books from the database.
     */
    fun loadMoreBooks(categoryId: Long) {
        val currentPagination = getPaginationState(categoryId)
        
        // Don't load if already loading or no more items
        if (currentPagination.isLoadingMore || !currentPagination.hasMoreItems) return
        
        // Mark as loading
        _uiState.update { current ->
            val newPaginationState = current.categoryPaginationState.toMutableMap()
            newPaginationState[categoryId] = currentPagination.copy(isLoadingMore = true)
            current.copy(categoryPaginationState = newPaginationState)
        }
        
        scope.launch {
            try {
                val sort = sorting.value
                val filtersList = filters.value
                val includeArchived = showArchivedBooks.value
                val currentOffset = currentPagination.loadedCount
                
                // Load next page from database
                val (newBooks, totalCount) = libraryUseCases.getLibraryCategory.awaitPaginated(
                    categoryId = categoryId,
                    sort = sort,
                    filters = filtersList,
                    limit = PaginationState.PAGE_SIZE,
                    offset = currentOffset,
                    includeArchived = includeArchived
                )
                
                // Merge with existing books, ensuring no duplicates
                val existingBooks = _paginatedBooks.value[categoryId] ?: emptyList()
                val existingIds = existingBooks.mapTo(HashSet(existingBooks.size)) { it.id }
                val uniqueNewBooks = newBooks.filter { it.id !in existingIds }
                val mergedBooks = existingBooks + uniqueNewBooks
                
                // Update paginated books state
                _paginatedBooks.update { current ->
                    current + (categoryId to mergedBooks)
                }
                
                // Update total counts
                _paginatedTotalCounts.update { current ->
                    current + (categoryId to totalCount)
                }
                
                // Update pagination state
                val newLoadedCount = mergedBooks.size
                _uiState.update { current ->
                    val newPaginationState = current.categoryPaginationState.toMutableMap()
                    newPaginationState[categoryId] = PaginationState(
                        loadedCount = newLoadedCount,
                        isLoadingMore = false,
                        hasMoreItems = newLoadedCount < totalCount,
                        totalItems = totalCount
                    )
                    current.copy(categoryPaginationState = newPaginationState)
                }
            } catch (e: Exception) {
                // On error, just stop loading
                _uiState.update { current ->
                    val newPaginationState = current.categoryPaginationState.toMutableMap()
                    newPaginationState[categoryId] = currentPagination.copy(isLoadingMore = false)
                    current.copy(categoryPaginationState = newPaginationState)
                }
            }
        }
    }
    
    /**
     * Reset pagination for a category and reload from database.
     * Call this when sort/filter changes or data needs to be refreshed.
     */
    fun resetPagination(categoryId: Long) {
        // Clear cached data for this category
        _paginatedBooks.update { current ->
            current - categoryId
        }
        _paginatedTotalCounts.update { current ->
            current - categoryId
        }
        
        // Reset pagination state
        _uiState.update { current ->
            val newPaginationState = current.categoryPaginationState.toMutableMap()
            newPaginationState[categoryId] = PaginationState(
                loadedCount = 0,
                isLoadingMore = false,
                hasMoreItems = true,
                totalItems = 0
            )
            current.copy(categoryPaginationState = newPaginationState)
        }
        
        // Reload from database with forceRefresh to ensure it loads
        loadInitialBooksForCategory(categoryId, forceRefresh = true)
    }
    
    /**
     * Reset pagination for all categories.
     * Call this when global settings change (sort, filter, archived).
     * 
     * @param newSort Optional new sort to use (if called from toggleSort before StateFlow updates)
     * @param newFilters Optional new filters to use (if called from toggleFilter before StateFlow updates)
     */
    fun resetAllPagination(
        newSort: LibrarySort? = null,
        newFilters: List<LibraryFilter>? = null
    ) {
        scope.launch {
            // Clear loading state
            loadingCategoriesMutex.withLock {
                loadingCategories.clear()
            }
            
            // Cancel all existing load jobs
            paginationLoadJobs.values.forEach { it?.cancel() }
            paginationLoadJobs.clear()
            
            // Clear all cached data
            _paginatedBooks.value = emptyMap()
            _paginatedTotalCounts.value = emptyMap()
            
            // Reset all pagination states
            _uiState.update { current ->
                current.copy(categoryPaginationState = emptyMap())
            }
            
            // Reload current category with forceRefresh to ensure it loads
            val currentCategoryId = categories.getOrNull(selectedCategoryIndex)?.id ?: 0L
            loadInitialBooksForCategoryWithParams(
                categoryId = currentCategoryId,
                forceRefresh = true,
                sortOverride = newSort,
                filtersOverride = newFilters
            )
        }
    }
    
    /**
     * Refresh the current category's books from database.
     * Call this when returning to the library screen to pick up any changes
     * (e.g., last_read_at updated after reading a chapter).
     * 
     * IMPORTANT: This method does NOT clear existing books before loading new ones.
     * This prevents the "disappearing books" issue when navigating quickly.
     * The new books will atomically replace the old ones once loaded.
     */
    fun refreshCurrentCategory() {
        val currentCategoryId = categories.getOrNull(selectedCategoryIndex)?.id ?: 0L
        Log.info { "$TAG: refreshCurrentCategory() called for categoryId=$currentCategoryId" }
        
        // Cancel any existing load job for this category to prevent race conditions
        paginationLoadJobs[currentCategoryId]?.cancel()
        paginationLoadJobs[currentCategoryId] = null
        
        // Launch a coroutine to handle the refresh properly
        scope.launch {
            // Clear loading state so the new load can proceed
            // This must happen BEFORE we check shouldLoad in the load method
            loadingCategoriesMutex.withLock {
                loadingCategories.remove(currentCategoryId)
            }
            
            // DON'T clear the books here - the new books will replace them atomically
            // This prevents the "disappearing books" issue when navigating quickly
            
            // Now load the books directly in this coroutine (not via loadInitialBooksForCategoryWithParams
            // which would launch another coroutine and potentially race with the loading state)
            try {
                val sort = sorting.value
                val filtersList = filters.value
                val includeArchived = showArchivedBooks.value
                
                Log.info { "$TAG: refreshCurrentCategory() loading books with sort=${sort.type}/${if (sort.isAscending) "asc" else "desc"}" }
                
                // Load first page from database
                val (books, totalCount) = libraryUseCases.getLibraryCategory.awaitPaginated(
                    categoryId = currentCategoryId,
                    sort = sort,
                    filters = filtersList,
                    limit = PaginationState.INITIAL_PAGE_SIZE,
                    offset = 0,
                    includeArchived = includeArchived
                )
                
                Log.info { "$TAG: refreshCurrentCategory() loaded ${books.size} books (total=$totalCount)" }
                
                // Track when this category was loaded
                lastCategoryLoadTime[currentCategoryId] = ireader.domain.utils.extensions.currentTimeToLong()
                
                // Update paginated books state - this atomically replaces the old books
                _paginatedBooks.update { current ->
                    current + (currentCategoryId to books)
                }
                
                // Update total counts
                _paginatedTotalCounts.update { current ->
                    current + (currentCategoryId to totalCount)
                }
                
                // Update pagination state
                _uiState.update { current ->
                    val newPaginationState = current.categoryPaginationState.toMutableMap()
                    newPaginationState[currentCategoryId] = PaginationState(
                        loadedCount = books.size,
                        isLoadingMore = false,
                        hasMoreItems = books.size < totalCount,
                        totalItems = totalCount
                    )
                    current.copy(categoryPaginationState = newPaginationState)
                }
                
                Log.info { "$TAG: refreshCurrentCategory() completed successfully" }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.error(e, "$TAG: Error refreshing category $currentCategoryId")
            }
        }
    }
    
    // Track when the current category was last loaded
    private var lastCategoryLoadTime = mutableMapOf<Long, Long>()
    private val STALE_THRESHOLD_MS = 5000L // Consider data stale after 5 seconds
    
    /**
     * Refresh the current category if data is stale (older than STALE_THRESHOLD_MS).
     * This is called when the library screen becomes visible to pick up changes
     * made while in the reader (e.g., last_read_at updates).
     */
    fun refreshCurrentCategoryIfStale() {
        val currentCategoryId = categories.getOrNull(selectedCategoryIndex)?.id ?: 0L
        val lastLoad = lastCategoryLoadTime[currentCategoryId] ?: 0L
        val now = ireader.domain.utils.extensions.currentTimeToLong()
        val timeSinceLastLoad = now - lastLoad
        
        Log.debug { "$TAG: refreshCurrentCategoryIfStale - categoryId=$currentCategoryId, timeSinceLastLoad=${timeSinceLastLoad}ms, threshold=${STALE_THRESHOLD_MS}ms" }
        
        if (timeSinceLastLoad > STALE_THRESHOLD_MS) {
            Log.debug { "$TAG: Data is stale, refreshing category $currentCategoryId" }
            refreshCurrentCategory()
        }
    }
    
    /**
     * Get paginated books for a category.
     * Returns books loaded from database via true DB pagination.
     */
    fun getPaginatedBooksForCategory(categoryId: Long): List<LibraryBook> {
        return _paginatedBooks.value[categoryId] ?: emptyList()
    }
    
    /**
     * Get paginated books for a category as BookItem list.
     */
    fun getPaginatedBookItemsForCategory(categoryId: Long): List<BookItem> {
        return getPaginatedBooksForCategory(categoryId).map { it.toBookItem() }
    }
    
    /**
     * Get total books count for a category from database.
     */
    private fun getTotalBooksForCategory(categoryId: Long): Int {
        return _paginatedTotalCounts.value[categoryId] ?: 0
    }
    
    /**
     * Get paginated books for a category.
     * Returns only the books that should be displayed based on current pagination state.
     * @deprecated Use getPaginatedBooksForCategory instead for true DB pagination
     */
    fun getPaginatedBooksForCategory(
        categoryId: Long,
        allCategoryBooks: List<BookItem>
    ): List<BookItem> {
        val paginationState = getPaginationState(categoryId)
        return allCategoryBooks.take(paginationState.loadedCount)
    }
    
    /**
     * Check if we should load more books based on scroll position.
     * Call this when the user scrolls near the end of the list.
     * 
     * @param categoryId The category ID
     * @param lastVisibleIndex The index of the last visible item
     * @param threshold How many items before the end to trigger loading (default: 10)
     */
    fun checkAndLoadMore(categoryId: Long, lastVisibleIndex: Int, threshold: Int = 10) {
        val paginationState = getPaginationState(categoryId)
        val loadedCount = paginationState.loadedCount
        
        // Load more if we're within threshold of the end
        if (lastVisibleIndex >= loadedCount - threshold && paginationState.canLoadMore) {
            loadMoreBooks(categoryId)
        }
    }
}

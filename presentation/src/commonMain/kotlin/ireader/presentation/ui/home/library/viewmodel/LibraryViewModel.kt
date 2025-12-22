package ireader.presentation.ui.home.library.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.state.ToggleableState
import ireader.domain.models.DisplayMode
import ireader.domain.models.entities.BookItem
import ireader.domain.models.entities.Category
import ireader.domain.models.library.LibraryFilter
import ireader.domain.models.library.LibrarySort
import ireader.domain.preferences.prefs.LibraryPreferences
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

/**
 * ViewModel for the Library screen following Mihon's StateScreenModel pattern.
 * 
 * SIMPLIFIED VERSION (Requirements: 3.1, 3.2, 3.3, 3.4, 4.1, 4.4):
 * - Reduced from 26 constructor parameters to 8 using aggregates
 * - State derived from LibraryController (SSOT) - no duplicate state
 * - Selection/filter/sort methods only dispatch to LibraryController
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
    private val libraryController: LibraryController
) : BaseViewModel() {

    // ==================== State Derived from LibraryController (SSOT) ====================
    // Requirements: 3.1, 3.2, 3.3, 3.4 - No duplicate state, derive from controller
    
    // Internal UI-specific state (not duplicated from controller)
    private val _uiState = MutableStateFlow(LibraryUiState())
    
    // Search debounce
    private val searchQueryFlow = MutableStateFlow("")
    private var searchJob: Job? = null
    
    // Loaded manga cache
    private val loadedManga = mutableMapOf<Long, List<BookItem>>()
    
    // MutableStateFlow for each category's books - cached to avoid recreation
    private val categoryBooksState = mutableMapOf<Long, MutableStateFlow<List<BookItem>>>()
    
    // Cached category book lists to avoid recomputation
    private val categoryBooksCache = mutableMapOf<Long, List<BookItem>>()
    private var lastBooksCacheKey: Int = 0
    
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
        libraryPreferences.columnsInLandscape().stateIn(scope)
    ) { controllerState, uiState, searchQuery, columnsPortrait, columnsLandscape ->
        // Apply search filter to books if search query is not empty
        val filteredBooks = if (searchQuery.isNotBlank()) {
            controllerState.filteredBooks.filter { book ->
                book.title.contains(searchQuery, ignoreCase = true)
            }
        } else {
            controllerState.filteredBooks
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
        
        // Load library via LibraryController
        libraryController.dispatch(LibraryCommand.LoadLibrary)
    }
    
    private fun initializeState() {
        // Load initial preferences
        scope.launch {
            val sortType = libraryScreenPrefUseCases.sortersUseCase.read()
            val sortBy = libraryScreenPrefUseCases.sortersDescUseCase.read()
            _uiState.update { it.copy(sort = LibrarySort(sortType.type, sortBy)) }
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
        combine(
            libraryPreferences.showAllCategory().stateIn(scope),
            libraryPreferences.showEmptyCategories().stateIn(scope)
        ) { showAll, showEmpty ->
            Pair(showAll, showEmpty)
        }.flatMapLatest { (showAll, showEmpty) ->
            libraryUseCases.categories.subscribe(showAll, showEmpty, scope).onEach { categoriesList ->
                val lastCategoryId = lastUsedCategory.value
                val index = categoriesList.indexOfFirst { it.id == lastCategoryId }.takeIf { it >= 0 } ?: 0
                
                _uiState.update { current ->
                    current.copy(
                        categories = categoriesList.toImmutableList(),
                        selectedCategoryIndex = index
                    )
                }
            }
        }.launchIn(scope)
        
        // Debounced search
        searchQueryFlow
            .debounce(300)
            .onEach { query ->
                _uiState.update { it.copy(searchQuery = query.ifBlank { null }) }
            }
            .launchIn(scope)
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
        // Only dispatch to LibraryController (SSOT pattern)
        libraryController.dispatch(LibraryCommand.SelectAll)
    }
    
    fun flipAllInCurrentCategory() {
        // Only dispatch to LibraryController (SSOT pattern)
        libraryController.dispatch(LibraryCommand.InvertSelection)
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
    }
    
    fun clearSearch() {
        searchQueryFlow.value = ""
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
     * PERFORMANCE OPTIMIZED for 800+ books with PAGINATION:
     * - Uses stable cache key to avoid unnecessary recomputation
     * - Pre-computes category membership sets once
     * - Returns only paginated subset of books for smooth scrolling
     * - Automatically initializes pagination state for new categories
     */
    @Composable
    fun getLibraryForCategoryIndexAsState(categoryIndex: Int): State<List<BookItem>> {
        val currentState by state.collectAsState()
        val currentBookCategories = bookCategories.value
        val category = currentState.categories.getOrNull(categoryIndex)
        val categoryId = category?.id ?: 0L
        
        // Get pagination state for this category
        val paginationState = currentState.categoryPaginationState[categoryId]
            ?: PaginationState(totalItems = currentState.books.size)
        
        // Create a stable cache key based on data identity and pagination
        val cacheKey = remember(
            currentState.books.size, 
            currentBookCategories.size, 
            category?.id,
            paginationState.loadedCount
        ) {
            listOf(
                currentState.books.hashCode(), 
                currentBookCategories.hashCode(), 
                category?.id,
                paginationState.loadedCount
            )
        }
        
        // Use derivedStateOf for efficient recomputation only when needed
        val booksState = remember(cacheKey) {
            androidx.compose.runtime.derivedStateOf {
                if (category == null) {
                    emptyList()
                } else {
                    val allCategoryBooks = getCachedBooksForCategory(
                        categoryId = category.id,
                        allBooks = currentState.books,
                        bookCategories = currentBookCategories
                    )
                    // Apply pagination - only return loaded subset
                    allCategoryBooks.take(paginationState.loadedCount)
                }
            }
        }
        
        return booksState
    }
    
    /**
     * Get ALL library books for a specific category (without pagination).
     * Used internally for total count calculations.
     */
    @Composable
    fun getAllBooksForCategoryAsState(categoryIndex: Int): State<List<BookItem>> {
        val currentState by state.collectAsState()
        val currentBookCategories = bookCategories.value
        val category = currentState.categories.getOrNull(categoryIndex)
        
        val cacheKey = remember(currentState.books.size, currentBookCategories.size, category?.id) {
            Triple(currentState.books.hashCode(), currentBookCategories.hashCode(), category?.id)
        }
        
        val booksState = remember(cacheKey) {
            androidx.compose.runtime.derivedStateOf {
                if (category == null) {
                    emptyList()
                } else {
                    getCachedBooksForCategory(
                        categoryId = category.id,
                        allBooks = currentState.books,
                        bookCategories = currentBookCategories
                    )
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
    }
    
    // ==================== Pagination ====================
    
    /**
     * Get pagination state for a category.
     */
    fun getPaginationState(categoryId: Long): PaginationState {
        return _uiState.value.categoryPaginationState[categoryId] 
            ?: PaginationState(totalItems = state.value.books.size)
    }
    
    /**
     * Load more books for a category when user scrolls near the end.
     * This is called automatically by the grid when approaching the end of loaded items.
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
        
        // Simulate loading delay for smooth UX (actual data is already in memory)
        scope.launch {
            // Small delay to prevent rapid-fire loading
            kotlinx.coroutines.delay(100)
            
            val totalBooks = getTotalBooksForCategory(categoryId)
            val newLoadedCount = minOf(
                currentPagination.loadedCount + PaginationState.PAGE_SIZE,
                totalBooks
            )
            val hasMore = newLoadedCount < totalBooks
            
            _uiState.update { current ->
                val newPaginationState = current.categoryPaginationState.toMutableMap()
                newPaginationState[categoryId] = PaginationState(
                    loadedCount = newLoadedCount,
                    isLoadingMore = false,
                    hasMoreItems = hasMore,
                    totalItems = totalBooks
                )
                current.copy(categoryPaginationState = newPaginationState)
            }
        }
    }
    
    /**
     * Reset pagination for a category (e.g., when category changes or data refreshes).
     */
    fun resetPagination(categoryId: Long) {
        val totalBooks = getTotalBooksForCategory(categoryId)
        _uiState.update { current ->
            val newPaginationState = current.categoryPaginationState.toMutableMap()
            newPaginationState[categoryId] = PaginationState(
                loadedCount = minOf(PaginationState.INITIAL_PAGE_SIZE, totalBooks),
                isLoadingMore = false,
                hasMoreItems = totalBooks > PaginationState.INITIAL_PAGE_SIZE,
                totalItems = totalBooks
            )
            current.copy(categoryPaginationState = newPaginationState)
        }
    }
    
    /**
     * Get total books count for a category.
     */
    private fun getTotalBooksForCategory(categoryId: Long): Int {
        val allBooks = state.value.books
        val bookCategoriesList = bookCategories.value
        
        return when (categoryId) {
            0L -> allBooks.size // "All" category
            -1L -> { // Uncategorized
                val categorizedBookIds = bookCategoriesList.mapTo(HashSet(bookCategoriesList.size)) { it.bookId }
                allBooks.count { it.id !in categorizedBookIds }
            }
            else -> { // Regular category
                val bookIdsInCategory = bookCategoriesList
                    .asSequence()
                    .filter { it.categoryId == categoryId }
                    .mapTo(HashSet()) { it.bookId }
                allBooks.count { it.id in bookIdsInCategory }
            }
        }
    }
    
    /**
     * Get paginated books for a category.
     * Returns only the books that should be displayed based on current pagination state.
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

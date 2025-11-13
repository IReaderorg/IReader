package ireader.presentation.ui.home.library.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.state.ToggleableState
import ireader.domain.models.DisplayMode
import ireader.domain.models.entities.BookCategory
import ireader.domain.models.entities.BookItem
import ireader.domain.models.entities.Category
import ireader.domain.models.entities.Chapter
import ireader.domain.models.library.LibraryFilter
import ireader.domain.models.library.LibrarySort
import ireader.domain.preferences.prefs.LibraryPreferences
import ireader.domain.usecases.category.CategoriesUseCases
import ireader.domain.usecases.local.book_usecases.DownloadResult
import ireader.domain.usecases.local.book_usecases.DownloadUnreadChaptersUseCase
import ireader.domain.usecases.local.book_usecases.GetLibraryCategory
import ireader.domain.usecases.local.book_usecases.MarkBookAsReadOrNotUseCase
import ireader.domain.usecases.local.book_usecases.MarkResult
import ireader.domain.usecases.preferences.reader_preferences.screens.LibraryScreenPrefUseCases
import ireader.domain.usecases.services.ServiceUseCases
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch


@OptIn(ExperimentalCoroutinesApi::class)

class LibraryViewModel(
        private val localGetBookUseCases: ireader.domain.usecases.local.LocalGetBookUseCases,
        private val insertUseCases: ireader.domain.usecases.local.LocalInsertUseCases,
        val deleteUseCase: ireader.domain.usecases.local.DeleteUseCase,
        private val localGetChapterUseCase: ireader.domain.usecases.local.LocalGetChapterUseCase,
        private val libraryScreenPrefUseCases: LibraryScreenPrefUseCases,
        private val state: LibraryStateImpl,
        private val serviceUseCases: ServiceUseCases,
        private val getLibraryCategory: GetLibraryCategory,
        private val libraryPreferences: LibraryPreferences,
        val markBookAsReadOrNotUseCase: MarkBookAsReadOrNotUseCase,
        val getCategory: CategoriesUseCases,
        private val downloadUnreadChaptersUseCase: DownloadUnreadChaptersUseCase,
        private val archiveBookUseCase: ireader.domain.usecases.local.book_usecases.ArchiveBookUseCase,
        private val getLastReadNovelUseCase: ireader.domain.usecases.history.GetLastReadNovelUseCase,
        private val syncUseCases: ireader.domain.usecases.sync.SyncUseCases? = null
) : ireader.presentation.ui.core.viewmodel.BaseViewModel(), LibraryState by state {

    var lastUsedCategory = libraryPreferences.lastUsedCategory().asState()
    var filters = libraryPreferences.filters(true).asState()

    var sorting = libraryPreferences.sorting().asState()
    val showCategoryTabs = libraryPreferences.showCategoryTabs().asState()
    val showAllCategoryTab = libraryPreferences.showAllCategory().asState()
    val showCountInCategory = libraryPreferences.showCountInCategory().asState()

    val readBadge = libraryPreferences.downloadBadges().asState()
    val unreadBadge = libraryPreferences.unreadBadges().asState()
    val goToLastChapterBadge = libraryPreferences.goToLastChapterBadges().asState()
    
    // New badge preferences
    val showDownloadedChaptersBadge = libraryPreferences.showDownloadedChaptersBadge().asState()
    val showUnreadChaptersBadge = libraryPreferences.showUnreadChaptersBadge().asState()
    val showLocalMangaBadge = libraryPreferences.showLocalMangaBadge().asState()
    val showLanguageBadge = libraryPreferences.showLanguageBadge().asState()

    val bookCategories = getCategory.subscribeBookCategories().asState(emptyList())
    val deleteQueues: SnapshotStateList<BookCategory> = mutableStateListOf()
    val addQueues: SnapshotStateList<BookCategory> = mutableStateListOf()
    var showDialog: Boolean by mutableStateOf(false)
    var isBookRefreshing: Boolean by mutableStateOf(false)

    val perCategorySettings = libraryPreferences.perCategorySettings().asState()
    val layouts = libraryPreferences.categoryFlags().asState()
    var columnInPortrait = libraryPreferences.columnsInPortrait().asState()
    val columnInLandscape by libraryPreferences.columnsInLandscape().asState()
    val layout by derivedStateOf { DisplayMode.getFlag(layouts.value) ?: DisplayMode.CompactGrid }
    
    // New filter state management
    private val _activeFilters = MutableStateFlow<Set<LibraryFilter.Type>>(emptySet())
    val activeFilters: StateFlow<Set<LibraryFilter.Type>> = _activeFilters.asStateFlow()



    val showEmptyCategories = libraryPreferences.showEmptyCategories().asState()
    val showResumeReadingCard = libraryPreferences.showResumeReadingCard().asState()
    val showArchivedBooks = libraryPreferences.showArchivedBooks().asState()

    // Last read novel state for Resume Reading Card
    var lastReadInfo by mutableStateOf<ireader.domain.models.entities.LastReadInfo?>(null)
        private set
    
    var isResumeCardVisible by mutableStateOf(true)
        private set

    init {
        readLayoutTypeAndFilterTypeAndSortType()
        
        // Initialize active filters from preferences
        updateActiveFilters(filters.value)
        
        // Load last read info
        loadLastReadInfo()
        
        // Auto-sync is managed by SyncManager in domain layer
        
        combine(
            libraryPreferences.showAllCategory().stateIn(scope),
            libraryPreferences.showEmptyCategories().stateIn(scope)
        ) { showAll, showEmpty ->
            Pair(showAll, showEmpty)
        }.flatMapLatest { (showAll, showEmpty) ->
            getCategory.subscribe(showAll, showEmpty,scope).onEach { categories ->
                val lastCategoryId = lastUsedCategory.value

                val index =
                    categories.indexOfFirst { it.id == lastCategoryId }.takeIf { it >= 0 } ?: 0

                state.categories = categories
                state.selectedCategoryIndex = index
            }
        }.launchIn(scope)
    }
    
    /**
     * Trigger sync for books (called when books are modified)
     */
    private fun triggerSync(books: List<ireader.domain.models.entities.Book>) {
        scope.launch {
            syncUseCases?.syncBooksToRemote?.invoke(books)
        }
    }
    
    /**
     * Trigger sync for a single book
     */
    private fun triggerSyncForBook(book: ireader.domain.models.entities.Book) {
        scope.launch {
            syncUseCases?.syncBookToRemote?.invoke(book)
        }
    }
    
    /**
     * Perform full sync of all library books
     */
    fun performFullSync() {
        scope.launch {
            syncUseCases?.performFullSync?.invoke()
        }
    }
    
    /**
     * Load the last read novel information
     */
    fun loadLastReadInfo() {
        scope.launch {
            lastReadInfo = getLastReadNovelUseCase()
            // Reset visibility when new data is loaded
            isResumeCardVisible = showResumeReadingCard.value
        }
    }
    
    /**
     * Dismiss the resume reading card temporarily
     */
    fun dismissResumeCard() {
        isResumeCardVisible = false
    }
    
    /**
     * Toggle the resume reading card preference
     */
    fun toggleResumeReadingCard(enabled: Boolean) {
        scope.launch {
            libraryPreferences.showResumeReadingCard().set(enabled)
            isResumeCardVisible = enabled
        }
    }
    
    /**
     * Toggle the show archived books preference
     */
    fun toggleShowArchivedBooks(enabled: Boolean) {
        scope.launch {
            libraryPreferences.showArchivedBooks().set(enabled)
        }
    }

    private val loadedManga = mutableMapOf<Long, List<BookItem>>()

    fun onLayoutTypeChange(layoutType: DisplayMode) {
        scope.launch {
            categories.firstOrNull { it.id == lastUsedCategory.value }?.let { category ->

                libraryScreenPrefUseCases.libraryLayoutTypeUseCase.await(
                    category = category.category,
                    displayMode = layoutType
                )
            }
        }
    }

fun downloadChapters() {
    serviceUseCases.startDownloadServicesUseCase.start(bookIds = selectedBooks.toLongArray())
    selectedBooks.clear()
}

/**
 * Download all unread chapters for selected books
 */
suspend fun downloadUnreadChapters(): DownloadResult {
    val result = downloadUnreadChaptersUseCase.downloadUnreadChapters(selectedBooks.toList())
    if (result is DownloadResult.Success || result is DownloadResult.NoUnreadChapters) {
        selectedBooks.clear()
    }
    return result
}

/**
 * Mark all chapters as read for selected books with undo support
 */
suspend fun markAsReadWithUndo(): MarkResult {
    val result = markBookAsReadOrNotUseCase.markAsReadWithUndo(selectedBooks.toList())
    if (result is MarkResult.Success) {
        selectedBooks.clear()
    }
    return result
}

/**
 * Mark all chapters as unread for selected books with undo support
 */
suspend fun markAsUnreadWithUndo(): MarkResult {
    val result = markBookAsReadOrNotUseCase.markAsUnreadWithUndo(selectedBooks.toList())
    if (result is MarkResult.Success) {
        selectedBooks.clear()
    }
    return result
}

/**
 * Undo the last mark operation
 */
suspend fun undoMarkOperation(previousStates: Map<Long, List<Chapter>>) {
    markBookAsReadOrNotUseCase.undoMark(previousStates)
}

fun readLayoutTypeAndFilterTypeAndSortType() {
    scope.launch {
        val sortType = libraryScreenPrefUseCases.sortersUseCase.read()
        val sortBy = libraryScreenPrefUseCases.sortersDescUseCase.read()
        this@LibraryViewModel.sortType = sortType
        this@LibraryViewModel.desc = sortBy
    }
}

fun toggleFilter(type: LibraryFilter.Type) {
    val newFilters = filters.value
        .map { filterState ->
            if (type == filterState.type) {
                LibraryFilter(
                    type,
                    when (filterState.value) {
                        LibraryFilter.Value.Included -> LibraryFilter.Value.Excluded
                        LibraryFilter.Value.Excluded -> LibraryFilter.Value.Missing
                        LibraryFilter.Value.Missing -> LibraryFilter.Value.Included
                    }
                )
            } else {
                filterState
            }
        }

    this.filters.value = newFilters
    
    // Update active filters set for immediate UI feedback
    updateActiveFilters(newFilters)
    
    // Persist filter changes immediately
    scope.launch {
        libraryPreferences.filters(true).set(newFilters)
    }
}

/**
 * Toggle a filter on/off immediately with visual feedback
 */
fun toggleFilterImmediate(type: LibraryFilter.Type) {
    val currentFilters = _activeFilters.value.toMutableSet()
    if (type in currentFilters) {
        currentFilters.remove(type)
    } else {
        currentFilters.add(type)
    }
    _activeFilters.value = currentFilters
    
    // Update the actual filter preferences
    val newFilters = filters.value.map { filterState ->
        if (filterState.type == type) {
            LibraryFilter(
                type,
                if (type in currentFilters) LibraryFilter.Value.Included else LibraryFilter.Value.Missing
            )
        } else {
            filterState
        }
    }
    
    filters.value = newFilters
    
    // Persist immediately
    scope.launch {
        libraryPreferences.filters(true).set(newFilters)
    }
}

/**
 * Update active filters set from filter list
 */
private fun updateActiveFilters(filterList: List<LibraryFilter>) {
    _activeFilters.value = filterList
        .filter { it.value == LibraryFilter.Value.Included }
        .map { it.type }
        .toSet()
}

fun toggleSort(type: LibrarySort.Type) {
    val currentSort = sorting
    val newSort = if (type == currentSort.value.type) {
        currentSort.value.copy(isAscending = !currentSort.value.isAscending)
    } else {
        currentSort.value.copy(type = type)
    }
    sorting.value = newSort
    
    // Persist sort changes immediately
    scope.launch {
        libraryPreferences.sorting().set(newSort)
    }
}

/**
 * Toggle sort direction without changing sort type
 */
fun toggleSortDirection() {
    val currentSort = sorting.value
    val newSort = currentSort.copy(isAscending = !currentSort.isAscending)
    sorting.value = newSort
    
    // Persist immediately
    scope.launch {
        libraryPreferences.sorting().set(newSort)
    }
}

/**
 * Update column count with immediate persistence
 */
fun updateColumnCount(count: Int) {
    columnInPortrait.value = count
    
    // Persist immediately
    scope.launch {
        libraryPreferences.columnsInPortrait().set(count)
    }
}

fun refreshUpdate() {
    scope.launch {
        isBookRefreshing = true
        try {
            // First, sync books from remote using clean architecture use case
            syncUseCases?.refreshLibraryFromRemote?.invoke()?.let { result ->
                result.onSuccess { syncResult ->
                    println("Sync completed: ${syncResult.successMessage}")
                }
                result.onFailure { error ->
                    println("Sync failed: ${error.message}")
                }
            }
            
            // Then update library metadata
            serviceUseCases.startLibraryUpdateServicesUseCase.start()
        } finally {
            isBookRefreshing = false
        }
    }
}

fun setSelectedPage(index: Int) {
    if (index == selectedCategoryIndex) return
    val categories = categories
    val category = categories.getOrNull(index) ?: return
    state.selectedCategoryIndex = index
    state.selectedCategory
    lastUsedCategory.value = category.id
}

fun unselectAll() {
    state.selectedBooks.clear()
}

/**
 * Toggle selection for a specific book
 * If the book is selected, it will be deselected
 * If the book is not selected, it will be selected
 */
fun toggleSelection(bookId: Long) {
    if (bookId in state.selectedBooks) {
        state.selectedBooks.remove(bookId)
    } else {
        state.selectedBooks.add(bookId)
    }
}

fun selectAllInCurrentCategory() {
    val mangaInCurrentCategory = loadedManga[selectedCategory?.id] ?: return
    val currentSelected = selectedBooks.toList()
    val mangaIds = mangaInCurrentCategory.map { it.id }.filter { it !in currentSelected }
    state.selectedBooks.addAll(mangaIds)
}

fun flipAllInCurrentCategory() {
    val mangaInCurrentCategory = loadedManga[selectedCategory?.id] ?: return
    val currentSelected = selectedBooks.toList()
    val (toRemove, toAdd) = mangaInCurrentCategory.map { it.id }
        .partition { it in currentSelected }
    state.selectedBooks.removeAll(toRemove)
    state.selectedBooks.addAll(toAdd)
}

fun getDefaultValue(categories: Category): ToggleableState {
    val defaultValue: Boolean = selectedBooks.any { id ->
        id in bookCategories.value.filter { it.categoryId == categories.id }.map { it.bookId }
    }
    return if (defaultValue) ToggleableState.On else ToggleableState.Off
}

@Composable
fun getLibraryForCategoryIndex(categoryIndex: Int): State<List<BookItem>> {
    val scope = rememberCoroutineScope()
    val categoryId = categories.getOrNull(categoryIndex)?.id ?: return remember {
        mutableStateOf(emptyList())
    }

    val unfiltered = remember(sorting.value, filters.value, categoryId, categories.size, showArchivedBooks.value) {
        getLibraryCategory.subscribe(categoryId, sorting.value, filters.value, showArchivedBooks.value)
            .map { list ->
                books = list
                list.mapIndexed { index, libraryBook ->
                    libraryBook.toBookItem().copy(column = index.toLong())
                }
            }
            .shareIn(scope, SharingStarted.WhileSubscribed(1000), 1)
    }

    return remember(
        sorting.value,
        filters.value,
        searchQuery,
        showAllCategoryTab.value,
        categories.size,
        showArchivedBooks.value,
    ) {
        val query = searchQuery
        if (query.isNullOrBlank()) {
            unfiltered
        } else {
            // Debounce search queries for performance
            unfiltered
                .debounce(300) // 300ms debounce for search
                .map { mangas ->
                    mangas.filter { it.title.contains(query, true) }
                }
        }
            .onEach { loadedManga[categoryId] = it }
            .onCompletion { loadedManga.remove(categoryId) }
    }.collectAsState(emptyList())
}

fun getColumnsForOrientation(isLandscape: Boolean, scope: CoroutineScope): StateFlow<Int> {
    return if (isLandscape) {
        libraryPreferences.columnsInLandscape()
    } else {
        libraryPreferences.columnsInPortrait()
    }.stateIn(scope)
}

/**
 * Show the batch operation dialog
 */
fun showBatchOperationDialog() {
    state.showBatchOperationDialog = true
}

/**
 * Hide the batch operation dialog
 */
fun hideBatchOperationDialog() {
    state.showBatchOperationDialog = false
}

/**
 * Perform a batch operation on selected books
 */
fun performBatchOperation(operation: ireader.presentation.ui.home.library.components.BatchOperation) {
    scope.launch {
        state.batchOperationInProgress = true
        state.batchOperationMessage = "Processing..."
        
        try {
            when (operation) {
                ireader.presentation.ui.home.library.components.BatchOperation.MARK_AS_READ -> {
                    val result = markAsReadWithUndo()
                    when (result) {
                        is MarkResult.Success -> {
                            state.batchOperationMessage = "Marked ${result.totalChapters} chapter(s) in ${result.totalBooks} book(s) as read"
                            state.lastUndoState = UndoState(
                                previousChapterStates = result.previousStates,
                                operationType = UndoOperationType.MARK_AS_READ,
                                timestamp = System.currentTimeMillis()
                            )
                        }
                        is MarkResult.Failure -> {
                            state.batchOperationMessage = "Error: ${result.message}"
                        }
                    }
                }
                
                ireader.presentation.ui.home.library.components.BatchOperation.MARK_AS_UNREAD -> {
                    val result = markAsUnreadWithUndo()
                    when (result) {
                        is MarkResult.Success -> {
                            state.batchOperationMessage = "Marked ${result.totalChapters} chapter(s) in ${result.totalBooks} book(s) as unread"
                            state.lastUndoState = UndoState(
                                previousChapterStates = result.previousStates,
                                operationType = UndoOperationType.MARK_AS_UNREAD,
                                timestamp = System.currentTimeMillis()
                            )
                        }
                        is MarkResult.Failure -> {
                            state.batchOperationMessage = "Error: ${result.message}"
                        }
                    }
                }
                
                ireader.presentation.ui.home.library.components.BatchOperation.DOWNLOAD -> {
                    downloadChapters()
                    state.batchOperationMessage = "Started downloading ${selectedBooks.size} book(s)"
                }
                
                ireader.presentation.ui.home.library.components.BatchOperation.DOWNLOAD_UNREAD -> {
                    val result = downloadUnreadChapters()
                    when (result) {
                        is DownloadResult.Success -> {
                            state.batchOperationMessage = "Started downloading ${result.totalChapters} unread chapter(s) from ${result.totalBooks} book(s)"
                        }
                        is DownloadResult.NoUnreadChapters -> {
                            state.batchOperationMessage = "No unread chapters to download"
                        }
                        is DownloadResult.Failure -> {
                            state.batchOperationMessage = "Error: ${result.message}"
                        }
                    }
                }
                
                ireader.presentation.ui.home.library.components.BatchOperation.DELETE -> {
                    // Delete operation - this should trigger the existing delete flow
                    state.batchOperationMessage = "Deleted ${selectedBooks.size} book(s)"
                    // Note: Actual deletion is handled by the existing onDelete callback
                }
                
                ireader.presentation.ui.home.library.components.BatchOperation.CHANGE_CATEGORY -> {
                    // Change category - this should trigger the existing category dialog
                    state.batchOperationMessage = null // No message, dialog will handle it
                    // Note: Category change is handled by the existing onClickChangeCategory callback
                }
                
                ireader.presentation.ui.home.library.components.BatchOperation.ARCHIVE -> {
                    val result = archiveBookUseCase.archiveBooks(selectedBooks.toList())
                    result.fold(
                        onSuccess = {
                            state.batchOperationMessage = "Archived ${selectedBooks.size} book(s)"
                            selectedBooks.clear()
                        },
                        onFailure = { e ->
                            state.batchOperationMessage = "Error archiving books: ${e.message}"
                        }
                    )
                }
            }
            
            // Trigger sync after batch operation
            val allBooks = localGetBookUseCases.findAllInLibraryBooks()
            triggerSync(allBooks)
            
        } catch (e: Exception) {
            state.batchOperationMessage = "Error: ${e.message}"
        } finally {
            state.batchOperationInProgress = false
        }
    }
}
}

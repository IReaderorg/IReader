package ireader.domain.services.bookdetail

import ireader.core.log.Log
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.usecases.history.HistoryUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * BookDetail Controller - The central coordinator for all book detail screen operations.
 * 
 * This is the SINGLE SOURCE OF TRUTH for book detail-related state including:
 * - Book and chapter data
 * - Chapter selection state
 * - Filter and sort state
 * - Loading and error states
 * 
 * Responsibilities:
 * - Owns and manages the BookDetailState (single source of truth)
 * - Processes BookDetailCommands and updates state accordingly
 * - Coordinates between repositories for data operations
 * - Emits BookDetailEvents for one-time occurrences
 * 
 * Requirements: 3.1, 3.3, 3.4, 3.5, 4.1, 4.2, 4.3, 4.4, 4.5, 5.1
 */
class BookDetailController(
    private val bookRepository: BookRepository,
    private val chapterRepository: ChapterRepository,
    private val historyUseCase: HistoryUseCase
) {
    companion object {
        private const val TAG = "BookDetailController"
    }
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // Mutex to ensure commands are processed sequentially
    private val commandMutex = Mutex()
    
    // State - single source of truth
    // Requirements: 3.5
    private val _state = MutableStateFlow(BookDetailState())
    val state: StateFlow<BookDetailState> = _state.asStateFlow()
    
    // Events - one-time occurrences
    // Requirements: 3.4
    private val _events = MutableSharedFlow<BookDetailEvent>()
    val events: SharedFlow<BookDetailEvent> = _events.asSharedFlow()
    
    // Active subscriptions
    private var bookSubscriptionJob: Job? = null
    private var chaptersSubscriptionJob: Job? = null
    private var historySubscriptionJob: Job? = null
    private var combinedSubscriptionJob: Job? = null
    
    // Current book ID for tracking
    private var currentBookId: Long? = null


    /**
     * Process a command - ALL interactions go through here.
     * Commands are processed sequentially using a mutex to prevent race conditions.
     * Requirements: 3.3
     */
    fun dispatch(command: BookDetailCommand) {
        Log.debug { "$TAG: dispatch($command)" }
        
        scope.launch {
            commandMutex.withLock {
                try {
                    processCommand(command)
                } catch (e: Exception) {
                    Log.error(e, "$TAG: Error processing command")
                    handleError(BookDetailError.DatabaseError(e.message ?: "Unknown error"))
                }
            }
        }
    }
    
    private suspend fun processCommand(command: BookDetailCommand) {
        when (command) {
            // Lifecycle
            is BookDetailCommand.LoadBook -> loadBook(command.bookId)
            is BookDetailCommand.Cleanup -> cleanup()
            
            // Selection
            is BookDetailCommand.SelectChapter -> selectChapter(command.chapterId)
            is BookDetailCommand.DeselectChapter -> deselectChapter(command.chapterId)
            is BookDetailCommand.ToggleChapterSelection -> toggleChapterSelection(command.chapterId)
            is BookDetailCommand.ClearSelection -> clearSelection()
            is BookDetailCommand.SelectAll -> selectAll(command.onlyFiltered)
            is BookDetailCommand.SelectRange -> selectRange(command.fromChapterId, command.toChapterId)
            
            // Filter
            is BookDetailCommand.SetFilter -> setFilter(command.filter)
            is BookDetailCommand.SetSearchQuery -> setSearchQuery(command.query)
            
            // Sort
            is BookDetailCommand.SetSort -> setSort(command.sort)
            
            // Refresh
            is BookDetailCommand.RefreshChapters -> refreshChapters()
            is BookDetailCommand.RefreshBook -> refreshBook()
            
            // Navigation
            is BookDetailCommand.NavigateToReader -> navigateToReader(command.chapterId)
            is BookDetailCommand.ContinueReading -> continueReading()
            
            // Error
            is BookDetailCommand.ClearError -> clearError()
        }
    }
    
    // ========== Lifecycle Commands ==========
    
    /**
     * Load a book and subscribe to reactive updates.
     * Requirements: 3.1
     */
    private suspend fun loadBook(bookId: Long) {
        Log.debug { "$TAG: loadBook(bookId=$bookId)" }
        
        // Cancel existing subscriptions
        cancelSubscriptions()
        currentBookId = bookId
        
        _state.update { it.copy(isLoading = true, error = null) }
        
        try {
            // Load book
            val book = bookRepository.findBookById(bookId)
            if (book == null) {
                handleError(BookDetailError.NotFound(bookId))
                return
            }
            
            // Load chapters
            val chapters = chapterRepository.findChaptersByBookId(bookId)
            
            // Load history for last read chapter
            val history = historyUseCase.findHistoryByBookId(bookId)
            
            // Apply initial filtering and sorting
            val filteredChapters = applyFilterAndSort(chapters, _state.value.filter, _state.value.sort, _state.value.searchQuery)
            
            _state.update { 
                it.copy(
                    book = book,
                    chapters = chapters,
                    filteredChapters = filteredChapters,
                    lastReadChapterId = history?.chapterId,
                    isLoading = false
                )
            }
            
            _events.emit(BookDetailEvent.BookLoaded(book))
            _events.emit(BookDetailEvent.ChaptersLoaded(chapters.size))
            
            // Subscribe to reactive updates
            subscribeToUpdates(bookId)
            
        } catch (e: Exception) {
            Log.error(e, "$TAG: Failed to load book")
            handleError(BookDetailError.LoadFailed(e.message ?: "Failed to load book"))
        }
    }
    
    /**
     * Subscribe to reactive updates for book, chapters, and history.
     */
    private fun subscribeToUpdates(bookId: Long) {
        // Cancel any existing subscription
        combinedSubscriptionJob?.cancel()
        
        combinedSubscriptionJob = scope.launch {
            combine(
                bookRepository.subscribeBookById(bookId),
                chapterRepository.subscribeChaptersByBookId(bookId),
                historyUseCase.subscribeHistoryByBookId(bookId)
            ) { book, chapters, history ->
                Triple(book, chapters, history)
            }.collect { (book, chapters, history) ->
                if (book != null) {
                    val currentState = _state.value
                    val filteredChapters = applyFilterAndSort(
                        chapters, 
                        currentState.filter, 
                        currentState.sort, 
                        currentState.searchQuery
                    )
                    
                    _state.update { state ->
                        state.copy(
                            book = book,
                            chapters = chapters,
                            filteredChapters = filteredChapters,
                            lastReadChapterId = history?.chapterId,
                            // Preserve selection - remove any selected IDs that no longer exist
                            selectedChapterIds = state.selectedChapterIds.filter { id ->
                                chapters.any { it.id == id }
                            }.toSet()
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Clean up resources, cancel subscriptions, and reset state.
     */
    private fun cleanup() {
        Log.debug { "$TAG: cleanup()" }
        
        cancelSubscriptions()
        currentBookId = null
        _state.update { BookDetailState() }
    }
    
    private fun cancelSubscriptions() {
        bookSubscriptionJob?.cancel()
        chaptersSubscriptionJob?.cancel()
        historySubscriptionJob?.cancel()
        combinedSubscriptionJob?.cancel()
        bookSubscriptionJob = null
        chaptersSubscriptionJob = null
        historySubscriptionJob = null
        combinedSubscriptionJob = null
    }
    
    // ========== Selection Commands ==========
    
    /**
     * Select a chapter by ID.
     */
    private suspend fun selectChapter(chapterId: Long) {
        Log.debug { "$TAG: selectChapter(chapterId=$chapterId)" }
        
        _state.update { state ->
            state.copy(selectedChapterIds = state.selectedChapterIds + chapterId)
        }
        
        _events.emit(BookDetailEvent.SelectionChanged(_state.value.selectionCount))
    }
    
    /**
     * Deselect a chapter by ID.
     */
    private suspend fun deselectChapter(chapterId: Long) {
        Log.debug { "$TAG: deselectChapter(chapterId=$chapterId)" }
        
        _state.update { state ->
            state.copy(selectedChapterIds = state.selectedChapterIds - chapterId)
        }
        
        _events.emit(BookDetailEvent.SelectionChanged(_state.value.selectionCount))
    }
    
    /**
     * Toggle selection state of a chapter.
     */
    private suspend fun toggleChapterSelection(chapterId: Long) {
        Log.debug { "$TAG: toggleChapterSelection(chapterId=$chapterId)" }
        
        val currentSelection = _state.value.selectedChapterIds
        val newSelection = if (chapterId in currentSelection) {
            currentSelection - chapterId
        } else {
            currentSelection + chapterId
        }
        
        _state.update { state ->
            state.copy(selectedChapterIds = newSelection)
        }
        
        _events.emit(BookDetailEvent.SelectionChanged(_state.value.selectionCount))
    }
    
    /**
     * Clear all chapter selections.
     */
    private suspend fun clearSelection() {
        Log.debug { "$TAG: clearSelection()" }
        
        _state.update { state ->
            state.copy(selectedChapterIds = emptySet())
        }
        
        _events.emit(BookDetailEvent.SelectionChanged(0))
    }
    
    /**
     * Select all chapters (optionally only filtered ones).
     */
    private suspend fun selectAll(onlyFiltered: Boolean) {
        Log.debug { "$TAG: selectAll(onlyFiltered=$onlyFiltered)" }
        
        val chaptersToSelect = if (onlyFiltered) {
            _state.value.filteredChapters
        } else {
            _state.value.chapters
        }
        
        _state.update { state ->
            state.copy(selectedChapterIds = chaptersToSelect.map { it.id }.toSet())
        }
        
        _events.emit(BookDetailEvent.SelectionChanged(_state.value.selectionCount))
    }
    
    /**
     * Select chapters in a range.
     */
    private suspend fun selectRange(fromChapterId: Long, toChapterId: Long) {
        Log.debug { "$TAG: selectRange(from=$fromChapterId, to=$toChapterId)" }
        
        val chapters = _state.value.filteredChapters
        val fromIndex = chapters.indexOfFirst { it.id == fromChapterId }
        val toIndex = chapters.indexOfFirst { it.id == toChapterId }
        
        if (fromIndex == -1 || toIndex == -1) return
        
        val startIndex = minOf(fromIndex, toIndex)
        val endIndex = maxOf(fromIndex, toIndex)
        
        val rangeIds = chapters.subList(startIndex, endIndex + 1).map { it.id }.toSet()
        
        _state.update { state ->
            state.copy(selectedChapterIds = state.selectedChapterIds + rangeIds)
        }
        
        _events.emit(BookDetailEvent.SelectionChanged(_state.value.selectionCount))
    }


    // ========== Filter Commands ==========
    
    /**
     * Set the chapter filter.
     */
    private fun setFilter(filter: ChapterFilter) {
        Log.debug { "$TAG: setFilter($filter)" }
        
        val currentState = _state.value
        val filteredChapters = applyFilterAndSort(
            currentState.chapters,
            filter,
            currentState.sort,
            currentState.searchQuery
        )
        
        _state.update { state ->
            state.copy(
                filter = filter,
                filteredChapters = filteredChapters
            )
        }
    }
    
    /**
     * Set the search query for filtering chapters.
     */
    private fun setSearchQuery(query: String?) {
        Log.debug { "$TAG: setSearchQuery($query)" }
        
        val currentState = _state.value
        val filteredChapters = applyFilterAndSort(
            currentState.chapters,
            currentState.filter,
            currentState.sort,
            query
        )
        
        _state.update { state ->
            state.copy(
                searchQuery = query,
                filteredChapters = filteredChapters
            )
        }
    }
    
    // ========== Sort Commands ==========
    
    /**
     * Set the chapter sort order.
     */
    private fun setSort(sort: ChapterSortOrder) {
        Log.debug { "$TAG: setSort($sort)" }
        
        val currentState = _state.value
        val filteredChapters = applyFilterAndSort(
            currentState.chapters,
            currentState.filter,
            sort,
            currentState.searchQuery
        )
        
        _state.update { state ->
            state.copy(
                sort = sort,
                filteredChapters = filteredChapters
            )
        }
    }
    
    // ========== Refresh Commands ==========
    
    /**
     * Refresh chapters from the source.
     * Note: Actual remote fetch would be handled by the ViewModel/UseCase layer.
     * This method signals the refresh intent and updates loading state.
     */
    private suspend fun refreshChapters() {
        Log.debug { "$TAG: refreshChapters()" }
        
        _state.update { it.copy(isRefreshingChapters = true, error = null) }
        
        // Note: The actual remote fetch is handled by the ViewModel layer
        // which has access to the source/catalog. This controller manages
        // the state and will be updated via the subscription when new
        // chapters are inserted into the database.
        
        // For now, we just reload from database
        val bookId = currentBookId
        if (bookId != null) {
            try {
                val chapters = chapterRepository.findChaptersByBookId(bookId)
                val currentState = _state.value
                val filteredChapters = applyFilterAndSort(
                    chapters,
                    currentState.filter,
                    currentState.sort,
                    currentState.searchQuery
                )
                
                _state.update { state ->
                    state.copy(
                        chapters = chapters,
                        filteredChapters = filteredChapters,
                        isRefreshingChapters = false
                    )
                }
                
                _events.emit(BookDetailEvent.ChaptersRefreshed(0, chapters.size))
            } catch (e: Exception) {
                Log.error(e, "$TAG: Failed to refresh chapters")
                handleError(BookDetailError.RefreshFailed(e.message ?: "Failed to refresh chapters"))
            }
        } else {
            _state.update { it.copy(isRefreshingChapters = false) }
        }
    }
    
    /**
     * Refresh book details from the source.
     */
    private suspend fun refreshBook() {
        Log.debug { "$TAG: refreshBook()" }
        
        _state.update { it.copy(isRefreshingBook = true, error = null) }
        
        // Note: The actual remote fetch is handled by the ViewModel layer
        // which has access to the source/catalog. This controller manages
        // the state and will be updated via the subscription when the
        // book is updated in the database.
        
        val bookId = currentBookId
        if (bookId != null) {
            try {
                val book = bookRepository.findBookById(bookId)
                if (book != null) {
                    _state.update { state ->
                        state.copy(
                            book = book,
                            isRefreshingBook = false
                        )
                    }
                    _events.emit(BookDetailEvent.BookRefreshed)
                } else {
                    handleError(BookDetailError.NotFound(bookId))
                }
            } catch (e: Exception) {
                Log.error(e, "$TAG: Failed to refresh book")
                handleError(BookDetailError.RefreshFailed(e.message ?: "Failed to refresh book"))
            }
        } else {
            _state.update { it.copy(isRefreshingBook = false) }
        }
    }
    
    // ========== Navigation Commands ==========
    
    /**
     * Navigate to reader with a specific chapter.
     */
    private suspend fun navigateToReader(chapterId: Long) {
        Log.debug { "$TAG: navigateToReader(chapterId=$chapterId)" }
        
        val bookId = _state.value.book?.id
        if (bookId != null) {
            _events.emit(BookDetailEvent.NavigateToReader(bookId, chapterId))
        }
    }
    
    /**
     * Navigate to reader with the last read chapter or first chapter.
     */
    private suspend fun continueReading() {
        Log.debug { "$TAG: continueReading()" }
        
        val state = _state.value
        val chapter = state.getContinueReadingChapter()
        
        if (chapter != null && state.book != null) {
            _events.emit(BookDetailEvent.NavigateToReader(state.book!!.id, chapter.id))
        } else {
            _events.emit(BookDetailEvent.ShowSnackbar("No chapters available"))
        }
    }
    
    // ========== Error Handling ==========
    
    /**
     * Handle errors by updating state and emitting events.
     * Requirements: 4.2, 4.3
     */
    private suspend fun handleError(error: BookDetailError) {
        Log.error { "$TAG: Error - ${error.toUserMessage()}" }
        
        _state.update { 
            it.copy(
                error = error,
                isLoading = false,
                isRefreshingBook = false,
                isRefreshingChapters = false
            )
        }
        
        _events.emit(BookDetailEvent.Error(error))
    }
    
    /**
     * Clear the current error state.
     * Requirements: 4.5
     */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
    
    /**
     * Release all resources. Call when the controller is no longer needed.
     */
    fun release() {
        Log.debug { "$TAG: release()" }
        cleanup()
        scope.cancel()
    }
    
    // ========== Utility Functions ==========
    
    /**
     * Apply filter, sort, and search to chapters.
     */
    private fun applyFilterAndSort(
        chapters: List<Chapter>,
        filter: ChapterFilter,
        sort: ChapterSortOrder,
        searchQuery: String?
    ): List<Chapter> {
        var result = chapters
        
        // Apply search filter
        if (!searchQuery.isNullOrBlank()) {
            result = result.filter { chapter ->
                chapter.name.contains(searchQuery, ignoreCase = true)
            }
        }
        
        // Apply chapter filter
        result = when (filter) {
            is ChapterFilter.None -> result
            is ChapterFilter.Unread -> result.filter { !it.read }
            is ChapterFilter.Read -> result.filter { it.read }
            is ChapterFilter.Bookmarked -> result.filter { it.bookmark }
            is ChapterFilter.Downloaded -> result.filter { it.content.isNotEmpty() }
            is ChapterFilter.Combined -> {
                var filtered = result
                filter.showUnread?.let { show ->
                    filtered = if (show) filtered.filter { !it.read } else filtered.filter { it.read }
                }
                filter.showBookmarked?.let { show ->
                    filtered = if (show) filtered.filter { it.bookmark } else filtered
                }
                filter.showDownloaded?.let { show ->
                    filtered = if (show) filtered.filter { it.content.isNotEmpty() } else filtered
                }
                filtered
            }
        }
        
        // Apply sort
        result = when (sort.type) {
            ChapterSortOrder.Type.SOURCE -> {
                if (sort.ascending) result.sortedBy { it.number }
                else result.sortedByDescending { it.number }
            }
            ChapterSortOrder.Type.CHAPTER_NUMBER -> {
                if (sort.ascending) result.sortedBy { it.number }
                else result.sortedByDescending { it.number }
            }
            ChapterSortOrder.Type.UPLOAD_DATE -> {
                if (sort.ascending) result.sortedBy { it.dateUpload }
                else result.sortedByDescending { it.dateUpload }
            }
            ChapterSortOrder.Type.NAME -> {
                if (sort.ascending) result.sortedBy { it.name }
                else result.sortedByDescending { it.name }
            }
        }
        
        return result
    }
}

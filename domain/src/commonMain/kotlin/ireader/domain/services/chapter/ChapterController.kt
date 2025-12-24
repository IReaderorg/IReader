package ireader.domain.services.chapter

import ireader.core.log.Log
import ireader.domain.data.repository.BookRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.Chapter
import ireader.domain.usecases.chapter.controller.GetChaptersUseCase
import ireader.domain.usecases.chapter.controller.LoadChapterContentUseCase
import ireader.domain.usecases.chapter.controller.NavigateChapterUseCase
import ireader.domain.usecases.chapter.controller.UpdateProgressUseCase
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Chapter Controller - The central coordinator for all chapter operations.
 * 
 * This is the SINGLE SOURCE OF TRUTH for chapter-related state across all screens
 * (Book Detail, Reader, TTS).
 * 
 * Responsibilities:
 * - Owns and manages the ChapterState (single source of truth)
 * - Processes ChapterCommands and updates state accordingly
 * - Coordinates between use cases for data operations
 * - Emits ChapterEvents for one-time occurrences
 * 
 * NOT responsible for:
 * - UI concerns (UI observes state, sends commands)
 * - Platform-specific implementations
 * 
 * Requirements: 1.1, 2.1-2.5, 3.1-3.3, 4.1-4.5, 5.1-5.4, 6.1-6.4, 7.1-7.5, 8.1-8.4
 */
class ChapterController(
    private val getChaptersUseCase: GetChaptersUseCase,
    private val loadChapterContentUseCase: LoadChapterContentUseCase,
    private val updateProgressUseCase: UpdateProgressUseCase,
    private val navigateChapterUseCase: NavigateChapterUseCase,
    private val bookRepository: BookRepository,
    private val chapterNotifier: ChapterNotifier
) {
    companion object {
        private const val TAG = "ChapterController"
    }
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // Mutex to ensure commands are processed sequentially
    private val commandMutex = Mutex()
    
    // State - single source of truth
    private val _state = MutableStateFlow(ChapterState())
    val state: StateFlow<ChapterState> = _state.asStateFlow()
    
    // Events - one-time occurrences
    private val _events = MutableSharedFlow<ChapterEvent>()
    val events: SharedFlow<ChapterEvent> = _events.asSharedFlow()
    
    // Active subscriptions
    private var chaptersSubscriptionJob: Job? = null
    private var lastReadSubscriptionJob: Job? = null
    
    // Catalog for remote content loading
    private var currentCatalog: CatalogLocal? = null

    
    /**
     * Set the catalog for remote content loading.
     * Should be called before loading chapters that may need remote fetching.
     */
    fun setCatalog(catalog: CatalogLocal?) {
        currentCatalog = catalog
    }
    
    /**
     * Process a command - ALL interactions go through here.
     * Commands are processed sequentially using a mutex to prevent race conditions.
     */
    fun dispatch(command: ChapterCommand) {
        Log.debug { "$TAG: dispatch($command)" }
        
        scope.launch {
            commandMutex.withLock {
                try {
                    processCommand(command)
                } catch (e: Exception) {
                    Log.error(e, "$TAG: Error processing command")
                    handleError(ChapterError.DatabaseError(e.message ?: "Unknown error"))
                }
            }
        }
    }
    
    private suspend fun processCommand(command: ChapterCommand) {
        when (command) {
            // Lifecycle
            is ChapterCommand.LoadBook -> loadBook(command.bookId)
            is ChapterCommand.LoadChapter -> loadChapter(command.chapterId, command.startParagraph)
            is ChapterCommand.Cleanup -> cleanup()
            
            // Navigation
            is ChapterCommand.NextChapter -> nextChapter()
            is ChapterCommand.PreviousChapter -> previousChapter()
            is ChapterCommand.JumpToChapter -> jumpToChapter(command.chapterId)
            
            // Selection
            is ChapterCommand.SelectChapter -> selectChapter(command.chapterId)
            is ChapterCommand.DeselectChapter -> deselectChapter(command.chapterId)
            is ChapterCommand.SelectAll -> selectAll()
            is ChapterCommand.ClearSelection -> clearSelection()
            is ChapterCommand.InvertSelection -> invertSelection()
            
            // Filter & Sort
            is ChapterCommand.SetFilter -> setFilter(command.filter)
            is ChapterCommand.SetSort -> setSort(command.sort)
            
            // Progress
            is ChapterCommand.UpdateProgress -> updateProgress(command.chapterId, command.paragraphIndex)
            is ChapterCommand.MarkAsRead -> markAsRead(command.chapterId)
            is ChapterCommand.MarkAsUnread -> markAsUnread(command.chapterId)
            
            // Content
            is ChapterCommand.PreloadNextChapter -> preloadNextChapter()
            is ChapterCommand.RefreshChapters -> refreshChapters()
        }
    }
    
    // ========== Lifecycle Commands ==========
    
    /**
     * Load a book and subscribe to its chapters.
     * Requirements: 1.1, 1.2, 1.3, 3.2
     */
    private suspend fun loadBook(bookId: Long) {
        Log.debug { "$TAG: loadBook(bookId=$bookId)" }
        
        // Cancel existing subscriptions
        chaptersSubscriptionJob?.cancel()
        lastReadSubscriptionJob?.cancel()
        
        // Clear chapter state when switching books
        _state.update { 
            it.copy(
                isLoadingBook = true, 
                isLoadingChapters = true, 
                error = null,
                currentChapter = null,
                currentChapterIndex = 0,
                currentParagraphIndex = 0,
                chapters = emptyList(),
                filteredChapters = emptyList(),
                selectedChapterIds = emptySet(),
                lastReadChapterId = null
            ) 
        }
        
        try {
            // Load book
            val book = bookRepository.findBookById(bookId)
            if (book == null) {
                handleError(ChapterError.BookNotFound(bookId))
                return
            }
            
            _state.update { it.copy(book = book, isLoadingBook = false) }
            
            // Subscribe to chapters (reactive updates)
            chaptersSubscriptionJob = scope.launch {
                getChaptersUseCase.subscribeByBookId(bookId).collect { chapters ->
                    Log.debug { "$TAG: Chapters updated - ${chapters.size} chapters" }
                    updateChapters(chapters)
                }
            }
            
            // Subscribe to last read chapter
            lastReadSubscriptionJob = scope.launch {
                updateProgressUseCase.subscribeLastRead(bookId).collect { lastReadId ->
                    _state.update { it.copy(lastReadChapterId = lastReadId) }
                }
            }
            
        } catch (e: Exception) {
            Log.error(e, "$TAG: Failed to load book")
            handleError(ChapterError.DatabaseError(e.message ?: "Failed to load book"))
        }
    }

    
    /**
     * Update chapters list and apply current filter/sort.
     */
    private fun updateChapters(chapters: List<Chapter>) {
        val currentState = _state.value
        val filtered = applyFilterAndSort(chapters, currentState.filter, currentState.sort)
        
        // Update current chapter index if we have a current chapter
        val currentChapter = currentState.currentChapter
        val newIndex = if (currentChapter != null) {
            chapters.indexOfFirst { it.id == currentChapter.id }.coerceAtLeast(0)
        } else {
            currentState.currentChapterIndex
        }
        
        _state.update { 
            it.copy(
                chapters = chapters,
                filteredChapters = filtered,
                currentChapterIndex = newIndex,
                isLoadingChapters = false
            )
        }
        
        // Notify observers that chapters have been refreshed
        val bookId = currentState.book?.id
        if (bookId != null) {
            chapterNotifier.tryNotifyChange(ChapterNotifier.ChangeType.BookChaptersRefreshed(bookId))
        }
    }
    
    /**
     * Load a specific chapter's content.
     * Requirements: 4.1, 4.2, 4.3, 4.4
     */
    private suspend fun loadChapter(chapterId: Long, startParagraph: Int) {
        Log.debug { "$TAG: loadChapter(chapterId=$chapterId, startParagraph=$startParagraph)" }
        
        _state.update { it.copy(isLoadingContent = true, error = null) }
        
        try {
            // Find chapter in current list or fetch from database
            var chapter = _state.value.chapters.find { it.id == chapterId }
                ?: getChaptersUseCase.findById(chapterId)
            
            if (chapter == null) {
                handleError(ChapterError.ChapterNotFound(chapterId))
                return
            }
            
            // Load content if not available
            if (chapter.content.isEmpty()) {
                val result = loadChapterContentUseCase.loadContent(chapter, currentCatalog)
                result.fold(
                    onSuccess = { loadedChapter ->
                        chapter = loadedChapter
                        _events.emit(ChapterEvent.ContentFetched(chapterId))
                        // Notify observers that content was fetched
                        val bookId = _state.value.book?.id
                        if (bookId != null) {
                            chapterNotifier.tryNotifyChange(
                                ChapterNotifier.ChangeType.ContentFetched(chapterId, bookId)
                            )
                        }
                    },
                    onFailure = { error ->
                        Log.error(error, "$TAG: Failed to load chapter content")
                        handleError(ChapterError.ContentLoadFailed(error.message ?: "Failed to load content"))
                        return
                    }
                )
            }
            
            // Update state with loaded chapter
            val chapters = _state.value.chapters
            val index = chapters.indexOfFirst { it.id == chapterId }.coerceAtLeast(0)
            
            _state.update { 
                it.copy(
                    currentChapter = chapter,
                    currentChapterIndex = index,
                    currentParagraphIndex = startParagraph.coerceIn(0, (chapter?.content?.size ?: 1) - 1),
                    isLoadingContent = false
                )
            }
            
            // Update reading progress
            val book = _state.value.book
            if (book != null) {
                updateProgressUseCase.updateLastRead(book.id, chapterId)
            }
            
            _events.emit(ChapterEvent.ChapterLoaded(chapter!!))
            
            // Notify observers that current chapter changed
            val bookId = _state.value.book?.id
            if (bookId != null) {
                chapterNotifier.tryNotifyChange(
                    ChapterNotifier.ChangeType.CurrentChapterChanged(chapterId, bookId)
                )
            }
            
        } catch (e: Exception) {
            Log.error(e, "$TAG: Failed to load chapter")
            handleError(ChapterError.ContentLoadFailed(e.message ?: "Failed to load chapter"))
        }
    }
    
    /**
     * Clean up resources, cancel subscriptions, and reset state.
     */
    private fun cleanup() {
        Log.debug { "$TAG: cleanup()" }
        
        chaptersSubscriptionJob?.cancel()
        lastReadSubscriptionJob?.cancel()
        chaptersSubscriptionJob = null
        lastReadSubscriptionJob = null
        currentCatalog = null
        
        _state.update { ChapterState() }
    }
    
    // ========== Navigation Commands ==========
    
    /**
     * Navigate to the next chapter.
     * Requirements: 2.1, 2.4
     */
    private suspend fun nextChapter() {
        val currentState = _state.value
        val book = currentState.book ?: return
        val currentChapter = currentState.currentChapter ?: return
        
        Log.debug { "$TAG: nextChapter() - current: ${currentChapter.id}" }
        
        if (!currentState.canGoNext) {
            Log.debug { "$TAG: At last chapter, cannot go next" }
            return
        }
        
        val nextChapterId = navigateChapterUseCase.getNextChapterId(book.id, currentChapter.id)
        if (nextChapterId != null) {
            loadChapter(nextChapterId, 0)
        }
    }

    
    /**
     * Navigate to the previous chapter.
     * Requirements: 2.2, 2.3
     */
    private suspend fun previousChapter() {
        val currentState = _state.value
        val book = currentState.book ?: return
        val currentChapter = currentState.currentChapter ?: return
        
        Log.debug { "$TAG: previousChapter() - current: ${currentChapter.id}" }
        
        if (!currentState.canGoPrevious) {
            Log.debug { "$TAG: At first chapter, cannot go previous" }
            return
        }
        
        val prevChapterId = navigateChapterUseCase.getPreviousChapterId(book.id, currentChapter.id)
        if (prevChapterId != null) {
            loadChapter(prevChapterId, 0)
        }
    }
    
    /**
     * Jump to a specific chapter by ID.
     * Requirements: 2.5
     */
    private suspend fun jumpToChapter(chapterId: Long) {
        Log.debug { "$TAG: jumpToChapter(chapterId=$chapterId)" }
        
        val chapter = _state.value.chapters.find { it.id == chapterId }
            ?: getChaptersUseCase.findById(chapterId)
        
        if (chapter == null) {
            handleError(ChapterError.ChapterNotFound(chapterId))
            return
        }
        
        loadChapter(chapterId, 0)
    }
    
    // ========== Selection Commands ==========
    
    /**
     * Add a chapter to the selection set (idempotent).
     * Requirements: 7.1
     */
    private fun selectChapter(chapterId: Long) {
        _state.update { 
            it.copy(selectedChapterIds = it.selectedChapterIds + chapterId)
        }
    }
    
    /**
     * Remove a chapter from the selection set (idempotent).
     * Requirements: 7.2
     */
    private fun deselectChapter(chapterId: Long) {
        _state.update { 
            it.copy(selectedChapterIds = it.selectedChapterIds - chapterId)
        }
    }
    
    /**
     * Select all chapters in the current filtered list.
     * Requirements: 7.3
     */
    private fun selectAll() {
        val filteredIds = _state.value.filteredChapters.map { it.id }.toSet()
        _state.update { 
            it.copy(selectedChapterIds = filteredIds)
        }
    }
    
    /**
     * Clear all chapter selections.
     * Requirements: 7.4
     */
    private fun clearSelection() {
        _state.update { 
            it.copy(selectedChapterIds = emptySet())
        }
    }
    
    /**
     * Invert the current selection for all visible chapters.
     * Requirements: 7.5
     */
    private fun invertSelection() {
        val currentState = _state.value
        val filteredIds = currentState.filteredChapters.map { it.id }.toSet()
        val currentSelection = currentState.selectedChapterIds
        
        // New selection = filtered IDs that are NOT currently selected
        val invertedSelection = filteredIds - currentSelection
        
        _state.update { 
            it.copy(selectedChapterIds = invertedSelection)
        }
    }
    
    // ========== Filter & Sort Commands ==========
    
    /**
     * Apply a filter to the chapters list.
     * Requirements: 6.1, 6.3, 6.4
     */
    private fun setFilter(filter: ChapterFilter) {
        val currentState = _state.value
        val filtered = applyFilterAndSort(currentState.chapters, filter, currentState.sort)
        
        _state.update { 
            it.copy(
                filter = filter,
                filteredChapters = filtered
                // Selection is preserved (not modified)
            )
        }
    }
    
    /**
     * Apply a sort order to the chapters list.
     * Requirements: 6.2, 6.3
     */
    private fun setSort(sort: ChapterSort) {
        val currentState = _state.value
        val filtered = applyFilterAndSort(currentState.chapters, currentState.filter, sort)
        
        _state.update { 
            it.copy(
                sort = sort,
                filteredChapters = filtered
                // Selection is preserved (not modified)
            )
        }
    }
    
    /**
     * Apply filter and sort to chapters list.
     */
    private fun applyFilterAndSort(
        chapters: List<Chapter>,
        filter: ChapterFilter,
        sort: ChapterSort
    ): List<Chapter> {
        return chapters
            .filter(filter.toPredicate())
            .sortedWith(sort.toComparator())
    }

    
    // ========== Progress Commands ==========
    
    /**
     * Update reading progress for a chapter.
     * Requirements: 3.1, 3.3
     */
    private suspend fun updateProgress(chapterId: Long, paragraphIndex: Int) {
        Log.debug { "$TAG: updateProgress(chapterId=$chapterId, paragraphIndex=$paragraphIndex)" }
        
        try {
            updateProgressUseCase.updateParagraphIndex(chapterId, paragraphIndex)
            
            _state.update { 
                if (it.currentChapter?.id == chapterId) {
                    it.copy(currentParagraphIndex = paragraphIndex)
                } else {
                    it
                }
            }
            
            _events.emit(ChapterEvent.ProgressSaved(chapterId))
            
        } catch (e: Exception) {
            Log.error(e, "$TAG: Failed to update progress")
            handleError(ChapterError.DatabaseError(e.message ?: "Failed to save progress"))
        }
    }
    
    /**
     * Mark a chapter as read.
     */
    private suspend fun markAsRead(chapterId: Long) {
        Log.debug { "$TAG: markAsRead(chapterId=$chapterId)" }
        
        val book = _state.value.book ?: return
        
        try {
            updateProgressUseCase.updateLastRead(book.id, chapterId)
        } catch (e: Exception) {
            Log.error(e, "$TAG: Failed to mark as read")
            handleError(ChapterError.DatabaseError(e.message ?: "Failed to mark as read"))
        }
    }
    
    /**
     * Mark a chapter as unread.
     * Note: This is a placeholder - actual implementation depends on repository support.
     */
    private suspend fun markAsUnread(chapterId: Long) {
        Log.debug { "$TAG: markAsUnread(chapterId=$chapterId)" }
        // Implementation would require additional repository method
        // For now, this is a no-op as the use case doesn't support it directly
    }
    
    // ========== Content Commands ==========
    
    /**
     * Preload the next chapter's content in the background.
     * Requirements: 4.5
     */
    private suspend fun preloadNextChapter() {
        val currentState = _state.value
        val book = currentState.book ?: return
        val currentChapter = currentState.currentChapter ?: return
        
        if (!currentState.canGoNext) {
            Log.debug { "$TAG: No next chapter to preload" }
            return
        }
        
        Log.debug { "$TAG: preloadNextChapter()" }
        
        _state.update { it.copy(isPreloading = true) }
        
        try {
            val nextChapterId = navigateChapterUseCase.getNextChapterId(book.id, currentChapter.id)
            if (nextChapterId != null) {
                val nextChapter = getChaptersUseCase.findById(nextChapterId)
                if (nextChapter != null && nextChapter.content.isEmpty()) {
                    loadChapterContentUseCase.preloadContent(nextChapter, currentCatalog)
                    Log.debug { "$TAG: Preloaded next chapter: $nextChapterId" }
                }
            }
        } catch (e: Exception) {
            Log.error(e, "$TAG: Failed to preload next chapter")
            // Don't emit error for preload failures - it's a background optimization
        } finally {
            _state.update { it.copy(isPreloading = false) }
        }
    }
    
    /**
     * Refresh chapters from the database.
     */
    private suspend fun refreshChapters() {
        val bookId = _state.value.book?.id ?: return
        
        Log.debug { "$TAG: refreshChapters()" }
        
        _state.update { it.copy(isLoadingChapters = true) }
        
        try {
            val chapters = getChaptersUseCase.findByBookId(bookId)
            updateChapters(chapters)
        } catch (e: Exception) {
            Log.error(e, "$TAG: Failed to refresh chapters")
            handleError(ChapterError.DatabaseError(e.message ?: "Failed to refresh chapters"))
        }
    }
    
    // ========== Error Handling ==========
    
    /**
     * Handle errors by updating state and emitting events.
     * Requirements: 8.1, 8.2, 8.3, 8.4
     */
    private suspend fun handleError(error: ChapterError) {
        Log.error { "$TAG: Error - ${error.toUserMessage()}" }
        
        _state.update { 
            it.copy(
                error = error,
                isLoadingBook = false,
                isLoadingChapters = false,
                isLoadingContent = false,
                isPreloading = false
            )
        }
        
        _events.emit(ChapterEvent.Error(error))
    }
    
    /**
     * Clear the current error state.
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
}

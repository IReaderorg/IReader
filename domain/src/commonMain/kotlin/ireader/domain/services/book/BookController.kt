package ireader.domain.services.book

import ireader.core.log.Log
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.CategoryRepository
import ireader.domain.data.repository.ChapterRepository
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Book Controller - The central coordinator for all book-level operations.
 * 
 * This is the SINGLE SOURCE OF TRUTH for book-related state across all screens
 * (Book Detail, Reader, Library).
 * 
 * Responsibilities:
 * - Owns and manages the BookState (single source of truth)
 * - Processes BookCommands and updates state accordingly
 * - Coordinates between repositories for data operations
 * - Emits BookEvents for one-time occurrences
 * 
 * NOT responsible for:
 * - UI concerns (UI observes state, sends commands)
 * - Platform-specific implementations
 * - Chapter-level operations (handled by ChapterController)
 * 
 * Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 5.1, 5.2, 5.4
 */
class BookController(
    private val bookRepository: BookRepository,
    private val categoryRepository: CategoryRepository,
    private val chapterRepository: ChapterRepository,
    private val historyUseCase: HistoryUseCase
) {
    companion object {
        private const val TAG = "BookController"
    }
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // Mutex to ensure commands are processed sequentially
    private val commandMutex = Mutex()
    
    // State - single source of truth
    private val _state = MutableStateFlow(BookState())
    val state: StateFlow<BookState> = _state.asStateFlow()
    
    // Events - one-time occurrences
    private val _events = MutableSharedFlow<BookEvent>()
    val events: SharedFlow<BookEvent> = _events.asSharedFlow()
    
    // Active subscriptions
    private var bookSubscriptionJob: Job? = null
    private var chaptersSubscriptionJob: Job? = null
    private var categoriesSubscriptionJob: Job? = null
    private var historySubscriptionJob: Job? = null

    
    /**
     * Process a command - ALL interactions go through here.
     * Commands are processed sequentially using a mutex to prevent race conditions.
     * Requirements: 5.2
     */
    fun dispatch(command: BookCommand) {
        Log.debug { "$TAG: dispatch($command)" }
        
        scope.launch {
            commandMutex.withLock {
                try {
                    processCommand(command)
                } catch (e: Exception) {
                    Log.error(e, "$TAG: Error processing command")
                    handleError(BookError.DatabaseError(e.message ?: "Unknown error"))
                }
            }
        }
    }
    
    private suspend fun processCommand(command: BookCommand) {
        when (command) {
            // Lifecycle
            is BookCommand.LoadBook -> loadBook(command.bookId)
            is BookCommand.Cleanup -> cleanup()
            
            // Progress
            is BookCommand.UpdateReadingProgress -> updateReadingProgress(command.chapterId, command.progress)
            
            // Book operations
            is BookCommand.ToggleFavorite -> toggleFavorite()
            is BookCommand.SetCategory -> setCategory(command.categoryId)
            is BookCommand.UpdateMetadata -> updateMetadata(command.title, command.author, command.description)
            is BookCommand.RefreshFromSource -> refreshFromSource()
        }
    }
    
    // ========== Lifecycle Commands ==========
    
    /**
     * Load a book and subscribe to reactive updates.
     * Requirements: 2.1, 2.2
     */
    private suspend fun loadBook(bookId: Long) {
        Log.debug { "$TAG: loadBook(bookId=$bookId)" }
        
        // Cancel existing subscriptions
        cancelSubscriptions()
        
        _state.update { it.copy(isLoading = true, error = null) }
        
        try {
            // Load book
            val book = bookRepository.findBookById(bookId)
            if (book == null) {
                handleError(BookError.BookNotFound(bookId))
                return
            }
            
            _state.update { it.copy(book = book, isLoading = false) }
            
            // Subscribe to book updates (reactive)
            bookSubscriptionJob = scope.launch {
                bookRepository.subscribeBookById(bookId).collect { updatedBook ->
                    if (updatedBook != null) {
                        Log.debug { "$TAG: Book updated - ${updatedBook.title}" }
                        _state.update { it.copy(book = updatedBook) }
                    }
                }
            }
            
            // Subscribe to chapters for progress tracking
            chaptersSubscriptionJob = scope.launch {
                chapterRepository.subscribeChaptersByBookId(bookId).collect { chapters ->
                    val totalChapters = chapters.size
                    val readChapters = chapters.count { it.read }
                    Log.debug { "$TAG: Chapters updated - $readChapters/$totalChapters read" }
                    _state.update { 
                        it.copy(
                            totalChapters = totalChapters,
                            readChapters = readChapters
                        )
                    }
                }
            }
            
            // Subscribe to categories for this book
            categoriesSubscriptionJob = scope.launch {
                categoryRepository.getCategoriesByMangaIdAsFlow(bookId).collect { categories ->
                    Log.debug { "$TAG: Categories updated - ${categories.size} categories" }
                    _state.update { it.copy(categories = categories) }
                }
            }
            
            // Subscribe to history for last read chapter
            historySubscriptionJob = scope.launch {
                historyUseCase.subscribeHistoryByBookId(bookId).collect { history ->
                    Log.debug { "$TAG: History updated - lastReadChapterId=${history?.chapterId}" }
                    _state.update { 
                        it.copy(
                            lastReadChapterId = history?.chapterId,
                            readingProgress = history?.progress ?: 0f
                        )
                    }
                }
            }
            
            _events.emit(BookEvent.BookLoaded(book))
            
        } catch (e: Exception) {
            Log.error(e, "$TAG: Failed to load book")
            handleError(BookError.DatabaseError(e.message ?: "Failed to load book"))
        }
    }
    
    /**
     * Clean up resources, cancel subscriptions, and reset state.
     * Requirements: 2.5
     */
    private fun cleanup() {
        Log.debug { "$TAG: cleanup()" }
        
        cancelSubscriptions()
        _state.update { BookState() }
    }
    
    private fun cancelSubscriptions() {
        bookSubscriptionJob?.cancel()
        chaptersSubscriptionJob?.cancel()
        categoriesSubscriptionJob?.cancel()
        historySubscriptionJob?.cancel()
        bookSubscriptionJob = null
        chaptersSubscriptionJob = null
        categoriesSubscriptionJob = null
        historySubscriptionJob = null
    }
    
    // ========== Progress Commands ==========
    
    /**
     * Update reading progress for the current book.
     * Requirements: 2.3
     */
    @OptIn(ExperimentalTime::class)
    private suspend fun updateReadingProgress(chapterId: Long, progress: Float) {
        Log.debug { "$TAG: updateReadingProgress(chapterId=$chapterId, progress=$progress)" }
        
        val book = _state.value.book
        if (book == null) {
            Log.warn { "$TAG: Cannot update progress - no book loaded" }
            return
        }
        
        try {
            // Find existing history or create new one
            val existingHistory = historyUseCase.findHistory(chapterId)
            val history = ireader.domain.models.entities.History(
                id = existingHistory?.id ?: 0,
                chapterId = chapterId,
                readAt = Clock.System.now().toEpochMilliseconds(),
                readDuration = existingHistory?.readDuration ?: 0,
                progress = progress
            )
            
            historyUseCase.insertHistory(history)
            
            _state.update { 
                it.copy(
                    lastReadChapterId = chapterId,
                    readingProgress = progress
                )
            }
            
            _events.emit(BookEvent.ProgressSaved)
            
        } catch (e: Exception) {
            Log.error(e, "$TAG: Failed to update reading progress")
            handleError(BookError.UpdateFailed(e.message ?: "Failed to save progress"))
        }
    }
    
    // ========== Book Operations ==========
    
    /**
     * Toggle the favorite status of the current book.
     * Requirements: 2.4
     */
    private suspend fun toggleFavorite() {
        Log.debug { "$TAG: toggleFavorite()" }
        
        val book = _state.value.book
        if (book == null) {
            Log.warn { "$TAG: Cannot toggle favorite - no book loaded" }
            return
        }
        
        try {
            val newFavoriteStatus = !book.favorite
            val updatedBook = book.copy(favorite = newFavoriteStatus)
            
            bookRepository.updateBook(updatedBook)
            
            _state.update { it.copy(book = updatedBook) }
            _events.emit(BookEvent.FavoriteToggled(newFavoriteStatus))
            
        } catch (e: Exception) {
            Log.error(e, "$TAG: Failed to toggle favorite")
            handleError(BookError.UpdateFailed(e.message ?: "Failed to toggle favorite"))
        }
    }
    
    /**
     * Set the category for the current book.
     */
    private suspend fun setCategory(categoryId: Long) {
        Log.debug { "$TAG: setCategory(categoryId=$categoryId)" }
        
        val book = _state.value.book
        if (book == null) {
            Log.warn { "$TAG: Cannot set category - no book loaded" }
            return
        }
        
        try {
            // Note: Category assignment is typically handled through BookCategory table
            // This is a simplified implementation - actual implementation may need
            // to use a dedicated use case or repository method
            _events.emit(BookEvent.CategoryUpdated(categoryId))
            
        } catch (e: Exception) {
            Log.error(e, "$TAG: Failed to set category")
            handleError(BookError.UpdateFailed(e.message ?: "Failed to set category"))
        }
    }
    
    /**
     * Update book metadata.
     */
    private suspend fun updateMetadata(title: String?, author: String?, description: String?) {
        Log.debug { "$TAG: updateMetadata(title=$title, author=$author)" }
        
        val book = _state.value.book
        if (book == null) {
            Log.warn { "$TAG: Cannot update metadata - no book loaded" }
            return
        }
        
        try {
            val updatedBook = book.copy(
                title = title ?: book.title,
                author = author ?: book.author,
                description = description ?: book.description
            )
            
            bookRepository.updateBook(updatedBook)
            
            _state.update { it.copy(book = updatedBook) }
            _events.emit(BookEvent.MetadataUpdated)
            
        } catch (e: Exception) {
            Log.error(e, "$TAG: Failed to update metadata")
            handleError(BookError.UpdateFailed(e.message ?: "Failed to update metadata"))
        }
    }
    
    /**
     * Refresh book data from the source.
     */
    private suspend fun refreshFromSource() {
        Log.debug { "$TAG: refreshFromSource()" }
        
        val book = _state.value.book
        if (book == null) {
            Log.warn { "$TAG: Cannot refresh - no book loaded" }
            return
        }
        
        _state.update { it.copy(isRefreshing = true, error = null) }
        
        try {
            // Note: Actual refresh from source would require access to the source/catalog
            // This is a placeholder - the actual implementation would need to:
            // 1. Get the source for this book
            // 2. Fetch updated metadata from the source
            // 3. Update the book in the database
            
            // For now, we just reload from database
            val refreshedBook = bookRepository.findBookById(book.id)
            if (refreshedBook != null) {
                _state.update { it.copy(book = refreshedBook, isRefreshing = false) }
                _events.emit(BookEvent.RefreshCompleted)
            } else {
                handleError(BookError.RefreshFailed("Book no longer exists"))
            }
            
        } catch (e: Exception) {
            Log.error(e, "$TAG: Failed to refresh from source")
            handleError(BookError.RefreshFailed(e.message ?: "Failed to refresh"))
        }
    }
    
    // ========== Error Handling ==========
    
    /**
     * Handle errors by updating state and emitting events.
     * Requirements: 5.4
     */
    private suspend fun handleError(error: BookError) {
        Log.error { "$TAG: Error - ${error.toUserMessage()}" }
        
        _state.update { 
            it.copy(
                error = error,
                isLoading = false,
                isRefreshing = false
            )
        }
        
        _events.emit(BookEvent.Error(error))
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

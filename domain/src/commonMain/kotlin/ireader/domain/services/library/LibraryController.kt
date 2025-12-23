package ireader.domain.services.library

import ireader.core.log.Log
import ireader.domain.data.repository.CategoryRepository
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
 * Library Controller - The central coordinator for all library operations.
 * 
 * This is the SINGLE SOURCE OF TRUTH for library-related state across all screens.
 * 
 * Responsibilities:
 * - Owns and manages the LibraryState (single source of truth)
 * - Processes LibraryCommands and updates state accordingly
 * - Manages filtering, sorting, and selection
 * - Emits LibraryEvents for one-time occurrences
 * 
 * NOT responsible for:
 * - UI concerns (UI observes state, sends commands)
 * - Platform-specific implementations
 * - Book-level operations (handled by BookController)
 * - Loading books (handled by LibraryViewModel via pagination to prevent OOM)
 * 
 * Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 5.1, 5.2, 5.4, 7.3
 */
class LibraryController(
    private val categoryRepository: CategoryRepository
) {
    companion object {
        private const val TAG = "LibraryController"
    }
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // Mutex to ensure commands are processed sequentially
    private val commandMutex = Mutex()
    
    // State - single source of truth
    private val _state = MutableStateFlow(LibraryState())
    val state: StateFlow<LibraryState> = _state.asStateFlow()
    
    // Events - one-time occurrences
    private val _events = MutableSharedFlow<LibraryEvent>()
    val events: SharedFlow<LibraryEvent> = _events.asSharedFlow()
    
    // Active subscriptions
    // NOTE: booksSubscriptionJob removed - books are now loaded via pagination in LibraryViewModel
    // to prevent OOM with large libraries (10,000+ books)
    private var categoriesSubscriptionJob: Job? = null

    
    /**
     * Process a command - ALL interactions go through here.
     * Commands are processed sequentially using a mutex to prevent race conditions.
     * Requirements: 5.2
     */
    fun dispatch(command: LibraryCommand) {
        Log.debug { "$TAG: dispatch($command)" }
        
        scope.launch {
            commandMutex.withLock {
                try {
                    processCommand(command)
                } catch (e: Exception) {
                    Log.error(e, "$TAG: Error processing command")
                    handleError(LibraryError.DatabaseError(e.message ?: "Unknown error"))
                }
            }
        }
    }
    
    private suspend fun processCommand(command: LibraryCommand) {
        when (command) {
            // Lifecycle
            is LibraryCommand.LoadLibrary -> loadLibrary()
            is LibraryCommand.Cleanup -> cleanup()
            
            // Filter/Sort
            is LibraryCommand.SetFilter -> setFilter(command.filter)
            is LibraryCommand.SetSort -> setSort(command.sort)
            is LibraryCommand.SetCategory -> setCategory(command.categoryId)
            
            // Selection
            is LibraryCommand.SelectBook -> selectBook(command.bookId)
            is LibraryCommand.DeselectBook -> deselectBook(command.bookId)
            is LibraryCommand.SelectAll -> selectAll()
            is LibraryCommand.ClearSelection -> clearSelection()
            is LibraryCommand.InvertSelection -> invertSelection()
            
            // Refresh
            is LibraryCommand.RefreshLibrary -> refreshLibrary()
        }
    }
    
    // ========== Lifecycle Commands ==========
    
    /**
     * Load the library metadata (categories only, NOT books).
     * Requirements: 3.1
     * 
     * IMPORTANT: This method intentionally does NOT load books to prevent OOM
     * with large libraries (10,000+ books). Book loading is handled by
     * LibraryViewModel's pagination system which loads books on-demand from the database.
     */
    private suspend fun loadLibrary() {
        Log.debug { "$TAG: loadLibrary() - loading categories only (books loaded via pagination)" }
        
        // Cancel existing subscriptions
        cancelSubscriptions()
        
        _state.update { it.copy(isLoading = true, error = null) }
        
        try {
            // NOTE: We intentionally do NOT subscribe to library books here.
            // Loading all books at once causes OOM with large libraries (10,000+ books).
            // Instead, LibraryViewModel handles book loading via true DB pagination.
            // The books/filteredBooks fields in LibraryState remain empty - 
            // UI should use LibraryViewModel.getPaginatedBooksForCategory() instead.
            
            // Subscribe to categories only (lightweight)
            categoriesSubscriptionJob = scope.launch {
                categoryRepository.getAllAsFlow().collect { categories ->
                    Log.debug { "$TAG: Categories updated - ${categories.size} categories" }
                    _state.update { it.copy(categories = categories) }
                }
            }
            
            _state.update { it.copy(isLoading = false) }
            _events.emit(LibraryEvent.LibraryLoaded)
            
        } catch (e: Exception) {
            Log.error(e, "$TAG: Failed to load library")
            handleError(LibraryError.LoadFailed(e.message ?: "Failed to load library"))
        }
    }
    
    /**
     * Clean up resources, cancel subscriptions, and reset state.
     */
    private fun cleanup() {
        Log.debug { "$TAG: cleanup()" }
        
        cancelSubscriptions()
        _state.update { LibraryState() }
    }
    
    private fun cancelSubscriptions() {
        categoriesSubscriptionJob?.cancel()
        categoriesSubscriptionJob = null
    }
    
    // ========== Filter/Sort Commands ==========
    
    /**
     * Set the current filter and notify listeners.
     * Requirements: 3.2
     * 
     * NOTE: Since LibraryController no longer loads books (to prevent OOM),
     * this method only stores the filter setting. The actual filtering is
     * handled by LibraryViewModel's pagination system which applies the filter
     * when loading books from the database.
     */
    private suspend fun setFilter(filter: LibraryFilter) {
        Log.debug { "$TAG: setFilter($filter)" }
        
        // Preserve selection when changing filter
        val currentSelection = _state.value.selectedBookIds
        
        _state.update { state ->
            // Only update filter setting - LibraryViewModel handles actual filtering
            // via paginated database queries
            state.copy(
                filter = filter,
                selectedBookIds = currentSelection // Preserve selection
            )
        }
        
        _events.emit(LibraryEvent.FilterChanged(filter))
    }
    
    /**
     * Set the current sort and notify listeners.
     * Requirements: 3.3
     * 
     * NOTE: This no longer re-subscribes to books since book loading is handled
     * by LibraryViewModel's pagination system. The sort is stored in state and
     * LibraryViewModel will use it when loading paginated books.
     */
    private suspend fun setSort(sort: LibrarySort) {
        Log.debug { "$TAG: setSort($sort)" }
        
        // Preserve selection when changing sort
        val currentSelection = _state.value.selectedBookIds
        
        // Update sort in state - LibraryViewModel will handle reloading paginated books
        _state.update { it.copy(sort = sort, selectedBookIds = currentSelection) }
        
        _events.emit(LibraryEvent.SortChanged(sort))
    }
    
    /**
     * Set the current category filter.
     */
    private suspend fun setCategory(categoryId: Long?) {
        Log.debug { "$TAG: setCategory($categoryId)" }
        
        val newFilter = if (categoryId != null) {
            LibraryFilter.Category(categoryId)
        } else {
            LibraryFilter.None
        }
        
        _state.update { it.copy(currentCategoryId = categoryId) }
        setFilter(newFilter)
    }

    
    // ========== Selection Commands ==========
    
    /**
     * Select a book by ID (idempotent).
     * Requirements: 3.4
     */
    private suspend fun selectBook(bookId: Long) {
        Log.debug { "$TAG: selectBook($bookId)" }
        
        _state.update { state ->
            // Idempotent - adding same ID multiple times has same effect
            val newSelection = state.selectedBookIds + bookId
            state.copy(selectedBookIds = newSelection)
        }
        
        _events.emit(LibraryEvent.SelectionChanged(_state.value.selectionCount))
    }
    
    /**
     * Deselect a book by ID.
     */
    private suspend fun deselectBook(bookId: Long) {
        Log.debug { "$TAG: deselectBook($bookId)" }
        
        _state.update { state ->
            val newSelection = state.selectedBookIds - bookId
            state.copy(selectedBookIds = newSelection)
        }
        
        _events.emit(LibraryEvent.SelectionChanged(_state.value.selectionCount))
    }
    
    /**
     * Select all books in the current filtered view.
     * 
     * NOTE: Since LibraryController no longer loads books (to prevent OOM),
     * this method now accepts book IDs from the caller (LibraryViewModel).
     * The actual "select all" logic is handled by LibraryViewModel which
     * has access to the paginated books.
     * 
     * This method is kept for compatibility but will only work if book IDs
     * are provided via a separate mechanism (e.g., SelectAllWithIds command).
     */
    private suspend fun selectAll() {
        Log.debug { "$TAG: selectAll() - NOTE: filteredBooks is empty, use SelectAllWithIds instead" }
        
        // filteredBooks is now empty since we don't load books
        // LibraryViewModel should handle this by calling selectBook for each visible book
        // or by using a new SelectAllWithIds command
        
        _events.emit(LibraryEvent.SelectionChanged(_state.value.selectionCount))
    }
    
    /**
     * Clear all selections.
     * Requirements: 3.5
     */
    private suspend fun clearSelection() {
        Log.debug { "$TAG: clearSelection()" }
        
        _state.update { state ->
            state.copy(selectedBookIds = emptySet())
        }
        
        _events.emit(LibraryEvent.SelectionChanged(0))
    }
    
    /**
     * Invert the current selection.
     * 
     * NOTE: Since LibraryController no longer loads books (to prevent OOM),
     * this method cannot properly invert selection without knowing all book IDs.
     * LibraryViewModel should handle this by providing the visible book IDs.
     */
    private suspend fun invertSelection() {
        Log.debug { "$TAG: invertSelection() - NOTE: filteredBooks is empty, handled by ViewModel" }
        
        // filteredBooks is now empty since we don't load books
        // LibraryViewModel should handle this by computing the inversion
        // based on its paginated books
        
        _events.emit(LibraryEvent.SelectionChanged(_state.value.selectionCount))
    }
    
    // ========== Refresh Commands ==========
    
    /**
     * Refresh the library metadata (categories).
     * 
     * NOTE: This no longer loads all books since book loading is handled
     * by LibraryViewModel's pagination system. The refresh event signals
     * LibraryViewModel to reset its pagination and reload.
     */
    private suspend fun refreshLibrary() {
        Log.debug { "$TAG: refreshLibrary() - refreshing categories only" }
        
        _state.update { it.copy(isRefreshing = true, error = null) }
        
        try {
            // Categories are already subscribed via flow, so just emit refresh completed
            // LibraryViewModel will handle resetting pagination and reloading books
            
            _state.update { it.copy(isRefreshing = false) }
            _events.emit(LibraryEvent.RefreshCompleted)
            
        } catch (e: Exception) {
            Log.error(e, "$TAG: Failed to refresh library")
            handleError(LibraryError.RefreshFailed(e.message ?: "Failed to refresh"))
        }
    }
    
    // ========== Error Handling ==========
    
    /**
     * Handle errors by updating state and emitting events.
     * Requirements: 5.4
     */
    private suspend fun handleError(error: LibraryError) {
        Log.error { "$TAG: Error - ${error.toUserMessage()}" }
        
        _state.update { 
            it.copy(
                error = error,
                isLoading = false,
                isRefreshing = false
            )
        }
        
        _events.emit(LibraryEvent.Error(error))
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

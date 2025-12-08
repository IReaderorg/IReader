package ireader.domain.services.library

import ireader.core.log.Log
import ireader.domain.data.repository.CategoryRepository
import ireader.domain.data.repository.LibraryRepository
import ireader.domain.models.entities.LibraryBook
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
 * 
 * Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 5.1, 5.2, 5.4, 7.3
 */
class LibraryController(
    private val libraryRepository: LibraryRepository,
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
    private var booksSubscriptionJob: Job? = null
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
     * Load the library and subscribe to reactive updates.
     * Requirements: 3.1
     */
    private suspend fun loadLibrary() {
        Log.debug { "$TAG: loadLibrary()" }
        
        // Cancel existing subscriptions
        cancelSubscriptions()
        
        _state.update { it.copy(isLoading = true, error = null) }
        
        try {
            val currentSort = _state.value.sort
            
            // Subscribe to library books
            booksSubscriptionJob = scope.launch {
                libraryRepository.subscribe(currentSort.toDomainSort()).collect { books ->
                    Log.debug { "$TAG: Library updated - ${books.size} books" }
                    updateBooksAndApplyFilter(books)
                }
            }
            
            // Subscribe to categories
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
        booksSubscriptionJob?.cancel()
        categoriesSubscriptionJob?.cancel()
        booksSubscriptionJob = null
        categoriesSubscriptionJob = null
    }
    
    // ========== Filter/Sort Commands ==========
    
    /**
     * Set the current filter and reapply to books.
     * Requirements: 3.2
     */
    private suspend fun setFilter(filter: LibraryFilter) {
        Log.debug { "$TAG: setFilter($filter)" }
        
        // Preserve selection when changing filter
        val currentSelection = _state.value.selectedBookIds
        
        _state.update { state ->
            // Only apply filter, not sort - repository already sorted the data
            val predicate = filter.toPredicate()
            val filteredBooks = state.books.filter(predicate)
            state.copy(
                filter = filter,
                filteredBooks = filteredBooks,
                selectedBookIds = currentSelection // Preserve selection
            )
        }
        
        _events.emit(LibraryEvent.FilterChanged(filter))
    }
    
    /**
     * Set the current sort and re-subscribe to repository with new sort.
     * Requirements: 3.3
     */
    private suspend fun setSort(sort: LibrarySort) {
        Log.debug { "$TAG: setSort($sort)" }
        
        // Preserve selection when changing sort
        val currentSelection = _state.value.selectedBookIds
        
        // Update sort in state first
        _state.update { it.copy(sort = sort, selectedBookIds = currentSelection) }
        
        // Cancel existing subscription and re-subscribe with new sort
        booksSubscriptionJob?.cancel()
        booksSubscriptionJob = scope.launch {
            libraryRepository.subscribe(sort.toDomainSort()).collect { books ->
                Log.debug { "$TAG: Library updated with new sort - ${books.size} books" }
                updateBooksAndApplyFilter(books)
            }
        }
        
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
     */
    private suspend fun selectAll() {
        Log.debug { "$TAG: selectAll()" }
        
        _state.update { state ->
            val allIds = state.filteredBooks.map { it.id }.toSet()
            state.copy(selectedBookIds = allIds)
        }
        
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
     */
    private suspend fun invertSelection() {
        Log.debug { "$TAG: invertSelection()" }
        
        _state.update { state ->
            val allIds = state.filteredBooks.map { it.id }.toSet()
            val invertedSelection = allIds - state.selectedBookIds
            state.copy(selectedBookIds = invertedSelection)
        }
        
        _events.emit(LibraryEvent.SelectionChanged(_state.value.selectionCount))
    }
    
    // ========== Refresh Commands ==========
    
    /**
     * Refresh the library from the database.
     */
    private suspend fun refreshLibrary() {
        Log.debug { "$TAG: refreshLibrary()" }
        
        _state.update { it.copy(isRefreshing = true, error = null) }
        
        try {
            val currentSort = _state.value.sort
            val books = libraryRepository.findAll(currentSort.toDomainSort())
            
            updateBooksAndApplyFilter(books)
            
            _state.update { it.copy(isRefreshing = false) }
            _events.emit(LibraryEvent.RefreshCompleted)
            
        } catch (e: Exception) {
            Log.error(e, "$TAG: Failed to refresh library")
            handleError(LibraryError.RefreshFailed(e.message ?: "Failed to refresh"))
        }
    }
    
    // ========== Helper Methods ==========
    
    /**
     * Update books and apply current filter (but NOT sort - repository already sorted).
     * The repository handles sorting with proper database queries for LastRead, DateAdded, etc.
     */
    private fun updateBooksAndApplyFilter(books: List<LibraryBook>) {
        _state.update { state ->
            // Only apply filter, not sort - repository already sorted the data correctly
            val predicate = state.filter.toPredicate()
            val filteredBooks = books.filter(predicate)
            state.copy(
                books = books,
                filteredBooks = filteredBooks,
                isLoading = false
            )
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

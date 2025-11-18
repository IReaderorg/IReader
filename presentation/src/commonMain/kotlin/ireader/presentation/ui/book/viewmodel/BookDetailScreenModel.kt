package ireader.presentation.ui.book.viewmodel

import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.usecases.local.LocalGetBookUseCases
import ireader.domain.usecases.local.LocalGetChapterUseCase
import ireader.domain.usecases.local.LocalInsertUseCases
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.domain.catalogs.interactor.GetLocalCatalog
import ireader.domain.models.entities.CatalogLocal
import ireader.presentation.core.viewmodel.IReaderStateScreenModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * BookDetailScreenModel following Mihon's StateScreenModel pattern.
 * Replaces the current ViewModel-based approach with predictable state management.
 * 
 * Features:
 * - Sealed state classes for Loading, Success, Error states
 * - Proper error handling with user-friendly messages
 * - Reactive state updates using Flow
 * - Clean separation of business logic
 */
class BookDetailScreenModel(
    private val bookId: Long,
    private val getBookUseCases: LocalGetBookUseCases,
    private val getChapterUseCase: LocalGetChapterUseCase,
    private val localInsertUseCases: LocalInsertUseCases,
    private val remoteUseCases: RemoteUseCases,
    private val getLocalCatalog: GetLocalCatalog,
) : IReaderStateScreenModel<BookDetailScreenModel.State>(State()) {

    /**
     * Sealed state classes for predictable UI state management
     */
    data class State(
        val book: Book? = null,
        val chapters: List<Chapter> = emptyList(),
        val isLoading: Boolean = true,
        val isChaptersLoading: Boolean = false,
        val error: String? = null,
        val lastReadChapterId: Long? = null,
        val catalogSource: CatalogLocal? = null,
        val hasSelection: Boolean = false,
        val selectedChapterIds: Set<Long> = emptySet(),
        val searchMode: Boolean = false,
        val searchQuery: String? = null,
        val filters: List<ChaptersFilters> = ChaptersFilters.getDefault(true),
        val sorting: ChapterSort = ChapterSort.default,
    )

    init {
        loadBookDetails()
    }

    private fun loadBookDetails() {
        updateState { it.copy(isLoading = true, error = null) }
        
        launchIO {
            try {
                // Subscribe to book changes
                getBookUseCases.subscribeBookById(bookId).onEach { book ->
                    updateState { currentState ->
                        currentState.copy(
                            book = book,
                            isLoading = false
                        )
                    }
                    
                    // Load catalog source if not already loaded
                    if (book != null && state.value.catalogSource == null) {
                        val catalog = getLocalCatalog.get(book.sourceId)
                        updateState { it.copy(catalogSource = catalog) }
                        
                        // Initialize book if needed
                        if (!book.initialized) {
                            initializeBook(book, catalog)
                        }
                    }
                }.launchIn(this)
                
                // Subscribe to chapters
                getChapterUseCase.subscribeChaptersByBookId(bookId).onEach { chapters ->
                    updateState { currentState ->
                        currentState.copy(
                            chapters = chapters.filteredAndSorted(
                                currentState.filters,
                                currentState.sorting,
                                currentState.searchQuery
                            ),
                            isChaptersLoading = false
                        )
                    }
                }.launchIn(this)
                
            } catch (e: Exception) {
                logError("Failed to load book details", e)
                updateState { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

    private suspend fun initializeBook(book: Book, catalog: CatalogLocal?) {
        if (catalog?.source == null) return
        
        updateState { it.copy(isLoading = true) }
        
        try {
            // Update book as initialized
            val updatedBook = book.copy(
                initialized = true,
                lastUpdate = System.currentTimeMillis()
            )
            localInsertUseCases.updateBook.update(updatedBook)
            
            // Fetch remote book details
            remoteUseCases.getBookDetail(
                book = updatedBook,
                catalog = catalog,
                onError = { error ->
                    val errorMessage = error?.toString() ?: "Failed to load book details"
                    logError("Failed to fetch remote book details", Exception(errorMessage))
                    updateState { 
                        it.copy(
                            isLoading = false,
                            error = errorMessage
                        )
                    }
                },
                onSuccess = { resultBook ->
                    launchIO {
                        localInsertUseCases.updateBook.update(resultBook)
                        updateState { it.copy(isLoading = false) }
                    }
                }
            )
            
            // Fetch chapters
            updateState { it.copy(isChaptersLoading = true) }
            remoteUseCases.getRemoteChapters(
                book = updatedBook,
                catalog = catalog,
                onError = { error ->
                    val errorMessage = error?.toString() ?: "Failed to load chapters"
                    logError("Failed to fetch chapters", Exception(errorMessage))
                    updateState { 
                        it.copy(
                            isChaptersLoading = false,
                            error = errorMessage
                        )
                    }
                },
                onSuccess = { chapters ->
                    launchIO {
                        localInsertUseCases.insertChapters(chapters)
                        updateState { it.copy(isChaptersLoading = false) }
                    }
                },
                commands = emptyList(),
                oldChapters = emptyList()
            )
            
        } catch (e: Exception) {
            logError("Failed to initialize book", e)
            updateState { 
                it.copy(
                    isLoading = false,
                    isChaptersLoading = false,
                    error = e.message ?: "Failed to initialize book"
                )
            }
        }
    }

    /**
     * Toggle favorite status of the book
     */
    fun toggleFavorite() {
        val book = state.value.book ?: return
        
        launchIO {
            try {
                val updatedBook = book.copy(
                    favorite = !book.favorite,
                    dateAdded = if (!book.favorite) System.currentTimeMillis() else book.dateAdded
                )
                localInsertUseCases.updateBook.update(updatedBook)
                logInfo("Toggled favorite for book: ${book.title}")
            } catch (e: Exception) {
                logError("Failed to toggle favorite", e)
                updateState { 
                    it.copy(error = "Failed to update favorite status")
                }
            }
        }
    }

    /**
     * Toggle chapter selection
     */
    fun toggleChapterSelection(chapterId: Long) {
        updateState { currentState ->
            val newSelection = if (chapterId in currentState.selectedChapterIds) {
                currentState.selectedChapterIds - chapterId
            } else {
                currentState.selectedChapterIds + chapterId
            }
            
            currentState.copy(
                selectedChapterIds = newSelection,
                hasSelection = newSelection.isNotEmpty()
            )
        }
    }

    /**
     * Clear all chapter selections
     */
    fun clearSelection() {
        updateState { 
            it.copy(
                selectedChapterIds = emptySet(),
                hasSelection = false
            )
        }
    }

    /**
     * Toggle search mode
     */
    fun toggleSearchMode() {
        updateState { currentState ->
            if (currentState.searchMode) {
                // Exiting search mode - clear query and update chapters
                currentState.copy(
                    searchMode = false,
                    searchQuery = null,
                    chapters = state.value.chapters.filteredAndSorted(
                        currentState.filters,
                        currentState.sorting,
                        null
                    )
                )
            } else {
                // Entering search mode
                currentState.copy(searchMode = true)
            }
        }
    }

    /**
     * Update search query
     */
    fun updateSearchQuery(query: String?) {
        updateState { currentState ->
            currentState.copy(
                searchQuery = query,
                chapters = state.value.chapters.filteredAndSorted(
                    currentState.filters,
                    currentState.sorting,
                    query
                )
            )
        }
    }

    /**
     * Toggle filter
     */
    fun toggleFilter(filterType: ChaptersFilters.Type) {
        updateState { currentState ->
            val newFilters = currentState.filters.map { filter ->
                if (filter.type == filterType) {
                    filter.copy(
                        value = when (filter.value) {
                            ChaptersFilters.Value.Included -> ChaptersFilters.Value.Excluded
                            ChaptersFilters.Value.Excluded -> ChaptersFilters.Value.Missing
                            ChaptersFilters.Value.Missing -> ChaptersFilters.Value.Included
                        }
                    )
                } else {
                    filter
                }
            }
            
            currentState.copy(
                filters = newFilters,
                chapters = state.value.chapters.filteredAndSorted(
                    newFilters,
                    currentState.sorting,
                    currentState.searchQuery
                )
            )
        }
    }

    /**
     * Update sorting
     */
    fun updateSorting(sortType: ChapterSort.Type) {
        updateState { currentState ->
            val newSorting = if (currentState.sorting.type == sortType) {
                currentState.sorting.copy(isAscending = !currentState.sorting.isAscending)
            } else {
                currentState.sorting.copy(type = sortType)
            }
            
            currentState.copy(
                sorting = newSorting,
                chapters = state.value.chapters.filteredAndSorted(
                    currentState.filters,
                    newSorting,
                    currentState.searchQuery
                )
            )
        }
    }

    /**
     * Retry loading book details
     */
    fun retry() {
        loadBookDetails()
    }

    /**
     * Clear error state
     */
    fun clearError() {
        updateState { it.copy(error = null) }
    }
}

/**
 * Extension function to filter and sort chapters
 */
private fun List<Chapter>.filteredAndSorted(
    filters: List<ChaptersFilters>,
    sorting: ChapterSort,
    searchQuery: String?
): List<Chapter> {
    var result = this
    
    // Apply search filter
    if (!searchQuery.isNullOrBlank()) {
        result = result.filter { chapter ->
            chapter.name.contains(searchQuery, ignoreCase = true)
        }
    }
    
    // Apply filters
    val activeFilters = filters.filter { 
        it.value == ChaptersFilters.Value.Included || it.value == ChaptersFilters.Value.Excluded 
    }
    
    for (filter in activeFilters) {
        val filterFn: (Chapter) -> Boolean = when (filter.type) {
            ChaptersFilters.Type.Unread -> { chapter -> !chapter.read }
            ChaptersFilters.Type.Read -> { chapter -> chapter.read }
            ChaptersFilters.Type.Bookmarked -> { chapter -> chapter.bookmark }
            ChaptersFilters.Type.Downloaded -> { chapter -> chapter.content.joinToString("").isNotBlank() }
            ChaptersFilters.Type.Duplicate -> { chapter ->
                // Simple duplicate detection by name similarity
                result.any { other ->
                    other.id != chapter.id && 
                    other.name.trim().equals(chapter.name.trim(), ignoreCase = true)
                }
            }
        }
        
        result = when (filter.value) {
            ChaptersFilters.Value.Included -> result.filter(filterFn)
            ChaptersFilters.Value.Excluded -> result.filterNot(filterFn)
            ChaptersFilters.Value.Missing -> result
        }
    }
    
    // Apply sorting
    result = when (sorting.type) {
        ChapterSort.Type.Default -> {
            if (sorting.isAscending) result.sortedBy { it.sourceOrder }
            else result.sortedByDescending { it.sourceOrder }
        }
        ChapterSort.Type.ByName -> {
            if (sorting.isAscending) result.sortedBy { it.name }
            else result.sortedByDescending { it.name }
        }
        ChapterSort.Type.BySource -> {
            if (sorting.isAscending) result.sortedBy { it.sourceOrder }
            else result.sortedByDescending { it.sourceOrder }
        }
        ChapterSort.Type.ByChapterNumber -> {
            if (sorting.isAscending) result.sortedBy { it.number }
            else result.sortedByDescending { it.number }
        }
        ChapterSort.Type.DateUpload -> {
            if (sorting.isAscending) result.sortedBy { it.dateUpload }
            else result.sortedByDescending { it.dateUpload }
        }
        ChapterSort.Type.DateFetched -> {
            if (sorting.isAscending) result.sortedBy { it.dateFetch }
            else result.sortedByDescending { it.dateFetch }
        }
        ChapterSort.Type.Bookmark -> {
            if (sorting.isAscending) result.sortedBy { it.bookmark }
            else result.sortedByDescending { it.bookmark }
        }
        ChapterSort.Type.Read -> {
            if (sorting.isAscending) result.sortedBy { it.read }
            else result.sortedByDescending { it.read }
        }
    }
    
    return result
}

// Using ChaptersFilters and ChapterSort from separate files
// See ChapterFilters.kt and ChapterSort.kt
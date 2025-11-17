package ireader.presentation.ui.book.viewmodel

import ireader.core.source.Source
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.preferences.prefs.ChapterDisplayMode
import ireader.domain.usecases.local.LocalGetBookUseCases
import ireader.domain.usecases.local.LocalGetChapterUseCase
import ireader.domain.usecases.local.LocalInsertUseCases
import ireader.presentation.core.viewmodel.IReaderStateScreenModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Enhanced BookDetailScreenModel following Mihon's StateScreenModel pattern.
 * Replaces the current ViewModel-based approach with predictable state management.
 */
class BookDetailScreenModelNew(
    private val bookId: Long,
    private val getBookUseCases: LocalGetBookUseCases,
    private val getChapterUseCase: LocalGetChapterUseCase,
    private val insertUseCases: LocalInsertUseCases,
) : IReaderStateScreenModel<BookDetailScreenModelNew.State>(State()) {

    /**
     * Comprehensive state for book detail screen
     */
    data class State(
        val book: Book? = null,
        val chapters: List<Chapter> = emptyList(),
        val source: Source? = null,
        val isLoading: Boolean = true,
        val isRefreshing: Boolean = false,
        val error: String? = null,
        val isTogglingFavorite: Boolean = false,
        val lastReadChapterId: Long? = null,
        val selectedChapterIds: Set<Long> = emptySet(),
        val searchQuery: String? = null,
        val isSearchMode: Boolean = false,
        val chapterDisplayMode: ChapterDisplayMode = ChapterDisplayMode.Default,
        val filters: List<ChaptersFilters> = ChaptersFilters.getDefault(true),
        val sorting: ChapterSort = ChapterSort.default,
        val showMigrationDialog: Boolean = false,
        val showEpubExportDialog: Boolean = false,
        val showEditInfoDialog: Boolean = false,
    )

    init {
        loadBookDetails()
    }

    /**
     * Load book details and chapters
     */
    private fun loadBookDetails() {
        logDebug("Loading book details for ID: $bookId")
        
        // Subscribe to book changes
        getBookUseCases.subscribeBookById(bookId)
            .onEach { book ->
                updateState { currentState ->
                    currentState.copy(
                        book = book,
                        isLoading = false,
                        error = if (book == null) "Book not found" else null
                    )
                }
                logDebug("Book loaded: ${book?.title ?: "null"}")
            }
            .launchIn(screenModelScope)

        // Subscribe to chapter changes
        getChapterUseCase.subscribeChaptersByBookId(bookId)
            .onEach { chapters ->
                updateState { currentState ->
                    currentState.copy(chapters = chapters)
                }
                logDebug("Chapters loaded: ${chapters.size}")
            }
            .launchIn(screenModelScope)
    }

    /**
     * Refresh book and chapter data
     */
    fun refresh() {
        logInfo("Refreshing book details")
        updateState { it.copy(isRefreshing = true, error = null) }
        
        launchIO {
            try {
                // Reload book data
                val book = getBookUseCases.findBookById(bookId)
                val chapters = getChapterUseCase.findChaptersByBookId(bookId)
                
                updateState { currentState ->
                    currentState.copy(
                        book = book,
                        chapters = chapters,
                        isRefreshing = false,
                        error = if (book == null) "Book not found" else null
                    )
                }
                logInfo("Book details refreshed successfully")
            } catch (e: Exception) {
                updateState { currentState ->
                    currentState.copy(
                        isRefreshing = false,
                        error = "Failed to refresh: ${e.message}"
                    )
                }
                logError("Failed to refresh book details", e)
            }
        }
    }

    /**
     * Toggle book favorite status
     */
    fun toggleBookFavorite() {
        val currentBook = state.value.book ?: return
        
        logInfo("Toggling favorite status for book: ${currentBook.title}")
        updateState { it.copy(isTogglingFavorite = true) }
        
        launchIO {
            try {
                val updatedBook = currentBook.copy(favorite = !currentBook.favorite)
                insertUseCases.insertBook(updatedBook)
                
                updateState { currentState ->
                    currentState.copy(
                        isTogglingFavorite = false,
                        book = updatedBook
                    )
                }
                
                val action = if (updatedBook.favorite) "added to" else "removed from"
                logInfo("Book '${currentBook.title}' $action library")
            } catch (e: Exception) {
                updateState { currentState ->
                    currentState.copy(
                        isTogglingFavorite = false,
                        error = "Failed to update favorite status: ${e.message}"
                    )
                }
                logError("Error toggling favorite status", e)
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
            currentState.copy(selectedChapterIds = newSelection)
        }
    }

    /**
     * Clear chapter selection
     */
    fun clearChapterSelection() {
        updateState { it.copy(selectedChapterIds = emptySet()) }
    }

    /**
     * Toggle search mode
     */
    fun toggleSearchMode() {
        updateState { currentState ->
            currentState.copy(
                isSearchMode = !currentState.isSearchMode,
                searchQuery = if (!currentState.isSearchMode) null else currentState.searchQuery
            )
        }
    }

    /**
     * Update search query
     */
    fun updateSearchQuery(query: String?) {
        updateState { it.copy(searchQuery = query) }
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
            currentState.copy(filters = newFilters)
        }
    }

    /**
     * Update sorting
     */
    fun updateSorting(sort: ChapterSort) {
        updateState { it.copy(sorting = sort) }
    }

    /**
     * Show migration dialog
     */
    fun showMigrationDialog() {
        updateState { it.copy(showMigrationDialog = true) }
    }

    /**
     * Hide migration dialog
     */
    fun hideMigrationDialog() {
        updateState { it.copy(showMigrationDialog = false) }
    }

    /**
     * Show EPUB export dialog
     */
    fun showEpubExportDialog() {
        updateState { it.copy(showEpubExportDialog = true) }
    }

    /**
     * Hide EPUB export dialog
     */
    fun hideEpubExportDialog() {
        updateState { it.copy(showEpubExportDialog = false) }
    }

    /**
     * Show edit info dialog
     */
    fun showEditInfoDialog() {
        updateState { it.copy(showEditInfoDialog = true) }
    }

    /**
     * Hide edit info dialog
     */
    fun hideEditInfoDialog() {
        updateState { it.copy(showEditInfoDialog = false) }
    }

    /**
     * Clear error state
     */
    fun clearError() {
        updateState { it.copy(error = null) }
    }

    /**
     * Retry loading book details
     */
    fun retry() {
        logInfo("Retrying to load book details")
        updateState { it.copy(isLoading = true, error = null) }
        loadBookDetails()
    }

    override fun handleError(error: Throwable) {
        updateState { currentState ->
            currentState.copy(
                isLoading = false,
                isRefreshing = false,
                isTogglingFavorite = false,
                error = error.message ?: "Unknown error occurred"
            )
        }
        super.handleError(error)
    }
}
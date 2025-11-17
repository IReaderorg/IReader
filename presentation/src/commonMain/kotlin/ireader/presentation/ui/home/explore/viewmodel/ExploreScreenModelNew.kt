package ireader.presentation.ui.home.explore.viewmodel

import ireader.domain.models.entities.Book
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.usecases.book.GetBook
import ireader.domain.usecases.book.ToggleFavorite
import ireader.presentation.core.viewmodel.IReaderStateScreenModel

/**
 * New ExploreScreenModel following Mihon's StateScreenModel pattern.
 * Replaces the current ViewModel-based approach with predictable state management.
 */
class ExploreScreenModelNew(
    private val catalogSource: CatalogLocal,
    private val getBook: GetBook,
    private val toggleFavorite: ToggleFavorite,
) : IReaderStateScreenModel<ExploreScreenModelNew.State>(State()) {

    /**
     * Sealed state classes for predictable UI state management
     */
    data class State(
        val books: List<Book> = emptyList(),
        val isLoading: Boolean = false,
        val isLoadingMore: Boolean = false,
        val error: String? = null,
        val searchQuery: String = "",
        val hasNextPage: Boolean = true,
        val currentPage: Int = 1,
        val isRefreshing: Boolean = false,
    )

    init {
        logDebug("Initializing ExploreScreenModel for source: ${catalogSource.name}")
    }

    /**
     * Load books from the catalog source
     */
    fun loadBooks(refresh: Boolean = false) {
        if (refresh) {
            logInfo("Refreshing books from source: ${catalogSource.name}")
            updateState { 
                it.copy(
                    isRefreshing = true,
                    error = null,
                    currentPage = 1,
                    books = emptyList(),
                    hasNextPage = true
                )
            }
        } else {
            logDebug("Loading books from source: ${catalogSource.name}")
            updateState { it.copy(isLoading = true, error = null) }
        }

        launchIO {
            try {
                // TODO: Implement actual book loading from catalog source
                // This would typically involve calling the catalog source's getPopularBooks or similar method
                
                // Placeholder implementation
                val books = loadBooksFromSource(state.value.currentPage, state.value.searchQuery)
                
                updateState { currentState ->
                    val newBooks = if (refresh || currentState.currentPage == 1) {
                        books
                    } else {
                        currentState.books + books
                    }
                    
                    currentState.copy(
                        books = newBooks,
                        isLoading = false,
                        isRefreshing = false,
                        isLoadingMore = false,
                        hasNextPage = books.isNotEmpty(),
                        currentPage = if (books.isNotEmpty()) currentState.currentPage + 1 else currentState.currentPage
                    )
                }
                
                logInfo("Loaded ${books.size} books from source: ${catalogSource.name}")
            } catch (e: Exception) {
                updateState { currentState ->
                    currentState.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isLoadingMore = false,
                        error = "Failed to load books: ${e.message}"
                    )
                }
                logError("Failed to load books from source: ${catalogSource.name}", e)
            }
        }
    }

    /**
     * Load more books (pagination)
     */
    fun loadMoreBooks() {
        if (state.value.isLoadingMore || !state.value.hasNextPage) return
        
        logDebug("Loading more books, page: ${state.value.currentPage}")
        updateState { it.copy(isLoadingMore = true) }
        
        launchIO {
            try {
                val books = loadBooksFromSource(state.value.currentPage, state.value.searchQuery)
                
                updateState { currentState ->
                    currentState.copy(
                        books = currentState.books + books,
                        isLoadingMore = false,
                        hasNextPage = books.isNotEmpty(),
                        currentPage = if (books.isNotEmpty()) currentState.currentPage + 1 else currentState.currentPage
                    )
                }
                
                logDebug("Loaded ${books.size} more books")
            } catch (e: Exception) {
                updateState { currentState ->
                    currentState.copy(
                        isLoadingMore = false,
                        error = "Failed to load more books: ${e.message}"
                    )
                }
                logError("Failed to load more books", e)
            }
        }
    }

    /**
     * Search for books
     */
    fun searchBooks(query: String) {
        logInfo("Searching books with query: '$query'")
        updateState { 
            it.copy(
                searchQuery = query,
                isLoading = true,
                error = null,
                currentPage = 1,
                books = emptyList(),
                hasNextPage = true
            )
        }
        
        launchIO {
            try {
                val books = searchBooksFromSource(query)
                
                updateState { currentState ->
                    currentState.copy(
                        books = books,
                        isLoading = false,
                        hasNextPage = books.isNotEmpty()
                    )
                }
                
                logInfo("Found ${books.size} books for query: '$query'")
            } catch (e: Exception) {
                updateState { currentState ->
                    currentState.copy(
                        isLoading = false,
                        error = "Search failed: ${e.message}"
                    )
                }
                logError("Search failed for query: '$query'", e)
            }
        }
    }

    /**
     * Toggle favorite status for a book
     */
    fun toggleBookFavorite(book: Book) {
        logInfo("Toggling favorite for book: ${book.title}")
        
        launchIO {
            try {
                val success = toggleFavorite.await(book)
                
                if (success) {
                    // Update the book in the current list
                    updateState { currentState ->
                        val updatedBooks = currentState.books.map { currentBook ->
                            if (currentBook.id == book.id) {
                                currentBook.copy(favorite = !currentBook.favorite)
                            } else {
                                currentBook
                            }
                        }
                        currentState.copy(books = updatedBooks)
                    }
                    
                    val action = if (book.favorite) "removed from" else "added to"
                    logInfo("Book '${book.title}' $action library")
                } else {
                    updateState { currentState ->
                        currentState.copy(error = "Failed to update favorite status")
                    }
                    logWarn("Failed to toggle favorite for book: ${book.title}")
                }
            } catch (e: Exception) {
                updateState { currentState ->
                    currentState.copy(error = "Failed to update favorite: ${e.message}")
                }
                logError("Error toggling favorite for book: ${book.title}", e)
            }
        }
    }

    /**
     * Clear error state
     */
    fun clearError() {
        updateState { it.copy(error = null) }
    }

    /**
     * Refresh books
     */
    fun refresh() {
        loadBooks(refresh = true)
    }

    /**
     * Clear search and load popular books
     */
    fun clearSearch() {
        logInfo("Clearing search")
        updateState { 
            it.copy(
                searchQuery = "",
                currentPage = 1,
                books = emptyList(),
                hasNextPage = true
            )
        }
        loadBooks()
    }

    // Placeholder methods - these would be implemented with actual catalog source calls
    private suspend fun loadBooksFromSource(page: Int, query: String): List<Book> {
        // TODO: Implement actual catalog source integration
        // This would call catalogSource.source.getPopularBooks() or similar
        return emptyList()
    }

    private suspend fun searchBooksFromSource(query: String): List<Book> {
        // TODO: Implement actual catalog source search
        // This would call catalogSource.source.searchBooks(query) or similar
        return emptyList()
    }

    override fun handleError(error: Throwable) {
        updateState { currentState ->
            currentState.copy(
                isLoading = false,
                isRefreshing = false,
                isLoadingMore = false,
                error = error.message ?: "Unknown error occurred"
            )
        }
        super.handleError(error)
    }
}
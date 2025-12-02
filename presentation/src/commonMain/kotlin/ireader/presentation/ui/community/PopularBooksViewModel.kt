package ireader.presentation.ui.community

import androidx.compose.runtime.Stable
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.PopularBooksRepository
import ireader.domain.models.remote.PopularBook
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Popular Books screen following Mihon's StateScreenModel pattern.
 * 
 * Uses a single immutable StateFlow<PopularBooksScreenState> instead of mutableStateOf.
 * This provides:
 * - Single source of truth for UI state
 * - Atomic state updates
 * - Better Compose performance with @Immutable state
 */
@Stable
class PopularBooksViewModel(
    private val popularBooksRepository: PopularBooksRepository,
    private val bookRepository: BookRepository
) : BaseViewModel() {
    
    private val _state = MutableStateFlow(PopularBooksScreenState())
    val state: StateFlow<PopularBooksScreenState> = _state.asStateFlow()
    
    private var lastLoadTime = 0L
    private val minLoadInterval = 2000L // 2 seconds rate limit
    private val pageSize = 10
    
    init {
        loadInitialBooks()
    }
    
    private fun loadInitialBooks() {
        scope.launch {
            _state.update { it.copy(isInitialLoading = true, error = null) }
            
            popularBooksRepository.getPopularBooks(limit = pageSize)
                .onSuccess { books ->
                    _state.update { current ->
                        current.copy(
                            books = books,
                            isInitialLoading = false,
                            hasMore = books.size >= pageSize,
                            currentPage = 1
                        )
                    }
                    lastLoadTime = System.currentTimeMillis()
                    // Lookup local books for covers
                    lookupLocalBooks(books)
                }
                .onFailure { error ->
                    _state.update { current ->
                        current.copy(
                            isInitialLoading = false,
                            error = error.message ?: "Failed to load popular books"
                        )
                    }
                }
        }
    }
    
    fun loadMore() {
        val currentState = _state.value
        if (currentState.isLoadingMore || !currentState.hasMore || currentState.isRateLimited) return
        
        // Rate limiting check
        val now = System.currentTimeMillis()
        val timeSinceLastLoad = now - lastLoadTime
        
        if (timeSinceLastLoad < minLoadInterval) {
            // Show rate limit message
            _state.update { it.copy(isRateLimited = true) }
            scope.launch {
                delay(minLoadInterval - timeSinceLastLoad)
                _state.update { it.copy(isRateLimited = false) }
                loadMore() // Retry after delay
            }
            return
        }
        
        scope.launch {
            _state.update { it.copy(isLoadingMore = true) }
            
            val nextPage = currentState.currentPage + 1
            val offset = currentState.currentPage * pageSize
            
            // For now, we'll fetch more books and skip already loaded ones
            popularBooksRepository.getPopularBooks(limit = offset + pageSize)
                .onSuccess { allBooks ->
                    val newBooks = allBooks.drop(offset)
                    _state.update { current ->
                        current.copy(
                            books = current.books + newBooks,
                            isLoadingMore = false,
                            hasMore = newBooks.size >= pageSize,
                            currentPage = nextPage
                        )
                    }
                    lastLoadTime = System.currentTimeMillis()
                }
                .onFailure { error ->
                    _state.update { current ->
                        current.copy(
                            isLoadingMore = false,
                            error = error.message ?: "Failed to load more books"
                        )
                    }
                }
        }
    }
    
    fun refresh() {
        _state.update { current ->
            current.copy(
                books = emptyList(),
                currentPage = 0,
                hasMore = true,
                error = null
            )
        }
        loadInitialBooks()
    }
    
    fun checkBookInLibrary(bookId: String, title: String, sourceId: Long, onResult: (BookNavigationAction) -> Unit) {
        scope.launch {
            _state.update { it.copy(loadingBookIds = it.loadingBookIds + bookId) }
            
            try {
                // Try to find book in local library by title and source
                val localBook = bookRepository.findDuplicateBook(title, sourceId)
                
                if (localBook != null) {
                    // Found in library - open it
                    onResult(BookNavigationAction.OpenLocalBook(localBook.id))
                } else {
                    // Not in library - open global search
                    onResult(BookNavigationAction.OpenGlobalSearch(title))
                }
            } catch (e: Exception) {
                // Fallback to global search
                onResult(BookNavigationAction.OpenGlobalSearch(title))
            } finally {
                _state.update { it.copy(loadingBookIds = it.loadingBookIds - bookId) }
            }
        }
    }
    
    private fun lookupLocalBooks(books: List<PopularBook>) {
        scope.launch {
            books.forEach { book ->
                try {
                    // Search for book in local library by title
                    val localBook = bookRepository.findDuplicateBook(
                        title = book.title,
                        sourceId = book.sourceId
                    )
                    
                    if (localBook != null) {
                        // Update book with local data
                        _state.update { current ->
                            current.copy(
                                books = current.books.map {
                                    if (it.bookId == book.bookId) {
                                        it.copy(
                                            localBookId = localBook.id,
                                            coverUrl = localBook.cover.ifEmpty { null },
                                            isInLibrary = true
                                        )
                                    } else it
                                }
                            )
                        }
                    }
                } catch (e: Exception) {
                    // Ignore lookup errors
                }
            }
        }
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

package ireader.presentation.ui.community

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.PopularBooksRepository
import ireader.domain.models.remote.PopularBook
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PopularBooksViewModel(
    private val popularBooksRepository: PopularBooksRepository,
    private val bookRepository: BookRepository
) : BaseViewModel() {
    
    var state by mutableStateOf(PopularBooksState())
        private set
    
    private var lastLoadTime = 0L
    private val minLoadInterval = 2000L // 2 seconds rate limit
    private val pageSize = 10
    
    init {
        loadInitialBooks()
    }
    
    private fun loadInitialBooks() {
        scope.launch {
            state = state.copy(isInitialLoading = true, error = null)
            
            popularBooksRepository.getPopularBooks(limit = pageSize)
                .onSuccess { books ->
                    state = state.copy(
                        books = books,
                        isInitialLoading = false,
                        hasMore = books.size >= pageSize,
                        currentPage = 1
                    )
                    lastLoadTime = System.currentTimeMillis()
                    // Lookup local books for covers
                    lookupLocalBooks(books)
                }
                .onFailure { error ->
                    state = state.copy(
                        isInitialLoading = false,
                        error = error.message ?: "Failed to load popular books"
                    )
                }
        }
    }
    
    fun loadMore() {
        if (state.isLoadingMore || !state.hasMore || state.isRateLimited) return
        
        // Rate limiting check
        val now = System.currentTimeMillis()
        val timeSinceLastLoad = now - lastLoadTime
        
        if (timeSinceLastLoad < minLoadInterval) {
            // Show rate limit message
            state = state.copy(isRateLimited = true)
            scope.launch {
                delay(minLoadInterval - timeSinceLastLoad)
                state = state.copy(isRateLimited = false)
                loadMore() // Retry after delay
            }
            return
        }
        
        scope.launch {
            state = state.copy(isLoadingMore = true)
            
            val nextPage = state.currentPage + 1
            val offset = state.currentPage * pageSize
            
            // For now, we'll fetch more books and skip already loaded ones
            popularBooksRepository.getPopularBooks(limit = offset + pageSize)
                .onSuccess { allBooks ->
                    val newBooks = allBooks.drop(offset)
                    state = state.copy(
                        books = state.books + newBooks,
                        isLoadingMore = false,
                        hasMore = newBooks.size >= pageSize,
                        currentPage = nextPage
                    )
                    lastLoadTime = System.currentTimeMillis()
                }
                .onFailure { error ->
                    state = state.copy(
                        isLoadingMore = false,
                        error = error.message ?: "Failed to load more books"
                    )
                }
        }
    }
    
    fun refresh() {
        state = state.copy(
            books = emptyList(),
            currentPage = 0,
            hasMore = true,
            error = null
        )
        loadInitialBooks()
    }
    
    fun checkBookInLibrary(bookId: String, title: String, sourceId: Long, onResult: (BookNavigationAction) -> Unit) {
        scope.launch {
            state = state.copy(loadingBookIds = state.loadingBookIds + bookId)
            
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
                state = state.copy(loadingBookIds = state.loadingBookIds - bookId)
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
                        val updatedBooks = state.books.map {
                            if (it.bookId == book.bookId) {
                                it.copy(
                                    localBookId = localBook.id,
                                    coverUrl = localBook.cover.ifEmpty { null },
                                    isInLibrary = true
                                )
                            } else it
                        }
                        state = state.copy(books = updatedBooks)
                    }
                } catch (e: Exception) {
                    // Ignore lookup errors
                }
            }
        }
    }
}

data class PopularBooksState(
    val books: List<PopularBook> = emptyList(),
    val isInitialLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRateLimited: Boolean = false,
    val hasMore: Boolean = true,
    val currentPage: Int = 0,
    val error: String? = null,
    val loadingBookIds: Set<String> = emptySet()
)

sealed class BookNavigationAction {
    data class OpenLocalBook(val bookId: Long) : BookNavigationAction()
    data class OpenGlobalSearch(val query: String) : BookNavigationAction()
    data class OpenExternalUrl(val url: String) : BookNavigationAction()
}

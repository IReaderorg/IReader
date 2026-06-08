package ireader.presentation.ui.community

import androidx.compose.runtime.Stable
import ireader.domain.catalogs.CatalogStore
import ireader.domain.data.repository.AnnouncementsRepository
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.CommunityVotesRepository
import ireader.domain.data.repository.DiscordWidgetRepository
import ireader.domain.data.repository.PopularBooksRepository
import ireader.domain.models.remote.PopularBook
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ireader.domain.utils.extensions.currentTimeToLong

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
    private val bookRepository: BookRepository,
    private val communityVotesRepository: CommunityVotesRepository? = null,
    private val announcementsRepository: AnnouncementsRepository? = null,
    private val discordWidgetRepository: DiscordWidgetRepository? = null,
    private val catalogStore: CatalogStore? = null
) : BaseViewModel() {

    private val _state = MutableStateFlow(PopularBooksScreenState())
    val state: StateFlow<PopularBooksScreenState> = _state.asStateFlow()

    private var lastLoadTime = 0L
    private val minLoadInterval = 2000L // 2 seconds rate limit
    private val pageSize = 10

    init {
        loadInitialBooks()
        loadAnnouncements()
        loadDiscordPresence()
    }

    private fun loadDiscordPresence() {
        val repo = discordWidgetRepository ?: return
        scope.launch {
            val count = repo.getOnlineCount()
            if (count != null) _state.update { it.copy(discordOnline = count) }
        }
    }

    private fun loadAnnouncements() {
        val repo = announcementsRepository ?: return
        scope.launch {
            repo.getAnnouncements(limit = 5).onSuccess { list ->
                _state.update { it.copy(announcements = list) }
            }
        }
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
                    lastLoadTime = currentTimeToLong()
                    rebuildGroups()
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
        val now = currentTimeToLong()
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
                    lastLoadTime = currentTimeToLong()
                    rebuildGroups()
                    lookupLocalBooks(newBooks)
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
                groups = emptyList(),
                currentPage = 0,
                hasMore = true,
                error = null
            )
        }
        loadInitialBooks()
    }
    
    fun checkBookInLibrary(bookId: String, title: String, sourceId: Long, sourceName: String, onResult: (BookNavigationAction) -> Unit) {
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
            rebuildGroups()
        }
    }
    
    /** Collapse duplicate titles (across sources) into single grouped entries. */
    private fun rebuildGroups() {
        _state.update { current ->
            val groups = current.books
                .groupBy { normalizeTitle(it.title) }
                .map { (key, variants) ->
                    val byReaders = variants.sortedByDescending { it.readerCount }
                    val withLocal = variants.firstOrNull { it.localBookId != null }
                    val cover = variants.firstNotNullOfOrNull { it.coverUrl }
                    PopularBookGroup(
                        key = key,
                        title = byReaders.first().title,
                        coverUrl = cover,
                        description = variants.firstNotNullOfOrNull { it.description },
                        totalReaders = variants.sumOf { it.readerCount },
                        lastRead = variants.maxOf { it.lastRead },
                        localBookId = withLocal?.localBookId,
                        sources = byReaders.map {
                            BookSourceVariant(
                                sourceId = it.sourceId,
                                sourceName = it.sourceName.ifBlank { "Source ${it.sourceId}" },
                                bookId = it.bookId,
                                bookUrl = it.bookUrl,
                                readers = it.readerCount,
                            )
                        }.distinctBy { it.sourceId },
                    )
                }
                .sortedByDescending { it.totalReaders }
            current.copy(groups = groups)
        }
    }

    fun openBookDetail(group: PopularBookGroup) {
        _state.update { it.copy(selectedBook = group) }
    }

    fun dismissBookDetail() {
        _state.update { it.copy(selectedBook = null) }
    }

    /** Open a specific source variant: local book if owned, else if source installed
     *  go to global search, else prompt to install the source. */
    fun openSource(group: PopularBookGroup, variant: BookSourceVariant, onResult: (BookNavigationAction) -> Unit) {
        scope.launch {
            _state.update { it.copy(resolvingSourceFor = group.key) }
            try {
                val local = runCatching { bookRepository.findDuplicateBook(group.title, variant.sourceId) }.getOrNull()
                when {
                    local != null -> onResult(BookNavigationAction.OpenLocalBook(local.id))
                    catalogStore?.get(variant.sourceId) != null -> onResult(BookNavigationAction.OpenGlobalSearch(group.title))
                    else -> onResult(BookNavigationAction.SourceMissing(variant.sourceName))
                }
            } finally {
                _state.update { it.copy(resolvingSourceFor = null) }
            }
        }
    }

    /** Cast a free daily Power-Stone vote for a community book (drives Trending). */
    fun vote(bookId: String) {
        val repo = communityVotesRepository ?: return
        if (_state.value.votedBookIds.contains(bookId)) return
        scope.launch {
            repo.vote(bookId).onSuccess { recorded ->
                if (recorded) _state.update { it.copy(votedBookIds = it.votedBookIds + bookId) }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

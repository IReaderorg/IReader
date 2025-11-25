package ireader.presentation.ui.community

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.domain.data.repository.AllReviewsRepository
import ireader.domain.models.remote.BookReview
import ireader.domain.models.remote.ChapterReview
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AllReviewsViewModel(
    private val allReviewsRepository: AllReviewsRepository
) : BaseViewModel() {
    
    var state by mutableStateOf(AllReviewsState())
        private set
    
    private var lastLoadTime = 0L
    private val minLoadInterval = 2000L // 2 seconds rate limit
    private val pageSize = 10
    
    init {
        loadInitialReviews()
    }
    
    private fun loadInitialReviews() {
        scope.launch {
            state = state.copy(isInitialLoading = true, error = null)
            
            when (state.selectedTab) {
                ReviewTab.BOOKS -> loadInitialBookReviews()
                ReviewTab.CHAPTERS -> loadInitialChapterReviews()
            }
        }
    }
    
    private suspend fun loadInitialBookReviews() {
        allReviewsRepository.getAllBookReviews(limit = pageSize, offset = 0)
            .onSuccess { reviews ->
                state = state.copy(
                    bookReviews = reviews,
                    isInitialLoading = false,
                    bookHasMore = reviews.size >= pageSize,
                    bookCurrentPage = 1
                )
                lastLoadTime = System.currentTimeMillis()
            }
            .onFailure { error ->
                state = state.copy(
                    isInitialLoading = false,
                    error = error.message ?: "Failed to load reviews"
                )
            }
    }
    
    private suspend fun loadInitialChapterReviews() {
        allReviewsRepository.getAllChapterReviews(limit = pageSize, offset = 0)
            .onSuccess { reviews ->
                state = state.copy(
                    chapterReviews = reviews,
                    isInitialLoading = false,
                    chapterHasMore = reviews.size >= pageSize,
                    chapterCurrentPage = 1
                )
                lastLoadTime = System.currentTimeMillis()
            }
            .onFailure { error ->
                state = state.copy(
                    isInitialLoading = false,
                    error = error.message ?: "Failed to load reviews"
                )
            }
    }
    
    fun loadMore() {
        if (state.isLoadingMore || state.isRateLimited) return
        
        when (state.selectedTab) {
            ReviewTab.BOOKS -> {
                if (!state.bookHasMore) return
                loadMoreBookReviews()
            }
            ReviewTab.CHAPTERS -> {
                if (!state.chapterHasMore) return
                loadMoreChapterReviews()
            }
        }
    }
    
    private fun loadMoreBookReviews() {
        // Rate limiting check
        val now = System.currentTimeMillis()
        val timeSinceLastLoad = now - lastLoadTime
        
        if (timeSinceLastLoad < minLoadInterval) {
            state = state.copy(isRateLimited = true)
            scope.launch {
                delay(minLoadInterval - timeSinceLastLoad)
                state = state.copy(isRateLimited = false)
                loadMoreBookReviews()
            }
            return
        }
        
        scope.launch {
            state = state.copy(isLoadingMore = true)
            
            val offset = state.bookCurrentPage * pageSize
            
            allReviewsRepository.getAllBookReviews(limit = pageSize, offset = offset)
                .onSuccess { newReviews ->
                    state = state.copy(
                        bookReviews = state.bookReviews + newReviews,
                        isLoadingMore = false,
                        bookHasMore = newReviews.size >= pageSize,
                        bookCurrentPage = state.bookCurrentPage + 1
                    )
                    lastLoadTime = System.currentTimeMillis()
                }
                .onFailure { error ->
                    state = state.copy(
                        isLoadingMore = false,
                        error = error.message ?: "Failed to load more reviews"
                    )
                }
        }
    }
    
    private fun loadMoreChapterReviews() {
        // Rate limiting check
        val now = System.currentTimeMillis()
        val timeSinceLastLoad = now - lastLoadTime
        
        if (timeSinceLastLoad < minLoadInterval) {
            state = state.copy(isRateLimited = true)
            scope.launch {
                delay(minLoadInterval - timeSinceLastLoad)
                state = state.copy(isRateLimited = false)
                loadMoreChapterReviews()
            }
            return
        }
        
        scope.launch {
            state = state.copy(isLoadingMore = true)
            
            val offset = state.chapterCurrentPage * pageSize
            
            allReviewsRepository.getAllChapterReviews(limit = pageSize, offset = offset)
                .onSuccess { newReviews ->
                    state = state.copy(
                        chapterReviews = state.chapterReviews + newReviews,
                        isLoadingMore = false,
                        chapterHasMore = newReviews.size >= pageSize,
                        chapterCurrentPage = state.chapterCurrentPage + 1
                    )
                    lastLoadTime = System.currentTimeMillis()
                }
                .onFailure { error ->
                    state = state.copy(
                        isLoadingMore = false,
                        error = error.message ?: "Failed to load more reviews"
                    )
                }
        }
    }
    
    fun switchTab(tab: ReviewTab) {
        if (state.selectedTab == tab) return
        
        state = state.copy(selectedTab = tab)
        
        // Load initial data for the new tab if empty
        when (tab) {
            ReviewTab.BOOKS -> {
                if (state.bookReviews.isEmpty() && !state.isInitialLoading) {
                    scope.launch { loadInitialBookReviews() }
                }
            }
            ReviewTab.CHAPTERS -> {
                if (state.chapterReviews.isEmpty() && !state.isInitialLoading) {
                    scope.launch { loadInitialChapterReviews() }
                }
            }
        }
    }
    
    fun refresh() {
        state = when (state.selectedTab) {
            ReviewTab.BOOKS -> state.copy(
                bookReviews = emptyList(),
                bookCurrentPage = 0,
                bookHasMore = true,
                error = null
            )
            ReviewTab.CHAPTERS -> state.copy(
                chapterReviews = emptyList(),
                chapterCurrentPage = 0,
                chapterHasMore = true,
                error = null
            )
        }
        loadInitialReviews()
    }
}

data class AllReviewsState(
    val bookReviews: List<BookReview> = emptyList(),
    val chapterReviews: List<ChapterReview> = emptyList(),
    val selectedTab: ReviewTab = ReviewTab.BOOKS,
    val isInitialLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRateLimited: Boolean = false,
    val bookHasMore: Boolean = true,
    val chapterHasMore: Boolean = true,
    val bookCurrentPage: Int = 0,
    val chapterCurrentPage: Int = 0,
    val error: String? = null
)

enum class ReviewTab {
    BOOKS, CHAPTERS
}

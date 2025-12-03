package ireader.presentation.ui.community

import androidx.compose.runtime.Stable
import ireader.domain.data.repository.AllReviewsRepository
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * ViewModel for the All Reviews screen following Mihon's StateScreenModel pattern.
 * 
 * Uses a single immutable StateFlow<AllReviewsScreenState> instead of mutableStateOf.
 * This provides:
 * - Single source of truth for UI state
 * - Atomic state updates
 * - Better Compose performance with @Immutable state
 */
@Stable
class AllReviewsViewModel(
    private val allReviewsRepository: AllReviewsRepository
) : BaseViewModel() {
    
    private val _state = MutableStateFlow(AllReviewsScreenState())
    val state: StateFlow<AllReviewsScreenState> = _state.asStateFlow()
    
    private var lastLoadTime = 0L
    private val minLoadInterval = 2000L // 2 seconds rate limit
    private val pageSize = 10
    
    init {
        loadInitialReviews()
    }
    
    private fun loadInitialReviews() {
        scope.launch {
            _state.update { it.copy(isInitialLoading = true, error = null) }
            
            when (_state.value.selectedTab) {
                ReviewTab.BOOKS -> loadInitialBookReviews()
                ReviewTab.CHAPTERS -> loadInitialChapterReviews()
            }
        }
    }
    
    private suspend fun loadInitialBookReviews() {
        allReviewsRepository.getAllBookReviews(limit = pageSize, offset = 0)
            .onSuccess { reviews ->
                _state.update { current ->
                    current.copy(
                        bookReviews = reviews,
                        isInitialLoading = false,
                        bookHasMore = reviews.size >= pageSize,
                        bookCurrentPage = 1
                    )
                }
                lastLoadTime = currentTimeToLong()
            }
            .onFailure { error ->
                _state.update { current ->
                    current.copy(
                        isInitialLoading = false,
                        error = error.message ?: "Failed to load reviews"
                    )
                }
            }
    }
    
    private suspend fun loadInitialChapterReviews() {
        allReviewsRepository.getAllChapterReviews(limit = pageSize, offset = 0)
            .onSuccess { reviews ->
                _state.update { current ->
                    current.copy(
                        chapterReviews = reviews,
                        isInitialLoading = false,
                        chapterHasMore = reviews.size >= pageSize,
                        chapterCurrentPage = 1
                    )
                }
                lastLoadTime = currentTimeToLong()
            }
            .onFailure { error ->
                _state.update { current ->
                    current.copy(
                        isInitialLoading = false,
                        error = error.message ?: "Failed to load reviews"
                    )
                }
            }
    }
    
    fun loadMore() {
        val currentState = _state.value
        if (currentState.isLoadingMore || currentState.isRateLimited) return
        
        when (currentState.selectedTab) {
            ReviewTab.BOOKS -> {
                if (!currentState.bookHasMore) return
                loadMoreBookReviews()
            }
            ReviewTab.CHAPTERS -> {
                if (!currentState.chapterHasMore) return
                loadMoreChapterReviews()
            }
        }
    }
    
    private fun loadMoreBookReviews() {
        // Rate limiting check
        val now = currentTimeToLong()
        val timeSinceLastLoad = now - lastLoadTime
        
        if (timeSinceLastLoad < minLoadInterval) {
            _state.update { it.copy(isRateLimited = true) }
            scope.launch {
                delay(minLoadInterval - timeSinceLastLoad)
                _state.update { it.copy(isRateLimited = false) }
                loadMoreBookReviews()
            }
            return
        }
        
        scope.launch {
            _state.update { it.copy(isLoadingMore = true) }
            
            val offset = _state.value.bookCurrentPage * pageSize
            
            allReviewsRepository.getAllBookReviews(limit = pageSize, offset = offset)
                .onSuccess { newReviews ->
                    _state.update { current ->
                        current.copy(
                            bookReviews = current.bookReviews + newReviews,
                            isLoadingMore = false,
                            bookHasMore = newReviews.size >= pageSize,
                            bookCurrentPage = current.bookCurrentPage + 1
                        )
                    }
                    lastLoadTime = currentTimeToLong()
                }
                .onFailure { error ->
                    _state.update { current ->
                        current.copy(
                            isLoadingMore = false,
                            error = error.message ?: "Failed to load more reviews"
                        )
                    }
                }
        }
    }
    
    private fun loadMoreChapterReviews() {
        // Rate limiting check
        val now = currentTimeToLong()
        val timeSinceLastLoad = now - lastLoadTime
        
        if (timeSinceLastLoad < minLoadInterval) {
            _state.update { it.copy(isRateLimited = true) }
            scope.launch {
                delay(minLoadInterval - timeSinceLastLoad)
                _state.update { it.copy(isRateLimited = false) }
                loadMoreChapterReviews()
            }
            return
        }
        
        scope.launch {
            _state.update { it.copy(isLoadingMore = true) }
            
            val offset = _state.value.chapterCurrentPage * pageSize
            
            allReviewsRepository.getAllChapterReviews(limit = pageSize, offset = offset)
                .onSuccess { newReviews ->
                    _state.update { current ->
                        current.copy(
                            chapterReviews = current.chapterReviews + newReviews,
                            isLoadingMore = false,
                            chapterHasMore = newReviews.size >= pageSize,
                            chapterCurrentPage = current.chapterCurrentPage + 1
                        )
                    }
                    lastLoadTime = currentTimeToLong()
                }
                .onFailure { error ->
                    _state.update { current ->
                        current.copy(
                            isLoadingMore = false,
                            error = error.message ?: "Failed to load more reviews"
                        )
                    }
                }
        }
    }
    
    fun switchTab(tab: ReviewTab) {
        if (_state.value.selectedTab == tab) return
        
        _state.update { it.copy(selectedTab = tab) }
        
        // Load initial data for the new tab if empty
        when (tab) {
            ReviewTab.BOOKS -> {
                if (_state.value.bookReviews.isEmpty() && !_state.value.isInitialLoading) {
                    scope.launch { loadInitialBookReviews() }
                }
            }
            ReviewTab.CHAPTERS -> {
                if (_state.value.chapterReviews.isEmpty() && !_state.value.isInitialLoading) {
                    scope.launch { loadInitialChapterReviews() }
                }
            }
        }
    }
    
    fun refresh() {
        _state.update { current ->
            when (current.selectedTab) {
                ReviewTab.BOOKS -> current.copy(
                    bookReviews = emptyList(),
                    bookCurrentPage = 0,
                    bookHasMore = true,
                    error = null
                )
                ReviewTab.CHAPTERS -> current.copy(
                    chapterReviews = emptyList(),
                    chapterCurrentPage = 0,
                    chapterHasMore = true,
                    error = null
                )
            }
        }
        loadInitialReviews()
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

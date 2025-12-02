package ireader.presentation.ui.community

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import ireader.domain.models.remote.BookReview
import ireader.domain.models.remote.ChapterReview

/**
 * Tab options for reviews
 */
enum class ReviewTab {
    BOOKS, CHAPTERS
}

/**
 * Immutable state for the All Reviews screen following Mihon's StateScreenModel pattern.
 */
@Immutable
data class AllReviewsScreenState(
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
) {
    @Stable
    val isEmpty: Boolean get() = when (selectedTab) {
        ReviewTab.BOOKS -> bookReviews.isEmpty() && !isInitialLoading
        ReviewTab.CHAPTERS -> chapterReviews.isEmpty() && !isInitialLoading
    }
    
    @Stable
    val isInitialLoadingState: Boolean get() = isInitialLoading && when (selectedTab) {
        ReviewTab.BOOKS -> bookReviews.isEmpty()
        ReviewTab.CHAPTERS -> chapterReviews.isEmpty()
    }
    
    @Stable
    val hasContent: Boolean get() = when (selectedTab) {
        ReviewTab.BOOKS -> bookReviews.isNotEmpty()
        ReviewTab.CHAPTERS -> chapterReviews.isNotEmpty()
    }
    
    @Stable
    val currentHasMore: Boolean get() = when (selectedTab) {
        ReviewTab.BOOKS -> bookHasMore
        ReviewTab.CHAPTERS -> chapterHasMore
    }
}

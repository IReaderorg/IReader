package ireader.presentation.ui.community

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import ireader.domain.models.remote.PopularBook

/**
 * Immutable state for the Popular Books screen following Mihon's StateScreenModel pattern.
 */
@Immutable
data class PopularBooksScreenState(
    val books: List<PopularBook> = emptyList(),
    val isInitialLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRateLimited: Boolean = false,
    val hasMore: Boolean = true,
    val currentPage: Int = 0,
    val error: String? = null,
    val loadingBookIds: Set<String> = emptySet()
) {
    @Stable
    val isEmpty: Boolean get() = books.isEmpty() && !isInitialLoading
    
    @Stable
    val isInitialLoadingState: Boolean get() = isInitialLoading && books.isEmpty()
    
    @Stable
    val hasContent: Boolean get() = books.isNotEmpty()
}

/**
 * Navigation actions for book items
 */
sealed class BookNavigationAction {
    data class OpenLocalBook(val bookId: Long) : BookNavigationAction()
    data class OpenGlobalSearch(val query: String) : BookNavigationAction()
    data class OpenExternalUrl(val url: String) : BookNavigationAction()
}

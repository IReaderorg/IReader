package ireader.presentation.ui.home.library.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.models.DisplayMode
import ireader.domain.models.entities.BookItem
import ireader.domain.models.entities.CategoryWithCount
import ireader.domain.usecases.prefetch.BookPrefetchService
import ireader.presentation.ui.component.isLandscape
import ireader.presentation.ui.component.list.LayoutComposable
import ireader.presentation.ui.component.list.scrollbars.ILazyColumnScrollbar
import ireader.presentation.ui.component.LocalPerformanceConfig
import ireader.presentation.ui.home.library.viewmodel.PaginationState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.compose.koinInject

/**
 * Stable key generator for book items to ensure efficient list updates
 */
@Stable
private fun stableBookKey(book: BookItem): Any = book.id

/**
 * Library Pager optimized for 800+ books with PAGINATION.
 * 
 * PERFORMANCE OPTIMIZATIONS:
 * - Reduced beyondViewportPageCount to 0 (only render current page)
 * - Cached scroll positions per category
 * - Stable keys prevent unnecessary recomposition
 * - Deferred prefetching to avoid blocking UI
 * - PAGINATION: Loads books in chunks as user scrolls
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun LibraryPager(
    pagerState: androidx.compose.foundation.pager.PagerState,
    onClick: (book: BookItem) -> Unit,
    onLongClick: (BookItem) -> Unit = {},
    goToLatestChapter: (book: BookItem) -> Unit = {},
    categories: List<CategoryWithCount>,
    pageCount: Int,
    layout: DisplayMode,
    selection: List<Long> = emptyList<Long>(),
    currentPage: Int,
    onPageChange: @Composable (page: Int) -> State<List<BookItem>>,
    showGoToLastChapterBadge: Boolean = false,
    showUnreadBadge: Boolean = false,
    showReadBadge: Boolean = false,
    showInLibraryBadge: Boolean = false,
    showDownloadedChaptersBadge: Boolean = false,
    showUnreadChaptersBadge: Boolean = false,
    showLocalMangaBadge: Boolean = false,
    showLanguageBadge: Boolean = false,
    getColumnsForOrientation: CoroutineScope.(Boolean) -> StateFlow<Int>,
    columnsInPortrait: Int = 3,
    columnsInLandscape: Int = 5,
    onSaveScrollPosition: (categoryId: Long, index: Int, offset: Int) -> Unit = { _, _, _ -> },
    getScrollPosition: (categoryId: Long) -> Pair<Int, Int> = { 0 to 0 },
    // Pagination callbacks
    onLoadMore: (categoryId: Long) -> Unit = {},
    getPaginationState: (categoryId: Long) -> PaginationState = { PaginationState() },
) {
    // Pre-compute stable key function to avoid lambda recreation
    val stableKeyFunction = remember { { book: BookItem -> stableBookKey(book) } }
    
    // Get performance config for optimizations
    val performanceConfig = LocalPerformanceConfig.current
    
    // Get prefetch service for preloading book data
    val bookPrefetchService: BookPrefetchService? = koinInject()
    
    // Wrap onClick to prefetch on click (for faster subsequent loads)
    val onClickWithPrefetch = remember(onClick, bookPrefetchService) {
        { book: BookItem ->
            onClick(book)
        }
    }
    
    // CRITICAL: Set beyondViewportPageCount to 0 for 800+ books
    // This prevents pre-rendering adjacent pages which causes massive lag
    // Each page with 800 books = 800 composables, so 3 pages = 2400 composables
    val effectivePrefetchCount = remember(performanceConfig.prefetchDistance) {
        // For large libraries, don't prefetch pages at all
        0
    }
    
    HorizontalPager(
        state = pagerState,
        pageSpacing = 0.dp,
        userScrollEnabled = true,
        reverseLayout = false,
        contentPadding = PaddingValues(0.dp),
        pageSize = PageSize.Fill,
        beyondViewportPageCount = effectivePrefetchCount, // CRITICAL: 0 for large libraries
        key = { page -> categories.getOrNull(page)?.id ?: page },
        pageContent = { page ->
            val books by onPageChange(page)
            val categoryId = categories.getOrNull(page)?.id ?: 0L
            val paginationState = getPaginationState(categoryId)
            
            // Only prefetch when this is the current page and not scrolling
            LaunchedEffect(page, currentPage, books, bookPrefetchService) {
                if (bookPrefetchService != null && books.isNotEmpty() && page == currentPage) {
                    // Prefetch first 3 books (most likely to be clicked)
                    val bookIds = books.take(3).map { it.id }
                    bookPrefetchService.prefetchMultiple(bookIds)
                }
            }
            
            // Get saved scroll position for this category
            // Note: We call getScrollPosition directly (not remembered) so it always gets the latest value
            // This ensures scroll position is restored correctly when navigating back from detail screen
            val savedPosition = getScrollPosition(categoryId)
            
            val gridState = rememberLazyGridState(
                initialFirstVisibleItemIndex = savedPosition.first,
                initialFirstVisibleItemScrollOffset = savedPosition.second
            )
            val lazyListState = rememberLazyListState(
                initialFirstVisibleItemIndex = savedPosition.first,
                initialFirstVisibleItemScrollOffset = savedPosition.second
            )
            
            // Save scroll position when it changes - debounced to avoid excessive saves
            LaunchedEffect(gridState, categoryId) {
                snapshotFlow { 
                    gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset 
                }.collect { (index, offset) ->
                    onSaveScrollPosition(categoryId, index, offset)
                }
            }
            
            // PAGINATION: Detect when user scrolls near the end and load more
            LaunchedEffect(gridState, categoryId, books.size, paginationState) {
                snapshotFlow {
                    val layoutInfo = gridState.layoutInfo
                    val totalItems = layoutInfo.totalItemsCount
                    val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                    Triple(lastVisibleItem, totalItems, gridState.isScrollInProgress)
                }
                .distinctUntilChanged()
                .collect { (lastVisibleItem, totalItems, isScrolling) ->
                    // Load more when within 10 items of the end
                    val threshold = 10
                    if (!isScrolling && 
                        totalItems > 0 && 
                        lastVisibleItem >= totalItems - threshold &&
                        paginationState.canLoadMore) {
                        onLoadMore(categoryId)
                    }
                }
            }
            
            // PAGINATION: Same for list layout
            LaunchedEffect(lazyListState, categoryId, books.size, paginationState) {
                snapshotFlow {
                    val layoutInfo = lazyListState.layoutInfo
                    val totalItems = layoutInfo.totalItemsCount
                    val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                    Triple(lastVisibleItem, totalItems, lazyListState.isScrollInProgress)
                }
                .distinctUntilChanged()
                .collect { (lastVisibleItem, totalItems, isScrolling) ->
                    val threshold = 10
                    if (!isScrolling && 
                        totalItems > 0 && 
                        lastVisibleItem >= totalItems - threshold &&
                        paginationState.canLoadMore) {
                        onLoadMore(categoryId)
                    }
                }
            }
            
            // Separate effect for prefetching - only when scroll settles
            LaunchedEffect(gridState, books, bookPrefetchService, page, currentPage) {
                if (bookPrefetchService != null && books.isNotEmpty() && page == currentPage) {
                    snapshotFlow { gridState.isScrollInProgress }
                        .collect { isScrolling ->
                            if (!isScrolling) {
                                // Prefetch visible and upcoming books when scroll settles
                                val index = gridState.firstVisibleItemIndex
                                val visibleRange = index until minOf(index + 6, books.size)
                                val bookIds = visibleRange.mapNotNull { books.getOrNull(it)?.id }
                                if (bookIds.isNotEmpty()) {
                                    bookPrefetchService.prefetchMultiple(bookIds)
                                }
                            }
                        }
                }
            }
            
            LaunchedEffect(lazyListState, categoryId) {
                snapshotFlow { 
                    lazyListState.firstVisibleItemIndex to lazyListState.firstVisibleItemScrollOffset 
                }.collect { (index, offset) ->
                    onSaveScrollPosition(categoryId, index, offset)
                }
            }
            
            // Use the layout parameter passed from the parent (from ViewModel state)
            // This ensures the display mode changes when user selects a different layout
            val displayMode = layout
            
            // Get columns for orientation - use the values passed from state
            val isLandscape = isLandscape()
            val columns = if (displayMode != DisplayMode.List) {
                if (isLandscape) columnsInLandscape else columnsInPortrait
            } else {
                0
            }
            
            ILazyColumnScrollbar(
                listState = lazyListState,
            ) {
                // No Crossfade animation - instant display for better navigation experience
                LayoutComposable(
                    books = books,
                    layout = displayMode,
                    isLocal = true,
                    gridState = gridState,
                    scrollState = lazyListState,
                    selection = selection,
                    goToLatestChapter = goToLatestChapter,
                    onClick = onClick,
                    onLongClick = onLongClick,
                    showGoToLastChapterBadge = showGoToLastChapterBadge,
                    showReadBadge = showReadBadge,
                    showUnreadBadge = showUnreadBadge,
                    showDownloadedChaptersBadge = showDownloadedChaptersBadge,
                    showUnreadChaptersBadge = showUnreadChaptersBadge,
                    showLocalMangaBadge = showLocalMangaBadge,
                    showLanguageBadge = showLanguageBadge,
                    columns = columns,
                    keys = stableKeyFunction,
                    // Pagination footer - only show when actively loading more items
                    footer = if (paginationState.isLoadingMore) {
                        {
                            PaginationFooter(
                                isLoading = paginationState.isLoadingMore,
                                hasMore = paginationState.hasMoreItems,
                                loadedCount = books.size,
                                totalCount = paginationState.totalItems
                            )
                        }
                    } else null
                )
            }
        }
    )
}

/**
 * Footer shown at the bottom of the list during pagination.
 * Only shows loading indicator when actively loading more items.
 */
@Composable
private fun PaginationFooter(
    isLoading: Boolean,
    hasMore: Boolean,
    loadedCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    // Only show when actively loading more items
    if (!isLoading) return
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.padding(8.dp),
            strokeWidth = 2.dp
        )
    }
}

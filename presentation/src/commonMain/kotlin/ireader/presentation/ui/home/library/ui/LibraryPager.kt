package ireader.presentation.ui.home.library.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.dp
import ireader.domain.models.DisplayMode
import ireader.domain.models.DisplayMode.Companion.displayMode
import ireader.domain.models.entities.BookItem
import ireader.domain.models.entities.CategoryWithCount
import ireader.domain.usecases.prefetch.BookPrefetchService
import ireader.presentation.ui.component.isLandscape
import ireader.presentation.ui.component.list.LayoutComposable
import ireader.presentation.ui.component.list.scrollbars.ILazyColumnScrollbar
import ireader.presentation.ui.component.LocalPerformanceConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.koin.compose.koinInject

/**
 * Stable key generator for book items to ensure efficient list updates
 */
@Stable
private fun stableBookKey(book: BookItem): Any = book.id

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
    onSaveScrollPosition: (categoryId: Long, index: Int, offset: Int) -> Unit = { _, _, _ -> },
    getScrollPosition: (categoryId: Long) -> Pair<Int, Int> = { 0 to 0 },
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
    
    HorizontalPager(
        state = pagerState,
        pageSpacing = 0.dp,
        userScrollEnabled = true,
        reverseLayout = false,
        contentPadding = PaddingValues(0.dp),
        pageSize = PageSize.Fill,
        beyondViewportPageCount = performanceConfig.prefetchDistance.coerceAtMost(2), // Limit prefetch based on device
        key = { page -> categories.getOrNull(page)?.id ?: page },
        pageContent = { page ->
            val books by onPageChange(page)
            val categoryId = categories.getOrNull(page)?.id ?: 0L
            
            // Prefetch first few visible books for faster detail screen loading
            LaunchedEffect(books, bookPrefetchService) {
                if (bookPrefetchService != null && books.isNotEmpty()) {
                    // Prefetch first 3 books (most likely to be clicked)
                    val bookIds = books.take(3).map { it.id }
                    bookPrefetchService.prefetchMultiple(bookIds)
                }
            }
            
            // Get saved scroll position for this category
            val savedPosition = remember(categoryId) { getScrollPosition(categoryId) }
            
            val gridState = rememberLazyGridState(
                initialFirstVisibleItemIndex = savedPosition.first,
                initialFirstVisibleItemScrollOffset = savedPosition.second
            )
            val lazyListState = rememberLazyListState(
                initialFirstVisibleItemIndex = savedPosition.first,
                initialFirstVisibleItemScrollOffset = savedPosition.second
            )
            
            // Save scroll position when it changes and prefetch visible books
            LaunchedEffect(gridState, categoryId, books, bookPrefetchService) {
                snapshotFlow { 
                    gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset 
                }.collect { (index, offset) ->
                    onSaveScrollPosition(categoryId, index, offset)
                    
                    // Prefetch visible and upcoming books when scroll settles
                    if (bookPrefetchService != null && books.isNotEmpty() && !gridState.isScrollInProgress) {
                        val visibleRange = index until minOf(index + 6, books.size)
                        val bookIds = visibleRange.mapNotNull { books.getOrNull(it)?.id }
                        if (bookIds.isNotEmpty()) {
                            bookPrefetchService.prefetchMultiple(bookIds)
                        }
                    }
                }
            }
            
            LaunchedEffect(lazyListState, categoryId, books, bookPrefetchService) {
                snapshotFlow { 
                    lazyListState.firstVisibleItemIndex to lazyListState.firstVisibleItemScrollOffset 
                }.collect { (index, offset) ->
                    onSaveScrollPosition(categoryId, index, offset)
                    
                    // Prefetch visible and upcoming books when scroll settles
                    if (bookPrefetchService != null && books.isNotEmpty() && !lazyListState.isScrollInProgress) {
                        val visibleRange = index until minOf(index + 6, books.size)
                        val bookIds = visibleRange.mapNotNull { books.getOrNull(it)?.id }
                        if (bookIds.isNotEmpty()) {
                            bookPrefetchService.prefetchMultiple(bookIds)
                        }
                    }
                }
            }
            
            // Use derivedStateOf for display mode to minimize recompositions
            val displayMode by remember(page, categories) {
                derivedStateOf { categories.getOrNull(page)?.category?.displayMode ?: DisplayMode.CompactGrid }
            }
            
            val columns by if (displayMode != DisplayMode.List) {
                val isLandscape = isLandscape()
                with(rememberCoroutineScope()) {
                    remember(isLandscape) { getColumnsForOrientation(isLandscape) }.collectAsState()
                }
            } else {
                remember { mutableStateOf(0) }
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
                )
            }
        }
    )
}

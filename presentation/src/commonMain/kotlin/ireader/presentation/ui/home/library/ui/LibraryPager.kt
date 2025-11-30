package ireader.presentation.ui.home.library.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import ireader.domain.models.DisplayMode
import ireader.domain.models.DisplayMode.Companion.displayMode
import ireader.domain.models.entities.BookItem
import ireader.domain.models.entities.CategoryWithCount
import ireader.presentation.ui.component.isLandscape
import ireader.presentation.ui.component.list.LayoutComposable
import ireader.presentation.ui.component.list.scrollbars.ILazyColumnScrollbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

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

    ) {
    // Pre-compute stable key function to avoid lambda recreation
    val stableKeyFunction = remember { { book: BookItem -> stableBookKey(book) } }
    
    HorizontalPager(
        state = pagerState,
        pageSpacing = 0.dp,
        userScrollEnabled = true,
        reverseLayout = false,
        contentPadding = PaddingValues(0.dp),
        pageSize = PageSize.Fill,
        key = { page -> categories.getOrNull(page)?.id ?: page },
        pageContent = { page ->
            val books by onPageChange(page)
            val gridState = rememberLazyGridState()
            val lazyListState = rememberLazyListState()
            
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
                androidx.compose.animation.Crossfade(
                    targetState = displayMode,
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 300)
                ) { currentDisplayMode ->
                    LayoutComposable(
                        books = books,
                        layout = currentDisplayMode,
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
        }
    )
}

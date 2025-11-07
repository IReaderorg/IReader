package ireader.presentation.ui.home.library.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
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
    HorizontalPager(
        state = pagerState,
        pageSpacing = 0.dp,
        userScrollEnabled = true,
        reverseLayout = false,
        contentPadding = PaddingValues(0.dp),
        pageSize = PageSize.Fill,
        key = null,
        pageContent = { page ->
            val books by onPageChange(page)
            val gridState = rememberLazyGridState()
            val lazyListState = rememberLazyListState()
            val displayMode = categories[page].category.displayMode
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
                LayoutComposable(
                    books = books,
                    layout = layout,
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
                )
            }
        }
    )
}

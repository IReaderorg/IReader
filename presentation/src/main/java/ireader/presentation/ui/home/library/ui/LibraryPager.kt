package ireader.presentation.ui.home.library.ui

import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalConfiguration
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import ireader.common.models.entities.BookItem
import ireader.common.models.entities.CategoryWithCount
import ireader.domain.models.DisplayMode
import ireader.domain.models.DisplayMode.Companion.displayMode
import ireader.presentation.ui.component.list.LayoutComposable
import ireader.presentation.ui.component.list.scrollbars.LazyColumnScrollbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalPagerApi::class)
@Composable
internal fun LibraryPager(
    pagerState: PagerState,
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
    getColumnsForOrientation: CoroutineScope.(Boolean) -> StateFlow<Int>,

    ) {
    HorizontalPager(
        count = pageCount,
        state = pagerState,
    ) { page ->
        val books by onPageChange(page)
        val gridState = rememberLazyGridState()
        val lazyListState = rememberLazyListState()
        val displayMode = categories[page].category.displayMode
        val columns by if (displayMode != DisplayMode.List) {
            val window = LocalConfiguration.current
            val isLandscape = window.screenWidthDp > window.screenHeightDp

            with(rememberCoroutineScope()) {
                remember(isLandscape) { getColumnsForOrientation(isLandscape) }.collectAsState()
            }
        } else {
            remember { mutableStateOf(0) }
        }
        LazyColumnScrollbar(
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
                columns = columns,
            )
        }
    }
}

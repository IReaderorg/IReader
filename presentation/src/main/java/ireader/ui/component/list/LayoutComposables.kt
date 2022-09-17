package ireader.ui.component.list

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ireader.common.models.entities.BookItem
import ireader.core.api.source.Source
import ireader.domain.models.DisplayMode
import ireader.ui.component.list.layouts.ComfortableGridLayout
import ireader.ui.component.list.layouts.CompactGridLayoutComposable
import ireader.ui.component.list.layouts.CoverOnlyGrid
import ireader.ui.component.list.layouts.LinearListDisplay


@Composable
fun LayoutComposable(
    books: List<BookItem> = emptyList(),
    onClick: (book: BookItem) -> Unit,
    onLongClick: (BookItem) -> Unit = {},
    selection: List<Long> = emptyList<Long>(),
    layout: DisplayMode,
    scrollState: LazyListState,
    gridState: LazyGridState,
    source: Source? = null,
    isLocal: Boolean,
    goToLatestChapter: (book: BookItem) -> Unit = {},
    isLoading: Boolean = false,
    showGoToLastChapterBadge: Boolean = false,
    showUnreadBadge: Boolean = false,
    showReadBadge: Boolean = false,
    showInLibraryBadge: Boolean = false,
    columns: Int? = null,
    headers: ((url: String) -> okhttp3.Headers?)? = null,
    keys: ((item: BookItem) -> Any) = {
        it.id
    }

) {
    when (layout) {
        DisplayMode.ComfortableGrid -> {
            ComfortableGridLayout(
                books = books,
                onClick = { book ->
                    onClick(book)
                },
                selection = selection,
                onLongClick = { onLongClick(it) },
                scrollState = gridState,
                goToLatestChapter = { goToLatestChapter(it) },
                isLoading = isLoading,
                showGoToLastChapterBadge = showGoToLastChapterBadge,
                modifier = Modifier,
                showInLibraryBadge = showInLibraryBadge,
                showReadBadge = showReadBadge,
                showUnreadBadge = showUnreadBadge,
                headers = headers,
                columns = columns ?: 3,
                keys = keys
            )
        }
        DisplayMode.List -> {
            LinearListDisplay(
                books = books, onClick = { book ->
                    onClick(book)
                }, scrollState = scrollState,
                isLocal = isLocal,
                selection = selection,
                onLongClick = { onLongClick(it) },
                goToLatestChapter = { goToLatestChapter(it) },
                isLoading = isLoading,
                showGoToLastChapterBadge = showGoToLastChapterBadge,
                showInLibraryBadge = showInLibraryBadge,
                showReadBadge = showReadBadge,
                showUnreadBadge = showUnreadBadge,
                headers = headers,
                keys = keys
            )
        }
        DisplayMode.CompactGrid -> {
            CompactGridLayoutComposable(
                books = books,
                onClick = { book ->
                    onClick(book)
                }, scrollState = gridState,
                isLocal = isLocal,
                selection = selection,
                onLongClick = { onLongClick(it) },
                goToLatestChapter = { goToLatestChapter(it) },
                isLoading = isLoading,
                showGoToLastChapterBadge = showGoToLastChapterBadge,
                modifier = Modifier,
                showInLibraryBadge = showInLibraryBadge,
                showReadBadge = showReadBadge,
                showUnreadBadge = showUnreadBadge,
                columns = columns ?: 2,
                keys = keys
            )
        }
        else -> {
            CoverOnlyGrid(
                books = books,
                onClick = { book ->
                    onClick(book)
                }, scrollState = gridState,
                isLocal = isLocal,
                selection = selection,
                onLongClick = { onLongClick(it) },
                goToLatestChapter = { goToLatestChapter(it) },
                isLoading = isLoading,
                showGoToLastChapterBadge = showGoToLastChapterBadge,
                modifier = Modifier,
                showInLibraryBadge = showInLibraryBadge,
                showReadBadge = showReadBadge,
                showUnreadBadge = showUnreadBadge,
                columns = columns ?: 2,
                keys = keys
            )
        }
    }
}

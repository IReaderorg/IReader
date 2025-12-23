package ireader.presentation.ui.home.library.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ireader.domain.models.DisplayMode
import ireader.domain.models.entities.BookItem
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.no_results_found
import ireader.presentation.ui.component.list.layouts.ComfortableGridLayout
import ireader.presentation.ui.component.list.layouts.CompactGridLayoutComposable
import ireader.presentation.ui.component.list.layouts.LinearListDisplay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * Displays search results in a grid layout.
 * Used when the user is searching in the library.
 */
@Composable
internal fun LibrarySearchResults(
    books: List<BookItem>,
    layout: DisplayMode,
    selection: List<Long>,
    onClick: (BookItem) -> Unit,
    onLongClick: (BookItem) -> Unit,
    goToLatestChapter: (BookItem) -> Unit,
    showUnreadBadge: Boolean,
    showReadBadge: Boolean,
    showGoToLastChapterBadge: Boolean,
    getColumnsForOrientation: CoroutineScope.(Boolean) -> StateFlow<Int>,
    columnsInPortrait: Int,
    columnsInLandscape: Int
) {
    val gridState = rememberLazyGridState()
    val listState = rememberLazyListState()
    
    if (books.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = localize(Res.string.no_results_found),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }
    
    // Stable key function
    val keyFn: (BookItem) -> Any = remember { { book: BookItem -> book.id } }
    
    when (layout) {
        DisplayMode.CompactGrid -> {
            CompactGridLayoutComposable(
                books = books,
                selection = selection,
                onClick = onClick,
                onLongClick = onLongClick,
                scrollState = gridState,
                isLocal = true,
                goToLatestChapter = goToLatestChapter,
                showUnreadBadge = showUnreadBadge,
                showReadBadge = showReadBadge,
                showGoToLastChapterBadge = showGoToLastChapterBadge,
                columns = columnsInPortrait,
                keys = keyFn
            )
        }
        DisplayMode.ComfortableGrid -> {
            ComfortableGridLayout(
                books = books,
                selection = selection,
                onClick = onClick,
                onLongClick = onLongClick,
                scrollState = gridState,
                goToLatestChapter = goToLatestChapter,
                showUnreadBadge = showUnreadBadge,
                showReadBadge = showReadBadge,
                showGoToLastChapterBadge = showGoToLastChapterBadge,
                columns = columnsInPortrait,
                keys = keyFn
            )
        }
        DisplayMode.OnlyCover -> {
            ireader.presentation.ui.component.list.layouts.CoverOnlyGrid(
                books = books,
                selection = selection,
                onClick = onClick,
                onLongClick = onLongClick,
                scrollState = gridState,
                isLocal = true,
                goToLatestChapter = goToLatestChapter,
                showUnreadBadge = showUnreadBadge,
                showReadBadge = showReadBadge,
                showGoToLastChapterBadge = showGoToLastChapterBadge,
                columns = columnsInPortrait,
                keys = keyFn
            )
        }
        DisplayMode.List -> {
            LinearListDisplay(
                books = books,
                selection = selection,
                onClick = onClick,
                onLongClick = onLongClick,
                scrollState = listState,
                isLocal = true,
                goToLatestChapter = goToLatestChapter,
                showUnreadBadge = showUnreadBadge,
                showReadBadge = showReadBadge,
                showGoToLastChapterBadge = showGoToLastChapterBadge,
                keys = keyFn
            )
        }
    }
}

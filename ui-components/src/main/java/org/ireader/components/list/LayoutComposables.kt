package org.ireader.components.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import org.ireader.common_models.LayoutType
import org.ireader.common_models.entities.BookItem
import org.ireader.components.list.layouts.CompactGridLayoutComposable
import org.ireader.components.list.layouts.GridLayoutComposable
import org.ireader.components.list.layouts.LinearListDisplay
import org.ireader.core_api.source.Source

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LayoutComposable(
    navController: NavController,
    books: List<BookItem> = emptyList(),
    onClick: (book: BookItem) -> Unit,
    onLongClick: (BookItem) -> Unit = {},
    selection: List<Long> = emptyList<Long>(),
    layout: LayoutType,
    scrollState: LazyListState,
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState,
    source: Source? = null,
    isLocal: Boolean,
    goToLatestChapter: (book: BookItem) -> Unit = {},
    isLoading: Boolean = false,
    onEndReachValidator: (itemIndex: Int) -> Unit = {},
) {

    when (layout) {
        is LayoutType.GridLayout -> {
            GridLayoutComposable(
                books = books,
                onClick = { book ->
                    onClick(book)
                },
                selection = selection,
                onLongClick = { onLongClick(it) },
                scrollState = gridState,
                isLocal = isLocal,
                goToLatestChapter = { goToLatestChapter(it) },
                onEndReach = onEndReachValidator,
                isLoading = isLoading
            )
        }
        is LayoutType.ListLayout -> {
            LinearListDisplay(
                books = books, onClick = { book ->
                    onClick(book)
                }, scrollState = scrollState,
                isLocal = isLocal,
                selection = selection,
                onLongClick = { onLongClick(it) },
                goToLatestChapter = { goToLatestChapter(it) },
                onEndReach = onEndReachValidator,
                isLoading = isLoading
            )
        }
        is LayoutType.CompactGrid -> {
            CompactGridLayoutComposable(
                books = books,
                onClick = { book ->
                    onClick(book)
                }, scrollState = gridState,
                isLocal = isLocal,
                selection = selection,
                onLongClick = { onLongClick(it) },
                goToLatestChapter = { goToLatestChapter(it) },
                onEndReach = onEndReachValidator,
                isLoading = isLoading
            )
        }
    }
}

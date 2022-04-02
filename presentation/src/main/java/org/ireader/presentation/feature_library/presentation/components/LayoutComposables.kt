package org.ireader.presentation.feature_library.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.paging.compose.LazyPagingItems
import org.ireader.domain.models.LayoutType
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.History
import org.ireader.presentation.presentation.layouts.CompactGridLayoutComposable
import org.ireader.presentation.presentation.layouts.GridLayoutComposable
import org.ireader.presentation.presentation.layouts.LinearListDisplay
import tachiyomi.source.Source


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LayoutComposable(
    navController: NavController,
    books: List<Book> = emptyList(),
    lazyBook: LazyPagingItems<Book>? = null,
    histories: List<History> = emptyList(),
    onClick: (book: Book) -> Unit,
    onLongClick: (Book) -> Unit = {},
    selection: List<Long> = emptyList<Long>(),
    layout: LayoutType,
    scrollState: LazyListState,
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState,
    source: Source? = null,
    isLocal: Boolean,
    goToLatestChapter: (book: Book) -> Unit = {},
) {

    //TODO: Add an item change position animation
    when (layout) {
        is LayoutType.GridLayout -> {
            GridLayoutComposable(
                books = books,
                lazyBooks = lazyBook,
                onClick = { book ->
                    onClick(book)
                },
                selection = selection,
                onLongClick = { onLongClick(it) },
                scrollState = gridState,
                isLocal = isLocal,
                goToLatestChapter = { goToLatestChapter(it) }, histories = histories)
        }
        is LayoutType.ListLayout -> {
            LinearListDisplay(books = books, onClick = { book ->
                onClick(book)
            }, scrollState = scrollState,
                isLocal = isLocal,
                selection = selection,
                onLongClick = { onLongClick(it) },
                lazyBooks = lazyBook,
                goToLatestChapter = { goToLatestChapter(it) })
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
                lazyBooks = lazyBook,
                goToLatestChapter = { goToLatestChapter(it) }, histories = histories)

        }
    }
}
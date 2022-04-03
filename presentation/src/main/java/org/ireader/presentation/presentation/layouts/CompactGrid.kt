package org.ireader.presentation.presentation.layouts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.History
import org.ireader.presentation.utils.items


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompactGridLayoutComposable(
    modifier: Modifier = Modifier,
    lazyBooks: LazyPagingItems<Book>?,
    books: List<Book>,
    histories: List<History>,
    selection: List<Long> = emptyList(),
    onClick: (book: Book) -> Unit,
    onLongClick: (book: Book) -> Unit = {},
    scrollState: LazyGridState = rememberLazyGridState(),
    isLocal: Boolean,
    goToLatestChapter: (book: Book) -> Unit,
) {
    LazyVerticalGrid(
        state = scrollState,
        modifier = modifier.fillMaxSize(),
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp),
        content = {
            if (lazyBooks != null) {
                items(lazyBooks) { book ->
                    if (book != null) {
                        BookImage(
                            onClick = { onClick(book) }, book = book, ratio = 6f / 9f,
                            selected = book.id in selection,
                            onLongClick = { onLongClick(book) },
                        ) {
                            if (isLocal && histories.find { it.bookId == book.id }?.readAt != 0L) {
                                GoToLastReadComposable(onClick = { goToLatestChapter(book) })
                            }
                            if (!isLocal && book.favorite) {
                                TextBadge(text = "in Library")
                            }
                        }

                    }

                }
            } else {
                items(count = books.size) { index ->

                    BookImage(
                        onClick = { onClick(books[index]) }, book = books[index], ratio = 6f / 9f,
                        selected = books[index].id in selection,
                        onLongClick = { onLongClick(books[index]) },
                    ) {
                        if (isLocal && histories.find { it.bookId == books[index].id }?.readAt != 0L) {
                            GoToLastReadComposable(onClick = { goToLatestChapter(books[index]) })
                        }
                        if (!isLocal && books[index].favorite) {
                            TextBadge(text = "in Library")
                        }
                    }
                }
            }

        })
}
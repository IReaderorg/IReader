package org.ireader.presentation.presentation.layouts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import org.ireader.core.utils.items
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.History


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompactGridLayoutComposable(
    modifier: Modifier = Modifier,
    lazyBooks: LazyPagingItems<Book>,
    books: List<Book>,
    histories: List<History>,
    onClick: (book: Book) -> Unit,
    scrollState: LazyGridState = rememberLazyGridState(),
    isLocal: Boolean,
    goToLatestChapter: (book: Book) -> Unit,
) {
    LazyVerticalGrid(
        state = scrollState,
        modifier = modifier.fillMaxSize(),
        cells = GridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp),
        content = {
            if (books.isEmpty()) {
                items(lazyBooks) { book ->
                    if (book != null) {
                        BookImage(
                            onClick = { onClick(book) }, book = book, ratio = 6f / 9f
                        ) {
                            if (isLocal && histories.find { it.bookId == book.id }?.readAt != 0L) {
                                GoToLastReadComposable(onClick = { goToLatestChapter(book) })
                            }
                        }

                    }

                }
            } else {
                items(books) { book ->
                    if (book != null) {
                        BookImage(
                            onClick = { onClick(book) }, book = book, ratio = 6f / 9f
                        ) {
                            if (isLocal && histories.find { it.bookId == book.id }?.readAt != 0L) {
                                GoToLastReadComposable(onClick = { goToLatestChapter(book) })
                            }
                        }

                    }

                }
            }

        })
}
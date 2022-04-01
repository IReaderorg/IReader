package org.ireader.presentation.presentation.layouts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.compose.LazyPagingItems
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.History
import org.ireader.presentation.utils.items


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GridLayoutComposable(
    modifier: Modifier = Modifier,
    lazyBooks: LazyPagingItems<Book>?,
    books: List<Book>,
    histories: List<History>,
    onClick: (book: Book) -> Unit,
    scrollState: androidx.compose.foundation.lazy.grid.LazyGridState,
    isLocal: Boolean,
    goToLatestChapter: (book: Book) -> Unit,
) {
    LazyVerticalGrid(
        state = scrollState,
        modifier = modifier.fillMaxSize(),
        columns = GridCells.Fixed(3),
        content = {
            if (books.isEmpty() && lazyBooks != null) {
                items(lazyBooks) { book ->
                    if (book != null) {
                        BookImage(
                            onClick = { onClick(book) }, book = book, ratio = 6f / 10f
                        ) {
                            if (book.lastUpdated > 1 && isLocal && histories.find { it.bookId == book.id }?.readAt != 0L) {
                                GoToLastReadComposable(onClick = { goToLatestChapter(book) })
                            }
                        }
                    }
                }
            } else {
                items(count = books.size) { index ->
                    BookImage(
                        onClick = { onClick(books[index]) }, book = books[index], ratio = 6f / 10f
                    ) {
                        if (books[index].lastUpdated > 1 && isLocal && histories.find { it.bookId == books[index].id }?.readAt != 0L) {
                            GoToLastReadComposable(onClick = { goToLatestChapter(books[index]) })
                        }
                    }
                }
            }

        })
}
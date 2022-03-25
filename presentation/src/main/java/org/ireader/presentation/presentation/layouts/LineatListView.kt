package org.ireader.presentation.presentation.layouts

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.items
import org.ireader.domain.feature_services.io.BookCover
import org.ireader.domain.models.entities.Book
import org.ireader.presentation.presentation.components.BookImageComposable


@Composable
fun LinearBookItem(
    modifier: Modifier = Modifier,
    title: String,
    book: Book,
) {

    Box(
        modifier = modifier
            .padding(vertical = 8.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)) {
            BookImageComposable(
                image = BookCover.from(book),
                modifier = modifier
                    .aspectRatio(3f / 4f)
                    .clip(RoundedCornerShape(4.dp))
                    .border(.2.dp, MaterialTheme.colors.onBackground.copy(alpha = .1f)))
            Spacer(modifier = Modifier.width(15.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onBackground,
                textAlign = TextAlign.Center
            )
        }
    }

}

@Composable
fun LinearListDisplay(
    lazyBooks: LazyPagingItems<Book>?,
    books: List<Book>,
    onClick: (book: Book) -> Unit,
    scrollState: LazyListState = rememberLazyListState(),
    isLocal: Boolean,
    goToLatestChapter: (book: Book) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize(), state = scrollState) {
        if (lazyBooks != null) {
            items(lazyBooks) { book ->
                if (book != null) {
                    LinearBookItem(
                        title = book.title,
                        book = book,
                        modifier = Modifier.clickable {
                            onClick(book)
                        }
                    )
                }

            }
        } else {
            items(count = books.size) { index ->
                LinearBookItem(
                    title = books[index].title,
                    book = books[index],
                    modifier = Modifier.clickable {
                        onClick(books[index])
                    }
                )
            }
        }


    }
}


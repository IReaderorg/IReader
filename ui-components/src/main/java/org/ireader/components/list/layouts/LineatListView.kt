package org.ireader.components.list.layouts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.ireader.common_models.entities.BookItem
import org.ireader.components.components.BookImageComposable
import org.ireader.image_loader.BookCover


@Composable
fun LinearBookItem(
    modifier: Modifier = Modifier,
    title: String,
    selected: Boolean = false,
    book: BookItem,
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
                    .border(.2.dp,
                        if (selected) MaterialTheme.colors.primary.copy(alpha = .5f) else MaterialTheme.colors.onBackground.copy(
                            alpha = .1f)))
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LinearListDisplay(
    books: List<BookItem>,
    selection: List<Long> = emptyList(),
    onClick: (book: BookItem) -> Unit,
    onLongClick: (book: BookItem) -> Unit = {},
    scrollState: LazyListState = rememberLazyListState(),
    isLocal: Boolean,
    goToLatestChapter: (book: BookItem) -> Unit,
    isLoading: Boolean = false,
    onEndReach: (itemIndex: Int) -> Unit = {},
) {
    LazyColumn(modifier = Modifier.fillMaxSize(), state = scrollState) {
            items(count = books.size) { index ->
                onEndReach(index)
                LinearBookItem(
                    title = books[index].title,
                    book = books[index],
                    modifier = Modifier.combinedClickable(
                        onClick = { onClick(books[index]) },
                        onLongClick = { onClick(books[index]) },
                    ),
                    selected = books[index].id in selection
                )
            }
            item {
                    Spacer(modifier = Modifier.height(25.dp) )
                if (isLoading) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
}


package ireader.ui.component.list.layouts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ireader.common.models.entities.BookItem
import ireader.ui.component.components.BookImageComposable
import ireader.ui.imageloader.BookCover

@Composable
fun LinearBookItem(
    modifier: Modifier = Modifier,
    title: String,
    selected: Boolean = false,
    book: BookItem,
    headers: ((url: String) -> okhttp3.Headers?)? = null,

) {

    Box(
        modifier = modifier
            .padding(vertical = 8.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
        ) {
            BookImageComposable(
                image = BookCover.from(book),
                modifier = modifier
                    .aspectRatio(3f / 4f)
                    .clip(RoundedCornerShape(4.dp))
                    .border(
                        .2.dp,
                        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = .5f) else MaterialTheme.colorScheme.onBackground.copy(
                            alpha = .1f
                        )
                    ),
                headers = headers
            )
            Spacer(modifier = Modifier.width(15.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
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
    showGoToLastChapterBadge: Boolean = false,
    showUnreadBadge: Boolean = false,
    showReadBadge: Boolean = false,
    showInLibraryBadge: Boolean = false,
    headers: ((url: String) -> okhttp3.Headers?)? = null,
) {
    LazyColumn(modifier = Modifier.fillMaxSize(), state = scrollState) {
        items(
            count = books.size, key = { index ->
                books[index].id
            },
            contentType = { "books" }
        ) { index ->
            LinearBookItem(
                title = books[index].title,
                book = books[index],
                modifier = Modifier.combinedClickable(
                    onClick = { onClick(books[index]) },
                    onLongClick = { onClick(books[index]) },
                ).animateItemPlacement(),
                selected = books[index].id in selection,
                headers = headers
            )
        }
        item {
            Spacer(modifier = Modifier.height(25.dp))
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

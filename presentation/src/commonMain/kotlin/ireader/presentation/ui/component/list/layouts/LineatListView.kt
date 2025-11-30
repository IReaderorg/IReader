package ireader.presentation.ui.component.list.layouts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
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
import ireader.domain.models.BookCover
import ireader.domain.models.entities.BookItem
import ireader.presentation.ui.component.LocalPerformanceConfig
import ireader.presentation.ui.component.PerformanceConfig
import ireader.presentation.ui.component.components.IBookImageComposable
import ireader.presentation.ui.component.optimizedForList
import ireader.presentation.ui.component.rememberIsScrollingFast

@Composable
fun LinearBookItem(
    modifier: Modifier = Modifier,
    title: String,
    selected: Boolean = false,
    book: BookItem,
    headers: ((url: String) -> okhttp3.Headers?)? = null,
    isScrollingFast: Boolean = false,
    performanceConfig: PerformanceConfig = LocalPerformanceConfig.current,
) {

    Box(
        modifier = modifier
            .padding(vertical = 8.dp, horizontal = 8.dp)
            .optimizedForList(enableLayerPromotion = performanceConfig.enableComplexAnimations),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Book cover thumbnail - show placeholder during fast scroll
            if (!isScrollingFast) {
                IBookImageComposable(
                    image = BookCover.from(book),
                    modifier = Modifier
                        .width(48.dp)
                        .height(64.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .border(
                            .2.dp,
                            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = .5f) else MaterialTheme.colorScheme.onBackground.copy(
                                alpha = .1f
                            )
                        ),
                    headers = headers,
                    crossfadeDurationMs = performanceConfig.crossfadeDurationMs
                )
            } else {
                // Simple placeholder during fast scroll
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(64.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            .2.dp,
                            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = .5f) else MaterialTheme.colorScheme.onBackground.copy(
                                alpha = .1f
                            )
                        )
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Book information column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                // Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Author
                if (book.author.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = book.author,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Unread count
                if (book.unread != null && book.unread!! > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${book.unread} unread",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
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
    keys: ((item: BookItem) -> Any)
) {
    // Performance optimization: track fast scrolling to defer expensive operations
    val performanceConfig = LocalPerformanceConfig.current
    val isScrollingFast = rememberIsScrollingFast(scrollState)
    
    LazyColumn(modifier = Modifier.fillMaxSize(), state = scrollState) {
        items(
            items = books, key = keys,
            contentType = { "books" }
        ) { book ->
            LinearBookItem(
                title = book.title,
                book = book,
                modifier = Modifier.combinedClickable(
                    onClick = { onClick(book) },
                    onLongClick = { onClick(book) },
                ).animateItem(),
                selected = book.id in selection,
                headers = headers,
                isScrollingFast = isScrollingFast,
                performanceConfig = performanceConfig
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

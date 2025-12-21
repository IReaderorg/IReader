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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ireader.domain.models.BookCover
import ireader.domain.models.entities.BookItem
import ireader.presentation.ui.component.LocalPerformanceConfig
import ireader.presentation.ui.component.PerformanceConfig
import ireader.presentation.ui.component.components.IBookImageComposable
import ireader.presentation.ui.component.rememberIsScrollingFast

/**
 * NATIVE-LIKE LINEAR BOOK ITEM
 * Optimized for 60fps scroll with GPU layer promotion
 */
@Composable
fun LinearBookItem(
    modifier: Modifier = Modifier,
    title: String,
    selected: Boolean = false,
    book: BookItem,
    headers: ((url: String) -> Map<String, String>?)? = null,
    isScrollingFast: Boolean = false,
    performanceConfig: PerformanceConfig = LocalPerformanceConfig.current,
) {
    // Cache BookCover to prevent recreation
    val bookCover = remember(book.id, book.cover) { BookCover.from(book) }
    
    // Pre-compute border color
    val borderColor = remember(selected) {
        if (selected) androidx.compose.ui.graphics.Color(0x806200EE) 
        else androidx.compose.ui.graphics.Color(0x1A000000)
    }

    Box(
        modifier = modifier
            .padding(vertical = 4.dp, horizontal = 8.dp)
            // GPU layer promotion for smooth scrolling
            .graphicsLayer { compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Auto },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Book cover - always show (no placeholder during scroll for native feel)
            IBookImageComposable(
                image = bookCover,
                modifier = Modifier
                    .width(48.dp)
                    .height(64.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(4.dp)),
                headers = headers,
                crossfadeDurationMs = 0 // Instant display
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Book info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (book.author.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = book.author,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                if (book.unread != null && book.unread!! > 0) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${book.unread} unread",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * NATIVE-LIKE LINEAR LIST
 * Optimized for 60fps scroll
 */
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
    headers: ((url: String) -> Map<String, String>?)? = null,
    keys: ((item: BookItem) -> Any)
) {
    val performanceConfig = LocalPerformanceConfig.current
    val isScrollingFast = rememberIsScrollingFast(scrollState)
    
    // O(1) selection lookup
    val selectionSet = remember(selection) { selection.toHashSet() }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = scrollState
    ) {
        items(
            items = books,
            key = keys,
            contentType = { "book_list" }
        ) { book ->
            val isSelected = book.id in selectionSet
            
            LinearBookItem(
                title = book.title,
                book = book,
                modifier = Modifier.combinedClickable(
                    onClick = { onClick(book) },
                    onLongClick = { onLongClick(book) },
                ),
                selected = isSelected,
                headers = headers,
                isScrollingFast = isScrollingFast,
                performanceConfig = performanceConfig
            )
        }
        
        // Bottom spacer for navigation bar
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

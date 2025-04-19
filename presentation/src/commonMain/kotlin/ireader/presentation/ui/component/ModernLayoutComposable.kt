package ireader.presentation.ui.component

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ireader.core.source.Source
import ireader.domain.models.DisplayMode
import ireader.domain.models.entities.BookItem
import ireader.i18n.localize
import ireader.presentation.ui.component.components.BookShimmerLoading
import ireader.presentation.ui.component.list.isScrolledToTheEnd
import kotlinx.coroutines.delay
import ireader.i18n.resources.MR
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ModernLayoutComposable(
    books: List<BookItem> = emptyList(),
    onClick: (book: BookItem) -> Unit,
    onLongClick: (BookItem) -> Unit = {},
    selection: List<Long> = emptyList<Long>(),
    layout: DisplayMode,
    scrollState: LazyListState,
    gridState: LazyGridState,
    source: Source? = null,
    isLocal: Boolean = false,
    isLoading: Boolean = false,
    showInLibraryBadge: Boolean = false,
    emptyContent: @Composable () -> Unit = { 
        EmptyContent()
    },
    columns: Int? = null,
    headers: ((url: String) -> okhttp3.Headers?)? = null,
    keys: ((item: BookItem) -> Any) = {
        it.id
    }
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            books.isEmpty() && !isLoading -> {
                emptyContent()
            }
            books.isEmpty() && isLoading -> {
                BookShimmerLoading(columns = columns ?: 3)
            }
            layout == DisplayMode.List -> {
                ModernListLayout(
                    books = books,
                    onClick = onClick,
                    onLongClick = onLongClick,
                    scrollState = scrollState,
                    isLoading = isLoading,
                    headers = headers,
                    keys = keys
                )
            }
            else -> {
                // Handle different grid layouts based on DisplayMode
                val adjustedColumns = when (layout) {
                    DisplayMode.ComfortableGrid -> columns ?: 3
                    DisplayMode.CompactGrid -> columns ?: 2
                    DisplayMode.OnlyCover -> columns ?: 2
                    else -> columns ?: 2
                }
                
                ModernGridLayout(
                    books = books,
                    onClick = onClick,
                    onLongClick = onLongClick,
                    gridState = gridState,
                    isLoading = isLoading,
                    showInLibraryBadge = showInLibraryBadge,
                    columns = adjustedColumns,
                    headers = headers,
                    keys = keys,
                    showTitle = layout != DisplayMode.OnlyCover,
                    compactMode = layout == DisplayMode.CompactGrid
                )
            }
        }
        
        // Loading indicator at the bottom when loading more items
        if (isLoading && books.isNotEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ModernGridLayout(
    books: List<BookItem>,
    onClick: (BookItem) -> Unit,
    onLongClick: (BookItem) -> Unit,
    gridState: LazyGridState,
    isLoading: Boolean,
    showInLibraryBadge: Boolean,
    columns: Int,
    headers: ((url: String) -> okhttp3.Headers?)?,
    keys: ((item: BookItem) -> Any),
    showTitle: Boolean = true,
    compactMode: Boolean = false
) {
    val cells = if (columns > 1) {
        GridCells.Fixed(columns)
    } else {
        GridCells.Adaptive(130.dp)
    }
    
    // Adjust padding for compact mode
    val itemPadding = if (compactMode) 4.dp else 8.dp
    
    LazyVerticalGrid(
        columns = cells,
        state = gridState,
        contentPadding = PaddingValues(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = books,
            key = { keys(it) }
        ) { book ->
            AnimatedBookItem(
                book = book,
                index = books.indexOf(book),
                onClick = onClick,
                onLongClick = onLongClick,
                headers = headers,
                showTitle = showTitle,
                elevation = if (compactMode) 2.dp else 4.dp
            )
        }
        
        // Add empty item at the bottom for loading indicator space
        if (isLoading && books.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ModernListLayout(
    books: List<BookItem>,
    onClick: (BookItem) -> Unit,
    onLongClick: (BookItem) -> Unit,
    scrollState: LazyListState,
    isLoading: Boolean,
    headers: ((url: String) -> okhttp3.Headers?)?,
    keys: ((item: BookItem) -> Any)
) {
    LazyColumn(
        state = scrollState,
        contentPadding = PaddingValues(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = books,
            key = { keys(it) }
        ) { book ->
            ModernListItem(
                book = book,
                index = books.indexOf(book),
                onClick = onClick,
                onLongClick = onLongClick,
                headers = headers
            )
        }
        
        // Add empty item at the bottom for loading indicator space
        if (isLoading && books.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun ModernListItem(
    book: BookItem,
    index: Int,
    onClick: (BookItem) -> Unit,
    onLongClick: (BookItem) -> Unit = {},
    headers: ((url: String) -> okhttp3.Headers?)? = null
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(key1 = Unit) {
        delay(index * 30L) // Stagger animation based on index
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 300)) +
                slideInHorizontally(
                    initialOffsetX = { -50 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .combinedClickable(
                    onClick = { onClick(book) },
                    onLongClick = { onLongClick(book) }
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Cover image
                    Box(
                        modifier = Modifier
                            .height(120.dp)
                            .width(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        BookCoverImage(
                            book = book,
                            headers = headers
                        )
                        
                        // Reading progress overlay at the bottom of the cover
                        // First try to use the dedicated progress field, then fallback to calculating from unread
                        val progress = book.progress ?: run {
                            book.unread?.let { unreadCount ->
                                val totalChapters = book.totalChapters ?: 0
                                if (totalChapters > 0) (totalChapters - unreadCount.toFloat()) / totalChapters else 0f
                            } ?: 0f
                        }
                        
                        if (progress > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .height(24.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                                    )
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Progress bar
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                                shape = RoundedCornerShape(2.dp)
                                            )
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(progress)
                                                .height(4.dp)
                                                .background(
                                                    color = MaterialTheme.colorScheme.primary,
                                                    shape = RoundedCornerShape(2.dp)
                                                )
                                        )
                                    }
                                    
                                    // Percentage text
                                    Text(
                                        text = "${(progress * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.align(Alignment.CenterHorizontally),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Book details column
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 2.dp)
                    ) {
                        // Title
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            color = MaterialTheme.colorScheme.onSurface,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Author
                        if (book.author.isNotBlank()) {
                            Text(
                                text = book.author,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                        
                        // Description if available
                        book.description?.let { description ->
                            if (description.isNotBlank()) {
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                        
                        // Status chips row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Show favorite indicator
                            if (book.favorite) {
                                ChipIndicator(
                                    text = localize(MR.strings.in_library),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            
                            // Unread chapters indicator
                            book.unread?.let { unreadCount ->
                                if (unreadCount > 0) {
                                    ChipIndicator(
                                        text = "$unreadCount ${localize(MR.strings.unread_chapters)}",
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }
                            
                            // Source name indicator, if space permits
                            Spacer(modifier = Modifier.weight(1f))
                            
                            Text(
                                text = book.sourceId.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                
                // Bottom action bar with metadata
                book.lastRead?.let { lastRead ->
                    if (lastRead > 0) {
                        Divider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Last read date
                            Text(
                                text = "${localize(MR.strings.last_read)}: ${formatRelativeTime(lastRead)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            
                            // Total chapters
                            book.totalChapters?.let { total ->
                                Text(
                                    text = "$total ${localize(MR.strings.total_chapter)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChipIndicator(
    text: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .background(
                color = color.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

// Simple relative time formatter - you might want to replace this with a more sophisticated implementation
private fun formatRelativeTime(timestamp: Long): String {
    val current = System.currentTimeMillis()
    val diff = current - timestamp
    
    return when {
        diff < 60 * 60 * 1000 -> "< 1 hour ago"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} hours ago"
        diff < 48 * 60 * 60 * 1000 -> "Yesterday"
        else -> "${diff / (24 * 60 * 60 * 1000)} days ago"
    }
}

@Composable
fun EmptyContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Optional icon
        Icon(
            imageVector = Icons.Default.MenuBook,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = localize(MR.strings.no_results_found),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = localize(MR.strings.try_another_search),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
} 
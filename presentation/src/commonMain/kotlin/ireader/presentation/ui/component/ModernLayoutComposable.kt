package ireader.presentation.ui.component

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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

@OptIn(ExperimentalAnimationApi::class)
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
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .clickable { onClick(book) },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cover image
                Box(
                    modifier = Modifier
                        .height(100.dp)
                        .width(70.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    BookCoverImage(
                        book = book,
                        headers = headers
                    )
                    
                    // In library badge if the book is a favorite
                    if (book.favorite) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(8.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Title and details
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        color = MaterialTheme.colorScheme.onSurface,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
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
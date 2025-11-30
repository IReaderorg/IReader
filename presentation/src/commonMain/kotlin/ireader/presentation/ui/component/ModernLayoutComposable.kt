package ireader.presentation.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import coil3.request.crossfade
import coil3.toUri
import ireader.core.source.Source
import ireader.domain.models.BookCover
import ireader.domain.models.DisplayMode
import ireader.domain.models.entities.BookItem
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.components.BookShimmerLoading
import kotlinx.coroutines.delay
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import androidx.compose.runtime.Stable

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
    // Use remember to avoid recalculating on every recomposition
    val cells = remember(columns) {
        when {
            columns > 1 -> GridCells.Fixed(columns)
            else -> GridCells.Adaptive(minSize = 140.dp) // Adaptive sizing for better responsiveness
        }
    }
    
    // Use remember for spacing values to avoid recalculation
    val horizontalSpacing = remember(compactMode) { if (compactMode) 6.dp else 12.dp }
    val verticalSpacing = remember(compactMode) { if (compactMode) 6.dp else 12.dp }
    val contentPadding = remember(compactMode) { if (compactMode) 8.dp else 12.dp }
    val elevation = remember(compactMode) { if (compactMode) 2.dp else 4.dp }
    
    // Use derivedStateOf for computed values that depend on state
    val shouldShowLoadingSpace by remember {
        derivedStateOf { isLoading && books.isNotEmpty() }
    }
    
    LazyVerticalGrid(
        columns = cells,
        state = gridState,
        contentPadding = PaddingValues(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        modifier = Modifier
            .fillMaxSize()
            .animateContentSize( // Smooth animations for layout changes
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
    ) {
        items(
            items = books,
            key = { keys(it) },
            contentType = { "book_item" } // Add contentType for better recycling
        ) { book ->
            // Use itemsIndexed alternative - pass index directly to avoid indexOf lookup
            val index = remember(book, books) { books.indexOf(book) }
            AnimatedBookItem(
                book = book,
                index = index,
                onClick = onClick,
                onLongClick = onLongClick,
                headers = headers,
                showTitle = showTitle,
                elevation = elevation
            )
        }
        
        // Add empty item at the bottom for loading indicator space
        if (shouldShowLoadingSpace) {
            item(
                span = { GridItemSpan(maxLineSpan) },
                contentType = "loading_spacer"
            ) {
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
    // Use derivedStateOf for computed values
    val shouldShowLoadingSpace by remember {
        derivedStateOf { isLoading && books.isNotEmpty() }
    }
    
    LazyColumn(
        state = scrollState,
        contentPadding = PaddingValues(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = books,
            key = { keys(it) },
            contentType = { "book_list_item" } // Add contentType for better recycling
        ) { book ->
            // Pass index directly to avoid indexOf lookup
            val index = remember(book, books) { books.indexOf(book) }
            ModernListItem(
                book = book,
                index = index,
                onClick = onClick,
                onLongClick = onLongClick,
                headers = headers
            )
        }
        
        // Add empty item at the bottom for loading indicator space
        if (shouldShowLoadingSpace) {
            item(contentType = "loading_spacer") {
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
    
    // Limit animation delay to first 15 items for better scroll performance
    val animationDelay = remember(index) {
        if (index < 15) index * 30L else 0L
    }
    
    LaunchedEffect(key1 = Unit) {
        if (animationDelay > 0) {
            delay(animationDelay)
        }
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
                    // Cover image with optimized sizing
                    Box(
                        modifier = Modifier
                            .height(140.dp)
                            .width(95.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        // Use optimized image request with proper sizing
                        val imageRequest = rememberOptimizedBookCoverRequest(
                            book = book,
                            targetWidth = 80.dp,
                            targetHeight = 120.dp
                        )
                        
                        val bookCover = remember(book.id, book.cover, book.lastRead) {
                            BookCover.from(book)
                        }
                        
                        SubcomposeAsyncImage(
                            model = imageRequest,
                            contentDescription = book.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            loading = {
                                ShimmerLoadingEffect()
                            },
                            error = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        )
                        
                        // Reading progress overlay at the bottom of the cover
                        // Use remember to cache progress calculation
                        val progress = remember(book.progress, book.unread, book.totalChapters) {
                            book.progress ?: run {
                                book.unread?.let { unreadCount ->
                                    val totalChapters = book.totalChapters ?: 0
                                    if (totalChapters > 0) (totalChapters - unreadCount.toFloat()) / totalChapters else 0f
                                } ?: 0f
                            }
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
                                    text = localize(Res.string.in_library),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            
                            // Unread chapters indicator
                            book.unread?.let { unreadCount ->
                                if (unreadCount > 0) {
                                    ChipIndicator(
                                        text = "$unreadCount ${localize(Res.string.unread_chapters)}",
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
                                text = "${localize(Res.string.last_read)}: ${formatRelativeTime(lastRead)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            
                            // Total chapters
                            book.totalChapters?.let { total ->
                                Text(
                                    text = "$total ${localize(Res.string.total_chapter)}",
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

@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun AnimatedBookItem(
    book: BookItem,
    index: Int,
    onClick: (BookItem) -> Unit,
    onLongClick: (BookItem) -> Unit = {},
    headers: ((url: String) -> okhttp3.Headers?)? = null,
    showTitle: Boolean = true,
    elevation: Dp = 4.dp
) {
    var visible by remember { mutableStateOf(false) }
    
    // Limit animation delay to first 20 items for better performance
    val animationDelay = remember(index) { 
        if (index < 20) index * 20L else 0L 
    }
    
    LaunchedEffect(key1 = Unit) {
        if (animationDelay > 0) {
            delay(animationDelay)
        }
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 300)) +
                scaleIn(
                    initialScale = 0.8f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
        modifier = Modifier.padding(4.dp)
    ) {
        EnhancedNovelCard(
            book = book,
            onClick = onClick,
            onLongClick = onLongClick,
            headers = headers,
            showTitle = showTitle,
            elevation = elevation
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EnhancedNovelCard(
    book: BookItem,
    onClick: (BookItem) -> Unit,
    onLongClick: (BookItem) -> Unit = {},
    headers: ((url: String) -> okhttp3.Headers?)? = null,
    showTitle: Boolean = true,
    elevation: Dp = 4.dp
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    // Use remember for computed values to minimize recomposition
    val progress = remember(book.progress, book.unread, book.totalChapters) {
        book.progress ?: run {
            book.unread?.let { unreadCount ->
                val totalChapters = book.totalChapters ?: 0
                if (totalChapters > 0) (totalChapters - unreadCount.toFloat()) / totalChapters else 0f
            } ?: 0f
        }
    }
    
    val hasUnreadChapters = remember(book.unread) { 
        (book.unread ?: 0) > 0 
    }
    
    val hasAuthor = remember(book.author) { 
        book.author.isNotBlank() 
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onClick(book) },
                onLongClick = { onLongClick(book) }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Cover image with proper aspect ratio (adjusted for better display)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.70f)
            ) {
                BookCoverImage(
                    book = book,
                    headers = headers
                )
                
                // Status badges overlay at top
                if (book.favorite || hasUnreadChapters) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // In library badge
                        if (book.favorite) {
                            StatusBadge(
                                icon = Icons.Default.Check,
                                backgroundColor = MaterialTheme.colorScheme.primary,
                                contentDescription = localizeHelper.localize(Res.string.in_library_1)
                            )
                        }
                        
                        // Download status badge
                        if (hasUnreadChapters) {
                            StatusBadge(
                                icon = Icons.Default.Download,
                                backgroundColor = MaterialTheme.colorScheme.tertiary,
                                contentDescription = localizeHelper.localize(Res.string.has_unread_chapters)
                            )
                        }
                    }
                }
                
                // Text overlay at bottom with gradient
                if (showTitle) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(80.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.8f)
                                    ),
                                    startY = 0f,
                                    endY = 200f
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = book.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            
                            if (hasAuthor) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = book.author,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.9f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                
                // Reading progress indicator at the very bottom
                if (progress > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .height(4.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    contentDescription: String?
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .background(
                color = backgroundColor.copy(alpha = 0.9f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun BookCoverImage(
    book: BookItem,
    headers: ((url: String) -> okhttp3.Headers?)? = null
) {
    // Use remember to cache the BookCover object
    val bookCover = remember(book.id, book.cover, ) {
        BookCover.from(book) 
    }
    
    val coverUrl = remember(bookCover.cover) { 
        bookCover.cover?.toUri() 
    }
    
    // Use the platform context for image requests
    val context = coil3.compose.LocalPlatformContext.current
    
    // Create optimized image request with proper sizing and caching
    val imageRequest = remember(coverUrl, context) {
        coil3.request.ImageRequest.Builder(context)
            .data(bookCover) // Use BookCover directly for better caching
            .memoryCacheKey(bookCover.cover) // Explicit memory cache key
            .diskCacheKey("${bookCover.cover};${bookCover.lastModified}") // Explicit disk cache key
            .crossfade(true) // Smooth transition
            .build()
    }
    
    SubcomposeAsyncImage(
        model = imageRequest,
        contentDescription = book.title,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
        loading = {
            ShimmerLoadingEffect()
        },
        error = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    )
}

@Composable
fun ShimmerLoadingEffect() {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    )
    
    val transition = rememberInfiniteTransition(label = localizeHelper.localize(Res.string.shimmer))
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = localizeHelper.localize(Res.string.shimmer)
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = shimmerColors,
                    start = Offset(translateAnim - 1000f, translateAnim - 1000f),
                    end = Offset(translateAnim, translateAnim)
                )
            )
    )
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
            text = localize(Res.string.no_results_found),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = localize(Res.string.try_another_search),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
} 
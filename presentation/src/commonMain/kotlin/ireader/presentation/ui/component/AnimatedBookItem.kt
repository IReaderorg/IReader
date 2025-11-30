package ireader.presentation.ui.component

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ireader.domain.models.BookCover
import ireader.domain.models.DisplayMode
import ireader.domain.models.entities.BookItem
import ireader.presentation.imageloader.IImageLoader
import ireader.presentation.ui.component.list.layouts.BookImage
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedBookItem(
    book: BookItem,
    onClick: (BookItem) -> Unit,
    onLongClick: (BookItem) -> Unit = {},
    showTitle: Boolean = true,
    elevation: Dp = 4.dp,
    headers: ((url: String) -> okhttp3.Headers?)? = null,
    isSelected: Boolean = false,
    displayMode: DisplayMode = DisplayMode.CompactGrid,
    index: Int = 0
) {
    // Control how much stagger we want between items
    val staggerFactor = remember { 0.1f }
    // Index-based offset for staggered animation
    val indexOffset = remember { index * staggerFactor }

    // Animate visibility when the item appears
    val animSpec = remember {
        spring<Float>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    }
    
    // Animate scale values
    val animatedScale = animateFloatAsState(
        targetValue = 1f,
        animationSpec = animSpec,
        label = "scaleAnimation"
    )
    
    // Animate alpha values
    val animatedAlpha = animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 400,
            delayMillis = (indexOffset * 150).toInt(),
            easing = LinearOutSlowInEasing
        ),
        label = "alphaAnimation"
    )
    
    // Selection effect when a book is selected
    val selectionScale = animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "selectionAnimation"
    )
    
    // Calculate floating effect
    val floatAngle = remember { 
        androidx.compose.animation.core.Animatable(initialValue = 0f) 
    }
    
    LaunchedEffect(floatAngle) {
        // Create subtle floating animation
        floatAngle.animateTo(
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 5000 + (index % 3) * 1000,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            )
        )
    }
    
    // Subtle floating offset based on sin wave
    val floatOffset = remember(floatAngle.value) {
        sin(floatAngle.value * (PI / 180f).toFloat()) * 3f
    }
    
    // Apply combined animations and transitions
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                // Combine all animation effects
                alpha = animatedAlpha.value
                scaleX = animatedScale.value * selectionScale.value
                scaleY = animatedScale.value * selectionScale.value
                translationY = floatOffset
            }
    ) {
        BookCard(
            book = book,
            onClick = onClick,
            onLongClick = onLongClick,
            elevation = elevation,
            showTitle = showTitle,
            headers = headers,
            floatOffset = floatOffset,
            scale = 1f, // Scale is now applied at the parent level
            displayMode = displayMode,
            isSelected = isSelected
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookCard(
    book: BookItem,
    onClick: (BookItem) -> Unit,
    onLongClick: (BookItem) -> Unit = {},
    elevation: Dp = 4.dp,
    showTitle: Boolean = true,
    headers: ((url: String) -> okhttp3.Headers?)? = null,
    floatOffset: Float = 0f,
    scale: Float = 1f,
    displayMode: DisplayMode = DisplayMode.CompactGrid,
    isSelected: Boolean = false
) {
    // Determine if the book is new
    val isNew = remember { (book.id % 5).toInt() == 0 } 
    
    // Dynamic colors based on selection state
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    // Card shape based on display mode
    val cardShape = when (displayMode) {
        DisplayMode.List -> RoundedCornerShape(12.dp)
        else -> RoundedCornerShape(16.dp)
    }
    
    // Apply specific layout based on display mode
    when (displayMode) {
        DisplayMode.List -> ListModeBookCard(
            book = book,
            onClick = onClick,
            onLongClick = onLongClick,
            isSelected = isSelected,
            isNew = isNew,
            containerColor = containerColor,
            headers = headers
        )
        else -> GridModeBookCard(
            book = book,
            onClick = onClick,
            onLongClick = onLongClick,
            showTitle = showTitle,
            isSelected = isSelected,
            isNew = isNew,
            containerColor = containerColor,
            cardShape = cardShape,
            headers = headers,
            elevation = elevation
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GridModeBookCard(
    book: BookItem,
    onClick: (BookItem) -> Unit,
    onLongClick: (BookItem) -> Unit,
    showTitle: Boolean,
    isSelected: Boolean,
    isNew: Boolean,
    containerColor: Color,
    cardShape: RoundedCornerShape,
    headers: ((url: String) -> okhttp3.Headers?)?,
    elevation: Dp
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .shadow(
                elevation = elevation,
                shape = cardShape,
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )
            .combinedClickable(
                onClick = { onClick(book) },
                onLongClick = { onLongClick(book) }
            ),
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            hoveredElevation = 6.dp
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.70f) // Adjusted aspect ratio for better display
            ) {
                BookCoverImage(
                    book = book,
                    headers = headers,
                    isSelected = isSelected,
                    isBlurred = false // Will be controlled by preferences in the library screen
                )
                
                // Status indicators row at the top
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Favorite indicator
                    if (book.favorite) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(6.dp)
                                )
                        )
                    }
                    
                    // New book indicator
                    if (isNew) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.tertiary,
                                    shape = RoundedCornerShape(6.dp)
                                )
                        )
                    }
                }
                
                // Reading progress indicator at the bottom
                if (book.unread?.toLong() != 0L) {
                    val progress = 0.3f // Example progress - would be calculated from book's read status
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth(progress)
                            .height(3.dp)
                            .padding(start = 4.dp, end = 4.dp, bottom = 4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                }
            }
            
            if (showTitle) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ListModeBookCard(
    book: BookItem,
    onClick: (BookItem) -> Unit,
    onLongClick: (BookItem) -> Unit,
    isSelected: Boolean,
    isNew: Boolean,
    containerColor: Color,
    headers: ((url: String) -> okhttp3.Headers?)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .combinedClickable(
                onClick = { onClick(book) },
                onLongClick = { onLongClick(book) }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            hoveredElevation = 4.dp
        )
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
                    .width(70.dp)
                    .height(105.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                BookCoverImage(
                    book = book,
                    headers = headers,
                    isSelected = isSelected,
                    isBlurred = false // Will be controlled by preferences in the library screen
                )
            }
            
            // Book details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Author if available
                if (book.author.isNotBlank()) {
                    Text(
                        text = book.author,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Status indicators
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Favorite indicator
                    if (book.favorite) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                        )
                        
                        Text(
                            text = "Favorite",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // New indicator
                    if (isNew) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.tertiary,
                                    shape = CircleShape
                                )
                        )
                        
                        Text(
                            text = "New",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookCoverImage(
    book: BookItem,
    headers: ((url: String) -> okhttp3.Headers?)? = null,
    isSelected: Boolean = false,
    isBlurred: Boolean = false
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var revealed by remember(book.id) { mutableStateOf(!isBlurred) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(enabled = isBlurred && !revealed) {
                revealed = true
            }
    ) {
        // Apply subtle border effect for selected items
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
            )
        }
        
        IImageLoader(
            model = BookCover.from(book).cover,
            contentDescription = book.title,
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isBlurred && !revealed) {
                        Modifier.blur(20.dp)
                    } else {
                        Modifier
                    }
                ),
            contentScale = ContentScale.Crop
        )
        
        // Show reveal icon when blurred
        if (isBlurred && !revealed) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Visibility,
                contentDescription = localizeHelper.localize(Res.string.tap_to_reveal),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(32.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
} 
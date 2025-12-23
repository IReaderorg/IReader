package ireader.presentation.ui.book.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material.icons.filled.LastPage
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Modern pagination bar for chapter lists with paged loading support.
 * Features a sleek design with page number chips and smooth animations.
 */
@Composable
fun ChapterPaginationBar(
    currentPage: Int,
    totalPages: Int,
    isLoading: Boolean,
    onPageSelected: (Int) -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasPrevious by remember(currentPage) { derivedStateOf { currentPage > 1 } }
    val hasNext by remember(currentPage, totalPages) { derivedStateOf { currentPage < totalPages } }
    
    // Calculate visible page numbers
    val visiblePages = remember(currentPage, totalPages) {
        calculateVisiblePages(currentPage, totalPages)
    }
    
    val listState = rememberLazyListState()
    
    // Auto-scroll to current page when it changes
    LaunchedEffect(currentPage, visiblePages) {
        val index = visiblePages.indexOf(currentPage)
        if (index >= 0) {
            listState.animateScrollToItem(index)
        }
    }
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Page indicator row with navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // First + Previous buttons
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // First page button
                    SmallNavigationButton(
                        onClick = { onPageSelected(1) },
                        enabled = hasPrevious && !isLoading,
                        icon = Icons.Default.FirstPage,
                        contentDescription = "First page"
                    )
                    
                    // Previous button
                    SmallNavigationButton(
                        onClick = onPreviousPage,
                        enabled = hasPrevious && !isLoading,
                        icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous page"
                    )
                }
                
                // Current page indicator
                PageIndicatorChip(
                    currentPage = currentPage,
                    totalPages = totalPages,
                    isLoading = isLoading
                )
                
                // Next + Last buttons
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Next button
                    SmallNavigationButton(
                        onClick = onNextPage,
                        enabled = hasNext && !isLoading,
                        icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next page"
                    )
                    
                    // Last page button
                    SmallNavigationButton(
                        onClick = { onPageSelected(totalPages) },
                        enabled = hasNext && !isLoading,
                        icon = Icons.Default.LastPage,
                        contentDescription = "Last page"
                    )
                }
            }
            
            // Page number chips row
            LazyRow(
                state = listState,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(visiblePages, key = { it }) { page ->
                    PageNumberChip(
                        page = page,
                        isSelected = page == currentPage,
                        isLoading = isLoading && page == currentPage,
                        onClick = { 
                            if (page != currentPage && !isLoading) {
                                onPageSelected(page) 
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * Small navigation button for first/last/prev/next actions
 */
@Composable
private fun SmallNavigationButton(
    onClick: () -> Unit,
    enabled: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String
) {
    val alpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.38f,
        animationSpec = tween(150),
        label = "alpha"
    )
    
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(32.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(alpha),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Central page indicator chip showing "Page X of Y"
 */
@Composable
private fun PageIndicatorChip(
    currentPage: Int,
    totalPages: Int,
    isLoading: Boolean
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Text(
                text = "Page $currentPage of $totalPages",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Individual page number chip with selection state
 */
@Composable
private fun PageNumberChip(
    page: Int,
    isSelected: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.surfaceContainerHigh
        },
        animationSpec = tween(200),
        label = "backgroundColor"
    )
    
    val contentColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.onPrimary
            else -> MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(200),
        label = "contentColor"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = tween(150),
        label = "scale"
    )
    
    Surface(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .scale(scale)
            .defaultMinSize(minWidth = 36.dp, minHeight = 32.dp),
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor,
        contentColor = contentColor
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = page.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

/**
 * Calculate which page numbers to show based on current page and total pages.
 * Shows a window of pages around the current page with ellipsis indicators.
 */
private fun calculateVisiblePages(currentPage: Int, totalPages: Int): List<Int> {
    if (totalPages <= 7) {
        return (1..totalPages).toList()
    }
    
    val pages = mutableListOf<Int>()
    
    // Always show first page
    pages.add(1)
    
    // Calculate range around current page
    val rangeStart = maxOf(2, currentPage - 2)
    val rangeEnd = minOf(totalPages - 1, currentPage + 2)
    
    // Add pages in range
    for (i in rangeStart..rangeEnd) {
        if (i !in pages) {
            pages.add(i)
        }
    }
    
    // Always show last page
    if (totalPages !in pages) {
        pages.add(totalPages)
    }
    
    return pages.sorted()
}

/**
 * Animated wrapper for ChapterPaginationBar that shows/hides based on pagination support.
 */
@Composable
fun AnimatedChapterPaginationBar(
    visible: Boolean,
    currentPage: Int,
    totalPages: Int,
    isLoading: Boolean,
    onPageSelected: (Int) -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { -it / 2 },
        exit = fadeOut(tween(200)) + slideOutVertically(tween(200)) { -it / 2 },
        modifier = modifier
    ) {
        ChapterPaginationBar(
            currentPage = currentPage,
            totalPages = totalPages,
            isLoading = isLoading,
            onPageSelected = onPageSelected,
            onPreviousPage = onPreviousPage,
            onNextPage = onNextPage
        )
    }
}

/**
 * Compact pagination bar for use in tight spaces (e.g., bottom of screen)
 */
@Composable
fun CompactChapterPaginationBar(
    currentPage: Int,
    totalPages: Int,
    isLoading: Boolean,
    onPageSelected: (Int) -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasPrevious by remember(currentPage) { derivedStateOf { currentPage > 1 } }
    val hasNext by remember(currentPage, totalPages) { derivedStateOf { currentPage < totalPages } }
    
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Previous
            IconButton(
                onClick = onPreviousPage,
                enabled = hasPrevious && !isLoading,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous",
                    modifier = Modifier.size(20.dp)
                )
            }
            
            // Page indicator
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "$currentPage / $totalPages",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
            
            // Next
            IconButton(
                onClick = onNextPage,
                enabled = hasNext && !isLoading,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

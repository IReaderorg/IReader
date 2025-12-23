package ireader.presentation.ui.book.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A pagination bar for chapter lists that support paged loading.
 * Shows current page, total pages, and navigation controls.
 * 
 * Follows Compose best practices:
 * - Uses derivedStateOf for computed values
 * - Memoizes callbacks with remember
 * - Uses stable data types
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
    // Derived state for button enabled states
    val hasPrevious by remember(currentPage) {
        derivedStateOf { currentPage > 1 }
    }
    val hasNext by remember(currentPage, totalPages) {
        derivedStateOf { currentPage < totalPages }
    }
    
    // State for page selector dropdown
    var showPageSelector by remember { mutableStateOf(false) }
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous button
            FilledTonalIconButton(
                onClick = onPreviousPage,
                enabled = hasPrevious && !isLoading,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous page",
                    modifier = Modifier.size(20.dp)
                )
            }
            
            // Page indicator with dropdown
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = !isLoading) { showPageSelector = true }
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    
                    Text(
                        text = "Page $currentPage of $totalPages",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = "Select page",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Page selector dropdown
                DropdownMenu(
                    expanded = showPageSelector,
                    onDismissRequest = { showPageSelector = false }
                ) {
                    // Show page numbers in a scrollable list
                    // For large page counts, show a subset with current page centered
                    val pagesToShow = remember(currentPage, totalPages) {
                        when {
                            totalPages <= 10 -> (1..totalPages).toList()
                            currentPage <= 5 -> (1..10).toList()
                            currentPage >= totalPages - 4 -> ((totalPages - 9)..totalPages).toList()
                            else -> ((currentPage - 4)..(currentPage + 5)).toList()
                        }
                    }
                    
                    pagesToShow.forEach { page ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Page $page",
                                    fontWeight = if (page == currentPage) FontWeight.Bold else FontWeight.Normal,
                                    color = if (page == currentPage) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                showPageSelector = false
                                if (page != currentPage) {
                                    onPageSelected(page)
                                }
                            },
                            leadingIcon = if (page == currentPage) {
                                {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                MaterialTheme.colorScheme.primary,
                                                CircleShape
                                            )
                                    )
                                }
                            } else null
                        )
                    }
                }
            }
            
            // Next button
            FilledTonalIconButton(
                onClick = onNextPage,
                enabled = hasNext && !isLoading,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next page",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
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
        enter = fadeIn() + slideInVertically { -it },
        exit = fadeOut() + slideOutVertically { -it },
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

package ireader.presentation.ui.book.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.component.ShimmerBox
import ireader.presentation.ui.component.ShimmerCircle
import ireader.presentation.ui.component.shimmerBrush

/**
 * Shimmer loading placeholder for the Book Detail screen.
 * Shows a skeleton UI that mimics the actual layout to prevent
 * perceived freezing on low-end devices.
 */
@Composable
fun BookDetailShimmerLoading(
    modifier: Modifier = Modifier,
    appbarPadding: Dp = 0.dp
) {
    val brush = shimmerBrush()
    
    Box(modifier = modifier.fillMaxSize()) {
        // Background shimmer (mimics backdrop)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(brush)
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            // Top padding for app bar
            item {
                Spacer(modifier = Modifier.height(appbarPadding + 16.dp))
            }
            
            // Book header shimmer (cover + title + author)
            item {
                BookHeaderShimmer()
            }
            
            // Stats card shimmer
            item {
                BookStatsShimmer()
            }
            
            // Action buttons shimmer
            item {
                ActionButtonsShimmer()
            }
            
            // Summary shimmer
            item {
                SummaryShimmer()
            }
            
            // Chapter bar shimmer
            item {
                ChapterBarShimmer()
            }
            
            // Chapter list shimmer
            items(8) {
                ChapterRowShimmer()
            }
        }
    }
}

@Composable
private fun BookHeaderShimmer() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Book cover placeholder
        ShimmerBox(
            width = 120.dp,
            height = 180.dp,
            shape = RoundedCornerShape(12.dp)
        )
        
        // Title and metadata
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Title
            ShimmerBox(
                modifier = Modifier.fillMaxWidth(0.9f),
                height = 24.dp
            )
            ShimmerBox(
                modifier = Modifier.fillMaxWidth(0.6f),
                height = 24.dp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Author
            ShimmerBox(
                modifier = Modifier.fillMaxWidth(0.5f),
                height = 16.dp
            )
            
            // Source
            ShimmerBox(
                modifier = Modifier.fillMaxWidth(0.4f),
                height = 14.dp
            )
            
            // Status badge
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerBox(
                width = 80.dp,
                height = 24.dp,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
private fun BookStatsShimmer() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        repeat(3) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ShimmerBox(width = 40.dp, height = 20.dp)
                ShimmerBox(width = 60.dp, height = 14.dp)
            }
        }
    }
}

@Composable
private fun ActionButtonsShimmer() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Favorite button
        ShimmerBox(
            modifier = Modifier.weight(1f),
            height = 48.dp,
            shape = RoundedCornerShape(24.dp)
        )
        
        // WebView button
        ShimmerCircle(size = 48.dp)
        
        // Migrate button
        ShimmerCircle(size = 48.dp)
    }
}

@Composable
private fun SummaryShimmer() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Section title
        ShimmerBox(width = 80.dp, height = 18.dp)
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Summary text lines
        repeat(4) {
            ShimmerBox(
                modifier = Modifier.fillMaxWidth(if (it == 3) 0.7f else 1f),
                height = 14.dp
            )
        }
    }
}

@Composable
private fun ChapterBarShimmer() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Chapter count
        ShimmerBox(width = 100.dp, height = 20.dp)
        
        // Sort/filter buttons
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ShimmerCircle(size = 32.dp)
            ShimmerCircle(size = 32.dp)
            ShimmerCircle(size = 32.dp)
        }
    }
}

@Composable
private fun ChapterRowShimmer() {
    val brush = shimmerBrush()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Chapter content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Chapter title
            ShimmerBox(
                modifier = Modifier.fillMaxWidth(0.8f),
                height = 16.dp
            )
            
            // Chapter metadata
            ShimmerBox(
                modifier = Modifier.fillMaxWidth(0.5f),
                height = 12.dp
            )
        }
    }
    
    // Divider
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .padding(horizontal = 16.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    )
}

/**
 * Tablet layout shimmer with two panels
 */
@Composable
fun BookDetailShimmerLoadingTablet(
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxSize()) {
        // Left panel - Book info
        Column(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BookHeaderShimmer()
            BookStatsShimmer()
            ActionButtonsShimmer()
            SummaryShimmer()
        }
        
        // Right panel - Chapters
        Column(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxSize()
        ) {
            ChapterBarShimmer()
            repeat(10) {
                ChapterRowShimmer()
            }
        }
    }
}


/**
 * Placeholder UI for Book Detail screen - shows immediately with minimal info.
 * This replaces shimmer loading for a smoother, more responsive feel.
 * 
 * The UI progressively updates as data loads in the background.
 */
@Composable
fun BookDetailPlaceholder(
    bookId: Long,
    title: String = "Loading...",
    cover: String? = null,
    author: String? = null,
    isLoading: Boolean = true,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Simple background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            // Top padding
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
            
            // Book header with actual or placeholder data
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Book cover - show actual cover if available, otherwise placeholder
                    if (cover != null) {
                        // Would use actual image loader here
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    } else {
                        // Placeholder cover
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                    
                    // Title and metadata
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Title - show actual title or placeholder
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            color = if (title == "Loading...") 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            else 
                                MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Author - show if available
                        if (author != null) {
                            Text(
                                text = author,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else if (isLoading) {
                            // Placeholder for author
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.5f)
                                    .height(16.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Loading indicator
                        if (isLoading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "Loading details...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            // Placeholder for action buttons
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Favorite button placeholder
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    )
                    
                    // Other button placeholders
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    )
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    )
                }
            }
            
            // Placeholder for summary section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Summary",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    // Placeholder lines
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(if (it == 2) 0.7f else 1f)
                                .height(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        )
                    }
                }
            }
            
            // Placeholder for chapters section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Chapters",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    if (isLoading) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
            
            // Placeholder chapter rows
            items(5) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.4f)
                                .height(10.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        )
                    }
                }
            }
        }
    }
}

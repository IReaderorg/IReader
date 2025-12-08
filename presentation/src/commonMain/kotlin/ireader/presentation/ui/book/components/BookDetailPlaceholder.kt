package ireader.presentation.ui.book.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Placeholder UI for Book Detail screen - shows immediately with minimal info.
 * NO shimmer animation - uses static placeholders for instant display.
 * Data fills in progressively as it loads without freezing the UI.
 * 
 * Key optimizations:
 * - No animation overhead (no shimmer)
 * - Static placeholder colors for instant render
 * - Progressive data display with fade-in for smooth UX
 * - Minimal recomposition when data updates
 */
@Composable
fun BookDetailPlaceholder(
    bookId: Long,
    title: String = "",
    cover: String? = null,
    author: String? = null,
    isLoading: Boolean = true,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val placeholderColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    val placeholderColorLight = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
    
    Box(modifier = modifier.fillMaxSize()) {
        // Simple static background - no animation
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(placeholderColorLight)
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            // Top padding for app bar
            item(key = "top_padding") {
                Spacer(modifier = Modifier.height(80.dp))
            }
            
            // Book header - shows actual data when available
            item(key = "header") {
                PlaceholderHeader(
                    title = title,
                    author = author,
                    isLoading = isLoading,
                    placeholderColor = placeholderColor
                )
            }
            
            // Stats placeholder - static boxes
            item(key = "stats") {
                PlaceholderStats(placeholderColor = placeholderColor)
            }
            
            // Action buttons placeholder - static boxes
            item(key = "actions") {
                PlaceholderActions(placeholderColor = placeholderColor)
            }
            
            // Summary placeholder
            item(key = "summary") {
                PlaceholderSummary(placeholderColor = placeholderColor)
            }
            
            // Chapters header with loading indicator
            item(key = "chapters_header") {
                PlaceholderChaptersHeader(
                    isLoading = isLoading,
                    placeholderColor = placeholderColor
                )
            }
            
            // Static chapter row placeholders - no animation
            items(5, key = { "chapter_placeholder_$it" }) {
                PlaceholderChapterRow(placeholderColor = placeholderColorLight)
            }
        }
    }
}

@Composable
private fun PlaceholderHeader(
    title: String,
    author: String?,
    isLoading: Boolean,
    placeholderColor: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Book cover placeholder - static box with optional loading spinner
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(placeholderColor),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            }
        }
        
        // Title and metadata column
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Title - show actual title with fade-in, or placeholder
            AnimatedVisibility(
                visible = title.isNotEmpty(),
                enter = fadeIn(animationSpec = tween(200))
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (title.isEmpty()) {
                // Static placeholder for title
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(placeholderColor)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(placeholderColor)
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Author - show actual author with fade-in, or placeholder
            AnimatedVisibility(
                visible = author != null,
                enter = fadeIn(animationSpec = tween(200))
            ) {
                Text(
                    text = author ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (author == null && isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(placeholderColor)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Loading indicator - small and unobtrusive
            if (isLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 1.5.dp
                    )
                    Text(
                        text = "Loading...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaceholderStats(placeholderColor: androidx.compose.ui.graphics.Color) {
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
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(placeholderColor)
                )
                Box(
                    modifier = Modifier
                        .width(50.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(placeholderColor.copy(alpha = 0.5f))
                )
            }
        }
    }
}

@Composable
private fun PlaceholderActions(placeholderColor: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Main action button placeholder
        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(placeholderColor)
        )
        // Secondary action placeholders
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(placeholderColor)
        )
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(placeholderColor)
        )
    }
}

@Composable
private fun PlaceholderSummary(placeholderColor: androidx.compose.ui.graphics.Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Summary",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        // Static placeholder lines - no animation
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (index == 2) 0.7f else 1f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(placeholderColor.copy(alpha = 0.6f))
            )
        }
    }
}

@Composable
private fun PlaceholderChaptersHeader(
    isLoading: Boolean,
    placeholderColor: androidx.compose.ui.graphics.Color
) {
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
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp
            )
        }
    }
}

@Composable
private fun PlaceholderChapterRow(placeholderColor: androidx.compose.ui.graphics.Color) {
    Column {
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
                        .background(placeholderColor)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(placeholderColor.copy(alpha = 0.6f))
                )
            }
        }
        // Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .padding(horizontal = 20.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
        )
    }
}

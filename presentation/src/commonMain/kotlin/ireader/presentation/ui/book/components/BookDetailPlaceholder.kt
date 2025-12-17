package ireader.presentation.ui.book.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Placeholder UI for Book Detail screen.
 * 
 * NO shimmer, NO loading indicators, NO "Loading..." text.
 * Just static placeholder boxes that match the real UI layout.
 * When real data loads, it replaces this seamlessly.
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
    val backgroundColor = MaterialTheme.colorScheme.background
    
    Box(modifier = modifier.fillMaxSize()) {
        // Backdrop placeholder - gradient background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            placeholderColorLight,
                            placeholderColorLight.copy(alpha = 0.15f),
                            backgroundColor,
                        ),
                        startY = 0f,
                        endY = 600f
                    )
                )
        )
        
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            // Top padding for app bar
            Spacer(modifier = Modifier.height(80.dp))
            
            // Book header
            PlaceholderHeader(
                title = title,
                author = author,
                placeholderColor = placeholderColor
            )
            
            // Stats
            PlaceholderStats(placeholderColor = placeholderColor)
            
            // Action buttons
            PlaceholderActions(placeholderColor = placeholderColor)
            
            // Summary
            PlaceholderSummary(placeholderColor = placeholderColor)
            
            // Chapters header
            PlaceholderChaptersHeader(placeholderColor = placeholderColor)
            
            // Chapter rows (3 visible)
            repeat(3) {
                PlaceholderChapterRow(placeholderColor = placeholderColorLight)
            }
        }
    }
}

@Composable
private fun PlaceholderHeader(
    title: String,
    author: String?,
    placeholderColor: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Book cover placeholder - just a static box
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(placeholderColor)
        )
        
        // Title and metadata column
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Show title if available, otherwise placeholder box
            if (title.isNotEmpty()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
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
            
            // Show author if available, otherwise placeholder box
            if (author != null) {
                Text(
                    text = author,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(placeholderColor)
                )
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
        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(placeholderColor)
        )
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
private fun PlaceholderChaptersHeader(placeholderColor: androidx.compose.ui.graphics.Color) {
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
        // No loading indicator - just empty space
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .padding(horizontal = 20.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
        )
    }
}

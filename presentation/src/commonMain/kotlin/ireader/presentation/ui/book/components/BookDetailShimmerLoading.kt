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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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

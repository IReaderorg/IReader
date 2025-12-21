package ireader.presentation.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Creates a shimmer brush effect for loading placeholders.
 * The shimmer animates from left to right continuously.
 */
@Composable
fun shimmerBrush(
    targetValue: Float = 1000f,
    durationMillis: Int = 1000
): Brush {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnimation - 200f, translateAnimation - 200f),
        end = Offset(translateAnimation, translateAnimation)
    )
}

/**
 * A shimmer placeholder box with customizable size and shape.
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    width: Dp = 100.dp,
    height: Dp = 20.dp,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(4.dp)
) {
    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(shape)
            .background(shimmerBrush())
    )
}

/**
 * A circular shimmer placeholder (for avatars, icons, etc.)
 */
@Composable
fun ShimmerCircle(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(shimmerBrush())
    )
}

/**
 * Library-style grid shimmer loading screen.
 * Shows a grid of book cover placeholders.
 */
@Composable
fun LibraryShimmerLoading(
    modifier: Modifier = Modifier,
    columns: Int = 3
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(12, key = { "shimmer-$it" }) {
            Column(
                modifier = Modifier.padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Book cover placeholder
                ShimmerBox(
                    width = 100.dp,
                    height = 140.dp,
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Title placeholder
                ShimmerBox(
                    width = 80.dp,
                    height = 14.dp
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Subtitle placeholder
                ShimmerBox(
                    width = 60.dp,
                    height = 12.dp
                )
            }
        }
    }
}

/**
 * List-style shimmer loading screen.
 * Shows a list of item placeholders with icon and text.
 */
@Composable
fun ListShimmerLoading(
    modifier: Modifier = Modifier,
    itemCount: Int = 8
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(itemCount) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon/Avatar placeholder
                ShimmerCircle(size = 48.dp)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    // Title
                    ShimmerBox(
                        modifier = Modifier.fillMaxWidth(0.7f),
                        height = 16.dp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Subtitle
                    ShimmerBox(
                        modifier = Modifier.fillMaxWidth(0.5f),
                        height = 12.dp
                    )
                }
            }
        }
    }
}

/**
 * History-style shimmer loading screen.
 * Shows grouped items with date headers.
 */
@Composable
fun HistoryShimmerLoading(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Date header
        ShimmerBox(width = 120.dp, height = 20.dp)
        Spacer(modifier = Modifier.height(8.dp))
        
        repeat(3) {
            HistoryItemShimmer()
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Another date header
        ShimmerBox(width = 100.dp, height = 20.dp)
        Spacer(modifier = Modifier.height(8.dp))
        
        repeat(4) {
            HistoryItemShimmer()
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun HistoryItemShimmer() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Book cover
        ShimmerBox(
            width = 50.dp,
            height = 70.dp,
            shape = RoundedCornerShape(4.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            ShimmerBox(
                modifier = Modifier.fillMaxWidth(0.8f),
                height = 16.dp
            )
            Spacer(modifier = Modifier.height(6.dp))
            ShimmerBox(
                modifier = Modifier.fillMaxWidth(0.6f),
                height = 14.dp
            )
            Spacer(modifier = Modifier.height(4.dp))
            ShimmerBox(
                modifier = Modifier.fillMaxWidth(0.4f),
                height = 12.dp
            )
        }
    }
}

/**
 * Extensions/Sources shimmer loading screen.
 */
@Composable
fun ExtensionsShimmerLoading(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Section header
        ShimmerBox(width = 150.dp, height = 24.dp)
        Spacer(modifier = Modifier.height(8.dp))
        
        repeat(5) {
            ExtensionItemShimmer()
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Another section
        ShimmerBox(width = 180.dp, height = 24.dp)
        Spacer(modifier = Modifier.height(8.dp))
        
        repeat(4) {
            ExtensionItemShimmer()
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ExtensionItemShimmer() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Extension icon
        ShimmerBox(
            width = 48.dp,
            height = 48.dp,
            shape = RoundedCornerShape(8.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            ShimmerBox(
                modifier = Modifier.fillMaxWidth(0.6f),
                height = 16.dp
            )
            Spacer(modifier = Modifier.height(4.dp))
            ShimmerBox(
                modifier = Modifier.fillMaxWidth(0.4f),
                height = 12.dp
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        // Action button placeholder
        ShimmerBox(
            width = 70.dp,
            height = 32.dp,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

/**
 * Settings/More screen shimmer loading.
 */
@Composable
fun SettingsShimmerLoading(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(8) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerCircle(size = 40.dp)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    ShimmerBox(
                        modifier = Modifier.fillMaxWidth(0.5f),
                        height = 16.dp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ShimmerBox(
                        modifier = Modifier.fillMaxWidth(0.7f),
                        height = 12.dp
                    )
                }
            }
        }
    }
}

/**
 * Generic full-screen shimmer loading with customizable content.
 */
@Composable
fun FullScreenShimmerLoading(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Animated shimmer circle
            ShimmerCircle(size = 64.dp)
            ShimmerBox(width = 120.dp, height = 16.dp)
        }
    }
}

/**
 * Updates screen shimmer loading.
 */
@Composable
fun UpdatesShimmerLoading(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Date header
        ShimmerBox(width = 80.dp, height = 18.dp)
        Spacer(modifier = Modifier.height(4.dp))
        
        repeat(4) {
            UpdateItemShimmer()
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        ShimmerBox(width = 100.dp, height = 18.dp)
        Spacer(modifier = Modifier.height(4.dp))
        
        repeat(3) {
            UpdateItemShimmer()
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun UpdateItemShimmer() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ShimmerBox(
            width = 45.dp,
            height = 65.dp,
            shape = RoundedCornerShape(4.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            ShimmerBox(
                modifier = Modifier.fillMaxWidth(0.7f),
                height = 14.dp
            )
            Spacer(modifier = Modifier.height(4.dp))
            ShimmerBox(
                modifier = Modifier.fillMaxWidth(0.5f),
                height = 12.dp
            )
        }
    }
}


/**
 * Generic screen shimmer loading - can be used for any screen
 * that doesn't have a specific shimmer design.
 */
@Composable
fun GenericScreenShimmerLoading(
    modifier: Modifier = Modifier,
    hasTopBar: Boolean = true
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (hasTopBar) {
            // Top bar placeholder
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerCircle(size = 40.dp)
                ShimmerBox(width = 150.dp, height = 24.dp)
                ShimmerCircle(size = 40.dp)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Content area
        repeat(6) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShimmerBox(
                    width = 60.dp,
                    height = 60.dp,
                    shape = RoundedCornerShape(8.dp)
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShimmerBox(
                        modifier = Modifier.fillMaxWidth(0.8f),
                        height = 18.dp
                    )
                    ShimmerBox(
                        modifier = Modifier.fillMaxWidth(0.6f),
                        height = 14.dp
                    )
                }
            }
        }
    }
}

/**
 * Reader screen shimmer loading
 */
@Composable
fun ReaderShimmerLoading(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Chapter title
        ShimmerBox(
            modifier = Modifier.fillMaxWidth(0.7f),
            height = 28.dp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Text content lines
        repeat(20) { index ->
            ShimmerBox(
                modifier = Modifier.fillMaxWidth(
                    when {
                        index % 5 == 4 -> 0.6f // Shorter line at end of paragraph
                        index % 7 == 0 -> 0.85f
                        else -> 0.95f
                    }
                ),
                height = 16.dp
            )
            
            // Paragraph spacing
            if (index % 5 == 4) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

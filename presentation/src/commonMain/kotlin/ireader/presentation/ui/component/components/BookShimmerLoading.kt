package ireader.presentation.ui.component.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

@Composable
fun BookShimmerLoading(
    modifier: Modifier = Modifier,
    columns: Int = 3,
    itemCount: Int = 12
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    // Create more vibrant shimmer colors with higher contrast
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
    )
    
    // Make the animation faster and more noticeable
    val transition = rememberInfiniteTransition(label = localizeHelper.localize(Res.string.shimmertransition))
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 800,
                easing = FastOutLinearInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = localizeHelper.localize(Res.string.shimmeranimation)
    )
    
    // Second animation for a pulsating effect
    val pulseAnim by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = localizeHelper.localize(Res.string.pulseanimation)
    )
    
    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 500f, translateAnim - 500f),
        end = Offset(translateAnim, translateAnim)
    )
    
    val cells = if (columns > 1) {
        GridCells.Fixed(columns)
    } else {
        GridCells.Adaptive(130.dp)
    }
    
    LazyVerticalGrid(
        columns = cells,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp)
    ) {
        items(itemCount) { index ->
            // Staggered animation effect by offsetting the animation based on item index
            val itemDelay = index * 100
            val itemTransition = rememberInfiniteTransition(label = "itemTransition$index")
            val itemScale by itemTransition.animateFloat(
                initialValue = 0.92f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 1200,
                        delayMillis = itemDelay % 300,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "itemScaleAnimation$index"
            )
            
            EnhancedShimmerBookItem(
                brush = brush,
                scale = itemScale,
                pulse = pulseAnim
            )
        }
    }
}

@Composable
fun EnhancedShimmerBookItem(
    brush: Brush,
    scale: Float = 1.0f,
    pulse: Float = 1.0f
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = (2.dp * pulse)
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(bottom = 12.dp)) {
            // Book cover with enhanced aspect ratio
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.67f) // 2:3 book cover ratio
                    .background(brush)
            )
            
            // Space between cover and title
            Spacer(modifier = Modifier.height(12.dp))
            
            // Title with rounded corners and variable width
            Spacer(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(16.dp)
                    .padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            
            // Author/subtitle with shorter width for visual hierarchy
            Spacer(modifier = Modifier.height(8.dp))
            Spacer(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(12.dp)
                    .padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
        }
    }
}

// Keeping the legacy implementation for backwards compatibility
@Composable
fun ShimmerBookItem(
    brush: Brush
) {
    EnhancedShimmerBookItem(brush = brush)
}

@Composable
fun BookItemShimmer(
    brush: Brush,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        // Cover
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.67f)
                .clip(RoundedCornerShape(8.dp))
                .background(brush)
        )

        // Title
        Spacer(modifier = Modifier.height(8.dp))
        Spacer(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(14.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(brush)
        )

        // Subtitle/Author
        Spacer(modifier = Modifier.height(4.dp))
        Spacer(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(12.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(brush)
        )
    }
}

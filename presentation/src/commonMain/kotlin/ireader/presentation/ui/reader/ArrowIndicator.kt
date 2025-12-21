package ireader.presentation.ui.reader

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.presentation.ui.reader.reverse_swip_refresh.SwipeRefreshState
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*

/**
 * Premium arrow indicator with void-like appearance and smooth animations.
 * Shows when user drags at boundaries with chapter navigation info.
 * Colors are based on reader text color for consistency.
 */
@Composable
fun ArrowIndicator(
    icon: ImageVector,
    swipeRefreshState: SwipeRefreshState,
    refreshTriggerDistance: Dp,
    color: Color = MaterialTheme.colorScheme.primary,
    maxSize: Dp = 40.dp,
    chapterName: String? = null,
    isTop: Boolean = true
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    // Early exit: Don't render anything if there's no swipe activity
    // This is the primary guard to prevent showing indicator without user interaction
    val hasSwipeActivity = swipeRefreshState.isSwipeInProgress || 
                           swipeRefreshState.indicatorOffset > 1f // Need at least 1px offset
    
    if (!hasSwipeActivity) {
        return
    }
    
    val trigger = with(LocalDensity.current) { refreshTriggerDistance.toPx() }
    val progress = (swipeRefreshState.indicatorOffset / trigger).coerceIn(0f, 1f)
    
    // Only render when there's meaningful drag progress (> 8% to avoid flickering)
    if (progress < 0.08f) {
        return
    }
    
    // Smooth bounce animation when fully pulled
    val infiniteTransition = rememberInfiniteTransition(label = localizeHelper.localize(Res.string.bounce))
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (progress > 0.7f) 8f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = localizeHelper.localize(Res.string.bounceoffset)
    )
    
    // Glow pulse effect
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = localizeHelper.localize(Res.string.glowpulse)
    )
    
    // Animated glow when near trigger
    val glowAlpha by animateFloatAsState(
        targetValue = if (progress > 0.6f) glowPulse else 0f,
        animationSpec = tween(200),
        label = localizeHelper.localize(Res.string.glow)
    )
    
    // Derive colors from the provided text color
    val isDarkText = (color.red * 0.299f + color.green * 0.587f + color.blue * 0.114f) < 0.5f
    val voidColor = if (isDarkText) Color(0xFFF5F5F5) else Color(0xFF050508)
    val voidGlowColor = if (isDarkText) Color(0xFFE0E0E0) else Color(0xFF1A1A2E)
    val accentColor = color.copy(alpha = 0.8f)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height((progress * 140).dp)
            .clipToBounds() // Prevent any content from overflowing outside the box
            .graphicsLayer { alpha = progress }
            .background(
                Brush.verticalGradient(
                    colors = if (isTop) {
                        listOf(
                            voidColor.copy(alpha = 0.95f * progress),
                            voidGlowColor.copy(alpha = 0.5f * progress),
                            Color.Transparent
                        )
                    } else {
                        listOf(
                            Color.Transparent,
                            voidGlowColor.copy(alpha = 0.5f * progress),
                            voidColor.copy(alpha = 0.95f * progress)
                        )
                    }
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Ambient glow orb
        if (glowAlpha > 0f) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .graphicsLayer { alpha = glowAlpha }
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.3f),
                                accentColor.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .graphicsLayer { translationY = bounceOffset * (if (isTop) 1f else -1f) }
                .padding(12.dp)
        ) {
            // Chapter info at top for bottom indicator - only show when progress is high enough
            if (!isTop && chapterName != null && progress > 0.5f) {
                Text(
                    text = localizeHelper.localize(Res.string.next_chapter_2),
                    color = color.copy(alpha = 0.6f * progress),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Truncate chapter name to single line
                val truncatedName = if (chapterName.length > 30) chapterName.take(30) + "..." else chapterName
                Text(
                    text = truncatedName,
                    color = color.copy(alpha = 0.9f * progress),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Arrow icon with glow ring - NO rotation
            Box(contentAlignment = Alignment.Center) {
                // Glow ring behind arrow
                if (progress > 0.5f) {
                    Box(
                        modifier = Modifier
                            .size((maxSize.value * 1.8f).dp * progress)
                            .graphicsLayer { alpha = glowAlpha * 0.5f }
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        accentColor.copy(alpha = 0.4f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                }
                
                // Arrow icon - removed rotation to fix tilting issue
                Image(
                    imageVector = icon,
                    contentDescription = if (isTop) "Previous chapter" else "Next chapter",
                    modifier = Modifier
                        .size(maxSize * progress.coerceAtLeast(0.4f))
                        .graphicsLayer {
                            scaleY = progress.coerceAtLeast(0.5f)
                            scaleX = progress.coerceAtLeast(0.5f)
                        },
                    colorFilter = ColorFilter.tint(color.copy(alpha = progress.coerceAtLeast(0.4f)))
                )
            }
            
            // Release text when fully pulled
            if (progress > 0.8f) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .background(
                            color.copy(alpha = 0.15f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = localizeHelper.localize(Res.string.release_to_load),
                        color = color.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            // Chapter info at bottom for top indicator - only show when progress is high enough
            if (isTop && chapterName != null && progress > 0.5f) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = localizeHelper.localize(Res.string.previous_chapter_2),
                    color = color.copy(alpha = 0.6f * progress),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Truncate chapter name to single line
                val truncatedName = if (chapterName.length > 30) chapterName.take(30) + "..." else chapterName
                Text(
                    text = truncatedName,
                    color = color.copy(alpha = 0.9f * progress),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            } else if (chapterName == null && progress > 0.3f) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isTop) "First chapter" else "Last chapter",
                    color = color.copy(alpha = 0.5f * progress),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Simple arrow indicator without chapter info (legacy compatibility).
 */
@Composable
fun SimpleArrowIndicator(
    icon: ImageVector,
    swipeRefreshState: SwipeRefreshState,
    refreshTriggerDistance: Dp,
    color: Color = MaterialTheme.colorScheme.primary,
    maxSize: Dp = 40.dp,
) {
    Box {
        if (!swipeRefreshState.isRefreshing) {
            val trigger = with(LocalDensity.current) { refreshTriggerDistance.toPx() }
            val progress = (swipeRefreshState.indicatorOffset / trigger).coerceIn(0f, 1f)
            Image(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(maxSize)
                    .graphicsLayer {
                        scaleY = progress
                        scaleX = progress
                    },
                colorFilter = ColorFilter.tint(color)
            )
        }
    }
}

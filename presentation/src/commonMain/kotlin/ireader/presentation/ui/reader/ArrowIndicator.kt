package ireader.presentation.ui.reader

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.presentation.ui.reader.reverse_swip_refresh.SwipeRefreshState

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
    val trigger = with(LocalDensity.current) { refreshTriggerDistance.toPx() }
    val progress = (swipeRefreshState.indicatorOffset / trigger).coerceIn(0f, 1f)
    
    // Smooth bounce animation when fully pulled
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (progress > 0.7f) 8f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounceOffset"
    )
    
    // Glow pulse effect
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )
    
    // Animated glow when near trigger
    val glowAlpha by animateFloatAsState(
        targetValue = if (progress > 0.6f) glowPulse else 0f,
        animationSpec = tween(200),
        label = "glow"
    )
    
    // Derive colors from the provided text color
    val isDarkText = (color.red * 0.299f + color.green * 0.587f + color.blue * 0.114f) < 0.5f
    val voidColor = if (isDarkText) Color(0xFFF5F5F5) else Color(0xFF050508)
    val voidGlowColor = if (isDarkText) Color(0xFFE0E0E0) else Color(0xFF1A1A2E)
    val accentColor = color.copy(alpha = 0.8f)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height((progress * 140).dp.coerceAtLeast(0.dp))
            .alpha(progress.coerceAtLeast(0.1f))
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
                    .alpha(glowAlpha)
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
            // Chapter info at top for bottom indicator
            if (!isTop && chapterName != null && progress > 0.25f) {
                Text(
                    text = "NEXT CHAPTER",
                    color = color.copy(alpha = 0.6f * progress),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = chapterName,
                    color = color.copy(alpha = 0.9f * progress),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .widthIn(max = 220.dp)
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
                            .alpha(glowAlpha * 0.5f)
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
                        text = "Release to load",
                        color = color.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            // Chapter info at bottom for top indicator
            if (isTop && chapterName != null && progress > 0.25f) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "PREVIOUS CHAPTER",
                    color = color.copy(alpha = 0.6f * progress),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = chapterName,
                    color = color.copy(alpha = 0.9f * progress),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .widthIn(max = 220.dp)
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

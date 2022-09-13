package ireader.ui.reader

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ireader.ui.reader.reverse_swip_refresh.SwipeRefreshState

@Composable
fun ArrowIndicator(
    icon: ImageVector,
    swipeRefreshState: SwipeRefreshState,
    refreshTriggerDistance: Dp,
    color: Color = MaterialTheme.colorScheme.primary,
    maxSize: Dp = 40.dp,
) {
    Box {
        if (swipeRefreshState.isRefreshing) {
            // If we're refreshing, show an indeterminate progress indicator
        } else {
            // Otherwise we display a determinate progress indicator with the current swipe progress
            val trigger = with(LocalDensity.current) { refreshTriggerDistance.toPx() }
            val progress = (swipeRefreshState.indicatorOffset / trigger).coerceIn(0f, 1f)
            Image(
                imageVector = icon, "contentDescription",
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

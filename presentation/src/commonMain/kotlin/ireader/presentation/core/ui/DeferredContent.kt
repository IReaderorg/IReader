package ireader.presentation.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

/**
 * Deferred content wrapper that shows a shimmer placeholder while content loads.
 * This prevents the UI from appearing frozen during heavy initialization.
 * 
 * @param showShimmer Whether to show shimmer (true when loading)
 * @param shimmerContent The shimmer/skeleton placeholder to show
 * @param content The actual content to display when ready
 */
@Composable
fun DeferredContent(
    showShimmer: Boolean,
    shimmerContent: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Shimmer placeholder - fades out when content is ready
        AnimatedVisibility(
            visible = showShimmer,
            enter = fadeIn(animationSpec = tween(150)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            shimmerContent()
        }
        
        // Actual content - fades in when ready
        AnimatedVisibility(
            visible = !showShimmer,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(150))
        ) {
            content()
        }
    }
}

/**
 * Auto-deferred content that shows shimmer for a brief moment
 * to allow the UI thread to breathe before rendering heavy content.
 * 
 * @param delayMs Delay before showing actual content (default 50ms)
 * @param shimmerContent The shimmer placeholder
 * @param content The actual content
 */
@Composable
fun AutoDeferredContent(
    delayMs: Long = 50,
    shimmerContent: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    var isReady by remember { mutableStateOf(delayMs == 0L) }
    
    LaunchedEffect(Unit) {
        if (delayMs > 0) {
            delay(delayMs)
            isReady = true
        }
    }
    
    DeferredContent(
        showShimmer = !isReady,
        shimmerContent = shimmerContent,
        content = content
    )
}

/**
 * Content wrapper that defers rendering until after the first frame.
 * Shows shimmer immediately, then fades to actual content.
 * 
 * This is useful for heavy screens that would otherwise cause jank.
 */
@Composable
fun FirstFrameDeferredContent(
    shimmerContent: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    AutoDeferredContent(
        delayMs = 16, // One frame at 60fps
        shimmerContent = shimmerContent,
        content = content
    )
}

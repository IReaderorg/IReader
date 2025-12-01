package ireader.presentation.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

/**
 * A wrapper that shows shimmer loading immediately while deferring
 * heavy screen content to the next frame. This prevents UI freezing
 * when navigating to screens with heavy initialization.
 * 
 * Usage:
 * ```
 * DeferredScreen(
 *     shimmer = { MyScreenShimmerLoading() }
 * ) {
 *     // Heavy screen content here
 *     MyActualScreenContent()
 * }
 * ```
 * 
 * @param delayMs Delay before showing actual content (default 16ms = 1 frame)
 * @param shimmer The shimmer/skeleton loading placeholder
 * @param content The actual screen content
 */
@Composable
fun DeferredScreen(
    delayMs: Long = 16,
    shimmer: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    var isReady by remember { mutableStateOf(delayMs == 0L) }
    
    LaunchedEffect(Unit) {
        if (delayMs > 0) {
            delay(delayMs)
            isReady = true
        }
    }
    
    if (isReady) {
        content()
    } else {
        shimmer()
    }
}

/**
 * A simpler version that just defers content without shimmer.
 * Shows nothing during the delay, then shows content.
 * Useful when you want minimal overhead.
 */
@Composable
fun DeferredContent(
    delayMs: Long = 16,
    content: @Composable () -> Unit
) {
    var isReady by remember { mutableStateOf(delayMs == 0L) }
    
    LaunchedEffect(Unit) {
        if (delayMs > 0) {
            delay(delayMs)
            isReady = true
        }
    }
    
    if (isReady) {
        content()
    }
}

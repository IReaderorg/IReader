package ireader.presentation.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*

/**
 * A wrapper that shows shimmer loading immediately while deferring
 * heavy screen content to the next frame. This prevents UI freezing
 * when navigating to screens with heavy initialization.
 * 
 * Uses crossfade animation for smooth transition from shimmer to content.
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
 * @param delayMs Delay before showing actual content (default 50ms for smooth transition)
 * @param animationDurationMs Duration of crossfade animation (default 150ms)
 * @param shimmer The shimmer/skeleton loading placeholder
 * @param content The actual screen content
 */
@Composable
fun DeferredScreen(
    delayMs: Long = 50,
    animationDurationMs: Int = 150,
    shimmer: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var isReady by remember { mutableStateOf(delayMs == 0L) }
    
    LaunchedEffect(Unit) {
        if (delayMs > 0) {
            delay(delayMs)
            isReady = true
        }
    }
    
    AnimatedContent(
        targetState = isReady,
        transitionSpec = {
            fadeIn(animationSpec = tween(animationDurationMs)) togetherWith
                fadeOut(animationSpec = tween(animationDurationMs / 2))
        },
        label = localizeHelper.localize(Res.string.deferredscreentransition)
    ) { ready ->
        if (ready) {
            content()
        } else {
            shimmer()
        }
    }
}

/**
 * A simpler version that just defers content without shimmer.
 * Shows nothing during the delay, then fades in content.
 * Useful when you want minimal overhead.
 */
@Composable
fun DeferredContent(
    delayMs: Long = 16,
    animationDurationMs: Int = 100,
    content: @Composable () -> Unit
) {
    var isReady by remember { mutableStateOf(delayMs == 0L) }
    
    LaunchedEffect(Unit) {
        if (delayMs > 0) {
            delay(delayMs)
            isReady = true
        }
    }
    
    AnimatedVisibility(
        visible = isReady,
        enter = fadeIn(animationSpec = tween(animationDurationMs))
    ) {
        content()
    }
}

/**
 * A condition-based deferred screen that shows shimmer until a condition is met.
 * This is useful when you want to show shimmer until data is loaded, rather than
 * using a fixed delay.
 * 
 * The shimmer will be shown for at least [minDisplayMs] to prevent flicker
 * when data loads very quickly.
 * 
 * Usage:
 * ```
 * DeferredScreenWithCondition(
 *     isReady = viewModel.dataLoaded,
 *     shimmer = { MyScreenShimmerLoading() }
 * ) {
 *     MyActualScreenContent()
 * }
 * ```
 * 
 * @param isReady Condition that determines when to show actual content
 * @param minDisplayMs Minimum time to show shimmer (prevents flicker)
 * @param animationDurationMs Duration of crossfade animation
 * @param shimmer The shimmer/skeleton loading placeholder
 * @param content The actual screen content
 */
@Composable
fun DeferredScreenWithCondition(
    isReady: Boolean,
    minDisplayMs: Long = 100,
    animationDurationMs: Int = 150,
    shimmer: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var minTimeElapsed by remember { mutableStateOf(minDisplayMs == 0L) }
    var hasBeenReady by remember { mutableStateOf(isReady) }
    
    // Track if condition has ever been true
    LaunchedEffect(isReady) {
        if (isReady) hasBeenReady = true
    }
    
    // Ensure minimum display time for shimmer
    LaunchedEffect(Unit) {
        if (minDisplayMs > 0) {
            delay(minDisplayMs)
            minTimeElapsed = true
        }
    }
    
    // Show content when both conditions are met
    val showContent = (isReady || hasBeenReady) && minTimeElapsed
    
    AnimatedContent(
        targetState = showContent,
        transitionSpec = {
            fadeIn(animationSpec = tween(animationDurationMs)) togetherWith
                fadeOut(animationSpec = tween(animationDurationMs / 2))
        },
        label = localizeHelper.localize(Res.string.deferredscreenwithconditiontransition)
    ) { ready ->
        if (ready) {
            content()
        } else {
            shimmer()
        }
    }
}

/**
 * A lightweight version that shows content immediately but with a fade-in animation.
 * Use this when you want smooth appearance without shimmer loading.
 */
@Composable
fun FadeInScreen(
    animationDurationMs: Int = 200,
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(animationDurationMs))
    ) {
        content()
    }
}

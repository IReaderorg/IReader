package ireader.presentation.ui.component

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Performance optimizations specifically for low-end and older devices.
 * 
 * Key strategies:
 * 1. Reduce animation complexity and duration
 * 2. Limit concurrent image loads
 * 3. Use simpler composables when possible
 * 4. Reduce recomposition scope
 */

/**
 * Configuration for low-end device optimizations
 */
@Stable
data class PerformanceConfig(
    val animationDurationMs: Int = 300,
    val crossfadeDurationMs: Int = 200,
    val prefetchDistance: Int = 4,
    val maxConcurrentImageLoads: Int = 4,
    val enableComplexAnimations: Boolean = true,
    val enableBlurEffects: Boolean = true,
    val enableShadows: Boolean = true,
    // Image loading optimizations
    val maxImageSize: Int = 1024, // Max dimension in pixels
    val thumbnailSize: Int = 256, // Size for list thumbnails
    val enableImageCrossfade: Boolean = true,
    val useRgb565: Boolean = false, // Use RGB_565 for non-transparent images (saves 50% memory)
    val enableImagePlaceholder: Boolean = true,
    val deferImageLoadingOnScroll: Boolean = false // Defer loading during fast scroll
) {
    companion object {
        val Default = PerformanceConfig()
        
        val LowEnd = PerformanceConfig(
            animationDurationMs = 100,
            crossfadeDurationMs = 0, // Disable crossfade for instant display
            prefetchDistance = 1,
            maxConcurrentImageLoads = 2,
            enableComplexAnimations = false,
            enableBlurEffects = false,
            enableShadows = false,
            maxImageSize = 384, // Smaller images for low-end
            thumbnailSize = 128,
            enableImageCrossfade = false,
            useRgb565 = true,
            enableImagePlaceholder = false, // Simple color placeholder
            deferImageLoadingOnScroll = true
        )
        
        val Medium = PerformanceConfig(
            animationDurationMs = 200,
            crossfadeDurationMs = 100,
            prefetchDistance = 2,
            maxConcurrentImageLoads = 3,
            enableComplexAnimations = true,
            enableBlurEffects = false,
            enableShadows = true,
            maxImageSize = 512,
            thumbnailSize = 192,
            enableImageCrossfade = true,
            useRgb565 = false,
            enableImagePlaceholder = true,
            deferImageLoadingOnScroll = false
        )
    }
}

/**
 * Creates an animation spec optimized for the current performance tier
 */
@Composable
fun <T> rememberOptimizedAnimationSpec(
    config: PerformanceConfig = PerformanceConfig.Default
): FiniteAnimationSpec<T> {
    return remember(config.animationDurationMs) {
        tween(durationMillis = config.animationDurationMs)
    }
}

/**
 * Modifier that promotes composable to a separate layer for better scroll performance.
 * On low-end devices, this can significantly improve list scrolling.
 */
fun Modifier.optimizedForList(
    enableLayerPromotion: Boolean = true
): Modifier = if (enableLayerPromotion) {
    this.graphicsLayer {
        // Promote to separate layer - reduces recomposition impact
        compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen
    }
} else {
    this
}

/**
 * Modifier that conditionally applies shadow based on performance config
 */
fun Modifier.conditionalShadow(
    config: PerformanceConfig,
    elevation: Float = 4f
): Modifier = if (config.enableShadows) {
    this.graphicsLayer {
        shadowElevation = elevation
    }
} else {
    this
}

/**
 * Tracks if the list is currently being scrolled rapidly.
 * Use this to defer expensive operations during fast scrolling.
 */
@Composable
fun rememberIsScrollingFast(
    listState: LazyListState,
    threshold: Int = 3
): Boolean {
    val isScrollingFast by remember {
        derivedStateOf {
            val velocity = listState.firstVisibleItemScrollOffset
            listState.isScrollInProgress && velocity > threshold * 100
        }
    }
    return isScrollingFast
}

/**
 * Tracks if the grid is currently being scrolled rapidly.
 */
@Composable
fun rememberIsGridScrollingFast(
    gridState: LazyGridState,
    threshold: Int = 3
): Boolean {
    val isScrollingFast by remember {
        derivedStateOf {
            val velocity = gridState.firstVisibleItemScrollOffset
            gridState.isScrollInProgress && velocity > threshold * 100
        }
    }
    return isScrollingFast
}

/**
 * Returns a simplified placeholder during fast scrolling.
 * This reduces the rendering load when users are scrolling quickly.
 */
@Composable
inline fun <T> rememberScrollAwareContent(
    isScrollingFast: Boolean,
    noinline placeholder: @Composable () -> T,
    noinline content: @Composable () -> T
): @Composable () -> T {
    return remember(isScrollingFast) {
        if (isScrollingFast) placeholder else content
    }
}

/**
 * Calculates visible item range for efficient rendering
 */
@Composable
fun rememberVisibleItemRange(
    listState: LazyListState,
    buffer: Int = 2
): IntRange {
    val visibleRange by remember {
        derivedStateOf {
            val firstVisible = listState.firstVisibleItemIndex
            val visibleCount = listState.layoutInfo.visibleItemsInfo.size
            val start = (firstVisible - buffer).coerceAtLeast(0)
            val end = firstVisible + visibleCount + buffer
            start..end
        }
    }
    return visibleRange
}

/**
 * CompositionLocal for providing PerformanceConfig throughout the app
 */
val LocalPerformanceConfig = compositionLocalOf { PerformanceConfig.Default }

/**
 * Provides PerformanceConfig to the composition tree
 */
@Composable
fun PerformanceConfigProvider(
    config: PerformanceConfig,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalPerformanceConfig provides config) {
        content()
    }
}

/**
 * Best practices for low-end device optimization:
 * 
 * 1. IMAGE LOADING:
 *    - Use smaller image sizes (reduce resolution by 50% on low-end)
 *    - Disable crossfade animations or use shorter durations
 *    - Limit concurrent image loads to 2-3
 *    - Use placeholder colors instead of shimmer effects
 * 
 * 2. LISTS AND GRIDS:
 *    - Use proper keys in items()
 *    - Add contentType for better recycling
 *    - Reduce prefetch distance
 *    - Defer image loading during fast scroll
 *    - Use simpler item layouts
 * 
 * 3. ANIMATIONS:
 *    - Reduce animation duration by 50%
 *    - Disable complex animations (blur, parallax)
 *    - Use simpler easing curves
 *    - Limit stagger animations to first 10 items
 * 
 * 4. MEMORY:
 *    - Reduce image cache size
 *    - Clear unused bitmaps aggressively
 *    - Use RGB_565 instead of ARGB_8888 for non-transparent images
 * 
 * 5. RENDERING:
 *    - Disable shadows and elevation
 *    - Use solid colors instead of gradients
 *    - Avoid nested scrolling containers
 *    - Use graphicsLayer for complex items
 */

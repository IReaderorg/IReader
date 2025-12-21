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
 * Configuration for device-specific performance optimizations.
 * 
 * NATIVE-LIKE PERFORMANCE STRATEGY:
 * 1. Zero crossfade - images appear instantly like Settings app
 * 2. Minimal animations - 50ms max for transitions
 * 3. Aggressive prefetching - load ahead for smooth scrolling
 * 4. Hardware acceleration - use GPU for compositing
 * 5. Memory-efficient images - right-size for display
 */
@Stable
data class PerformanceConfig(
    // Animation settings - native apps use ~50-100ms
    val animationDurationMs: Int = 50, // Ultra-fast transitions
    val crossfadeDurationMs: Int = 0, // ZERO - instant image display
    val prefetchDistance: Int = 8, // Aggressive prefetch for smooth scroll
    val maxConcurrentImageLoads: Int = 12, // High parallelism for fast loading
    val enableComplexAnimations: Boolean = false, // Disabled for native feel
    val enableBlurEffects: Boolean = false, // Expensive - disabled
    val enableShadows: Boolean = false, // Disabled for performance
    // Image loading - optimized for instant display
    val maxImageSize: Int = 384, // Balanced quality/speed
    val thumbnailSize: Int = 192, // Smaller thumbnails load faster
    val enableImageCrossfade: Boolean = false, // CRITICAL: disabled for instant display
    val useRgb565: Boolean = false, // Keep quality
    val enableImagePlaceholder: Boolean = false, // No placeholder - instant display
    val deferImageLoadingOnScroll: Boolean = false, // Load immediately
    // New: Layer promotion for scroll performance
    val enableLayerPromotion: Boolean = true, // Promote list items to GPU layers
    val enableHardwareAcceleration: Boolean = true // Use hardware bitmaps
) {
    companion object {
        /**
         * Default config - NATIVE-LIKE PERFORMANCE.
         * Optimized for instant response like Android Settings app.
         */
        val Default = PerformanceConfig()
        
        /**
         * Maximum performance mode - absolute fastest rendering.
         * Use for users who want maximum speed.
         */
        val MaxPerformance = PerformanceConfig(
            animationDurationMs = 0, // Zero animations
            crossfadeDurationMs = 0,
            prefetchDistance = 10,
            maxConcurrentImageLoads = 16,
            enableComplexAnimations = false,
            enableBlurEffects = false,
            enableShadows = false,
            maxImageSize = 256, // Smaller for speed
            thumbnailSize = 128,
            enableImageCrossfade = false,
            useRgb565 = true, // Memory efficient
            enableImagePlaceholder = false,
            deferImageLoadingOnScroll = false,
            enableLayerPromotion = true,
            enableHardwareAcceleration = true
        )
        
        val LowEnd = PerformanceConfig(
            animationDurationMs = 0, // No animations
            crossfadeDurationMs = 0,
            prefetchDistance = 4,
            maxConcurrentImageLoads = 4,
            enableComplexAnimations = false,
            enableBlurEffects = false,
            enableShadows = false,
            maxImageSize = 192, // Very small for low-end
            thumbnailSize = 96,
            enableImageCrossfade = false,
            useRgb565 = true, // Save memory
            enableImagePlaceholder = false,
            deferImageLoadingOnScroll = true, // Defer on low-end
            enableLayerPromotion = false, // May cause issues on low-end
            enableHardwareAcceleration = false // May not be supported
        )
        
        val Medium = PerformanceConfig(
            animationDurationMs = 50,
            crossfadeDurationMs = 0,
            prefetchDistance = 6,
            maxConcurrentImageLoads = 8,
            enableComplexAnimations = false,
            enableBlurEffects = false,
            enableShadows = false,
            maxImageSize = 320,
            thumbnailSize = 160,
            enableImageCrossfade = false,
            useRgb565 = false,
            enableImagePlaceholder = false,
            deferImageLoadingOnScroll = false,
            enableLayerPromotion = true,
            enableHardwareAcceleration = true
        )
        
        /**
         * High quality mode - for users who prefer visual polish.
         * Still optimized but with some visual enhancements.
         */
        val HighQuality = PerformanceConfig(
            animationDurationMs = 100,
            crossfadeDurationMs = 100,
            prefetchDistance = 6,
            maxConcurrentImageLoads = 8,
            enableComplexAnimations = true,
            enableBlurEffects = false, // Still disabled - too expensive
            enableShadows = true,
            maxImageSize = 512,
            thumbnailSize = 256,
            enableImageCrossfade = true,
            useRgb565 = false,
            enableImagePlaceholder = true,
            deferImageLoadingOnScroll = false,
            enableLayerPromotion = true,
            enableHardwareAcceleration = true
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

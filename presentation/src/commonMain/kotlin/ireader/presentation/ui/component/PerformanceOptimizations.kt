package ireader.presentation.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Performance optimization utilities for Compose UI.
 * 
 * This file contains utilities and best practices for optimizing Compose performance:
 * - Minimizing recomposition scope
 * - Using stable data classes
 * - Optimizing scroll performance
 * - Profiling helpers
 */

/**
 * Marks a data class as stable for Compose to optimize recomposition.
 * Use this annotation on data classes that are used as parameters in Composables.
 * 
 * Example:
 * ```
 * @Stable
 * data class BookDisplayData(
 *     val id: Long,
 *     val title: String,
 *     val coverUrl: String?
 * )
 * ```
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class StableData

/**
 * Optimizes a Modifier for scroll performance by using graphicsLayer.
 * This promotes the composable to a separate layer, reducing recomposition impact.
 * 
 * Use this for list items that have complex layouts or animations.
 */
fun Modifier.optimizeForScroll(): Modifier = this.graphicsLayer {
    // Using graphicsLayer promotes to a separate layer
    // This reduces the impact of recomposition on scroll performance
}

/**
 * Creates a stable reference to a lambda to prevent unnecessary recompositions.
 * 
 * @param key The key to determine when to recreate the lambda
 * @param block The lambda to stabilize
 * @return A stable lambda reference
 */
@Composable
inline fun <T> rememberStableLambda(
    key: Any?,
    crossinline block: (T) -> Unit
): (T) -> Unit {
    return remember(key) {
        { value: T -> block(value) }
    }
}

/**
 * Performance monitoring data class for tracking scroll performance.
 */
@Stable
data class ScrollPerformanceMetrics(
    val averageFps: Float = 0f,
    val droppedFrames: Int = 0,
    val totalFrames: Int = 0
) {
    val frameDropRate: Float
        get() = if (totalFrames > 0) droppedFrames.toFloat() / totalFrames else 0f
}

/**
 * Best practices for list performance:
 * 
 * 1. Use proper keys in items() - Use stable, unique identifiers
 * 2. Use contentType in items() - Helps Compose recycle views efficiently
 * 3. Use remember for computed values - Avoid recalculating on every recomposition
 * 4. Use derivedStateOf for derived state - Only recompute when dependencies change
 * 5. Minimize recomposition scope - Keep composables small and focused
 * 6. Use graphicsLayer for complex items - Promotes to separate layer
 * 7. Avoid heavy operations in composition - Move to LaunchedEffect or ViewModel
 * 8. Use appropriate image sizes - Don't load full-resolution images for thumbnails
 * 9. Enable hardware acceleration - Use allowHardware(true) for images
 * 10. Limit animation delays - Only animate first few items in lists
 */

/**
 * Optimization checklist for list items:
 * 
 * ✓ Proper key function provided
 * ✓ ContentType specified
 * ✓ Computed values cached with remember
 * ✓ Derived state uses derivedStateOf
 * ✓ Click handlers are stable
 * ✓ Images use appropriate sizes
 * ✓ Complex layouts use graphicsLayer
 * ✓ Animations are limited for performance
 * ✓ No heavy operations in composition
 * ✓ Recomposition scope is minimized
 */

/**
 * Helper to check if a value should trigger recomposition.
 * Use this to debug unnecessary recompositions.
 */
@Composable
fun <T> rememberWithLogging(
    key: Any?,
    tag: String = "Remember",
    calculation: () -> T
): T {
    return remember(key) {
        println("[$tag] Recomputing for key: $key")
        calculation()
    }
}

/**
 * Optimized scroll state that tracks performance metrics.
 * This can be used to monitor scroll performance in debug builds.
 */
@Stable
class OptimizedScrollState {
    private var lastFrameTime = 0L
    private var frameCount = 0
    private var droppedFrameCount = 0
    
    fun onFrame(currentTime: Long) {
        if (lastFrameTime > 0) {
            val frameDuration = currentTime - lastFrameTime
            val targetFrameDuration = 16_666_667L // 60 FPS = ~16.67ms per frame
            
            if (frameDuration > targetFrameDuration * 1.5) {
                droppedFrameCount++
            }
            frameCount++
        }
        lastFrameTime = currentTime
    }
    
    fun getMetrics(): ScrollPerformanceMetrics {
        val avgFps = if (frameCount > 0) {
            1_000_000_000f / ((lastFrameTime - 0) / frameCount.toFloat())
        } else 0f
        
        return ScrollPerformanceMetrics(
            averageFps = avgFps,
            droppedFrames = droppedFrameCount,
            totalFrames = frameCount
        )
    }
    
    fun reset() {
        lastFrameTime = 0L
        frameCount = 0
        droppedFrameCount = 0
    }
}

/**
 * Performance tips for 60 FPS scroll:
 * 
 * 1. Keep list items lightweight
 *    - Minimize the number of composables per item
 *    - Use simple layouts when possible
 *    - Avoid nested LazyColumns/LazyRows
 * 
 * 2. Optimize images
 *    - Use appropriate image sizes (don't load 4K images for thumbnails)
 *    - Enable hardware bitmaps
 *    - Use proper caching strategies
 *    - Consider using placeholder images
 * 
 * 3. Minimize recomposition
 *    - Use remember for computed values
 *    - Use derivedStateOf for derived state
 *    - Keep composables small and focused
 *    - Use stable data classes
 * 
 * 4. Use proper keys
 *    - Use stable, unique identifiers
 *    - Avoid using index as key
 *    - Keys should be consistent across recompositions
 * 
 * 5. Limit animations
 *    - Only animate visible items
 *    - Limit stagger animations to first 20-30 items
 *    - Use simple animations for list items
 * 
 * 6. Profile and measure
 *    - Use Compose Layout Inspector
 *    - Monitor recomposition counts
 *    - Check for unnecessary recompositions
 *    - Measure scroll performance
 */

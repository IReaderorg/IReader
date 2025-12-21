package ireader.presentation.imageloader

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrokenImage
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope.Companion.DefaultFilterQuality
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Size
import ireader.core.log.Log
import ireader.presentation.ui.component.LocalPerformanceConfig
import ireader.presentation.ui.component.PerformanceConfig
import ireader.presentation.ui.component.components.LoadingScreen
import kotlin.time.measureTime


/**
 * Enhanced IImageLoader with Mihon's optimization patterns
 * Features:
 * - Proper caching strategies with memory and disk cache
 * - Placeholder handling with smooth transitions
 * - Memory-efficient loading with size constraints
 * - Performance monitoring and logging
 * - Accessibility improvements
 */
@Composable
fun IImageLoader(
    model: Any?,
    modifier: Modifier,
    placeholder: ColorPainter? = null,
    errorModifier: Modifier = modifier,
    contentDescription: String?,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
    contentAlignment: Alignment = Alignment.Center,
    onLoading: (@Composable BoxScope.(Float) -> Unit)? = {
        LoadingScreen(
            progress = it.coerceIn(0.0F, 1.0F),
            modifier = modifier then Modifier.fillMaxSize()
        )
    },
    onSuccess: (@Composable BoxScope.() -> Unit) = {},
    onError: (@Composable BoxScope.(Throwable) -> Unit)? = {
        Box(
            modifier = errorModifier then Modifier.fillMaxSize()
                .background(Color(0x1F888888)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.BrokenImage,
                contentDescription = contentDescription ?: "Failed to load image",
                tint = Color(0x1F888888),
                modifier = Modifier.size(24.dp)
            )
        }
    },
    animationSpec: FiniteAnimationSpec<Float>? = tween(),
    enableMemoryCache: Boolean = true,
    enableDiskCache: Boolean = true,
    crossfadeDurationMs: Int = 200,
) = ImageLoaderImage(
    data = model ?: "",
    contentDescription = contentDescription,
    modifier = modifier,
    alignment = alignment,
    contentScale = contentScale,
    alpha = alpha,
    filterQuality = filterQuality,
    colorFilter = colorFilter,
    contentAlignment = contentAlignment,
    onLoading = onLoading,
    onFailure = onError,
    onSuccess = onSuccess,
    errorModifier = errorModifier,
    animationSpec = animationSpec,
    enableMemoryCache = enableMemoryCache,
    enableDiskCache = enableDiskCache,
    placeholder = placeholder,
    crossfadeDurationMs = crossfadeDurationMs
)

private enum class ImageLoaderImageState {
    Loading,
    Success,
    Failure
}

/**
 * NATIVE-LIKE IMAGE LOADER
 * 
 * Key optimizations for instant display:
 * 1. ZERO crossfade - images appear instantly like Settings app
 * 2. Aggressive caching - memory + disk cache always enabled
 * 3. No loading indicators - image renders immediately or shows placeholder color
 * 4. Stable keys - prevent unnecessary recomposition
 * 5. Hardware acceleration - use GPU for rendering
 */
@Composable
fun ImageLoaderImage(
    data: Any,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    errorModifier: Modifier = modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DefaultFilterQuality,
    onLoading: (@Composable BoxScope.(Float) -> Unit)? = null, // Disabled by default for instant feel
    onSuccess: (@Composable BoxScope.() -> Unit) = {},
    onFailure: (@Composable BoxScope.(Throwable) -> Unit)? = {
        Box(
            modifier = errorModifier then Modifier.fillMaxSize()
                .background(Color(0x1F888888)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.BrokenImage,
                contentDescription = contentDescription ?: "Failed to load image",
                tint = Color(0x1F888888),
                modifier = Modifier.size(24.dp)
            )
        }
    },
    contentAlignment: Alignment = Alignment.Center,
    animationSpec: FiniteAnimationSpec<Float>? = null, // Disabled by default
    enableMemoryCache: Boolean = true,
    enableDiskCache: Boolean = true,
    placeholder: ColorPainter? = null,
    crossfadeDurationMs: Int = 0, // ZERO by default for instant display
) {
    val performanceConfig = LocalPerformanceConfig.current
    
    // CRITICAL: Always use zero crossfade for native-like feel
    // Only enable if user explicitly wants high quality mode
    val effectiveCrossfade = if (performanceConfig.enableImageCrossfade && crossfadeDurationMs > 0) {
        minOf(crossfadeDurationMs, performanceConfig.crossfadeDurationMs)
    } else {
        0
    }
    
    // Use medium filter quality - good balance of speed and quality
    val effectiveFilterQuality = if (performanceConfig.enableHardwareAcceleration) {
        FilterQuality.Medium
    } else {
        FilterQuality.Low
    }
    
    Box(modifier.fillMaxSize(), contentAlignment) {
        // Stable key prevents unnecessary recomposition
        val stableKey = remember(data) {
            when (data) {
                is ireader.domain.models.BookCover -> "${data.cover};${data.lastModified}"
                is String -> data
                else -> data.hashCode()
            }
        }
        
        key(stableKey) {
            val context = LocalPlatformContext.current
            
            // Build optimized request - cached for performance
            val request = remember(data, effectiveCrossfade, performanceConfig.thumbnailSize) {
                val builder = when (data) {
                    is ImageRequest -> data.newBuilder()
                    else -> ImageRequest.Builder(context = context).data(data)
                }
                
                builder
                    .memoryCachePolicy(coil3.request.CachePolicy.ENABLED)
                    .diskCachePolicy(coil3.request.CachePolicy.ENABLED)
                    .crossfade(effectiveCrossfade)
                    .size(Size(performanceConfig.thumbnailSize, performanceConfig.thumbnailSize))
                    .build()
            }
            
            // Track loading state - but don't show loading UI for native feel
            var loadingState by remember { mutableStateOf(ImageLoaderImageState.Loading) }
            val error = remember { mutableStateOf<Throwable?>(null) }
            
            val painter = rememberAsyncImagePainter(
                request,
                contentScale = contentScale,
                filterQuality = effectiveFilterQuality,
                onLoading = { loadingState = ImageLoaderImageState.Loading },
                onError = {
                    error.value = it.result.throwable
                    loadingState = ImageLoaderImageState.Failure
                },
                onSuccess = { loadingState = ImageLoaderImageState.Success }
            )
            
            // NATIVE-LIKE BEHAVIOR:
            // - Always render the image immediately (no loading state UI)
            // - Only show error state if image fails to load
            // - Success callback for any additional UI
            when (loadingState) {
                ImageLoaderImageState.Failure -> {
                    onFailure?.invoke(this, error.value ?: Exception("Unknown error"))
                }
                ImageLoaderImageState.Success -> {
                    onSuccess()
                }
                ImageLoaderImageState.Loading -> {
                    // NO loading indicator - image will appear when ready
                    // This is the key to native-like feel
                }
            }
            
            // Always render the image - shows immediately when cached
            Image(
                painter = painter,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                alignment = alignment,
                contentScale = contentScale,
                alpha = alpha,
                colorFilter = colorFilter
            )
        }
    }
}

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
    onLoading: (@Composable BoxScope.(Float) -> Unit)? = {
        LoadingScreen(
            progress = it.coerceIn(0.0F, 1.0F),
            modifier = modifier then Modifier.fillMaxSize()
        )
    },
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
    animationSpec: FiniteAnimationSpec<Float>? = tween(),
    enableMemoryCache: Boolean = true,
    enableDiskCache: Boolean = true,
    placeholder: ColorPainter? = null,
    crossfadeDurationMs: Int = 200,
) {
    // Get performance config for size optimization
    val performanceConfig = LocalPerformanceConfig.current
    
    // Calculate effective crossfade duration based on performance config
    val effectiveCrossfade = remember(crossfadeDurationMs, performanceConfig) {
        if (performanceConfig.enableImageCrossfade) {
            minOf(crossfadeDurationMs, performanceConfig.crossfadeDurationMs)
        } else {
            0 // Disable crossfade on low-end devices
        }
    }
    
    // Use lower filter quality on low-end devices for faster rendering
    val effectiveFilterQuality = remember(filterQuality, performanceConfig) {
        if (!performanceConfig.enableComplexAnimations) {
            FilterQuality.Low // Faster rendering on low-end
        } else {
            filterQuality
        }
    }
    
    Box(modifier.fillMaxSize(), contentAlignment) {
        // Use a stable key based on the actual cache key to prevent unnecessary recomposition
        // when different BookCover instances have the same values
        val stableKey = remember(data) {
            when (data) {
                is ireader.domain.models.BookCover -> "${data.cover};${data.lastModified}"
                is String -> data
                else -> data.hashCode()
            }
        }
        key(stableKey) {
            val context = LocalPlatformContext.current
            
            val request = remember(data, enableMemoryCache, enableDiskCache, effectiveCrossfade, performanceConfig) {
                when (data) {
                    is ImageRequest -> data.newBuilder()
                        .memoryCachePolicy(if (enableMemoryCache) coil3.request.CachePolicy.ENABLED else coil3.request.CachePolicy.DISABLED)
                        .diskCachePolicy(if (enableDiskCache) coil3.request.CachePolicy.ENABLED else coil3.request.CachePolicy.DISABLED)
                        .crossfade(effectiveCrossfade)
                        .apply {
                            // Limit image size based on performance tier
                            val maxSize = performanceConfig.thumbnailSize
                            size(Size(maxSize, maxSize))
                        }
                        .build()
                    else -> ImageRequest.Builder(context = context)
                        .data(data)
                        .memoryCachePolicy(if (enableMemoryCache) coil3.request.CachePolicy.ENABLED else coil3.request.CachePolicy.DISABLED)
                        .diskCachePolicy(if (enableDiskCache) coil3.request.CachePolicy.ENABLED else coil3.request.CachePolicy.DISABLED)
                        .crossfade(effectiveCrossfade)
                        .apply {
                            // Limit image size based on performance tier
                            val maxSize = performanceConfig.thumbnailSize
                            size(Size(maxSize, maxSize))
                        }
                        .build()
                }
            }
            
            var loadingState by remember { mutableStateOf(ImageLoaderImageState.Loading) }
            val progress = remember { mutableStateOf(-1F) }
            val error = remember { mutableStateOf<Throwable?>(null) }
            
            val painter = rememberAsyncImagePainter(
                request,
                contentScale = contentScale,
                filterQuality = effectiveFilterQuality,
                onLoading = {
                    progress.value = 0.0F
                    loadingState = ImageLoaderImageState.Loading
                },
                onError = {
                    progress.value = 0.0F
                    error.value = it.result.throwable
                    loadingState = ImageLoaderImageState.Failure
                },
                onSuccess = {
                    progress.value = 1.0F
                    loadingState = ImageLoaderImageState.Success
                }
            )
            
            // On low-end devices, skip animation entirely for faster rendering
            val useAnimation = animationSpec != null && performanceConfig.enableComplexAnimations
            
            if (useAnimation) {
                Crossfade(loadingState, animationSpec = animationSpec!!) { state ->
                    when (state) {
                        ImageLoaderImageState.Loading -> {
                            // Show placeholder on low-end, shimmer on high-end
                            if (performanceConfig.enableImagePlaceholder && onLoading != null) {
                                onLoading(progress.value)
                            } else {
                                // Simple color placeholder for low-end
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0x1F888888))
                                )
                            }
                            // Still show the image during loading so it appears when cached
                            Image(
                                painter = painter,
                                contentDescription = contentDescription,
                                modifier = Modifier.fillMaxSize(),
                                alignment = alignment,
                                contentScale = contentScale,
                                alpha = alpha,
                                colorFilter = colorFilter,
                            )
                        }

                        ImageLoaderImageState.Success -> {
                            onSuccess()
                            Image(
                                painter = painter,
                                contentDescription = contentDescription,
                                modifier = Modifier.fillMaxSize(),
                                alignment = alignment,
                                contentScale = contentScale,
                                alpha = alpha,
                                colorFilter = colorFilter,
                            )
                        }

                        ImageLoaderImageState.Failure -> {
                            if (onFailure != null) {
                                onFailure(error.value ?: return@Crossfade)
                            }
                        }
                    }
                }
            } else {
                // No animation - direct rendering for low-end devices
                when (loadingState) {
                    ImageLoaderImageState.Loading -> {
                        // Simple placeholder
                        if (!performanceConfig.enableImagePlaceholder) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0x1F888888))
                            )
                        }
                    }
                    ImageLoaderImageState.Failure -> {
                        if (onFailure != null) {
                            onFailure(error.value ?: Exception("Unknown error"))
                        }
                    }
                    else -> {}
                }
                // Always render the image
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
}

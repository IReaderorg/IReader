package ireader.presentation.ui.core.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

/**
 * AsyncImage component for loading images asynchronously with caching and loading states.
 * 
 * This implementation uses Coil3 for image loading, which provides:
 * - Asynchronous image loading from URLs
 * - Memory and disk caching
 * - Loading and error states
 * - Placeholder and error composables
 */
@Composable
fun AsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    error: @Composable (() -> Unit)? = null,
    contentScale: ContentScale = ContentScale.Fit,
    alignment: Alignment = Alignment.Center,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val context = LocalPlatformContext.current
    
    // Convert model to ImageRequest if it's not already
    val request = remember(model) {
        when (model) {
            is ImageRequest -> model
            null -> null
            else -> ImageRequest.Builder(context)
                .data(model)
                .build()
        }
    }
    
    if (request == null) {
        // Show error state if model is null
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            error?.invoke() ?: Icon(
                imageVector = Icons.Default.BrokenImage,
                contentDescription = localizeHelper.localize(Res.string.error_loading_image),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
        return
    }
    
    var imageState by remember { mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty) }
    
    val painter = rememberAsyncImagePainter(
        model = request,
        contentScale = contentScale,
        filterQuality = filterQuality,
        onState = { state ->
            imageState = state
        }
    )
    
    Box(modifier = modifier) {
        when (val state = imageState) {
            is AsyncImagePainter.State.Loading -> {
                // Show loading state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    placeholder?.invoke() ?: CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            is AsyncImagePainter.State.Success -> {
                // Show the loaded image
                Image(
                    painter = painter,
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale,
                    alignment = alignment,
                    alpha = alpha,
                    colorFilter = colorFilter
                )
            }
            
            is AsyncImagePainter.State.Error -> {
                // Show error state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.Center
                ) {
                    error?.invoke() ?: Icon(
                        imageVector = Icons.Default.BrokenImage,
                        contentDescription = localizeHelper.localize(Res.string.error_loading_image),
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            is AsyncImagePainter.State.Empty -> {
                // Show placeholder for empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    placeholder?.invoke()
                }
            }
        }
    }
}

/**
 * Simplified overload for AsyncImage with just URL and content description
 */
@Composable
fun AsyncImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    AsyncImage(
        model = url,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale
    )
}

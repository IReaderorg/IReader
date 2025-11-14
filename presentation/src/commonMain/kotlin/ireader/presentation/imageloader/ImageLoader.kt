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
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.AsyncImagePainter.State.Empty.painter
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import ireader.core.log.Log
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.components.LoadingScreen


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
        LaunchedEffect(it) {
            Log.warn(it, "Error loading image")
        }
        Box(
            modifier = errorModifier then Modifier.fillMaxSize()
                .background(Color(0x1F888888)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.BrokenImage,
                contentDescription = null,
                tint = Color(0x1F888888),
                modifier = Modifier.size(24.dp)
            )
        }
    },
    animationSpec: FiniteAnimationSpec<Float>? = tween()
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
    animationSpec = animationSpec

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
        LaunchedEffect(it) {
            Log.warn(it, "Error loading image")
        }
        Box(
            modifier = errorModifier then Modifier.fillMaxSize()
                .background(Color(0x1F888888)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.BrokenImage,
                contentDescription = null,
                tint = Color(0x1F888888),
                modifier = Modifier.size(24.dp)
            )
        }
    },
    contentAlignment: Alignment = Alignment.Center,
    animationSpec: FiniteAnimationSpec<Float>? = tween()
) {
    Box(modifier.fillMaxSize(), contentAlignment) {
        key(data) {
            val context = LocalPlatformContext.current
            val request = remember(data) {
                when (data) {
                    is ImageRequest -> data
                    else -> ImageRequest.Builder(context = context).data(data).build()
                }

            }
            var loadingState by remember { mutableStateOf(ImageLoaderImageState.Loading) }
            val progress = remember { mutableStateOf(-1F) }
            val error = remember { mutableStateOf<Throwable?>(null) }
            val painter = rememberAsyncImagePainter(
                request,
                contentScale = contentScale,
                filterQuality = filterQuality,
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
            if (animationSpec != null) {
                Crossfade(loadingState, animationSpec = animationSpec) { state ->
                    when (state) {
                        ImageLoaderImageState.Loading -> if (onLoading != null) {
                            onLoading(progress.value)
                        }

                        ImageLoaderImageState.Success -> {
                            onSuccess()
//                            Image(
//                                painter = painter,
//                                contentDescription = contentDescription,
//                                modifier = Modifier.fillMaxSize(),
//                                alignment = alignment,
//                                contentScale = contentScale,
//                                alpha = alpha,
//                                colorFilter = colorFilter,
//
//                                )
                        }

                        ImageLoaderImageState.Failure -> {
                            if (onFailure != null) {
                                onFailure(error.value ?: return@Crossfade)
                            }
                        }
                    }
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

            } else {

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

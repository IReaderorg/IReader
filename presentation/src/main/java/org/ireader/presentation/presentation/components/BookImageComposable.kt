package org.ireader.presentation.presentation.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.compose.LocalImageLoader
import coil.compose.rememberImagePainter
import coil.request.CachePolicy
import okhttp3.Headers
import okhttp3.internal.addHeaderLenient
import org.ireader.presentation.R

@OptIn(ExperimentalCoilApi::class)
@Composable
fun BookImageComposable(
    image: Any,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.TopCenter,
    contentScale: ContentScale = ContentScale.FillHeight,
    iconBadge: (@Composable () -> Unit)? = null,
    headers: Headers? = null,
    @DrawableRes placeholder: Int = R.drawable.ic_no_image_placeholder,
) {

    val painter = rememberImagePainter(data = image, imageLoader = LocalImageLoader.current) {
        crossfade(durationMillis = 700)
        placeholder(placeholder)
        error(placeholder)
        addHeader("cache-control", "max-age")
        if (headers != null) {
            headers(headers)
        }
    }

    Image(
        modifier = modifier,
        contentScale = contentScale,
        painter = painter,
        contentDescription = "an image",
        alignment = alignment,
    )
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun CircleImageComposable(
    image: Any,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.TopCenter,
    contentScale: ContentScale = ContentScale.FillHeight,
    iconBadge: (@Composable () -> Unit)? = null,
    headers: Headers? = null,
) {
    val context = LocalContext.current


    CompositionLocalProvider(LocalImageLoader provides ImageLoader(context)) {
        LocalImageLoader.current.newBuilder().apply {
            crossfade(durationMillis = 700)
            placeholder(R.drawable.ic_wallpaper)
            error(R.drawable.ic_wallpaper)
            memoryCachePolicy(CachePolicy.ENABLED)
            addHeaderLenient(Headers.Builder(), "cache-control", "max-age")
        }

    }
}
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
import coil.util.CoilUtils
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.internal.addHeaderLenient
import org.ireader.presentation.R
import org.koin.androidx.compose.get

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
    val context = LocalContext.current
    val okHttpClient: OkHttpClient = get()

    val painter = rememberImagePainter(data = image) {
        crossfade(durationMillis = 700)
        placeholder(placeholder)
        error(placeholder)
        memoryCachePolicy(CachePolicy.ENABLED)
        diskCachePolicy(CachePolicy.READ_ONLY)
        addHeader("cache-control", "max-age")
        if (headers != null) {
            headers(headers)
        }
        okHttpClient.newBuilder()
            .cache(CoilUtils.createDefaultCache(context))
            .build()
    }
    Image(
        modifier = modifier,
        contentScale = contentScale,
        painter = painter,
        contentDescription = "an image",
        alignment = alignment
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
    // Get
    val imageLoader = LocalImageLoader.current

// Set
    CompositionLocalProvider(LocalImageLoader provides ImageLoader(context)) {
        imageLoader.newBuilder().apply {
            crossfade(durationMillis = 700)
            placeholder(R.drawable.ic_wallpaper)
            error(R.drawable.ic_wallpaper)
            memoryCachePolicy(CachePolicy.ENABLED)
            addHeaderLenient(Headers.Builder(), "cache-control", "max-age")
//            if (headers != null) {
//                addHeaderLenient(headers,"")
//            }
        }

    }
//    val okHttpClient: OkHttpClient = get()
//    val painter = rememberImagePainter(data = image) {
//        crossfade(durationMillis = 700)
//        placeholder(R.drawable.ic_wallpaper)
//        error(R.drawable.ic_wallpaper)
//        memoryCachePolicy(CachePolicy.ENABLED)
//        addHeader("cache-control", "max-age")
//        if (headers != null) {
//            headers(headers)
//        }
//        okHttpClient.newBuilder()
//            .cache(CoilUtils.createDefaultCache(context))
//            .build()
//    }
//    Image(
//        modifier = modifier,
//        contentScale = contentScale,
//        painter = painter,
//        contentDescription = "an image",
//        alignment = alignment
//    )
}
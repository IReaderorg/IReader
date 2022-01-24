package ir.kazemcodes.infinity.core.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import coil.request.CachePolicy
import coil.util.CoilUtils
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import com.zhuinden.simplestackextensions.servicesktx.lookup
import ir.kazemcodes.infinity.R
import okhttp3.Headers
import okhttp3.OkHttpClient

@OptIn(ExperimentalCoilApi::class)
@Composable
fun BookImageComposable(
    image: Any,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.TopCenter,
    contentScale: ContentScale = ContentScale.FillHeight,
    iconBadge: (@Composable () -> Unit)? = null,
    headers: Headers?=null
) {
    val context = LocalContext.current
    val backstack = LocalBackstack.current
    val okHttpClient = remember {
        backstack.lookup<OkHttpClient>()
    }
    val painter = rememberImagePainter(data = image) {
        crossfade(durationMillis = 700)
        placeholder(R.drawable.ic_no_image_placeholder)
        error(R.drawable.ic_no_image_placeholder)
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
    Box {
        Image(
            modifier = modifier,
            painter = painter,
            contentDescription = "image",
            alignment = alignment,
            contentScale = contentScale,
        )
        if (iconBadge != null) {
            iconBadge()
        }
    }

}
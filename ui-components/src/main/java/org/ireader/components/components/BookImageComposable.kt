package org.ireader.components.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.ireader.image_loader.BookCover

@Composable
fun BookImageComposable(
    image: BookCover,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.TopCenter,
    contentScale: ContentScale = ContentScale.FillHeight,
    iconBadge: (@Composable () -> Unit)? = null,
    useSavedCoverImage: Boolean = false,
    headers: ((url:String) -> okhttp3.Headers?)? = null
) {
    val context = LocalContext.current
    AsyncImage(
        modifier = modifier.fillMaxSize(),
        contentScale = contentScale,
        model = if (image.favorite) image else headers?.let { it(image.cover) }
            ?.let { ImageRequest.Builder(context).headers(it).data(image.cover).build() }?:  image.cover,
        contentDescription = "an image",
        alignment = alignment,
    )
}

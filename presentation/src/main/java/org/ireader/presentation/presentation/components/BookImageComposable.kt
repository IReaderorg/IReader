package org.ireader.presentation.presentation.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import org.ireader.domain.feature_service.io.BookCover
import org.ireader.presentation.R

@OptIn(ExperimentalCoilApi::class)
@Composable
fun BookImageComposable(
    image: BookCover,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.TopCenter,
    contentScale: ContentScale = ContentScale.FillHeight,
    iconBadge: (@Composable () -> Unit)? = null,
    @DrawableRes placeholder: Int = R.drawable.ic_no_image_placeholder,
) {

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .apply {
                data(image)
                crossfade(700)
                placeholder(placeholder)
                error(placeholder)
            }
            .build(),
        contentScale = ContentScale.Crop
    )

    Image(
        modifier = modifier
            .fillMaxSize(),
        contentScale = contentScale,
        painter = painter,
        contentDescription = "an image",
        alignment = alignment,
    )
}
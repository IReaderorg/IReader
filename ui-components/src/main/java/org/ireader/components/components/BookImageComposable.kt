package org.ireader.components.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import org.ireader.core_ui.R
import org.ireader.image_loader.BookCover

@OptIn(ExperimentalCoilApi::class)
@Composable
fun BookImageComposable(
    image: BookCover,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.TopCenter,
    contentScale: ContentScale = ContentScale.FillHeight,
    iconBadge: (@Composable () -> Unit)? = null,
    showLoading:Boolean = false,
    @DrawableRes placeholder: Int = R.drawable.ic_no_image_placeholder,
) {
    Box(modifier = modifier
        .fillMaxSize(),) {
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
        if (showLoading && !image.favorite && painter.state is AsyncImagePainter.State.Loading) {
            ShowLoading(modifier = Modifier.align(Alignment.Center), size = 24.dp)
        }

        Image(
            modifier = Modifier.fillMaxSize(),
            contentScale = contentScale,
            painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .apply {
                        data(image)
                        crossfade(700)
                        error(placeholder)
                        if (!showLoading) {
                            placeholder(placeholder)
                        }
                    }
                    .build(),
                contentScale = ContentScale.Crop
            ),
            contentDescription = "an image",
            alignment = alignment,
        )
    }

}

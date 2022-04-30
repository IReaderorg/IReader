package org.ireader.components.components

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
    @DrawableRes placeholder: Int = R.drawable.ic_no_image_placeholder,
) {

//    val painter = rememberAsyncImagePainter(
//        model = ImageRequest.Builder(LocalContext.current)
//            .apply {
//                data(image)
//                crossfade(700)
//                placeholder(placeholder)
//                error(placeholder)
//            }
//            .build(),
//        contentScale = ContentScale.Crop
//    )
//    SubcomposeAsyncImage(
//        model = ImageRequest.Builder(LocalContext.current)
//            .apply {
//                data(image)
//                crossfade(700)
//                placeholder(placeholder)
//                error(placeholder)
//            }
//            .build(),
//        contentDescription = "image",
//    )


    Image(
        modifier = modifier
            .fillMaxSize(),
        contentScale = contentScale,
        painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .apply {
                data(image)
                crossfade(700)
                placeholder(placeholder)
                error(placeholder)
            }
            .build(),
        contentScale = ContentScale.Crop
    ),
        contentDescription = "an image",
        alignment = alignment,
    )
}

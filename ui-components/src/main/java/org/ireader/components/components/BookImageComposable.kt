package org.ireader.components.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.AsyncImage
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
//        val painter = rememberAsyncImagePainter(
//            model = ImageRequest.Builder(LocalContext.current)
//                .apply {
//                    data(image)
//                    crossfade(700)
//                    placeholder(placeholder)
//                    error(placeholder)
//                }
//                .build(),
//            contentScale = ContentScale.Crop
//        )
        var isLoaded by remember {
            mutableStateOf(false)
        }
        var isLoading by remember {
            mutableStateOf(false)
        }
        if (showLoading && !image.favorite && !isLoaded) {
            ShowLoading(modifier = Modifier.align(Alignment.Center), size = 16.dp)
        }


        AsyncImage(
            modifier = Modifier.fillMaxSize(),
            contentScale = contentScale,
            model = image,
            placeholder = if (!showLoading) { painterResource(id = placeholder) } else null,
            error =  painterResource(id = placeholder),
            onSuccess = {
                isLoaded = true
            },
            onError = {
                isLoaded = true
            },
            onLoading = {
                isLoading = true
            },
            contentDescription = "an image",
            alignment = alignment,
            filterQuality = FilterQuality.High,
        )

//        Image(
//            modifier = Modifier.fillMaxSize(),
//            contentScale = contentScale,
//            painter = rememberAsyncImagePainter(
//                model = ImageRequest.Builder(LocalContext.current)
//                    .apply {
//                        data(image)
//                        crossfade(700)
//                        error(placeholder)
//                        if (!showLoading) {
//                            placeholder(placeholder)
//                        }
//                    }
//                    .build(),
//                contentScale = ContentScale.Crop
//            ),
//            contentDescription = "an image",
//            alignment = alignment,
//        )
    }

}

package org.ireader.components.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import org.ireader.image_loader.BookCover


@Composable
fun BookImageComposable(
    image: BookCover,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.TopCenter,
    contentScale: ContentScale = ContentScale.FillHeight,
    iconBadge: (@Composable () -> Unit)? = null,
    showLoading:Boolean = false,
) {
        AsyncImage(
            modifier = modifier.fillMaxSize(),
            contentScale = contentScale,
            model = image.cover,
            contentDescription = "an image",
            alignment = alignment,
        )
}

package ireader.presentation.ui.component.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import io.ktor.http.*
import ireader.domain.models.BookCover
import ireader.presentation.imageloader.IImageLoader

@Composable
fun IBookImageComposable(
        image: BookCover,
        modifier: Modifier = Modifier,
        alignment: Alignment = Alignment.TopCenter,
        contentScale: ContentScale = ContentScale.FillHeight,
        headers: ((url: String) -> okhttp3.Headers?)? = null
) {
    IImageLoader(
            modifier = modifier.fillMaxSize(),
            contentScale = contentScale,
            model =  Url(image.cover?:""),
            contentDescription = "an image",
            alignment = alignment,
    )
}

package ireader.presentation.ui.component.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import coil3.toUri
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
            model =  image.cover?.toUri(), // coil3 only supports Uri data type right now
            contentDescription = "an image",
            alignment = alignment,
    )
}
enum class BookImageCover(val ratio: Float) {
    Square(1f / 1f),
    Book(2f / 3f);

    @Composable
    operator fun invoke(
            modifier: Modifier = Modifier,
            data: Any?,
            contentDescription: String? = null,
            shape: Shape? = null,
            onClick: (() -> Unit)? = null,
    ) {
        IImageLoader(
                model = data,
                placeholder = ColorPainter(CoverPlaceholderColor),
                contentDescription = contentDescription,
                modifier = modifier
                        .aspectRatio(ratio)
                        .clip(shape ?: RoundedCornerShape(4.dp))
                        .then(
                                if (onClick != null) {
                                    Modifier.clickable(
                                            role = Role.Button,
                                            onClick = onClick,
                                    )
                                } else Modifier,
                        ),
                contentScale = ContentScale.Crop,
        )
    }
}

private val CoverPlaceholderColor = Color(0x1F888888)
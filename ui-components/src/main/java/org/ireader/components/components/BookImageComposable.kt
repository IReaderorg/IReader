package org.ireader.components.components

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
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
    headers: ((url: String) -> okhttp3.Headers?)? = null
) {
    val context = LocalContext.current
    AsyncImage(
        modifier = modifier.fillMaxSize(),
        contentScale = contentScale,
        model = headers?.let { it(image.cover) }
            ?.let { ImageRequest.Builder(context).headers(it).data(image.cover).build() } ?: image.cover,
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
        AsyncImage(
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

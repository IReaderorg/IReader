package ireader.presentation.ui.component.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import ireader.domain.models.BookCover

@Composable
expect fun IBookImageComposable(
    image: BookCover,
    modifier: Modifier,
    alignment: Alignment,
    contentScale: ContentScale,
    headers: ((url: String) -> okhttp3.Headers?)?
)
@Composable
fun BookImageComposable(
    image: BookCover,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.TopCenter,
    contentScale: ContentScale = ContentScale.FillHeight,
    headers: ((url: String) -> okhttp3.Headers?)? = null
)  = IBookImageComposable(
    image, modifier, alignment, contentScale, headers
)


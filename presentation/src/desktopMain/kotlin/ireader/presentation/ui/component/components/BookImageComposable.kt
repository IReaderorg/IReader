package ireader.presentation.ui.component.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import ireader.domain.models.BookCover
import okhttp3.Headers

@Composable
actual fun IBookImageComposable(
    image: BookCover,
    modifier: Modifier,
    alignment: Alignment,
    contentScale: ContentScale,
    headers: ((url: String) -> Headers?)?
) {
}
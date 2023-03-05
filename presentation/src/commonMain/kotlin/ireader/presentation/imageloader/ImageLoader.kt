package ireader.presentation.imageloader

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale

@Composable
expect fun ImageLoader(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier,
    alignment: Alignment ,
    contentScale: ContentScale ,
    alpha: Float ,
    colorFilter: ColorFilter? ,
    filterQuality: FilterQuality ,
)
@Composable
fun IImageLoader(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
)  = ImageLoader(
    model, contentDescription, modifier, alignment, contentScale, alpha, colorFilter, filterQuality
)
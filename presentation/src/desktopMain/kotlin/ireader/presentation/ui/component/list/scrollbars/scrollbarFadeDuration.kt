package ireader.presentation.ui.component.list.scrollbars


import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

actual fun scrollbarFadeDuration(): Int = 500

/**
 * Code Taken from tachiyomi
 * https://github.com/tachiyomiorg/tachiyomi
 */
@Composable
actual fun VerticalFastScroller(listState: LazyListState, modifier: Modifier, thumbAllowed: () -> Boolean, thumbColor: Color, topContentPadding: Dp, bottomContentPadding: Dp, endContentPadding: Dp, content: @Composable () -> Unit) {
    content()
}

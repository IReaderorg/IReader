package ireader.presentation.ui.component.list.scrollbars

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ireader.domain.models.prefs.PreferenceValues

@Composable
expect fun LazyColumnScrollbar(
        listState: LazyListState,
        rightSide: Boolean = true,
        thickness: Dp = 6.dp,
        padding: Dp = 8.dp,
        thumbMinHeight: Float = 0.1f,
        thumbColor: Color,
        thumbSelectedColor: Color,
        indicatorContent: (@Composable (index: Int, isThumbSelected: Boolean) -> Unit)?,
        thumbShape: Shape ,
        enable: Boolean,
        selectionMode: PreferenceValues.ScrollbarSelectionMode,
        content: @Composable () -> Unit,
)
@Composable
fun ILazyColumnScrollbar(
        listState: LazyListState,
        rightSide: Boolean = true,
        thickness: Dp = 6.dp,
        padding: Dp = 8.dp,
        thumbMinHeight: Float = 0.1f,
        thumbColor: Color = MaterialTheme.colorScheme.primaryContainer,
        thumbSelectedColor: Color = MaterialTheme.colorScheme.primary,
        indicatorContent: (@Composable (index: Int, isThumbSelected: Boolean) -> Unit)? = null,
        thumbShape: Shape = CircleShape,
        enable: Boolean = true,
        selectionMode: PreferenceValues.ScrollbarSelectionMode = PreferenceValues.ScrollbarSelectionMode.Thumb,
        content: @Composable () -> Unit,
) = LazyColumnScrollbar(listState, rightSide, thickness, padding, thumbMinHeight, thumbColor, thumbSelectedColor, indicatorContent, thumbShape, enable, selectionMode, content)
package ireader.presentation.ui.component.list.scrollbars

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ireader.domain.models.prefs.PreferenceValues

@Composable
expect fun ColumnScrollbar(
        state: ScrollState,
        rightSide: Boolean ,
        thickness: Dp,
        padding: Dp,
        thumbMinHeight: Float ,
        thumbColor: Color ,
        thumbSelectedColor: Color,
        thumbShape: Shape ,
        enabled: Boolean ,
        selectionMode: PreferenceValues.ScrollbarSelectionMode ,
        indicatorContent: (@Composable (normalizedOffset: Float, isThumbSelected: Boolean) -> Unit)? ,
        content: @Composable () -> Unit
)

@Composable
fun IColumnScrollbar(
        state: ScrollState,
        rightSide: Boolean = true,
        thickness: Dp = 6.dp,
        padding: Dp = 8.dp,
        thumbMinHeight: Float = 0.1f,
        thumbColor: Color = Color(0xFF2A59B6),
        thumbSelectedColor: Color = Color(0xFF5281CA),
        thumbShape: Shape = CircleShape,
        enabled: Boolean = true,
        selectionMode: PreferenceValues.ScrollbarSelectionMode = PreferenceValues.ScrollbarSelectionMode.Thumb,
        indicatorContent: (@Composable (normalizedOffset: Float, isThumbSelected: Boolean) -> Unit)? = null,
        content: @Composable () -> Unit
) = ColumnScrollbar(state, rightSide, thickness, padding, thumbMinHeight, thumbColor, thumbSelectedColor, thumbShape, enabled, selectionMode, indicatorContent, content)
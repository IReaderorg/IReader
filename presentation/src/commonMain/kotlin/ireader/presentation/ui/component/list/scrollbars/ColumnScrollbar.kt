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

/**
 * RTL-aware Column scrollbar wrapper.
 * In RTL layouts, the scrollbar position is automatically mirrored unless explicitly set.
 * 
 * @param rightSide If true, scrollbar appears on the right in LTR and left in RTL.
 *                  If false, scrollbar appears on the left in LTR and right in RTL.
 */
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
) {
    // RTL support: Mirror scrollbar position in RTL layouts
    val layoutDirection = androidx.compose.ui.platform.LocalLayoutDirection.current
    val isRtl = layoutDirection == androidx.compose.ui.unit.LayoutDirection.Rtl
    val adjustedRightSide = if (isRtl) !rightSide else rightSide
    
    ColumnScrollbar(state, adjustedRightSide, thickness, padding, thumbMinHeight, thumbColor, thumbSelectedColor, thumbShape, enabled, selectionMode, indicatorContent, content)
}
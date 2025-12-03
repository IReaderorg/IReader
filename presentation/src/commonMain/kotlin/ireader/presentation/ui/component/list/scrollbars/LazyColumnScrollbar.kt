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
/**
 * RTL-aware LazyColumn scrollbar wrapper.
 * In RTL layouts, the scrollbar position is automatically mirrored unless explicitly set.
 * 
 * @param rightSide If true, scrollbar appears on the right in LTR and left in RTL.
 *                  If false, scrollbar appears on the left in LTR and right in RTL.
 */
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
) {
    // RTL support: Mirror scrollbar position in RTL layouts
    val layoutDirection = androidx.compose.ui.platform.LocalLayoutDirection.current
    val isRtl = layoutDirection == androidx.compose.ui.unit.LayoutDirection.Rtl
    val adjustedRightSide = if (isRtl) !rightSide else rightSide
    
    LazyColumnScrollbar(listState, adjustedRightSide, thickness, padding, thumbMinHeight, thumbColor, thumbSelectedColor, indicatorContent, thumbShape, enable, selectionMode, content)
}

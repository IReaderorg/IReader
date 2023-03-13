package ireader.presentation.ui.component.list.scrollbars

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import ireader.domain.models.prefs.PreferenceValues

@Composable
actual fun LazyColumnScrollbar(listState: LazyListState, rightSide: Boolean, thickness: Dp, padding: Dp, thumbMinHeight: Float, thumbColor: Color, thumbSelectedColor: Color, indicatorContent: @Composable ((index: Int, isThumbSelected: Boolean) -> Unit)?, thumbShape: Shape, enable: Boolean, selectionMode: PreferenceValues.ScrollbarSelectionMode, content: @Composable () -> Unit) {
    content()
}
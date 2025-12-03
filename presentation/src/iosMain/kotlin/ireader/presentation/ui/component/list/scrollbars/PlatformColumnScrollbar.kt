package ireader.presentation.ui.component.list.scrollbars

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import ireader.domain.models.prefs.PreferenceValues

@Composable
actual fun ColumnScrollbar(
    state: ScrollState,
    rightSide: Boolean,
    thickness: Dp,
    padding: Dp,
    thumbMinHeight: Float,
    thumbColor: Color,
    thumbSelectedColor: Color,
    thumbShape: Shape,
    enabled: Boolean,
    selectionMode: PreferenceValues.ScrollbarSelectionMode,
    indicatorContent: @Composable ((normalizedOffset: Float, isThumbSelected: Boolean) -> Unit)?,
    content: @Composable () -> Unit
) {
    // iOS uses native scrollbars
    Box(modifier = Modifier.fillMaxSize()) {
        content()
    }
}

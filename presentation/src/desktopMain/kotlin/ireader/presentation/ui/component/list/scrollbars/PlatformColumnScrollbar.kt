package ireader.presentation.ui.component.list.scrollbars

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ireader.domain.models.prefs.PreferenceValues

@Composable
actual fun ColumnScrollbar(state: ScrollState, rightSide: Boolean, thickness: Dp, padding: Dp, thumbMinHeight: Float, thumbColor: Color, thumbSelectedColor: Color, thumbShape: Shape, enabled: Boolean, selectionMode: PreferenceValues.ScrollbarSelectionMode, indicatorContent: @Composable ((normalizedOffset: Float, isThumbSelected: Boolean) -> Unit)?, content: @Composable () -> Unit) {
    val adapter = rememberScrollbarAdapter(state)
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd)
                .fillMaxHeight(),
            adapter = adapter,
            style = ScrollbarStyle(minimalHeight = thumbMinHeight.dp,thickness = thickness, shape = thumbShape, unhoverColor = thumbColor, hoverColor = thumbSelectedColor, hoverDurationMillis = 500
        )
)
        content()
    }

}
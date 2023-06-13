package ireader.presentation.ui.core.modifier

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

actual fun Modifier.systemGestureExclusion(): Modifier = this


@Composable
actual fun Modifier.supportDesktopScroll(
    scrollState: ScrollState,
    scope: CoroutineScope,enable:Boolean
): Modifier {
    return if (enable) {
        this.draggable(
            orientation = Orientation.Vertical,
            state = rememberDraggableState { delta ->
                scope.launch {
                    scrollState.scrollBy(-delta)
                }
            },
        )
    } else {
        this
    }
}

@Composable
actual fun Modifier.supportDesktopScroll(
    scrollState: LazyListState,
    scope: CoroutineScope,enable:Boolean
): Modifier  {
    return if (enable) {
        this.draggable(
            orientation = Orientation.Vertical,
            state = rememberDraggableState { delta ->
                scope.launch {
                    scrollState.scrollBy(-delta)
                }
            },
        )
    } else {
        this
    }
}
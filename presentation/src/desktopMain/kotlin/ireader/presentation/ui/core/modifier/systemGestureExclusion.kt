package ireader.presentation.ui.core.modifier

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

actual fun Modifier.systemGestureExclusion(): Modifier = this


@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun Modifier.supportDesktopScroll(
    scrollState: ScrollState,
    scope: CoroutineScope,
    enable: Boolean
): Modifier {
    return if (enable) {
        this
            // Enhanced mouse wheel and trackpad support with smooth scrolling
            .onPointerEvent(PointerEventType.Scroll) { event ->
                val delta = event.changes.first().scrollDelta
                scope.launch {
                    // Multiply by 40 for better scroll speed (standard scroll unit)
                    // Negative because scroll delta is inverted
                    scrollState.scrollBy(-delta.y * 40f)
                }
            }
            // Keep draggable support for touch/drag interactions
            .draggable(
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun Modifier.supportDesktopScroll(
    scrollState: LazyListState,
    scope: CoroutineScope,
    enable: Boolean
): Modifier {
    return if (enable) {
        this
            // Enhanced mouse wheel and trackpad support with smooth scrolling
            .onPointerEvent(PointerEventType.Scroll) { event ->
                val delta = event.changes.first().scrollDelta
                scope.launch {
                    // Multiply by 40 for better scroll speed (standard scroll unit)
                    // Negative because scroll delta is inverted
                    scrollState.scrollBy(-delta.y * 40f)
                }
            }
            // Keep draggable support for touch/drag interactions
            .draggable(
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
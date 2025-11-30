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
    val draggableState = rememberDraggableState { delta ->
        scope.launch {
            scrollState.scrollBy(-delta)
        }
    }
    
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
                state = draggableState,
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
    val draggableState = rememberDraggableState { delta ->
        scope.launch {
            scrollState.scrollBy(-delta)
        }
    }
    
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
                state = draggableState,
            )
    } else {
        this
    }
}

/**
 * Desktop horizontal scroll support for ScrollState.
 * Enables horizontal scrolling with:
 * - Mouse wheel horizontal scroll (Shift+Scroll on most systems)
 * - Trackpad horizontal gestures
 * - Horizontal drag interactions
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun Modifier.supportDesktopHorizontalScroll(
    scrollState: ScrollState,
    scope: CoroutineScope,
    enable: Boolean
): Modifier {
    val draggableState = rememberDraggableState { delta ->
        scope.launch {
            scrollState.scrollBy(-delta)
        }
    }
    
    return if (enable) {
        this
            .onPointerEvent(PointerEventType.Scroll) { event ->
                val delta = event.changes.first().scrollDelta
                scope.launch {
                    // Handle horizontal scroll from trackpad or Shift+Scroll
                    // delta.x is horizontal scroll, delta.y with Shift key is also horizontal
                    val horizontalDelta = if (delta.x != 0f) {
                        delta.x
                    } else {
                        // When Shift is held, vertical scroll becomes horizontal
                        delta.y
                    }
                    scrollState.scrollBy(-horizontalDelta * 40f)
                }
            }
            .draggable(
                orientation = Orientation.Horizontal,
                state = draggableState,
            )
    } else {
        this
    }
}

/**
 * Desktop bidirectional scroll support for combined vertical and horizontal scrolling.
 * Enables:
 * - Vertical scrolling with mouse wheel (Y-axis)
 * - Horizontal scrolling with Shift+Scroll or trackpad horizontal gesture (X-axis)
 * - Drag interactions in both directions
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun Modifier.supportDesktopBidirectionalScroll(
    verticalScrollState: ScrollState,
    horizontalScrollState: ScrollState,
    scope: CoroutineScope,
    enable: Boolean
): Modifier {
    val verticalDraggableState = rememberDraggableState { delta ->
        scope.launch {
            verticalScrollState.scrollBy(-delta)
        }
    }
    val horizontalDraggableState = rememberDraggableState { delta ->
        scope.launch {
            horizontalScrollState.scrollBy(-delta)
        }
    }
    
    return if (enable) {
        this
            .onPointerEvent(PointerEventType.Scroll) { event ->
                val delta = event.changes.first().scrollDelta
                scope.launch {
                    // Handle vertical scroll
                    if (delta.y != 0f && delta.x == 0f) {
                        verticalScrollState.scrollBy(-delta.y * 40f)
                    }
                    // Handle horizontal scroll (trackpad or Shift+Scroll)
                    if (delta.x != 0f) {
                        horizontalScrollState.scrollBy(-delta.x * 40f)
                    }
                }
            }
            // Vertical drag
            .draggable(
                orientation = Orientation.Vertical,
                state = verticalDraggableState,
            )
            // Horizontal drag
            .draggable(
                orientation = Orientation.Horizontal,
                state = horizontalDraggableState,
            )
    } else {
        this
    }
}

/**
 * Desktop horizontal scroll support for LazyListState (LazyRow).
 * Enables horizontal scrolling with:
 * - Mouse wheel (both X and Y axis scroll horizontally for LazyRow)
 * - Trackpad horizontal gestures
 * - Horizontal drag interactions
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun Modifier.supportDesktopHorizontalLazyListScroll(
    lazyListState: LazyListState,
    scope: CoroutineScope,
    enable: Boolean
): Modifier {
    val draggableState = rememberDraggableState { delta ->
        scope.launch {
            lazyListState.scrollBy(-delta)
        }
    }
    
    return if (enable) {
        this
            .onPointerEvent(PointerEventType.Scroll) { event ->
                val delta = event.changes.first().scrollDelta
                scope.launch {
                    // For horizontal LazyRow, use both X and Y scroll deltas
                    // Y scroll (mouse wheel) should also scroll horizontally
                    val scrollAmount = if (delta.x != 0f) {
                        -delta.x * 40f
                    } else {
                        -delta.y * 40f
                    }
                    lazyListState.scrollBy(scrollAmount)
                }
            }
            .draggable(
                orientation = Orientation.Horizontal,
                state = draggableState,
            )
    } else {
        this
    }
}
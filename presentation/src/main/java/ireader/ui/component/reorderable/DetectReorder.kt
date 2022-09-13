package ireader.ui.component.reorderable

import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChangeConsumed

fun Modifier.detectReorder(state: ReorderableState<*>) =
    this.then(
        Modifier.pointerInput(Unit) {
            forEachGesture {
                awaitPointerEventScope {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var drag: PointerInputChange?
                    var overSlop = Offset.Zero
                    do {
                        drag = awaitPointerSlopOrCancellation(down.id, down.type) { change, over ->
                            change.consumePositionChange()
                            overSlop = over
                        }
                    } while (drag != null && !drag.positionChangeConsumed())
                    if (drag != null) {
                        state.ch.trySend(StartDrag(down.id, overSlop))
                    }
                }
            }
        }
    )

fun Modifier.detectReorderAfterLongPress(state: ReorderableState<*>) =
    this.then(
        Modifier.pointerInput(Unit) {
            forEachGesture {
                val down = awaitPointerEventScope {
                    awaitFirstDown(requireUnconsumed = false)
                }
                awaitLongPressOrCancellation(down)?.also {
                    state.ch.trySend(StartDrag(down.id))
                }
            }
        }
    )

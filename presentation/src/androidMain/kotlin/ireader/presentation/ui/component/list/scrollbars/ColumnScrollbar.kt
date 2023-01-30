@file:JvmName("ColumnScrollbarKt")

package ireader.presentation.ui.component.list.scrollbars

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import kotlinx.coroutines.launch
import ireader.domain.models.prefs.PreferenceValues
/**
 * Scrollbar for Column
 *
 * @param rightSide true -> right,  false -> left
 * @param thickness Thickness of the scrollbar thumb
 * @param padding   Padding of the scrollbar
 * @param thumbMinHeight Thumb minimum height proportional to total scrollbar's height (eg: 0.1 -> 10% of total)
 */
@Composable
fun ColumnScrollbar(
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
    if (!enabled) content()
    else BoxWithConstraints {
        content()
        ColumnScrollbar(
            state = state,
            rightSide = rightSide,
            thickness = thickness,
            padding = padding,
            thumbMinHeight = thumbMinHeight,
            thumbColor = thumbColor,
            thumbSelectedColor = thumbSelectedColor,
            thumbShape = thumbShape,
            visibleHeightDp = with(LocalDensity.current) { constraints.maxHeight.toDp() },
            indicatorContent = indicatorContent,
            selectionMode = selectionMode,
        )
    }
}

/**
 * Scrollbar for LazyColumn
 *
 * @param rightSide true -> right,  false -> left
 * @param thickness Thickness of the scrollbar thumb
 * @param padding   Padding of the scrollbar
 * @param thumbMinHeight Thumb minimum height proportional to total scrollbar's height (eg: 0.1 -> 10% of total)
 * @param visibleHeightDp Visible height of column view
 */
@Composable
private fun ColumnScrollbar(
    state: ScrollState,
    rightSide: Boolean = true,
    thickness: Dp = 6.dp,
    padding: Dp = 8.dp,
    thumbMinHeight: Float = 0.1f,
    thumbColor: Color = Color(0xFF2A59B6),
    thumbSelectedColor: Color = Color(0xFF5281CA),
    thumbShape: Shape = CircleShape,
    selectionMode: PreferenceValues.ScrollbarSelectionMode = PreferenceValues.ScrollbarSelectionMode.Thumb,
    indicatorContent: (@Composable (normalizedOffset: Float, isThumbSelected: Boolean) -> Unit)? = null,
    visibleHeightDp: Dp,
) {
    val coroutineScope = rememberCoroutineScope()

    var isSelected by remember { mutableStateOf(false) }

    var dragOffset by remember { mutableStateOf(0f) }

    val fullHeightDp = with(LocalDensity.current) { state.maxValue.toDp() + visibleHeightDp }

    val normalizedThumbSizeReal by remember(visibleHeightDp, state.maxValue) {
        derivedStateOf {
            if (fullHeightDp == 0.dp) 1f else {
                val normalizedDp = visibleHeightDp / fullHeightDp
                normalizedDp.coerceIn(0f, 1f)
            }
        }
    }

    val normalizedThumbSize by remember(normalizedThumbSizeReal) {
        derivedStateOf {
            normalizedThumbSizeReal.coerceAtLeast(thumbMinHeight)
        }
    }

    val normalizedThumbSizeUpdated by rememberUpdatedState(newValue = normalizedThumbSize)

    fun offsetCorrection(top: Float): Float {
        val topRealMax = 1f
        val topMax = 1f - normalizedThumbSizeUpdated
        return top * topMax / topRealMax
    }

    fun offsetCorrectionInverse(top: Float): Float {
        val topRealMax = 1f
        val topMax = 1f - normalizedThumbSizeUpdated
        if (topMax == 0f) return top
        return (top * topRealMax / topMax).coerceAtLeast(0f)
    }

    val normalizedOffsetPosition by remember {
        derivedStateOf {
            if (state.maxValue == 0) return@derivedStateOf 0f
            val normalized = state.value.toFloat() / state.maxValue.toFloat()
            offsetCorrection(normalized)
        }
    }

    fun setDragOffset(value: Float) {
        val maxValue = (1f - normalizedThumbSize).coerceAtLeast(0f)
        dragOffset = value.coerceIn(0f, maxValue)
    }

    fun setScrollOffset(newOffset: Float) {
        setDragOffset(newOffset)
        val exactIndex = offsetCorrectionInverse(state.maxValue * dragOffset).toInt()
        coroutineScope.launch {
            state.scrollTo(exactIndex)
        }
    }

    val isInAction = state.isScrollInProgress || isSelected

    val alpha by animateFloatAsState(
        targetValue = if (isInAction) 1f else 0f, animationSpec = tween(
            durationMillis = if (isInAction) 75 else 500, delayMillis = if (isInAction) 0 else 500
        )
    )

    val displacement by animateFloatAsState(
        targetValue = if (isInAction) 0f else 14f, animationSpec = tween(
            durationMillis = if (isInAction) 75 else 500, delayMillis = if (isInAction) 0 else 500
        )
    )

    BoxWithConstraints(
        Modifier
            .alpha(alpha)
            .fillMaxWidth()
    ) {
        if (indicatorContent != null) BoxWithConstraints(
            Modifier
                .align(if (rightSide) Alignment.TopEnd else Alignment.TopStart)
                .fillMaxHeight()
                .graphicsLayer {
                    translationX = (if (rightSide) displacement.dp else -displacement.dp).toPx()
                    translationY = constraints.maxHeight.toFloat() * normalizedOffsetPosition
                }) {
            ConstraintLayout(
                Modifier.align(Alignment.TopEnd)
            ) {
                val (box, content) = createRefs()
                Box(
                    Modifier
                        .fillMaxHeight(normalizedThumbSize)
                        .padding(
                            start = if (rightSide) 0.dp else padding,
                            end = if (!rightSide) 0.dp else padding,
                        )
                        .width(thickness)
                        .constrainAs(box) {
                            if (rightSide) end.linkTo(parent.end)
                            else start.linkTo(parent.start)
                        }) {}

                Box(Modifier.constrainAs(content) {
                    top.linkTo(box.top)
                    bottom.linkTo(box.bottom)
                    if (rightSide) end.linkTo(box.start)
                    else start.linkTo(box.end)
                }) {
                    indicatorContent(
                        normalizedOffset = offsetCorrectionInverse(normalizedOffsetPosition),
                        isThumbSelected = isSelected
                    )
                }
            }
        }

        BoxWithConstraints(
            Modifier
                .align(if (rightSide) Alignment.TopEnd else Alignment.TopStart)
                .fillMaxHeight()
                .draggable(
                    state = rememberDraggableState { delta ->
                        if (isSelected) {
                            setScrollOffset(dragOffset + delta / constraints.maxHeight.toFloat())
                        }
                    },
                    orientation = Orientation.Vertical,
                    enabled = selectionMode != PreferenceValues.ScrollbarSelectionMode.Disabled,
                    startDragImmediately = true,
                    onDragStarted = { offset ->
                        val newOffset = offset.y / constraints.maxHeight.toFloat()
                        val currentOffset = normalizedOffsetPosition
                        when (selectionMode) {
                            PreferenceValues.ScrollbarSelectionMode.Full -> {
                                if (newOffset in currentOffset..(currentOffset + normalizedThumbSizeUpdated))
                                    setDragOffset(currentOffset)
                                else
                                    setScrollOffset(newOffset)
                                isSelected = true
                            }
                            PreferenceValues.ScrollbarSelectionMode.Thumb -> {
                                if (newOffset in currentOffset..(currentOffset + normalizedThumbSizeUpdated)) {
                                    setDragOffset(currentOffset)
                                    isSelected = true
                                }
                            }
                            PreferenceValues.ScrollbarSelectionMode.Disabled -> Unit
                        }
                    },
                    onDragStopped = {
                        isSelected = false
                    })
                .graphicsLayer {
                    translationX = (if (rightSide) displacement.dp else -displacement.dp).toPx()
                }) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .graphicsLayer {
                        translationY = constraints.maxHeight.toFloat() * normalizedOffsetPosition
                    }
                    .padding(horizontal = padding)
                    .width(thickness)
                    .clip(thumbShape)
                    .background(if (isSelected) thumbSelectedColor else thumbColor)
                    .fillMaxHeight(normalizedThumbSize))
        }
    }
}
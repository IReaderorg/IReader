package ireader.presentation.ui.component.list.scrollbars

/*
 * MIT License
 *
 * Copyright (c) 2021 nani
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import kotlinx.coroutines.launch
import ireader.domain.models.prefs.PreferenceValues
import kotlin.math.floor


/**
 * Scrollbar for LazyColumn
 * taken from https://github.com/nanihadesuka/LazyColumnScrollbar
 *
 * @param rightSide true -> right,  false -> left
 * @param thickness Thickness of the scrollbar thumb
 * @param padding   Padding of the scrollbar
 * @param thumbMinHeight Thumb minimum height proportional to total scrollbar's height (eg: 0.1 -> 10% of total)
 */
@Composable
fun LazyColumnScrollbar(
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
    Box {
        content()
        if (enable) {
            LazyColumnScrollbar(
                listState = listState,
                rightSide = rightSide,
                thickness = thickness,
                padding = padding,
                thumbMinHeight = thumbMinHeight,
                thumbColor = thumbColor,
                thumbSelectedColor = thumbSelectedColor,
                thumbShape = thumbShape,
                indicatorContent = indicatorContent,
                selectionMode = selectionMode
            )
        }
    }
}

/**
 * Scrollbar for LazyColumn
 *
 * @param rightSide true -> right,  false -> left
 * @param thickness Thickness of the scrollbar thumb
 * @param padding   Padding of the scrollbar
 * @param thumbMinHeight Thumb minimum height proportional to total scrollbar's height (eg: 0.1 -> 10% of total)
 */
@Composable
private fun LazyColumnScrollbar(
    listState: LazyListState,
    rightSide: Boolean = true,
    thickness: Dp = 6.dp,
    padding: Dp = 8.dp,
    thumbMinHeight: Float = 0.1f,
    thumbColor: Color = Color(0xFF2A59B6),
    thumbSelectedColor: Color = Color(0xFF5281CA),
    thumbShape: Shape = CircleShape,
    isDraggable: Boolean = true,
    indicatorContent: (@Composable (index: Int, isThumbSelected: Boolean) -> Unit)? = null,
    selectionMode: PreferenceValues.ScrollbarSelectionMode = PreferenceValues.ScrollbarSelectionMode.Thumb,
) {
    val fistIndex = remember {
        derivedStateOf { listState.firstVisibleItemIndex }
    }
    val coroutineScope = rememberCoroutineScope()

    var isSelected by remember { mutableStateOf(false) }

    var dragOffset by remember { mutableStateOf(0f) }

    fun LazyListItemInfo.fractionHiddenTop() =
        if (size == 0) 0f else -offset.toFloat() / size.toFloat()

    fun LazyListItemInfo.fractionVisibleBottom(viewportEndOffset: Int) =
        if (size == 0) 0f else (viewportEndOffset - offset).toFloat() / size.toFloat()

    val normalizedThumbSizeReal by remember {
        derivedStateOf {
            listState.layoutInfo.let {
                if (it.totalItemsCount == 0)
                    return@let 0f

                val firstPartial =
                    it.visibleItemsInfo.first().fractionHiddenTop()
                val lastPartial =
                    1f - it.visibleItemsInfo.last().fractionVisibleBottom(it.viewportEndOffset)
                val realVisibleSize =
                    it.visibleItemsInfo.size.toFloat() - firstPartial - lastPartial
                realVisibleSize / it.totalItemsCount.toFloat()
            }
        }
    }

    val normalizedThumbSize by remember {
        derivedStateOf {
            normalizedThumbSizeReal.coerceAtLeast(thumbMinHeight).coerceAtMost(.1f)
        }
    }

    fun offsetCorrection(top: Float): Float {
        if (normalizedThumbSizeReal >= thumbMinHeight)
            return top
        val topRealMax = 1f - normalizedThumbSizeReal
        val topMax = 1f - thumbMinHeight
        return top * topMax / topRealMax
    }

    fun offsetCorrectionInverse(top: Float): Float {
        if (normalizedThumbSizeReal >= thumbMinHeight)
            return top
        val topRealMax = 1f - normalizedThumbSizeReal
        val topMax = 1f - thumbMinHeight
        return top * topRealMax / topMax
    }

    val normalizedOffsetPosition by remember {
        derivedStateOf {
            listState.layoutInfo.let {
                if (it.totalItemsCount == 0 || it.visibleItemsInfo.isEmpty())
                    return@let 0f

                val top = it.visibleItemsInfo
                    .first()
                    .run { index.toFloat() + fractionHiddenTop() } / it.totalItemsCount.toFloat()
                offsetCorrection(top)
            }
        }
    }

    fun setDragOffset(value: Float) {
        val maxValue = (1f - normalizedThumbSize).coerceAtLeast(0f)
        dragOffset = value.coerceIn(0f, maxValue)
    }

    fun setScrollOffset(newOffset: Float) {
        setDragOffset(newOffset)
        val totalItemsCount = listState.layoutInfo.totalItemsCount.toFloat()
        val exactIndex = offsetCorrectionInverse(totalItemsCount * dragOffset)
        val index: Int = floor(exactIndex).toInt()
        val remainder: Float = exactIndex - floor(exactIndex)

        coroutineScope.launch {
            listState.scrollToItem(index = index, scrollOffset = 0)
            val offset = listState.layoutInfo.visibleItemsInfo
                .firstOrNull()
                ?.size
                ?.let { it.toFloat() * remainder }
                ?.toInt() ?: 0
            listState.scrollToItem(index = index, scrollOffset = offset)
        }
    }

    val isInAction = listState.isScrollInProgress || isSelected

    val alpha by animateFloatAsState(
        targetValue = if (isInAction) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (isInAction) 75 else 500,
            delayMillis = if (isInAction) 0 else 500
        )
    )

    val displacement by animateFloatAsState(
        targetValue = if (isInAction) 0f else 14f,
        animationSpec = tween(
            durationMillis = if (isInAction) 75 else 500,
            delayMillis = if (isInAction) 0 else 500
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
                }
        ) {
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
                        }
                ) {}

                Box(
                    Modifier.constrainAs(content) {
                        top.linkTo(box.top)
                        bottom.linkTo(box.bottom)
                        if (rightSide) end.linkTo(box.start)
                        else start.linkTo(box.end)
                    }
                ) {
                    indicatorContent(
                        index = fistIndex.value,
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
                                if (newOffset in currentOffset..(currentOffset + normalizedThumbSize))
                                    setDragOffset(currentOffset)
                                else
                                    setScrollOffset(newOffset)
                                isSelected = true
                            }
                            PreferenceValues.ScrollbarSelectionMode.Thumb -> {
                                if (newOffset in currentOffset..(currentOffset + normalizedThumbSize)) {
                                    setDragOffset(currentOffset)
                                    isSelected = true
                                }
                            }
                            PreferenceValues.ScrollbarSelectionMode.Disabled -> Unit
                        }
                    },
                    onDragStopped = {
                        isSelected = false
                    }
                )
                .graphicsLayer {
                    translationX = (if (rightSide) displacement.dp else -displacement.dp).toPx()
                }
        ) {
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
                    .fillMaxHeight(normalizedThumbSize)
            )
        }
    }
}

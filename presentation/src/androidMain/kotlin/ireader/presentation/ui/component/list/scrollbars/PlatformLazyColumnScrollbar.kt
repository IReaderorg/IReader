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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
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
import ireader.domain.models.prefs.PreferenceValues
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
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
actual fun LazyColumnScrollbar(
    listState: LazyListState,
    rightSide: Boolean,
    thickness: Dp,
    padding: Dp ,
    thumbMinHeight: Float,
    thumbColor: Color ,
    thumbSelectedColor: Color ,
    indicatorContent: (@Composable (index: Int, isThumbSelected: Boolean) -> Unit)? ,
    thumbShape: Shape ,
    enable: Boolean,
    selectionMode: PreferenceValues.ScrollbarSelectionMode,
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
@Suppress("UNUSED_PARAMETER")
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
    val scrollbarState = remember { LazyColumnScrollbarState(listState, thumbMinHeight) }
    val coroutineScope = rememberCoroutineScope()
    var isSelected by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableFloatStateOf(0f) }

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
        LazyColumnScrollbarIndicator(
            scrollbarState = scrollbarState,
            rightSide = rightSide,
            thickness = thickness,
            padding = padding,
            displacement = displacement,
            indicatorContent = indicatorContent,
            isSelected = isSelected
        )

        LazyColumnScrollbarThumb(
            scrollbarState = scrollbarState,
            rightSide = rightSide,
            thickness = thickness,
            padding = padding,
            thumbColor = thumbColor,
            thumbSelectedColor = thumbSelectedColor,
            thumbShape = thumbShape,
            displacement = displacement,
            isSelected = isSelected,
            selectionMode = selectionMode,
            coroutineScope = coroutineScope,
            dragOffset = dragOffset,
            onDragOffsetChange = { dragOffset = it },
            onSelectedChange = { isSelected = it }
        )
    }
}

@Stable
private class LazyColumnScrollbarState(
    private val listState: LazyListState,
    private val thumbMinHeight: Float
) {
    val firstIndex by derivedStateOf { listState.firstVisibleItemIndex }
    
    private fun LazyListItemInfo.fractionHiddenTop() =
        if (size == 0) 0f else -offset.toFloat() / size.toFloat()

    private fun LazyListItemInfo.fractionVisibleBottom(viewportEndOffset: Int) =
        if (size == 0) 0f else (viewportEndOffset - offset).toFloat() / size.toFloat()

    val normalizedThumbSizeReal by derivedStateOf {
        listState.layoutInfo.let {
            if (it.totalItemsCount == 0) return@let 0f

            val firstPartial = it.visibleItemsInfo.first().fractionHiddenTop()
            val lastPartial = 1f - it.visibleItemsInfo.last().fractionVisibleBottom(it.viewportEndOffset)
            val realVisibleSize = it.visibleItemsInfo.size.toFloat() - firstPartial - lastPartial
            realVisibleSize / it.totalItemsCount.toFloat()
        }
    }

    val normalizedThumbSize by derivedStateOf {
        normalizedThumbSizeReal.coerceAtLeast(thumbMinHeight).coerceAtMost(.1f)
    }

    val normalizedOffsetPosition by derivedStateOf {
        listState.layoutInfo.let {
            if (it.totalItemsCount == 0 || it.visibleItemsInfo.isEmpty()) return@let 0f

            val top = it.visibleItemsInfo
                .first()
                .run { index.toFloat() + fractionHiddenTop() } / it.totalItemsCount.toFloat()
            offsetCorrection(top)
        }
    }

    fun offsetCorrection(top: Float): Float {
        if (normalizedThumbSizeReal >= thumbMinHeight) return top
        val topRealMax = 1f - normalizedThumbSizeReal
        val topMax = 1f - thumbMinHeight
        return top * topMax / topRealMax
    }

    fun offsetCorrectionInverse(top: Float): Float {
        if (normalizedThumbSizeReal >= thumbMinHeight) return top
        val topRealMax = 1f - normalizedThumbSizeReal
        val topMax = 1f - thumbMinHeight
        return top * topRealMax / topMax
    }

    @Suppress("UNUSED_PARAMETER")
    fun setDragOffset(value: Float, dragOffset: Float): Float {
        val maxValue = (1f - normalizedThumbSize).coerceAtLeast(0f)
        return value.coerceIn(0f, maxValue)
    }

    @Suppress("UNUSED_PARAMETER")
    suspend fun setScrollOffset(newOffset: Float, dragOffset: Float) {
        val totalItemsCount = listState.layoutInfo.totalItemsCount.toFloat()
        val exactIndex = offsetCorrectionInverse(totalItemsCount * dragOffset)
        val index: Int = floor(exactIndex).toInt()
        val remainder: Float = exactIndex - floor(exactIndex)

        listState.scrollToItem(index = index, scrollOffset = 0)
        val offset = listState.layoutInfo.visibleItemsInfo
            .firstOrNull()
            ?.size
            ?.let { it.toFloat() * remainder }
            ?.toInt() ?: 0
        listState.scrollToItem(index = index, scrollOffset = offset)
    }
}

@Composable
private fun BoxWithConstraintsScope.LazyColumnScrollbarIndicator(
    scrollbarState: LazyColumnScrollbarState,
    rightSide: Boolean,
    thickness: Dp,
    padding: Dp,
    displacement: Float,
    indicatorContent: (@Composable (index: Int, isThumbSelected: Boolean) -> Unit)?,
    isSelected: Boolean
) {
    if (indicatorContent != null) {
        BoxWithConstraints(
            Modifier
                .align(if (rightSide) Alignment.TopEnd else Alignment.TopStart)
                .fillMaxHeight()
                .graphicsLayer {
                    translationX = (if (rightSide) displacement.dp else -displacement.dp).toPx()
                    translationY = constraints.maxHeight.toFloat() * scrollbarState.normalizedOffsetPosition
                }
        ) {
            ConstraintLayout(Modifier.align(Alignment.TopEnd)) {
                val (box, content) = createRefs()
                Box(
                    Modifier
                        .fillMaxHeight(scrollbarState.normalizedThumbSize)
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
                    indicatorContent(scrollbarState.firstIndex, isSelected)
                }
            }
        }
    }
}

@Suppress("LongMethod")
@Composable
private fun BoxWithConstraintsScope.LazyColumnScrollbarThumb(
    scrollbarState: LazyColumnScrollbarState,
    rightSide: Boolean,
    thickness: Dp,
    padding: Dp,
    thumbColor: Color,
    thumbSelectedColor: Color,
    thumbShape: Shape,
    displacement: Float,
    isSelected: Boolean,
    selectionMode: PreferenceValues.ScrollbarSelectionMode,
    coroutineScope: CoroutineScope,
    dragOffset: Float,
    onDragOffsetChange: (Float) -> Unit,
    onSelectedChange: (Boolean) -> Unit
) {
    BoxWithConstraints(
        Modifier
            .align(if (rightSide) Alignment.TopEnd else Alignment.TopStart)
            .fillMaxHeight()
            .draggable(
                state = rememberDraggableState { delta ->
                    if (isSelected) {
                        val newOffset = dragOffset + delta / constraints.maxHeight.toFloat()
                        val correctedOffset = scrollbarState.setDragOffset(newOffset, dragOffset)
                        onDragOffsetChange(correctedOffset)
                        coroutineScope.launch {
                            scrollbarState.setScrollOffset(newOffset, correctedOffset)
                        }
                    }
                },
                orientation = Orientation.Vertical,
                enabled = selectionMode != PreferenceValues.ScrollbarSelectionMode.Disabled,
                startDragImmediately = true,
                onDragStarted = { offset ->
                    val newOffset = offset.y / constraints.maxHeight.toFloat()
                    val currentOffset = scrollbarState.normalizedOffsetPosition
                    when (selectionMode) {
                        PreferenceValues.ScrollbarSelectionMode.Full -> {
                            if (newOffset in currentOffset..(currentOffset + scrollbarState.normalizedThumbSize)) {
                                onDragOffsetChange(scrollbarState.setDragOffset(currentOffset, dragOffset))
                            } else {
                                coroutineScope.launch {
                                    scrollbarState.setScrollOffset(newOffset, dragOffset)
                                }
                            }
                            onSelectedChange(true)
                        }
                        PreferenceValues.ScrollbarSelectionMode.Thumb -> {
                            if (newOffset in currentOffset..(currentOffset + scrollbarState.normalizedThumbSize)) {
                                onDragOffsetChange(scrollbarState.setDragOffset(currentOffset, dragOffset))
                                onSelectedChange(true)
                            }
                        }
                        PreferenceValues.ScrollbarSelectionMode.Disabled -> Unit
                    }
                },
                onDragStopped = { onSelectedChange(false) }
            )
            .graphicsLayer {
                translationX = (if (rightSide) displacement.dp else -displacement.dp).toPx()
            }
    ) {
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .graphicsLayer {
                    translationY = constraints.maxHeight.toFloat() * scrollbarState.normalizedOffsetPosition
                }
                .padding(horizontal = padding)
                .width(thickness)
                .clip(thumbShape)
                .background(if (isSelected) thumbSelectedColor else thumbColor)
                .fillMaxHeight(scrollbarState.normalizedThumbSize)
        )
    }
}

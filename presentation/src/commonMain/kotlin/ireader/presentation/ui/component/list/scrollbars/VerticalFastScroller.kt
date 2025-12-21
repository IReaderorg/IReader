package ireader.presentation.ui.component.list.scrollbars

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import ireader.domain.utils.fastForEach
import ireader.domain.utils.fastMaxBy
import ireader.presentation.ui.core.modifier.systemGestureExclusion
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

expect fun scrollbarFadeDuration(): Int

/**
 * Vertical fast scroller for LazyList with optimized performance.
 * 
 * Features:
 * - Smooth thumb dragging with position indicator
 * - RTL layout support
 * - Item height caching for better scroll estimation
 * - Optimized recomposition with derivedStateOf
 * 
 * Based on tachiyomi implementation: https://github.com/tachiyomiorg/tachiyomi
 */
@Composable
fun IVerticalFastScroller(
    listState: LazyListState,
    modifier: Modifier = Modifier,
    thumbAllowed: () -> Boolean = { true },
    thumbColor: Color = MaterialTheme.colorScheme.primary,
    topContentPadding: Dp = Dp.Hairline,
    bottomContentPadding: Dp = Dp.Hairline,
    endContentPadding: Dp = Dp.Hairline,
    showPositionIndicator: Boolean = false,
    content: @Composable () -> Unit,
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    
    SubcomposeLayout(modifier = modifier) { constraints ->
        val contentPlaceable = subcompose("content", content).map { it.measure(constraints) }
        val contentHeight = contentPlaceable.fastMaxBy { it.height }?.height ?: 0
        val contentWidth = contentPlaceable.fastMaxBy { it.width }?.width ?: 0

        val scrollerConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val scrollerPlaceable = subcompose("scroller") {
            LazyListFastScrollerContent(
                listState = listState,
                contentHeight = contentHeight,
                thumbAllowed = thumbAllowed,
                thumbColor = thumbColor,
                topContentPadding = topContentPadding,
                bottomContentPadding = bottomContentPadding,
                endContentPadding = endContentPadding,
                showPositionIndicator = showPositionIndicator,
            )
        }.map { it.measure(scrollerConstraints) }
        val scrollerWidth = scrollerPlaceable.fastMaxBy { it.width }?.width ?: 0

        layout(contentWidth, contentHeight) {
            contentPlaceable.fastForEach { it.place(0, 0) }
            scrollerPlaceable.fastForEach {
                if (isRtl) {
                    it.placeRelative(0, 0)
                } else {
                    it.placeRelative(contentWidth - scrollerWidth, 0)
                }
            }
        }
    }
}

/**
 * Internal composable for LazyList fast scroller content.
 * Extracted to reduce recomposition scope.
 */
@Composable
private fun LazyListFastScrollerContent(
    listState: LazyListState,
    contentHeight: Int,
    thumbAllowed: () -> Boolean,
    thumbColor: Color,
    topContentPadding: Dp,
    bottomContentPadding: Dp,
    endContentPadding: Dp,
    showPositionIndicator: Boolean,
) {
    val layoutInfo by remember { derivedStateOf { listState.layoutInfo } }
    val showScroller by remember { 
        derivedStateOf { layoutInfo.visibleItemsInfo.size < layoutInfo.totalItemsCount }
    }
    
    if (!showScroller) return

    val density = LocalDensity.current
    val thumbTopPadding = remember(topContentPadding, density) { 
        with(density) { topContentPadding.toPx() } 
    }
    val thumbBottomPadding = remember(bottomContentPadding, density) { 
        with(density) { bottomContentPadding.toPx() } 
    }
    val thumbHeightPx = remember(density) { with(density) { ThumbLength.toPx() } }
    
    var thumbOffsetY by remember(thumbTopPadding) { mutableStateOf(thumbTopPadding) }

    val dragInteractionSource = remember { MutableInteractionSource() }
    val isThumbDragged by dragInteractionSource.collectIsDraggedAsState()
    
    val scrolled = remember {
        MutableSharedFlow<Unit>(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
    }

    val heightPx by remember(contentHeight, thumbTopPadding, thumbBottomPadding) {
        derivedStateOf {
            contentHeight.toFloat() - thumbTopPadding - thumbBottomPadding - layoutInfo.afterContentPadding
        }
    }
    val trackHeightPx by remember(heightPx, thumbHeightPx) {
        derivedStateOf { heightPx - thumbHeightPx }
    }

    // Cached average item height for smoother scrolling
    val averageItemHeight by remember {
        derivedStateOf {
            val items = layoutInfo.visibleItemsInfo
            if (items.isEmpty()) 0 else items.sumOf { it.size } / items.size
        }
    }

    // Handle thumb drag
    LaunchedEffect(thumbOffsetY) {
        if (layoutInfo.totalItemsCount == 0 || !isThumbDragged) return@LaunchedEffect
        
        val scrollRatio = (thumbOffsetY - thumbTopPadding) / trackHeightPx
        val scrollItem = layoutInfo.totalItemsCount * scrollRatio
        val scrollItemRounded = scrollItem.roundToInt().coerceIn(0, layoutInfo.totalItemsCount - 1)
        
        // Use visible item size if available, otherwise use average
        val scrollItemSize = layoutInfo.visibleItemsInfo
            .find { it.index == scrollItemRounded }?.size ?: averageItemHeight
        
        val scrollItemOffset = (scrollItemSize * (scrollItem - scrollItemRounded))
            .coerceIn(0f, scrollItemSize.toFloat())
        
        listState.scrollToItem(
            index = scrollItemRounded,
            scrollOffset = scrollItemOffset.roundToInt()
        )
        scrolled.tryEmit(Unit)
    }

    // Handle list scroll - update thumb position
    val scrollProportion by remember {
        derivedStateOf {
            if (layoutInfo.totalItemsCount == 0) 0f
            else {
                val scrollOffset = computeScrollOffset(state = listState)
                val scrollRange = computeScrollRange(state = listState)
                val range = scrollRange.toFloat() - heightPx
                if (range > 0) scrollOffset.toFloat() / range else 0f
            }
        }
    }

    LaunchedEffect(listState.firstVisibleItemScrollOffset, listState.firstVisibleItemIndex) {
        if (layoutInfo.totalItemsCount == 0 || isThumbDragged) return@LaunchedEffect
        thumbOffsetY = (trackHeightPx * scrollProportion + thumbTopPadding).coerceIn(
            thumbTopPadding,
            thumbTopPadding + trackHeightPx
        )
        scrolled.tryEmit(Unit)
    }

    // Thumb visibility animation
    val alpha = remember { Animatable(0f) }
    val isThumbVisible by remember { derivedStateOf { alpha.value > 0f } }
    
    LaunchedEffect(scrolled, alpha) {
        scrolled.collectLatest {
            if (thumbAllowed()) {
                alpha.snapTo(1f)
                alpha.animateTo(0f, animationSpec = FadeOutAnimationSpec)
            } else {
                alpha.animateTo(0f, animationSpec = ImmediateFadeOutAnimationSpec)
            }
        }
    }

    FastScrollerThumb(
        thumbOffsetY = thumbOffsetY,
        onThumbOffsetChange = { thumbOffsetY = it },
        thumbTopPadding = thumbTopPadding,
        trackHeightPx = trackHeightPx,
        endContentPadding = endContentPadding,
        thumbColor = thumbColor,
        alpha = alpha.value,
        isThumbVisible = isThumbVisible,
        isThumbDragged = isThumbDragged,
        isScrollInProgress = listState.isScrollInProgress,
        dragInteractionSource = dragInteractionSource,
        showPositionIndicator = showPositionIndicator,
        currentPosition = if (showPositionIndicator) {
            "${listState.firstVisibleItemIndex + 1} / ${layoutInfo.totalItemsCount}"
        } else null,
    )
}


/**
 * Reusable thumb component for fast scrollers.
 * Handles drag interaction, position indicator, and gesture exclusion.
 */
@Composable
private fun FastScrollerThumb(
    thumbOffsetY: Float,
    onThumbOffsetChange: (Float) -> Unit,
    thumbTopPadding: Float,
    trackHeightPx: Float,
    endContentPadding: Dp,
    thumbColor: Color,
    alpha: Float,
    isThumbVisible: Boolean,
    isThumbDragged: Boolean,
    isScrollInProgress: Boolean,
    dragInteractionSource: MutableInteractionSource,
    showPositionIndicator: Boolean,
    currentPosition: String?,
) {
    Row(
        modifier = Modifier.offset { IntOffset(0, thumbOffsetY.roundToInt()) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Position indicator (shown when dragging)
        if (showPositionIndicator && isThumbDragged && currentPosition != null) {
            PositionIndicator(position = currentPosition)
        }

        Box(
            modifier = Modifier
                .then(
                    if (isThumbVisible && !isScrollInProgress) {
                        Modifier.draggable(
                            interactionSource = dragInteractionSource,
                            orientation = Orientation.Vertical,
                            state = rememberDraggableState { delta ->
                                val newOffsetY = thumbOffsetY + delta
                                onThumbOffsetChange(
                                    newOffsetY.coerceIn(thumbTopPadding, thumbTopPadding + trackHeightPx)
                                )
                            },
                        )
                    } else Modifier
                )
                .then(
                    if (isThumbVisible && !isThumbDragged && !isScrollInProgress) {
                        Modifier.systemGestureExclusion()
                    } else Modifier
                )
                .height(ThumbLength)
                .padding(horizontal = 8.dp)
                .padding(end = endContentPadding)
                .width(ThumbThickness)
                .graphicsLayer { this.alpha = alpha }
                .background(color = thumbColor, shape = ThumbShape),
        )
    }
}

/**
 * Position indicator shown during thumb drag.
 */
@Composable
private fun PositionIndicator(position: String) {
    Box(
        modifier = Modifier
            .padding(end = 12.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = position,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun rememberColumnWidthSums(
    columns: GridCells,
    horizontalArrangement: Arrangement.Horizontal,
    contentPadding: PaddingValues,
) = remember<Density.(Constraints) -> List<Int>>(
    columns,
    horizontalArrangement,
    contentPadding,
) {
    { constraints ->
        require(constraints.maxWidth != Constraints.Infinity) {
            "LazyVerticalGrid's width should be bound by parent."
        }
        val horizontalPadding = contentPadding.calculateStartPadding(LayoutDirection.Ltr) +
                contentPadding.calculateEndPadding(LayoutDirection.Ltr)
        val gridWidth = constraints.maxWidth - horizontalPadding.roundToPx()
        with(columns) {
            calculateCrossAxisCellSizes(
                gridWidth,
                horizontalArrangement.spacing.roundToPx(),
            ).toMutableList().apply {
                for (i in 1 until size) {
                    this[i] += this[i - 1]
                }
            }
        }
    }
}

/**
 * Vertical fast scroller for LazyVerticalGrid with optimized performance.
 * 
 * Features:
 * - Smooth thumb dragging with position indicator
 * - RTL layout support
 * - Column-aware scroll calculations
 * - Optimized recomposition with derivedStateOf
 */
@Composable
fun VerticalGridFastScroller(
    state: LazyGridState,
    columns: GridCells,
    arrangement: Arrangement.Horizontal,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    thumbAllowed: () -> Boolean = { true },
    thumbColor: Color = MaterialTheme.colorScheme.primary,
    topContentPadding: Dp = Dp.Hairline,
    bottomContentPadding: Dp = Dp.Hairline,
    endContentPadding: Dp = Dp.Hairline,
    showPositionIndicator: Boolean = false,
    content: @Composable () -> Unit,
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val slotSizesSums = rememberColumnWidthSums(
        columns = columns,
        horizontalArrangement = arrangement,
        contentPadding = contentPadding,
    )

    SubcomposeLayout(modifier = modifier) { constraints ->
        val contentPlaceable = subcompose("content", content).map { it.measure(constraints) }
        val contentHeight = contentPlaceable.fastMaxBy { it.height }?.height ?: 0
        val contentWidth = contentPlaceable.fastMaxBy { it.width }?.width ?: 0

        val scrollerConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val columnCount = slotSizesSums(constraints).size
        
        val scrollerPlaceable = subcompose("scroller") {
            LazyGridFastScrollerContent(
                state = state,
                contentHeight = contentHeight,
                columnCount = columnCount,
                thumbAllowed = thumbAllowed,
                thumbColor = thumbColor,
                topContentPadding = topContentPadding,
                bottomContentPadding = bottomContentPadding,
                endContentPadding = endContentPadding,
                showPositionIndicator = showPositionIndicator,
            )
        }.map { it.measure(scrollerConstraints) }
        val scrollerWidth = scrollerPlaceable.fastMaxBy { it.width }?.width ?: 0

        layout(contentWidth, contentHeight) {
            contentPlaceable.fastForEach { it.place(0, 0) }
            scrollerPlaceable.fastForEach {
                if (isRtl) {
                    it.placeRelative(0, 0)
                } else {
                    it.placeRelative(contentWidth - scrollerWidth, 0)
                }
            }
        }
    }
}


/**
 * Internal composable for LazyGrid fast scroller content.
 * Extracted to reduce recomposition scope.
 */
@Composable
private fun LazyGridFastScrollerContent(
    state: LazyGridState,
    contentHeight: Int,
    columnCount: Int,
    thumbAllowed: () -> Boolean,
    thumbColor: Color,
    topContentPadding: Dp,
    bottomContentPadding: Dp,
    endContentPadding: Dp,
    showPositionIndicator: Boolean,
) {
    val layoutInfo by remember { derivedStateOf { state.layoutInfo } }
    val showScroller by remember {
        derivedStateOf { layoutInfo.visibleItemsInfo.size < layoutInfo.totalItemsCount }
    }
    
    if (!showScroller) return

    val density = LocalDensity.current
    val thumbTopPadding = remember(topContentPadding, density) {
        with(density) { topContentPadding.toPx() }
    }
    val thumbBottomPadding = remember(bottomContentPadding, density) {
        with(density) { bottomContentPadding.toPx() }
    }
    val thumbHeightPx = remember(density) { with(density) { ThumbLength.toPx() } }
    
    var thumbOffsetY by remember(thumbTopPadding) { mutableStateOf(thumbTopPadding) }

    val dragInteractionSource = remember { MutableInteractionSource() }
    val isThumbDragged by dragInteractionSource.collectIsDraggedAsState()
    
    val scrolled = remember {
        MutableSharedFlow<Unit>(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
    }

    val heightPx by remember(contentHeight, thumbTopPadding, thumbBottomPadding) {
        derivedStateOf {
            contentHeight.toFloat() - thumbTopPadding - thumbBottomPadding - layoutInfo.afterContentPadding
        }
    }
    val trackHeightPx by remember(heightPx, thumbHeightPx) {
        derivedStateOf { heightPx - thumbHeightPx }
    }

    // Cached average item height for smoother scrolling
    val averageItemHeight by remember {
        derivedStateOf {
            val items = layoutInfo.visibleItemsInfo
            if (items.isEmpty()) 0 else items.sumOf { it.size.height } / items.size
        }
    }

    // Handle thumb drag with column-aware calculations
    LaunchedEffect(thumbOffsetY) {
        if (layoutInfo.totalItemsCount == 0 || !isThumbDragged) return@LaunchedEffect
        
        val scrollRatio = (thumbOffsetY - thumbTopPadding) / trackHeightPx
        val scrollItem = layoutInfo.totalItemsCount * scrollRatio
        val scrollItemWhole = scrollItem.toInt()
        val columnNum = ((scrollItemWhole + 1) % columnCount).takeIf { it != 0 } ?: columnCount
        val scrollItemFraction = if (scrollItemWhole == 0) scrollItem else scrollItem % scrollItemWhole
        val offsetPerItem = 1f / columnCount
        val offsetRatio = (offsetPerItem * scrollItemFraction) + (offsetPerItem * (columnNum - 1))

        // Get max item height from current row
        val scrollItemSize = (1..columnCount).maxOf { num ->
            val actualIndex = if (num != columnNum) scrollItemWhole + num - columnCount else scrollItemWhole
            layoutInfo.visibleItemsInfo.find { it.index == actualIndex }?.size?.height ?: averageItemHeight
        }

        val scrollItemOffset = (scrollItemSize * offsetRatio).coerceIn(0f, scrollItemSize.toFloat())

        state.scrollToItem(
            index = scrollItemWhole.coerceIn(0, layoutInfo.totalItemsCount - 1),
            scrollOffset = scrollItemOffset.roundToInt()
        )
        scrolled.tryEmit(Unit)
    }

    // Handle list scroll - update thumb position
    val scrollProportion by remember {
        derivedStateOf {
            if (layoutInfo.totalItemsCount == 0) 0f
            else {
                val scrollOffset = computeScrollOffset(state = state)
                val scrollRange = computeScrollRange(state = state)
                val range = scrollRange.toFloat() - heightPx
                if (range > 0) scrollOffset.toFloat() / range else 0f
            }
        }
    }

    LaunchedEffect(state.firstVisibleItemScrollOffset, state.firstVisibleItemIndex) {
        if (layoutInfo.totalItemsCount == 0 || isThumbDragged) return@LaunchedEffect
        thumbOffsetY = (trackHeightPx * scrollProportion + thumbTopPadding).coerceIn(
            thumbTopPadding,
            thumbTopPadding + trackHeightPx
        )
        scrolled.tryEmit(Unit)
    }

    // Thumb visibility animation
    val alpha = remember { Animatable(0f) }
    val isThumbVisible by remember { derivedStateOf { alpha.value > 0f } }
    
    LaunchedEffect(scrolled, alpha) {
        scrolled.collectLatest {
            if (thumbAllowed()) {
                alpha.snapTo(1f)
                alpha.animateTo(0f, animationSpec = FadeOutAnimationSpec)
            } else {
                alpha.animateTo(0f, animationSpec = ImmediateFadeOutAnimationSpec)
            }
        }
    }

    FastScrollerThumb(
        thumbOffsetY = thumbOffsetY,
        onThumbOffsetChange = { thumbOffsetY = it },
        thumbTopPadding = thumbTopPadding,
        trackHeightPx = trackHeightPx,
        endContentPadding = endContentPadding,
        thumbColor = thumbColor,
        alpha = alpha.value,
        isThumbVisible = isThumbVisible,
        isThumbDragged = isThumbDragged,
        isScrollInProgress = state.isScrollInProgress,
        dragInteractionSource = dragInteractionSource,
        showPositionIndicator = showPositionIndicator,
        currentPosition = if (showPositionIndicator) {
            "${state.firstVisibleItemIndex + 1} / ${layoutInfo.totalItemsCount}"
        } else null,
    )
}

// region Scroll Calculations

private fun computeScrollOffset(state: LazyGridState): Int {
    if (state.layoutInfo.totalItemsCount == 0) return 0
    val visibleItems = state.layoutInfo.visibleItemsInfo
    if (visibleItems.isEmpty()) return 0
    
    val startChild = visibleItems.first()
    val endChild = visibleItems.last()
    val minPosition = min(startChild.index, endChild.index)
    val maxPosition = max(startChild.index, endChild.index)
    val itemsBefore = minPosition.coerceAtLeast(0)
    val startDecoratedTop = startChild.offset.y
    val laidOutArea = abs((endChild.offset.y + endChild.size.height) - startDecoratedTop)
    val itemRange = abs(minPosition - maxPosition) + 1
    val avgSizePerRow = laidOutArea.toFloat() / itemRange
    return (itemsBefore * avgSizePerRow + (0 - startDecoratedTop)).roundToInt()
}

private fun computeScrollRange(state: LazyGridState): Int {
    if (state.layoutInfo.totalItemsCount == 0) return 0
    val visibleItems = state.layoutInfo.visibleItemsInfo
    if (visibleItems.isEmpty()) return 0
    
    val startChild = visibleItems.first()
    val endChild = visibleItems.last()
    val laidOutArea = (endChild.offset.y + endChild.size.height) - startChild.offset.y
    val laidOutRange = abs(startChild.index - endChild.index) + 1
    return (laidOutArea.toFloat() / laidOutRange * state.layoutInfo.totalItemsCount).roundToInt()
}

private fun computeScrollOffset(state: LazyListState): Int {
    if (state.layoutInfo.totalItemsCount == 0) return 0
    val visibleItems = state.layoutInfo.visibleItemsInfo
    if (visibleItems.isEmpty()) return 0
    
    val startChild = visibleItems.first()
    val endChild = visibleItems.last()
    val minPosition = min(startChild.index, endChild.index)
    val maxPosition = max(startChild.index, endChild.index)
    val itemsBefore = minPosition.coerceAtLeast(0)
    val startDecoratedTop = startChild.top
    val laidOutArea = abs(endChild.bottom - startDecoratedTop)
    val itemRange = abs(minPosition - maxPosition) + 1
    val avgSizePerRow = laidOutArea.toFloat() / itemRange
    return (itemsBefore * avgSizePerRow + (0 - startDecoratedTop)).roundToInt()
}

private fun computeScrollRange(state: LazyListState): Int {
    if (state.layoutInfo.totalItemsCount == 0) return 0
    val visibleItems = state.layoutInfo.visibleItemsInfo
    if (visibleItems.isEmpty()) return 0
    
    val startChild = visibleItems.first()
    val endChild = visibleItems.last()
    val laidOutArea = endChild.bottom - startChild.top
    val laidOutRange = abs(startChild.index - endChild.index) + 1
    return (laidOutArea.toFloat() / laidOutRange * state.layoutInfo.totalItemsCount).roundToInt()
}

// endregion

// region Constants

private val ThumbLength = 48.dp
private val ThumbThickness = 8.dp
private val ThumbShape = RoundedCornerShape(ThumbThickness / 2)

private val FadeOutAnimationSpec = tween<Float>(
    durationMillis = scrollbarFadeDuration(),
    delayMillis = 2000,
)
private val ImmediateFadeOutAnimationSpec = tween<Float>(
    durationMillis = scrollbarFadeDuration(),
)

private val LazyListItemInfo.top: Int
    get() = offset

private val LazyListItemInfo.bottom: Int
    get() = offset + size

// endregion

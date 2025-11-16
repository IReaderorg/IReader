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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
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

expect fun scrollbarFadeDuration() : Int

/**
 * Code Taken from tachiyomi
 * https://github.com/tachiyomiorg/tachiyomi
 */
@Composable
fun IVerticalFastScroller(        listState: LazyListState,
                                  modifier: Modifier = Modifier,
                                  thumbAllowed: () -> Boolean = { true },
                                  thumbColor: Color = MaterialTheme.colorScheme.primary,
                                  topContentPadding: Dp = Dp.Hairline,
                                  bottomContentPadding: Dp = Dp.Hairline,
                                  endContentPadding: Dp = Dp.Hairline,
                                  showPositionIndicator: Boolean = false,
                                  content: @Composable () -> Unit,) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val contentPlaceable = subcompose("content", content).map { it.measure(constraints) }
        val contentHeight = contentPlaceable.fastMaxBy { it.height }?.height ?: 0
        val contentWidth = contentPlaceable.fastMaxBy { it.width }?.width ?: 0

        val scrollerConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val scrollerPlaceable = subcompose("scroller") {
            val layoutInfo = listState.layoutInfo
            val showScroller = layoutInfo.visibleItemsInfo.size < layoutInfo.totalItemsCount
            if (!showScroller) return@subcompose

            val thumbTopPadding = with(LocalDensity.current) { topContentPadding.toPx() }
            var thumbOffsetY by remember(thumbTopPadding) { mutableStateOf(thumbTopPadding) }

            val dragInteractionSource = remember { MutableInteractionSource() }
            val isThumbDragged by dragInteractionSource.collectIsDraggedAsState()
            val scrolled = remember {
                MutableSharedFlow<Unit>(
                    extraBufferCapacity = 1,
                    onBufferOverflow = BufferOverflow.DROP_OLDEST,
                )
            }

            val thumbBottomPadding = with(LocalDensity.current) { bottomContentPadding.toPx() }
            val heightPx = contentHeight.toFloat() - thumbTopPadding - thumbBottomPadding - listState.layoutInfo.afterContentPadding
            val thumbHeightPx = with(LocalDensity.current) { ThumbLength.toPx() }
            val trackHeightPx = heightPx - thumbHeightPx
            
            // Item height cache for performance
            val itemHeightCache = remember { mutableMapOf<Int, Int>() }
            
            // Calculate average item height using derivedStateOf for performance
            val averageItemHeight by remember {
                derivedStateOf {
                    if (layoutInfo.visibleItemsInfo.isEmpty()) 0
                    else layoutInfo.visibleItemsInfo.sumOf { it.size } / layoutInfo.visibleItemsInfo.size
                }
            }

            // When thumb dragged
            LaunchedEffect(thumbOffsetY) {
                if (layoutInfo.totalItemsCount == 0 || !isThumbDragged) return@LaunchedEffect
                val scrollRatio = (thumbOffsetY - thumbTopPadding) / trackHeightPx
                val scrollItem = layoutInfo.totalItemsCount * scrollRatio
                val scrollItemRounded = scrollItem.roundToInt().coerceIn(0, layoutInfo.totalItemsCount - 1)
                
                // Try to get item size from visible items first
                val visibleItem = layoutInfo.visibleItemsInfo.find { it.index == scrollItemRounded }
                val scrollItemSize = if (visibleItem != null) {
                    // Cache the height for future use
                    itemHeightCache[scrollItemRounded] = visibleItem.size
                    visibleItem.size
                } else {
                    // Use cached value if available, otherwise use average
                    itemHeightCache[scrollItemRounded] ?: averageItemHeight
                }
                
                val scrollItemOffset = (scrollItemSize * (scrollItem - scrollItemRounded)).coerceIn(
                    0f,
                    scrollItemSize.toFloat()
                )
                
                listState.scrollToItem(
                    index = scrollItemRounded,
                    scrollOffset = scrollItemOffset.roundToInt()
                )
                scrolled.tryEmit(Unit)
            }

            // When list scrolled - optimized with derivedStateOf
            val scrollProportion by remember {
                derivedStateOf {
                    if (listState.layoutInfo.totalItemsCount == 0) 0f
                    else {
                        val scrollOffset = computeScrollOffset(state = listState)
                        val scrollRange = computeScrollRange(state = listState)
                        val range = scrollRange.toFloat() - heightPx
                        if (range > 0) scrollOffset.toFloat() / range else 0f
                    }
                }
            }
            
            LaunchedEffect(listState.firstVisibleItemScrollOffset) {
                if (listState.layoutInfo.totalItemsCount == 0 || isThumbDragged) return@LaunchedEffect
                thumbOffsetY = (trackHeightPx * scrollProportion + thumbTopPadding).coerceIn(
                    thumbTopPadding,
                    thumbTopPadding + trackHeightPx
                )
                scrolled.tryEmit(Unit)
            }

            // Thumb alpha
            val alpha = remember { Animatable(0f) }
            val isThumbVisible = alpha.value > 0f
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

            Row(
                modifier = Modifier.offset { IntOffset(0, thumbOffsetY.roundToInt()) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Position indicator (shown when dragging)
                if (showPositionIndicator && isThumbDragged) {
                    val currentPosition = remember {
                        derivedStateOf {
                            val firstVisible = listState.firstVisibleItemIndex
                            val total = listState.layoutInfo.totalItemsCount
                            "${firstVisible + 1} / $total"
                        }
                    }
                    
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
                            text = currentPosition.value,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                Box(
                    modifier = Modifier
                        .then(
                            // Recompose opts
                            if (isThumbVisible && !listState.isScrollInProgress) {
                                Modifier.draggable(
                                    interactionSource = dragInteractionSource,
                                    orientation = Orientation.Vertical,
                                    state = rememberDraggableState { delta ->
                                        val newOffsetY = thumbOffsetY + delta
                                        thumbOffsetY = newOffsetY.coerceIn(
                                            thumbTopPadding,
                                            thumbTopPadding + trackHeightPx,
                                        )
                                    },
                                )
                            } else Modifier,
                        )
                        .then(
                            // Exclude thumb from gesture area only when needed
                            if (isThumbVisible && !isThumbDragged && !listState.isScrollInProgress) {
                                Modifier.systemGestureExclusion()
                            } else Modifier,
                        )
                        .height(ThumbLength)
                        .padding(horizontal = 8.dp)
                        .padding(end = endContentPadding)
                        .width(ThumbThickness)
                        .alpha(alpha.value)
                        .background(color = thumbColor, shape = ThumbShape),
                )
            }
        }.map { it.measure(scrollerConstraints) }
        val scrollerWidth = scrollerPlaceable.fastMaxBy { it.width }?.width ?: 0

        layout(contentWidth, contentHeight) {
            contentPlaceable.fastForEach {
                it.place(0, 0)
            }
            scrollerPlaceable.fastForEach {
                it.placeRelative(contentWidth - scrollerWidth, 0)
            }
        }
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
        val scrollerPlaceable = subcompose("scroller") {
            val layoutInfo = state.layoutInfo
            val showScroller = layoutInfo.visibleItemsInfo.size < layoutInfo.totalItemsCount
            if (!showScroller) return@subcompose
            val thumbTopPadding = with(LocalDensity.current) { topContentPadding.toPx() }
            var thumbOffsetY by remember(thumbTopPadding) { mutableStateOf(thumbTopPadding) }

            val dragInteractionSource = remember { MutableInteractionSource() }
            val isThumbDragged by dragInteractionSource.collectIsDraggedAsState()
            val scrolled = remember {
                MutableSharedFlow<Unit>(
                    extraBufferCapacity = 1,
                    onBufferOverflow = BufferOverflow.DROP_OLDEST,
                )
            }

            val thumbBottomPadding = with(LocalDensity.current) { bottomContentPadding.toPx() }
            val heightPx = contentHeight.toFloat() - thumbTopPadding - thumbBottomPadding - state.layoutInfo.afterContentPadding
            val thumbHeightPx = with(LocalDensity.current) { ThumbLength.toPx() }
            val trackHeightPx = heightPx - thumbHeightPx

            val columnCount = remember { slotSizesSums(constraints).size }
            
            // Item height cache for performance
            val itemHeightCache = remember { mutableMapOf<Int, Int>() }
            
            // Calculate average item height using derivedStateOf for performance
            val averageItemHeight by remember {
                derivedStateOf {
                    if (layoutInfo.visibleItemsInfo.isEmpty()) 0
                    else layoutInfo.visibleItemsInfo.sumOf { it.size.height } / layoutInfo.visibleItemsInfo.size
                }
            }

            // When thumb dragged
            LaunchedEffect(thumbOffsetY) {
                if (layoutInfo.totalItemsCount == 0 || !isThumbDragged) return@LaunchedEffect
                val scrollRatio = (thumbOffsetY - thumbTopPadding) / trackHeightPx
                val scrollItem = layoutInfo.totalItemsCount * scrollRatio
                // I can't think of anything else rn but this'll do
                val scrollItemWhole = scrollItem.toInt()
                val columnNum = ((scrollItemWhole + 1) % columnCount).takeIf { it != 0 } ?: columnCount
                val scrollItemFraction = if (scrollItemWhole == 0) scrollItem else scrollItem % scrollItemWhole
                val offsetPerItem = 1f / columnCount
                val offsetRatio = (offsetPerItem * scrollItemFraction) + (offsetPerItem * (columnNum - 1))

                // Get item height with caching and fallback to average
                val scrollItemSize = (1..columnCount).maxOf { num ->
                    val actualIndex = if (num != columnNum) {
                        scrollItemWhole + num - columnCount
                    } else {
                        scrollItemWhole
                    }
                    
                    // Try to get from visible items first
                    val visibleItem = layoutInfo.visibleItemsInfo.find { it.index == actualIndex }
                    if (visibleItem != null) {
                        // Cache the height for future use
                        itemHeightCache[actualIndex] = visibleItem.size.height
                        visibleItem.size.height
                    } else {
                        // Use cached value if available, otherwise use average
                        itemHeightCache[actualIndex] ?: averageItemHeight
                    }
                }
                
                val scrollItemOffset = (scrollItemSize * offsetRatio).coerceIn(
                    0f,
                    scrollItemSize.toFloat()
                )

                state.scrollToItem(
                    index = scrollItemWhole.coerceIn(0, layoutInfo.totalItemsCount - 1),
                    scrollOffset = scrollItemOffset.roundToInt()
                )
                scrolled.tryEmit(Unit)
            }

            // When list scrolled - optimized with derivedStateOf
            val scrollProportion by remember {
                derivedStateOf {
                    if (state.layoutInfo.totalItemsCount == 0) 0f
                    else {
                        val scrollOffset = computeScrollOffset(state = state)
                        val scrollRange = computeScrollRange(state = state)
                        val range = scrollRange.toFloat() - heightPx
                        if (range > 0) scrollOffset.toFloat() / range else 0f
                    }
                }
            }
            
            LaunchedEffect(state.firstVisibleItemScrollOffset) {
                if (state.layoutInfo.totalItemsCount == 0 || isThumbDragged) return@LaunchedEffect
                thumbOffsetY = (trackHeightPx * scrollProportion + thumbTopPadding).coerceIn(
                    thumbTopPadding,
                    thumbTopPadding + trackHeightPx
                )
                scrolled.tryEmit(Unit)
            }

            // Thumb alpha
            val alpha = remember { Animatable(0f) }
            val isThumbVisible = alpha.value > 0f
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

            Row(
                modifier = Modifier.offset { IntOffset(0, thumbOffsetY.roundToInt()) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Position indicator (shown when dragging)
                if (showPositionIndicator && isThumbDragged) {
                    val currentPosition = remember {
                        derivedStateOf {
                            val firstVisible = state.firstVisibleItemIndex
                            val total = state.layoutInfo.totalItemsCount
                            "${firstVisible + 1} / $total"
                        }
                    }
                    
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
                            text = currentPosition.value,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                Box(
                    modifier = Modifier
                        .then(
                            // Recompose opts
                            if (isThumbVisible && !state.isScrollInProgress) {
                                Modifier.draggable(
                                    interactionSource = dragInteractionSource,
                                    orientation = Orientation.Vertical,
                                    state = rememberDraggableState { delta ->
                                        val newOffsetY = thumbOffsetY + delta
                                        thumbOffsetY = newOffsetY.coerceIn(
                                            thumbTopPadding,
                                            thumbTopPadding + trackHeightPx,
                                        )
                                    },
                                )
                            } else Modifier,
                        )
                        .then(
                            // Exclude thumb from gesture area only when needed
                            if (isThumbVisible && !isThumbDragged && !state.isScrollInProgress) {
                                Modifier.systemGestureExclusion()
                            } else Modifier,
                        )
                        .height(ThumbLength)
                        .padding(horizontal = 8.dp)
                        .padding(end = endContentPadding)
                        .width(ThumbThickness)
                        .alpha(alpha.value)
                        .background(color = thumbColor, shape = ThumbShape),
                )
            }
        }.map { it.measure(scrollerConstraints) }
        val scrollerWidth = scrollerPlaceable.fastMaxBy { it.width }?.width ?: 0

        layout(contentWidth, contentHeight) {
            contentPlaceable.fastForEach {
                it.place(0, 0)
            }
            scrollerPlaceable.fastForEach {
                it.placeRelative(contentWidth - scrollerWidth, 0)
            }
        }
    }
}

private fun computeScrollOffset(state: LazyGridState): Int {
    if (state.layoutInfo.totalItemsCount == 0) return 0
    val visibleItems = state.layoutInfo.visibleItemsInfo
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
    val startChild = visibleItems.first()
    val endChild = visibleItems.last()
    val laidOutArea = (endChild.offset.y + endChild.size.height) - startChild.offset.y
    val laidOutRange = abs(startChild.index - endChild.index) + 1
    return (laidOutArea.toFloat() / laidOutRange * state.layoutInfo.totalItemsCount).roundToInt()
}

private fun computeScrollOffset(state: LazyListState): Int {
    if (state.layoutInfo.totalItemsCount == 0) return 0
    val visibleItems = state.layoutInfo.visibleItemsInfo
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
    val startChild = visibleItems.first()
    val endChild = visibleItems.last()
    val laidOutArea = endChild.bottom - startChild.top
    val laidOutRange = abs(startChild.index - endChild.index) + 1
    return (laidOutArea.toFloat() / laidOutRange * state.layoutInfo.totalItemsCount).roundToInt()
}

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
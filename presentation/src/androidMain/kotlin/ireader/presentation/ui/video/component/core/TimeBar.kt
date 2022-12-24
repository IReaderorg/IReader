package ireader.presentation.ui.video.component.core

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * Composable component for time bar that can display a current playback position, buffered position
 * and duration.
 *
 * @param durationMs The duration, in milliseconds.
 * @param positionMs The current position, in milliseconds.
 * @param bufferedPositionMs The current buffered position, in milliseconds.
 * @param modifier The [Modifier] to be applied to this layout node.
 * @param enabled Whether scrubbing is enabled.
 * @param contentPadding Paddings which won't clip the content.
 * @param scrubberCenterAsAnchor Whether use the scrubber's center as the anchor. If true, you
 * should also consider using [contentPadding] to specify proper horizontal paddings. Or the
 * scrubber will be clipped when it's at the start or end position. Default is false.
 * @param onScrubStart Callback that will be invoked when the user starts moving the scrubber.
 * @param onScrubMove Callback that will be invoked when the user moves the scrubber.
 * @param onScrubStop Callback that will be invoked when the user stops moving the scrubber.
 * @param progress The progress bar. By default this will use a [TimeBarProgress].
 * @param scrubber The scrubber handle. By default this will use a [TimeBarScrubber].
 */
@Composable
fun TimeBar(
    durationMs: Long,
    positionMs: Long,
    bufferedPositionMs: Long,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    scrubberCenterAsAnchor: Boolean = false,
    onScrubStart: ((positionMs: Long) -> Unit)? = null,
    onScrubMove: ((positionMs: Long) -> Unit)? = null,
    onScrubStop: ((positionMs: Long) -> Unit)? = null,
    progress: @Composable (current: Float, scrubbed: Float, buffered: Float) -> Unit = { _, scrubbed, buffered ->
        // by default, use scrubbed progress as played progress
        TimeBarProgress(played = scrubbed, buffered = buffered)
    },
    scrubber: @Composable (enable: Boolean, scrubbing: Boolean) -> Unit = { enable, scrubbing ->
        TimeBarScrubber(enable, scrubbing)
    }
) {
    val layoutDirection = LocalLayoutDirection.current
    require(
        contentPadding.calculateLeftPadding(layoutDirection) >= 0.dp &&
                contentPadding.calculateTopPadding() >= 0.dp &&
                contentPadding.calculateRightPadding(layoutDirection) >= 0.dp &&
                contentPadding.calculateBottomPadding() >= 0.dp
    ) {
        "Padding must be non-negative"
    }
    val currentPosition by rememberUpdatedState(positionMs)
    val currentOnScrubStart by rememberUpdatedState(newValue = onScrubStart)
    val currentOnScrubMove by rememberUpdatedState(newValue = onScrubMove)
    val currentOnScrubStop by rememberUpdatedState(newValue = onScrubStop)
    BoxWithConstraints(modifier = modifier.clipToBounds()) {
        var scrubbing by remember { mutableStateOf(false) }
        var scrubPosition by remember { mutableStateOf(0L) }
        if (!scrubbing) scrubPosition = positionMs
        val playheadPosition by remember {
            derivedStateOf { if (scrubbing) scrubPosition else currentPosition }
        }
        val contentLeftPadding = with(LocalDensity.current) {
            contentPadding.calculateLeftPadding(layoutDirection).toPx()
        }
        val contentRightPadding = with(LocalDensity.current) {
            contentPadding.calculateRightPadding(layoutDirection).toPx()
        }
        val barWidth = constraints.maxWidth - contentLeftPadding - contentRightPadding
        var scrubberWidth by remember { mutableStateOf(0) }
        val boundWidth by remember(barWidth, scrubberCenterAsAnchor) {
            derivedStateOf {
                if (scrubberCenterAsAnchor) barWidth
                else barWidth - scrubberWidth
            }
        }
        val positionFraction: Float by remember(durationMs) {
            derivedStateOf { if (durationMs != 0L) playheadPosition.toFloat() / durationMs else 0f }
        }
        val drag = Modifier.draggable(
            enabled = enabled && durationMs >= 0,
            orientation = Orientation.Horizontal,
            state = rememberDraggableState { delta ->
                val targetProgress = ((positionFraction * boundWidth + delta) / boundWidth)
                    .coerceIn(0f, 1f)
                scrubPosition = (targetProgress * durationMs).roundToLong()
                currentOnScrubMove?.invoke(scrubPosition)
            },
            startDragImmediately = true,
            onDragStarted = { startPosition ->
                scrubbing = true
                val startX = (startPosition.x - contentLeftPadding).run {
                    if (scrubberCenterAsAnchor) this
                    else this - scrubberWidth / 2
                }
                scrubPosition =
                    (startX / boundWidth * durationMs).roundToLong().coerceIn(0L, durationMs)
                currentOnScrubStart?.invoke(scrubPosition)
            },
            onDragStopped = {
                currentOnScrubStop?.invoke(scrubPosition)
                scrubbing = false
            }
        )


            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(drag)
                    .padding(contentPadding)
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        ,
                ) {
                        progress(
                            if (durationMs > 0) currentPosition.toFloat() / durationMs else 0f,
                            if (durationMs > 0) scrubPosition.toFloat() / durationMs else 0f,
                            if (durationMs > 0) bufferedPositionMs.toFloat() / durationMs else 0f
                        )
                }
                if (durationMs >= 0) {
                    Box(
                        modifier = Modifier
                            .wrapContentSize(unbounded = true)
                            .onGloballyPositioned { scrubberWidth = it.size.width }
                            .offset {
                                val offsetX =
                                    if (scrubberCenterAsAnchor) positionFraction * boundWidth - (scrubberWidth / 2f)
                                    else positionFraction * boundWidth
                                IntOffset(offsetX.roundToInt(), 0)
                            }
                    ) {
                        scrubber(enabled, scrubbing)
                    }
                }
        }



    }
}

@Composable
fun TimeBar2(
    durationMs: Long,
    positionMs: Long,
    bufferedPositionMs: Long,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    scrubberCenterAsAnchor: Boolean = false,
    onScrubStart: ((positionMs: Long) -> Unit)? = null,
    onScrubMove: ((positionMs: Long) -> Unit)? = null,
    onScrubStop: ((positionMs: Long) -> Unit)? = null,
    progress: @Composable (current: Float, scrubbed: Float, buffered: Float) -> Unit = { _, scrubbed, buffered ->
        // by default, use scrubbed progress as played progress
        TimeBarProgress(played = scrubbed, buffered = buffered)
    },
    scrubber: @Composable (enable: Boolean, scrubbing: Boolean) -> Unit = { enable, scrubbing ->
        TimeBarScrubber(enable, scrubbing)
    }
) {
    val layoutDirection = LocalLayoutDirection.current
    require(
        contentPadding.calculateLeftPadding(layoutDirection) >= 0.dp &&
                contentPadding.calculateTopPadding() >= 0.dp &&
                contentPadding.calculateRightPadding(layoutDirection) >= 0.dp &&
                contentPadding.calculateBottomPadding() >= 0.dp
    ) {
        "Padding must be non-negative"
    }
    val currentPosition by rememberUpdatedState(positionMs)
    val currentOnScrubStart by rememberUpdatedState(newValue = onScrubStart)
    val currentOnScrubMove by rememberUpdatedState(newValue = onScrubMove)
    val currentOnScrubStop by rememberUpdatedState(newValue = onScrubStop)
    BoxWithConstraints(modifier = modifier.clipToBounds()) {
        var scrubbing by remember { mutableStateOf(false) }
        var scrubPosition by remember { mutableStateOf(0L) }
        if (!scrubbing) scrubPosition = positionMs
        val playheadPosition by remember {
            derivedStateOf { if (scrubbing) scrubPosition else currentPosition }
        }
        val contentLeftPadding = with(LocalDensity.current) {
            contentPadding.calculateLeftPadding(layoutDirection).toPx()
        }
        val contentRightPadding = with(LocalDensity.current) {
            contentPadding.calculateRightPadding(layoutDirection).toPx()
        }
        val barWidth = constraints.maxWidth - contentLeftPadding - contentRightPadding
        var scrubberWidth by remember { mutableStateOf(0) }
        val boundWidth by remember(barWidth, scrubberCenterAsAnchor) {
            derivedStateOf {
                if (scrubberCenterAsAnchor) barWidth
                else barWidth - scrubberWidth
            }
        }
        val positionFraction: Float by remember(durationMs) {
            derivedStateOf { if (durationMs != 0L) playheadPosition.toFloat() / durationMs else 0f }
        }
        val drag = Modifier.draggable(
            enabled = enabled && durationMs >= 0,
            orientation = Orientation.Horizontal,
            state = rememberDraggableState { delta ->
                val targetProgress = ((positionFraction * boundWidth + delta) / boundWidth)
                    .coerceIn(0f, 1f)
                scrubPosition = (targetProgress * durationMs).roundToLong()
                currentOnScrubMove?.invoke(scrubPosition)
            },
            startDragImmediately = true,
            onDragStarted = { startPosition ->
                scrubbing = true
                val startX = (startPosition.x - contentLeftPadding).run {
                    if (scrubberCenterAsAnchor) this
                    else this - scrubberWidth / 2
                }
                scrubPosition =
                    (startX / boundWidth * durationMs).roundToLong().coerceIn(0L, durationMs)
                currentOnScrubStart?.invoke(scrubPosition)
            },
            onDragStopped = {
                currentOnScrubStop?.invoke(scrubPosition)
                scrubbing = false
            }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(drag)
                .padding(contentPadding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                progress(
                    if (durationMs > 0) currentPosition.toFloat() / durationMs else 0f,
                    if (durationMs > 0) scrubPosition.toFloat() / durationMs else 0f,
                    if (durationMs > 0) bufferedPositionMs.toFloat() / durationMs else 0f
                )
            }
            if (durationMs >= 0) {
                Box(
                    modifier = Modifier
                        .wrapContentSize(unbounded = true)
                        .onGloballyPositioned { scrubberWidth = it.size.width }
                        .offset {
                            val offsetX =
                                if (scrubberCenterAsAnchor) positionFraction * boundWidth - (scrubberWidth / 2f)
                                else positionFraction * boundWidth
                            IntOffset(offsetX.roundToInt(), 0)
                        }
                ) {
                    scrubber(enabled, scrubbing)
                }
            }
        }
    }
}

/**
 * The default progress composable component for [TimeBar].
 *
 * @param played The played progress.
 * @param buffered The buffered progress.
 * @param modifier The [Modifier] to be applied to this layout node.
 * @param playedColor Color for the portion of the time bar representing media before the current
 * playback position.
 * @param bufferedColor  Color for the portion of the time bar after the current played position up
 * to the current buffered position.
 * @param playedColor Color for the portion of the time bar after the current buffered position.
 */
@Composable
fun TimeBarProgress(
    played: Float,
    buffered: Float,
    modifier: Modifier = Modifier,
    playedColor: Color = Color(0xFFFFFFFF),
    bufferedColor: Color = Color(0xCCFFFFFF),
    unplayedColor: Color = Color(0x33FFFFFF),
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        var left = 0f
        // draw played
        if (played > 0) {
            val playedRight = played * width
            val playedWidth = playedRight - left
            drawRect(
                playedColor,
                topLeft = Offset(left, 0f),
                size = size.copy(width = playedWidth)
            )
            left = playedRight
        }
        // draw buffered
        if (buffered > played) {
            val bufferedRight = buffered * width
            val bufferedWidth = bufferedRight - left
            drawRect(
                bufferedColor,
                topLeft = Offset(left, 0f),
                size = size.copy(width = bufferedWidth)
            )
            left = bufferedRight
        }
        // draw unplayed
        if (left < size.width) {
            drawRect(
                unplayedColor,
                topLeft = Offset(left, 0f),
                size = size.copy(width = size.width - left)
            )
        }
    }
}

/**
 * The default scrubber handle composable component for [TimeBar].
 *
 * @param enabled Whether scrubbing is enabled.
 * @param scrubbing Whether the user is scrubbing.
 * @param enabledSize Dimension for the diameter of the circular scrubber handle when scrubbing is
 * enabled but not in progress. Set to `0.dp` if no scrubber handle should be shown.
 * @param disabledSize Dimension for the diameter of the circular scrubber handle when scrubbing
 * isn't enabled. Set to `0.dp` if no scrubber handle should be shown.
 * @param draggedSize Dimension for the diameter of the circular scrubber handle when scrubbing is
 * in progress. Set to `0.dp` if no scrubber handle should be shown.
 * @param color Color for the scrubber handle.
 * @param shape Shape of the scrubber handle.
 */
@Composable
fun TimeBarScrubber(
    enabled: Boolean,
    scrubbing: Boolean,
    modifier: Modifier = Modifier,
    enabledSize: Dp = 12.dp,
    disabledSize: Dp = 0.dp,
    draggedSize: Dp = 16.dp,
    color: Color = Color(0xFFFFFFFF),
    shape: Shape = CircleShape
) {
    val size = when {
        !enabled -> disabledSize
        scrubbing -> draggedSize
        else -> enabledSize
    }
    Spacer(
        modifier = modifier
            .size(size)
            .background(color, shape)
    )
}


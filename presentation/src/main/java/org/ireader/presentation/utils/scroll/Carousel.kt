package org.ireader.presentation.utils.scroll


import androidx.annotation.FloatRange
import androidx.annotation.Px
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.LayoutDirection.Ltr
import androidx.compose.ui.unit.dp

/**
 * Carousel View.
 *
 * Carousel is a scroll indicator for [ScrollableState] views.
 * This can be added by using [Modifier.verticalScroll], [Modifier.horizontalScroll]
 * which accepts [CarouselScrollState] as a state maintainer.
 *
 * @param state is the state of the scroll using [CarouselScrollState]
 * @param modifier [Modifier] to be applied to the View. If size (width or height) is not
 * set, then it takes the default values [DefaultCarouselWidth] and [DefaultCarouselHeight]
 * for width and height respectively.
 * @param minPercentage is the min percentage in float in between 0f and 1f that the thumb can
 * be. percentage is with respective to the width of the bar.
 * @param maxPercentage is the max percentage in float in between 0f and 1f that the thumb can
 * be. percentage is with respective to the width of the bar.
 * @param colors [CarouselColors] that accepts color and brush for thumb and bg to draw
 * based on the [CarouselScrollState.isScrollInProgress]
 *
 * @see rememberCarouselScrollState
 * @see CarouselScrollState
 * @see Modifier.horizontalScroll
 * @see Modifier.verticalScroll
 *
 * @author Sahruday (SAHU)
 *
 * **int a, b = 84, 73**
 */
@Composable
fun Carousel(
    state: CarouselScrollState,
    modifier: Modifier = Modifier,
    @FloatRange(from = 0.toDouble(), to = 1.toDouble(), fromInclusive = false, toInclusive = false)
    minPercentage: Float = DefaultCarouselMinPercentage,
    @FloatRange(from = 0.toDouble(), to = 1.toDouble(), fromInclusive = false, toInclusive = false)
    maxPercentage: Float = DefaultCarouselMaxPercentage,
    colors: CarouselColors = CarouselDefaults.colors(),
) = CarouselImpl(
    scrolled = state.value,
    maxScroll = state.maxValue,
    length = state.scrollableLength,
    modifier = modifier,
    isScrollInProgress = state.isScrollInProgress,
    minPercentage = minPercentage,
    maxPercentage = maxPercentage,
    colors = colors
)

/**
 * Carousel View.
 *
 * Carousel is a scroll indicator for [ScrollableState] views.
 * This can be added by using [LazyRow], [LazyColumn] and [LazyVerticalGrid]
 * which accepts [LazyListState] as a state maintainer.
 *
 * **NOTE: Use this when all items has same length along main axis.**
 *
 * @param state is the state of the scroll using [LazyListState]
 * @param modifier [Modifier] to be applied to the View. If size (width or height) is not
 * set, then it takes the default values [DefaultCarouselWidth] and [DefaultCarouselHeight]
 * for width and height respectively.
 * @param minPercentage is the min percentage in float in between 0f and 1f that the thumb can
 * be. percentage is with respective to the width of the bar.
 * @param maxPercentage is the max percentage in float in between 0f and 1f that the thumb can
 * be. percentage is with respective to the width of the bar.
 * @param colors [CarouselColors] that accepts color and brush for thumb and bg to draw
 * based on the [LazyListState.isScrollInProgress]
 *
 * @see rememberLazyListState
 *
 * @author Sahruday (SAHU)
 *
 * **int a, b = 84, 73**
 */
@Composable
fun Carousel(
    state: LazyListState,
    modifier: Modifier = Modifier,
    @FloatRange(from = 0.toDouble(), to = 1.toDouble(), fromInclusive = false, toInclusive = false)
    minPercentage: Float = DefaultCarouselMinPercentage,
    @FloatRange(from = 0.toDouble(), to = 1.toDouble(), fromInclusive = false, toInclusive = false)
    maxPercentage: Float = DefaultCarouselMaxPercentage,
    colors: CarouselColors = CarouselDefaults.colors(),
) {
    val itemLengthInPx = state.layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 0
    val length = itemLengthInPx * state.layoutInfo.totalItemsCount
    Carousel(
        state = state,
        totalLength = length,
        modifier = modifier,
        minPercentage = minPercentage,
        maxPercentage = maxPercentage,
        colors = colors,
    ) {
        state.firstVisibleItemIndex.times(itemLengthInPx) + state.firstVisibleItemScrollOffset
    }
}

/**
 * Carousel View.
 *
 * Carousel is a scroll indicator for [ScrollableState] views.
 * This can be added by using [LazyRow], [LazyColumn] and [LazyVerticalGrid]
 * which accepts [LazyListState] as a state maintainer.
 *
 * **NOTE: Use this when items are of different sizes and were known to calculate scroll length**
 *
 * @param state is the state of the scroll using [LazyListState]
 * @param totalLength is the total length of all item combined in [Px] along the main axis.
 * @param modifier [Modifier] to be applied to the View. If size (width or height) is not
 * set, then it takes the default values [DefaultCarouselWidth] and [DefaultCarouselHeight]
 * for width and height respectively.
 * @param minPercentage is the min percentage in float in between 0f and 1f that the thumb can
 * be. percentage is with respective to the width of the bar.
 * @param maxPercentage is the max percentage in float in between 0f and 1f that the thumb can
 * be. percentage is with respective to the width of the bar.
 * @param colors [CarouselColors] that accepts color and brush for thumb and bg to draw
 * based on the [LazyListState.isScrollInProgress]
 * @param scrolled is a lambda to calculate the amount that scrolled along main axis in [Px]
 *
 * @see rememberLazyListState
 *
 * @author Sahruday (SAHU)
 *
 * **int a, b = 84, 73**
 */
@Composable
fun Carousel(
    state: LazyListState,
    totalLength: Int,
    modifier: Modifier = Modifier,
    @FloatRange(from = 0.toDouble(), to = 1.toDouble(), fromInclusive = false, toInclusive = false)
    minPercentage: Float = DefaultCarouselMinPercentage,
    @FloatRange(from = 0.toDouble(), to = 1.toDouble(), fromInclusive = false, toInclusive = false)
    maxPercentage: Float = DefaultCarouselMaxPercentage,
    colors: CarouselColors = CarouselDefaults.colors(),
    scrolled: () -> Int,
) = CarouselImpl(scrolled = scrolled.invoke(),
    maxScroll = totalLength - state.layoutInfo.viewportEndOffset,
    length = totalLength,
    modifier = modifier,
    isScrollInProgress = state.isScrollInProgress,
    minPercentage = minPercentage,
    maxPercentage = maxPercentage,
    colors = colors)

@Composable
private fun CarouselImpl(
    scrolled: Int,
    maxScroll: Int,
    length: Int,
    modifier: Modifier,
    isScrollInProgress: Boolean,
    minPercentage: Float,
    maxPercentage: Float,
    colors: CarouselColors,
) {
    require(0f < minPercentage) { "min should be > 0f." }
    require(minPercentage <= maxPercentage) { "min should be < max." }
    require(maxPercentage < 1f) { "max should be less than 1f." }
    if (length <= 0 || maxScroll <= 0) return //Will not draw when there is nothing to scroll.

    Canvas(modifier = modifier.size(DefaultCarouselWidth, DefaultCarouselHeight)) {
        val isLtr = layoutDirection == Ltr

        val width = drawContext.size.width
        val height = drawContext.size.height

        val isVertical = height > width
        val barLength = if (isVertical) height else width
        val barWidth = if (isVertical) width else height

        val viewportRatio = (length - maxScroll) / length.toFloat() //ViewPort length / length
        val ratio = viewportRatio.coerceIn(minPercentage, maxPercentage)

        val thumbLength = ratio * barLength
        val maxScrollLength = barLength - thumbLength

        val xOffset: Float = (scrolled / maxScroll.toFloat()) * maxScrollLength
        val yOffset = barWidth / 2

        val barStart = if (isLtr) xOffset else maxScrollLength - xOffset
        val barEnd = barStart + thumbLength //if (isLtr) xOffset + thumbWidth else length - xOffset

        fun drawLine(
            brush: Brush,
            startOffSet: Float,
            endOffset: Float,
        ) = drawLine(
            brush = brush,
            start = if (isVertical) Offset(yOffset, startOffSet) else Offset(startOffSet, yOffset),
            end = if (isVertical) Offset(yOffset, endOffset) else Offset(endOffset, yOffset),
            cap = StrokeCap.Round,
            strokeWidth = barWidth,
        )

        //Draw Background
        drawLine(colors.backgroundBrush(isScrollInProgress), 0f, barLength)

        //Draw Thumb
        drawLine(colors.thumbBrush(isScrollInProgress), barStart, barEnd)
    }
}

/**
 * Default maximum percentage of the thumb to occupy in the bar.
 */
const val DefaultCarouselMaxPercentage = 0.8f

/**
 * Default minimum percentage of the thumb to occupy in the bar.
 */
const val DefaultCarouselMinPercentage = 0.2f

/**
 * Default width when no width constraint is added using modifier
 */
val DefaultCarouselWidth = 60.dp

/**
 * Default height when no height constraint is added using modifier
 */
val DefaultCarouselHeight = 4.dp

/**
 * Represents the colors and brushes used by the [Carousel].
 *
 * @see CarouselDefaults.colors
 */
interface CarouselColors {

    /**
     * Represents the brush used to draw the carousel thumb line, depending on [isScrollInProgress].
     *
     * Thumb line uses brush only when when it is not null.
     *
     * @param isScrollInProgress is weather the scroll is action or not
     *
     * @see [ScrollableState.isScrollInProgress]
     */
    fun thumbBrush(isScrollInProgress: Boolean): Brush

    /**
     * Represents the brush used to draw the carousel bg line, depending on [isScrollInProgress].
     *
     * Background line uses brush only when when it is not null.
     *
     * @param isScrollInProgress is weather the scroll is action or not
     *
     * @see [ScrollableState.isScrollInProgress]
     */
    fun backgroundBrush(isScrollInProgress: Boolean): Brush

}

object CarouselDefaults {

    @Composable
    fun colors(
        thumbBrush: Brush,
        scrollingThumbBrush: Brush = thumbBrush,
        backgroundBrush: Brush,
        scrollingBackgroundBrush: Brush = backgroundBrush,
    ): CarouselColors = DefaultCarousalColors(
        thumbBrush = thumbBrush,
        scrollingThumbBrush = scrollingThumbBrush,
        backgroundBrush = backgroundBrush,
        scrollingBackgroundBrush = scrollingBackgroundBrush
    )

    @Composable
    fun colors(
        thumbColor: Color = MaterialTheme.colors.secondary,
        scrollingThumbColor: Color = thumbColor,
        backgroundColor: Color = contentColorFor(thumbColor).copy(alpha = BgAlpha),
        scrollingBackgroundColor: Color = backgroundColor,
    ): CarouselColors = DefaultCarousalColors(
        thumbBrush = SolidColor(thumbColor),
        scrollingThumbBrush = SolidColor(scrollingThumbColor),
        backgroundBrush = SolidColor(backgroundColor),
        scrollingBackgroundBrush = SolidColor(scrollingBackgroundColor)
    )

    const val BgAlpha = 0.25f
}

@Immutable
private class DefaultCarousalColors(
    private val thumbBrush: Brush,
    private val scrollingThumbBrush: Brush,
    private val backgroundBrush: Brush,
    private val scrollingBackgroundBrush: Brush,
) : CarouselColors {

    override fun thumbBrush(isScrollInProgress: Boolean): Brush =
        if (isScrollInProgress) thumbBrush else scrollingThumbBrush

    override fun backgroundBrush(isScrollInProgress: Boolean): Brush =
        if (isScrollInProgress) backgroundBrush else scrollingBackgroundBrush

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as DefaultCarousalColors

        if (thumbBrush != other.thumbBrush) return false
        if (scrollingThumbBrush != other.scrollingThumbBrush) return false
        if (backgroundBrush != other.backgroundBrush) return false
        if (scrollingBackgroundBrush != other.scrollingBackgroundBrush) return false

        return true
    }

    override fun hashCode(): Int {
        var result = thumbBrush.hashCode()
        result = 31 * result + scrollingThumbBrush.hashCode()
        result = 31 * result + backgroundBrush.hashCode()
        result = 31 * result + scrollingBackgroundBrush.hashCode()
        return result
    }
}
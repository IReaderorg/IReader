package ireader.presentation.ui.component.list.scrollbars

/*
 * MIT License
 *
 * Copyright (c) 2022 Albert Chang
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

/**
 * Code taken from https://gist.github.com/mxalbert1996/33a360fcab2105a31e5355af98216f5a
 * with some modifications to handle contentPadding.
 *
 * Migrated from Modifier.composed to Modifier.Node for better performance.
 */

import android.view.ViewConfiguration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastSumBy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

fun Modifier.drawHorizontalScrollbar(
    state: LazyListState,
    reverseScrolling: Boolean = false,
    positionOffsetPx: Float = 0f,
): Modifier = this.then(
    ScrollbarElement(
        state = state,
        orientation = Orientation.Horizontal,
        reverseScrolling = reverseScrolling,
        positionOffset = positionOffsetPx
    )
)

fun Modifier.drawVerticalScrollbar(
    state: LazyListState,
    reverseScrolling: Boolean = false,
    positionOffsetPx: Float = 0f,
): Modifier = this.then(
    ScrollbarElement(
        state = state,
        orientation = Orientation.Vertical,
        reverseScrolling = reverseScrolling,
        positionOffset = positionOffsetPx
    )
)

private data class ScrollbarElement(
    val state: LazyListState,
    val orientation: Orientation,
    val reverseScrolling: Boolean,
    val positionOffset: Float,
) : ModifierNodeElement<ScrollbarNode>() {
    override fun create() = ScrollbarNode(state, orientation, reverseScrolling, positionOffset)
    
    override fun update(node: ScrollbarNode) {
        node.update(state, orientation, reverseScrolling, positionOffset)
    }
    
    override fun InspectorInfo.inspectableProperties() {
        name = "drawScrollbar"
        properties["state"] = state
        properties["orientation"] = orientation
        properties["reverseScrolling"] = reverseScrolling
        properties["positionOffset"] = positionOffset
    }
}

private class ScrollbarNode(
    private var state: LazyListState,
    private var orientation: Orientation,
    private var reverseScrolling: Boolean,
    private var positionOffset: Float,
) : DrawModifierNode, Modifier.Node() {
    
    private val scrolled = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    
    private val alpha = Animatable(0f)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    
    // Cached values
    private var thickness: Float = 0f
    private val color: Color = Color.Black.copy(alpha = 0.364f)
    
    init {
        scope.launch {
            scrolled.collectLatest {
                alpha.snapTo(1f)
                alpha.animateTo(0f, animationSpec = FadeOutAnimationSpec)
            }
        }
    }
    
    fun update(
        state: LazyListState,
        orientation: Orientation,
        reverseScrolling: Boolean,
        positionOffset: Float,
    ) {
        this.state = state
        this.orientation = orientation
        this.reverseScrolling = reverseScrolling
        this.positionOffset = positionOffset
    }
    
    @Suppress("CyclomaticComplexMethod")
    override fun ContentDrawScope.draw() {
        drawContent()
        
        // Initialize thickness on first draw (needs context)
        if (thickness == 0f) {
            thickness = 8f // Default scrollbar thickness in pixels
        }
        
        val layoutInfo = state.layoutInfo
        val viewportSize = if (orientation == Orientation.Horizontal) {
            layoutInfo.viewportSize.width
        } else {
            layoutInfo.viewportSize.height
        } - layoutInfo.beforeContentPadding - layoutInfo.afterContentPadding
        
        val items = layoutInfo.visibleItemsInfo
        val itemsSize = items.fastSumBy { it.size }
        val showScrollbar = items.size < layoutInfo.totalItemsCount || itemsSize > viewportSize
        
        if (!showScrollbar || alpha.value <= 0f) return
        
        val estimatedItemSize = if (items.isEmpty()) 0f else itemsSize.toFloat() / items.size
        val totalSize = estimatedItemSize * layoutInfo.totalItemsCount
        val thumbSize = viewportSize / totalSize * viewportSize
        
        val startOffset = if (items.isEmpty()) 0f else items.first().run {
            val startPadding = if (reverseScrolling) layoutInfo.afterContentPadding else layoutInfo.beforeContentPadding
            startPadding + ((estimatedItemSize * index - offset) / totalSize * viewportSize)
        }
        
        val reverseDirection = if (orientation == Orientation.Horizontal) {
            reverseScrolling // Simplified - LTR assumed
        } else reverseScrolling
        
        val atEnd = orientation == Orientation.Vertical // Right side for vertical
        
        val topLeft = if (orientation == Orientation.Horizontal) {
            Offset(
                if (reverseDirection) size.width - startOffset - thumbSize else startOffset,
                if (atEnd) size.height - positionOffset - thickness else positionOffset,
            )
        } else {
            Offset(
                if (atEnd) size.width - positionOffset - thickness else positionOffset,
                if (reverseDirection) size.height - startOffset - thumbSize else startOffset,
            )
        }
        
        val scrollbarSize = if (orientation == Orientation.Horizontal) {
            Size(thumbSize, thickness)
        } else {
            Size(thickness, thumbSize)
        }
        
        drawRect(
            color = color,
            topLeft = topLeft,
            size = scrollbarSize,
            alpha = alpha.value,
        )
    }
}

private val FadeOutAnimationSpec = tween<Float>(
    durationMillis = ViewConfiguration.getScrollBarFadeDuration(),
    delayMillis = ViewConfiguration.getScrollDefaultDelay(),
)

@Preview(widthDp = 400, heightDp = 400, showBackground = true)
@Composable
private fun LazyListScrollbarPreview() {
    val state = rememberLazyListState()
    LazyColumn(
        modifier = Modifier.drawVerticalScrollbar(state),
        state = state,
    ) {
        items(50) {
            Text(
                text = "Item ${it + 1}",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            )
        }
    }
}

@Preview(widthDp = 400, showBackground = true)
@Composable
private fun LazyListHorizontalScrollbarPreview() {
    val state = rememberLazyListState()
    LazyRow(
        modifier = Modifier.drawHorizontalScrollbar(state),
        state = state,
    ) {
        items(50) {
            Text(
                text = (it + 1).toString(),
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 16.dp),
            )
        }
    }
}

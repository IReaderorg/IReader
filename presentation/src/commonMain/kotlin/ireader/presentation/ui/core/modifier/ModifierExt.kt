package ireader.presentation.ui.core.modifier

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import kotlinx.coroutines.CoroutineScope

fun Modifier.secondaryItemAlpha(): Modifier = this.alpha(.78f)

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.clickableNoIndication(
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
): Modifier = composed {
    this.combinedClickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onLongClick = onLongClick,
        onClick = onClick,
    )
}

expect fun Modifier.systemGestureExclusion(): Modifier

@Composable
expect fun Modifier.supportDesktopScroll(scrollState: ScrollState,scope: CoroutineScope,enable:Boolean = true): Modifier

@Composable
expect fun Modifier.supportDesktopScroll(scrollState: LazyListState,scope: CoroutineScope,enable:Boolean = true): Modifier

/**
 * Desktop horizontal scroll support for ScrollState.
 * Enables horizontal scrolling with mouse wheel (Shift+Scroll) and trackpad gestures.
 * On non-desktop platforms, this is a no-op.
 */
@Composable
expect fun Modifier.supportDesktopHorizontalScroll(
    scrollState: ScrollState,
    scope: CoroutineScope,
    enable: Boolean = true
): Modifier

/**
 * Desktop bidirectional scroll support for combined vertical and horizontal scrolling.
 * Enables vertical scrolling with mouse wheel and horizontal scrolling with Shift+Scroll.
 * On non-desktop platforms, this is a no-op.
 */
@Composable
expect fun Modifier.supportDesktopBidirectionalScroll(
    verticalScrollState: ScrollState,
    horizontalScrollState: ScrollState,
    scope: CoroutineScope,
    enable: Boolean = true
): Modifier

/**
 * Desktop horizontal scroll support for LazyListState (LazyRow).
 * Enables horizontal scrolling with mouse wheel and trackpad gestures.
 * On non-desktop platforms, this is a no-op.
 */
@Composable
expect fun Modifier.supportDesktopHorizontalLazyListScroll(
    lazyListState: LazyListState,
    scope: CoroutineScope,
    enable: Boolean = true
): Modifier
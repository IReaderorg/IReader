package ireader.presentation.core.util

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember

/**
 * LazyListState extensions for optimized scroll handling, following Mihon's patterns.
 */

/**
 * Determines if FAB should be expanded based on scroll state.
 * FAB expands when:
 * - At the top of the list
 * - Scrolling backward (up)
 * - Cannot scroll forward (at bottom)
 * 
 * Uses derivedStateOf for efficient recomposition.
 */
@Composable
fun LazyListState.shouldExpandFAB(): Boolean {
    return remember {
        derivedStateOf {
            (firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0) ||
                lastScrolledBackward ||
                !canScrollForward
        }
    }.value
}

/**
 * Returns true if the list is scrolled to the top.
 */
@Composable
fun LazyListState.isAtTop(): Boolean {
    return remember {
        derivedStateOf {
            firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0
        }
    }.value
}

/**
 * Returns true if the list is scrolled to the bottom.
 */
@Composable
fun LazyListState.isAtBottom(): Boolean {
    return remember {
        derivedStateOf {
            !canScrollForward
        }
    }.value
}

/**
 * Returns true if the list is currently being scrolled.
 */
@Composable
fun LazyListState.isScrolling(): Boolean {
    return remember {
        derivedStateOf {
            isScrollInProgress
        }
    }.value
}

/**
 * Returns the scroll progress as a float between 0 and 1.
 * 0 = at top, 1 = at bottom
 */
@Composable
fun LazyListState.scrollProgress(): Float {
    return remember {
        derivedStateOf {
            val totalItems = layoutInfo.totalItemsCount
            if (totalItems == 0) return@derivedStateOf 0f
            
            val firstVisibleIndex = firstVisibleItemIndex
            val firstVisibleOffset = firstVisibleItemScrollOffset
            val visibleItems = layoutInfo.visibleItemsInfo
            
            if (visibleItems.isEmpty()) return@derivedStateOf 0f
            
            val firstItemSize = visibleItems.firstOrNull()?.size ?: 1
            val scrolledPastFirst = firstVisibleIndex + (firstVisibleOffset.toFloat() / firstItemSize)
            
            (scrolledPastFirst / (totalItems - 1).coerceAtLeast(1)).coerceIn(0f, 1f)
        }
    }.value
}

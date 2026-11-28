package ireader.presentation.ui.component.animations

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue

import ireader.presentation.ui.component.AnimationPolicy
/**
 * State helpers that jump directly to the target value when max performance mode is on.
 *
 * Material's `open()`/`close()` and `animateScrollTo*()` always animate; these helpers keep the
 * same call shape but use the instant equivalents when [AnimationPolicy.animationsDisabled] is
 * true.
 */

suspend fun DrawerState.openRespectingPolicy() {
    if (AnimationPolicy.animationsDisabled) {
        snapTo(DrawerValue.Open)
    } else {
        open()
    }
}

suspend fun DrawerState.closeRespectingPolicy() {
    if (AnimationPolicy.animationsDisabled) {
        snapTo(DrawerValue.Closed)
    } else {
        close()
    }
}

suspend fun PagerState.scrollToPageRespectingPolicy(
    page: Int,
    pageOffsetFraction: Float = 0f,
    animationSpec: AnimationSpec<Float>? = null,
) {
    if (AnimationPolicy.animationsDisabled) {
        scrollToPage(page, pageOffsetFraction)
    } else if (animationSpec != null) {
        animateScrollToPage(page, pageOffsetFraction, animationSpec)
    } else {
        animateScrollToPage(page, pageOffsetFraction)
    }
}

suspend fun LazyListState.scrollToItemRespectingPolicy(
    index: Int,
    scrollOffset: Int = 0,
) {
    if (AnimationPolicy.animationsDisabled) {
        scrollToItem(index, scrollOffset)
    } else {
        animateScrollToItem(index, scrollOffset)
    }
}

suspend fun LazyGridState.scrollToItemRespectingPolicy(
    index: Int,
    scrollOffset: Int = 0,
) {
    if (AnimationPolicy.animationsDisabled) {
        scrollToItem(index, scrollOffset)
    } else {
        animateScrollToItem(index, scrollOffset)
    }
}

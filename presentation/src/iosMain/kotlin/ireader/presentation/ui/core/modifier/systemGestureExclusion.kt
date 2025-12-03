package ireader.presentation.ui.core.modifier

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope

actual fun Modifier.systemGestureExclusion(): Modifier = this

@Composable
actual fun Modifier.supportDesktopScroll(
    scrollState: ScrollState,
    scope: CoroutineScope,
    enable: Boolean
): Modifier {
    // iOS handles scrolling natively
    return this
}

@Composable
actual fun Modifier.supportDesktopScroll(
    scrollState: LazyListState,
    scope: CoroutineScope,
    enable: Boolean
): Modifier {
    // iOS handles scrolling natively
    return this
}

/**
 * iOS horizontal scroll support - native scrolling is used
 */
@Composable
actual fun Modifier.supportDesktopHorizontalScroll(
    scrollState: ScrollState,
    scope: CoroutineScope,
    enable: Boolean
): Modifier {
    // iOS handles horizontal scrolling natively
    return this
}

/**
 * iOS bidirectional scroll support - native scrolling is used
 */
@Composable
actual fun Modifier.supportDesktopBidirectionalScroll(
    verticalScrollState: ScrollState,
    horizontalScrollState: ScrollState,
    scope: CoroutineScope,
    enable: Boolean
): Modifier {
    // iOS handles bidirectional scrolling natively
    return this
}

/**
 * iOS horizontal LazyList scroll support - native scrolling is used
 */
@Composable
actual fun Modifier.supportDesktopHorizontalLazyListScroll(
    lazyListState: LazyListState,
    scope: CoroutineScope,
    enable: Boolean
): Modifier {
    // iOS handles horizontal scrolling natively
    return this
}

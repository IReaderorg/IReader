package ireader.presentation.ui.core.modifier

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope

actual fun Modifier.systemGestureExclusion(): Modifier = this.systemGestureExclusion()


@Composable
actual fun Modifier.supportDesktopScroll(
    scrollState: ScrollState,
    scope: CoroutineScope, enable:Boolean
): Modifier = this

@Composable
actual fun Modifier.supportDesktopScroll(
    scrollState: LazyListState,
    scope: CoroutineScope,enable:Boolean
): Modifier = this
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
expect fun Modifier.systemBarsPadding(): Modifier
expect fun Modifier.navigationBarsPadding(): Modifier
@Composable
expect fun Modifier.supportDesktopScroll(scrollState: ScrollState,scope: CoroutineScope,enable:Boolean = true): Modifier

@Composable
expect fun Modifier.supportDesktopScroll(scrollState: LazyListState,scope: CoroutineScope,enable:Boolean = true): Modifier
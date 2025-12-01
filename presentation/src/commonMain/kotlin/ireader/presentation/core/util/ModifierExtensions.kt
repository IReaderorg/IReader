package ireader.presentation.core.util

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind

/**
 * Modifier extensions optimized for Compose performance, following Mihon's patterns.
 */

/**
 * Standard alpha for secondary/disabled items
 */
const val SECONDARY_ALPHA = 0.78f
const val DISABLED_ALPHA = 0.38f

/**
 * Applies a selected background color efficiently using drawBehind.
 * Only draws when isSelected is true, avoiding unnecessary allocations.
 */
@Composable
fun Modifier.selectedBackground(isSelected: Boolean): Modifier {
    if (!isSelected) return this
    val alpha = if (isSystemInDarkTheme()) 0.16f else 0.22f
    val color = MaterialTheme.colorScheme.secondary.copy(alpha = alpha)
    return this.drawBehind { drawRect(color) }
}

/**
 * Applies secondary item alpha for less prominent items.
 */
fun Modifier.secondaryItemAlpha(): Modifier = this.alpha(SECONDARY_ALPHA)

/**
 * Applies disabled alpha for disabled items.
 */
fun Modifier.disabledAlpha(): Modifier = this.alpha(DISABLED_ALPHA)

/**
 * Clickable without ripple indication.
 * Useful for custom click handling where ripple is not desired.
 */
fun Modifier.clickableNoIndication(
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
): Modifier = this.combinedClickable(
    interactionSource = null,
    indication = null,
    onLongClick = onLongClick,
    onClick = onClick,
)

/**
 * Conditional modifier application.
 * Applies the modifier only if condition is true.
 */
inline fun Modifier.thenIf(
    condition: Boolean,
    crossinline block: Modifier.() -> Modifier,
): Modifier = if (condition) block() else this

/**
 * Conditional modifier application with else branch.
 */
inline fun Modifier.thenIfElse(
    condition: Boolean,
    crossinline ifTrue: Modifier.() -> Modifier,
    crossinline ifFalse: Modifier.() -> Modifier,
): Modifier = if (condition) ifTrue() else ifFalse()

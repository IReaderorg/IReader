package ireader.presentation.ui.component.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A container that automatically scrolls to keep focused text fields visible
 * when the keyboard appears.
 * 
 * Usage:
 * ```
 * KeyboardAwareContent {
 *     TextField(...)
 *     TextField(...)
 * }
 * ```
 */
@Composable
fun KeyboardAwareContent(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    
    // Track IME visibility
    val imeHeight = WindowInsets.ime.getBottom(density)
    val isImeVisible = imeHeight > 0
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .imePadding() // Add padding when keyboard is visible
    ) {
        content()
        
        // Add extra space at the bottom when keyboard is visible
        // This ensures the last text field can be scrolled above the keyboard
        if (isImeVisible) {
            Spacer(modifier = Modifier.height(with(density) { imeHeight.toDp() }))
        }
    }
}

/**
 * Modifier that automatically scrolls a text field into view when it gains focus.
 * Use this on individual text fields for fine-grained control.
 * 
 * Usage:
 * ```
 * TextField(
 *     modifier = Modifier.scrollIntoViewWhenFocused(scrollState)
 * )
 * ```
 */
@Composable
fun Modifier.scrollIntoViewWhenFocused(
    scrollState: androidx.compose.foundation.ScrollState,
    extraPadding: androidx.compose.ui.unit.Dp = 100.dp
): Modifier = composed {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    var fieldPosition by remember { mutableStateOf(0f) }
    val imeHeight = WindowInsets.ime.getBottom(density)
    this
        .onGloballyPositioned { coordinates ->
            fieldPosition = coordinates.positionInRoot().y
        }
        .onFocusChanged { focusState ->
            if (focusState.isFocused) {
                scope.launch {
                    // Small delay to let the keyboard animation start
                    delay(100)
                    
                    // Calculate scroll position to center the field
                    val extraPaddingPx = with(density) { extraPadding.toPx() }
                    val targetScroll = (fieldPosition - extraPaddingPx).coerceAtLeast(0f)
                    
                    scrollState.animateScrollTo(targetScroll.toInt())
                }
            }
        }
}

/**
 * A Box that handles keyboard visibility and adjusts content accordingly.
 * Simpler alternative to KeyboardAwareContent for non-scrollable content.
 */
@Composable
fun KeyboardAwareBox(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .imePadding() // Automatically adds padding when keyboard appears
    ) {
        content()
    }
}

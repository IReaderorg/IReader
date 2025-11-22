package ireader.presentation.ui.core.modifier

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Adds padding to accommodate the IME (keyboard) when it's visible.
 * This ensures text fields are not covered by the keyboard.
 */
fun Modifier.keyboardPadding(): Modifier = this.imePadding()

/**
 * Adds padding for both navigation bars and IME (keyboard).
 * Use this for bottom content that should stay above both the nav bar and keyboard.
 */
fun Modifier.navigationBarsWithImePadding(): Modifier = composed {
    this
        .navigationBarsPadding()
        .imePadding()
}

/**
 * Gets the current IME (keyboard) height as a Dp value.
 */
@Composable
fun rememberImeHeight(): State<Dp> {
    val density = LocalDensity.current
    val ime = WindowInsets.ime
    val imeHeight = with(density) { ime.getBottom(density).toDp() }
    return rememberUpdatedState(imeHeight)
}

/**
 * Checks if the IME (keyboard) is currently visible.
 */
@Composable
fun isImeVisible(): Boolean {
    val imeHeight = rememberImeHeight()
    return imeHeight.value > 0.dp
}

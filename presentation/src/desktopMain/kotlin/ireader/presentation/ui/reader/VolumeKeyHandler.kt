package ireader.presentation.ui.reader

import androidx.compose.foundation.focusable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type

/**
 * Desktop implementation of volume key handler for reader navigation
 * Uses Page Up/Page Down keys and Volume Up/Volume Down as equivalents
 */
@Composable
actual fun Modifier.volumeKeyHandler(
    enabled: Boolean,
    onVolumeUp: () -> Unit,
    onVolumeDown: () -> Unit
): Modifier {
    if (!enabled) return this

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(enabled) {
        if (enabled) {
            try {
                focusRequester.requestFocus()
            } catch (_: Throwable) {
                // Focus request best-effort
            }
        }
    }
    
    return this
        .focusRequester(focusRequester)
        .focusable()
        .onKeyEvent { event ->
            if (event.type == KeyEventType.KeyDown) {
                when (event.key) {
                    Key.PageUp, Key.VolumeUp -> {
                        onVolumeUp()
                        true
                    }
                    Key.PageDown, Key.VolumeDown -> {
                        onVolumeDown()
                        true
                    }
                    else -> false
                }
            } else {
                false
            }
        }
}

package ireader.presentation.ui.reader

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type

/**
 * Desktop implementation of volume key handler for reader navigation
 * Uses Page Up/Page Down keys as volume key equivalents
 */
@Composable
actual fun Modifier.volumeKeyHandler(
    enabled: Boolean,
    onVolumeUp: () -> Unit,
    onVolumeDown: () -> Unit
): Modifier {
    if (!enabled) return this
    
    return this.onKeyEvent { event ->
        if (event.type == KeyEventType.KeyDown) {
            when (event.key) {
                Key.PageUp -> {
                    onVolumeUp()
                    true
                }
                Key.PageDown -> {
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

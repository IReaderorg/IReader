package ireader.presentation.ui.reader

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * iOS implementation of volume key handler for reader navigation
 * iOS doesn't allow intercepting volume buttons for non-audio purposes
 */
@Composable
actual fun Modifier.volumeKeyHandler(
    enabled: Boolean,
    onVolumeUp: () -> Unit,
    onVolumeDown: () -> Unit
): Modifier {
    // iOS doesn't allow intercepting volume buttons
    // Users can use swipe gestures instead
    return this
}

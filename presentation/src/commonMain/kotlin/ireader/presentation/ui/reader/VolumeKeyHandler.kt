package ireader.presentation.ui.reader

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Platform-specific volume key handler for reader navigation
 */
@Composable
expect fun Modifier.volumeKeyHandler(
    enabled: Boolean,
    onVolumeUp: () -> Unit,
    onVolumeDown: () -> Unit
): Modifier

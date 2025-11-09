package ireader.presentation.ui.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

/**
 * Composable that enables or disables secure screen mode
 * When enabled, prevents screenshots and screen recording
 */
@Composable
expect fun SecureScreen(
    enabled: Boolean,
    content: @Composable () -> Unit
)

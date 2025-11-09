package ireader.presentation.ui.core

import androidx.compose.runtime.Composable

/**
 * Desktop stub implementation of SecureScreen
 * Secure screen is not applicable on desktop
 */
@Composable
actual fun SecureScreen(
    enabled: Boolean,
    content: @Composable () -> Unit
) {
    // Desktop doesn't support secure screen
    content()
}

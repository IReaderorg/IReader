package ireader.presentation.ui.core

import androidx.compose.runtime.Composable

/**
 * iOS implementation of SecureScreen
 * iOS handles secure content differently through UIKit
 */
@Composable
actual fun SecureScreen(
    enabled: Boolean,
    content: @Composable () -> Unit
) {
    // iOS secure screen would need UIKit integration
    // For now, just render the content
    content()
}

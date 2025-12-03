package ireader.presentation.ui.component

import androidx.compose.runtime.Composable

@Composable
actual fun IBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS handles back navigation through swipe gestures and navigation controller
    // This is a no-op as iOS doesn't have a hardware back button
}

package ireader.presentation.ui.core.navigation

import androidx.compose.runtime.Composable

/**
 * Predictive back gesture handler for iOS (no-op).
 * 
 * iOS uses swipe-from-edge gestures for back navigation,
 * which is handled by the navigation controller.
 */
@Composable
fun PredictiveBackHandler(
    enabled: Boolean = true,
    onBack: () -> Unit
) {
    // No-op for iOS - back gestures are handled by UINavigationController
}

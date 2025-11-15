package ireader.presentation.ui.core.navigation

import androidx.compose.runtime.Composable

/**
 * Predictive back gesture handler for Desktop (no-op).
 * 
 * Desktop doesn't have back gestures, so this is a no-op implementation
 * for cross-platform compatibility.
 */
@Composable
fun PredictiveBackHandler(
    enabled: Boolean = true,
    onBack: () -> Unit
) {
    // No-op for desktop
}

package ireader.presentation.ui.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Displays a loading indicator in the center of the screen.
 * 
 * @param modifier Modifier for the container
 * @param disableAnimation Whether to disable the loading animation (shows static indicator)
 */
@Composable
fun LoadingScreen(
    modifier: Modifier = Modifier,
    disableAnimation: Boolean = false
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (disableAnimation) {
            // Static progress indicator - just shows a circle without animation
            CircularProgressIndicator(progress = 0.75f)
        } else {
            // Animated progress indicator (default behavior)
            CircularProgressIndicator()
        }
    }
}

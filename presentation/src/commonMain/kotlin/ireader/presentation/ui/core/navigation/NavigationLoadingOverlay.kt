package ireader.presentation.ui.core.navigation

import ireader.presentation.ui.component.animations.AnimatedVisibility
import ireader.presentation.ui.component.animations.fadeIn
import ireader.presentation.ui.component.animations.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Loading overlay to show during navigation transitions.
 * 
 * This provides visual feedback when navigating between screens
 * and helps maintain a smooth user experience.
 */
@Composable
fun NavigationLoadingOverlay(
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isLoading,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

package ireader.presentation.ui.core.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Screen container that ensures consistent background colors across all screens.
 * 
 * This wrapper prevents white flashes by maintaining a persistent background
 * color that matches the current theme.
 */
@Composable
fun ScreenContainer(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        content()
    }
}

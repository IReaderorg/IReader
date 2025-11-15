package ireader.presentation.ui.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Stub implementation of AsyncImage for loading images.
 * TODO: Replace with proper image loading library (Coil, Kamel, etc.)
 * 
 * This is a temporary placeholder to fix compilation errors.
 * Proper implementation should:
 * - Load images from URLs asynchronously
 * - Show loading placeholder
 * - Handle errors gracefully
 * - Cache images
 */
@Composable
fun AsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    error: @Composable (() -> Unit)? = null,
) {
    // TODO: Implement actual image loading
    // For now, just show a placeholder box
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        placeholder?.invoke()
    }
}

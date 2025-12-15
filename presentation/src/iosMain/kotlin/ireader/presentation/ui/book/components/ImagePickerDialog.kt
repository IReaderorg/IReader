package ireader.presentation.ui.book.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import ireader.core.log.Log

/**
 * iOS implementation of image picker.
 * 
 * Note: Full iOS implementation would require UIImagePickerController integration.
 * This is a placeholder that dismisses immediately.
 */
@Composable
actual fun ImagePickerDialog(
    show: Boolean,
    onImageSelected: (uri: String) -> Unit,
    onDismiss: () -> Unit
) {
    LaunchedEffect(show) {
        if (show) {
            // TODO: Implement iOS image picker using UIImagePickerController
            // For now, just dismiss
            Log.info { "iOS image picker not yet implemented" }
            onDismiss()
        }
    }
}

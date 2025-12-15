package ireader.presentation.ui.book.components

import androidx.compose.runtime.Composable

/**
 * Platform-specific image picker dialog.
 * 
 * On Android: Uses ActivityResultContracts.GetContent to pick an image
 * On Desktop: Uses JFileChooser to select an image file
 * On iOS: Uses UIImagePickerController
 * 
 * @param show Whether to show the picker
 * @param onImageSelected Callback with the URI of the selected image (content:// or file://)
 * @param onDismiss Callback when the picker is dismissed without selection
 */
@Composable
expect fun ImagePickerDialog(
    show: Boolean,
    onImageSelected: (uri: String) -> Unit,
    onDismiss: () -> Unit
)

package ireader.presentation.ui.characterart

import androidx.compose.runtime.*

actual class ImagePicker {
    private var selectedPath: String? = null
    private var selectedBytes: ByteArray? = null
    
    actual suspend fun pickImage(
        onImagePicked: (bytes: ByteArray, fileName: String) -> Unit,
        onError: (String) -> Unit
    ) {
        // iOS implementation would use UIImagePickerController
        // This is a stub - actual implementation requires platform-specific code
        onError("Image picking not yet implemented for iOS")
    }
    
    actual fun getSelectedImagePath(): String? {
        return selectedPath
    }
    
    actual fun clearSelection() {
        selectedPath = null
        selectedBytes = null
    }
}

@Composable
actual fun rememberImagePicker(): ImagePicker {
    return remember { ImagePicker() }
}

/**
 * Composable that provides image picking functionality for iOS
 */
@Composable
fun ImagePickerHost(
    onImagePicked: (ByteArray, String) -> Unit,
    onError: (String) -> Unit,
    content: @Composable (launchPicker: () -> Unit, selectedPath: String?) -> Unit
) {
    val picker = remember { ImagePicker() }
    var selectedPath by remember { mutableStateOf<String?>(null) }
    
    content(
        launchPicker = {
            // iOS picker launch would go here
            onError("Image picking not yet implemented for iOS")
        },
        selectedPath = selectedPath
    )
}

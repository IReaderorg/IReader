package ireader.presentation.ui.characterart

import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

actual class ImagePicker {
    private var selectedFile: File? = null
    private var selectedBytes: ByteArray? = null
    
    actual suspend fun pickImage(
        onImagePicked: (bytes: ByteArray, fileName: String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            withContext(Dispatchers.IO) {
                val file = showFileChooser()
                
                if (file != null) {
                    // Validate it's an image
                    val image = ImageIO.read(file)
                    if (image == null) {
                        withContext(Dispatchers.Main) {
                            onError("Invalid image file")
                        }
                        return@withContext
                    }
                    
                    val bytes = file.readBytes()
                    selectedFile = file
                    selectedBytes = bytes
                    
                    withContext(Dispatchers.Main) {
                        onImagePicked(bytes, file.name)
                    }
                }
            }
        } catch (e: Exception) {
            onError(e.message ?: "Failed to load image")
        }
    }
    
    actual fun getSelectedImagePath(): String? {
        return selectedFile?.absolutePath
    }
    
    actual fun clearSelection() {
        selectedFile = null
        selectedBytes = null
    }
    
    private fun showFileChooser(): File? {
        val chooser = JFileChooser().apply {
            dialogTitle = "Select Character Art Image"
            fileSelectionMode = JFileChooser.FILES_ONLY
            isAcceptAllFileFilterUsed = false
            addChoosableFileFilter(
                FileNameExtensionFilter(
                    "Image files (*.jpg, *.jpeg, *.png, *.webp)",
                    "jpg", "jpeg", "png", "webp"
                )
            )
        }
        
        val result = chooser.showOpenDialog(null)
        return if (result == JFileChooser.APPROVE_OPTION) {
            chooser.selectedFile
        } else {
            null
        }
    }
    
    /**
     * Launch file picker dialog
     */
    fun launchPicker(
        onImagePicked: (ByteArray, String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val file = showFileChooser()
            
            if (file != null) {
                // Validate it's an image
                val image = ImageIO.read(file)
                if (image == null) {
                    onError("Invalid image file")
                    return
                }
                
                val bytes = file.readBytes()
                selectedFile = file
                selectedBytes = bytes
                
                onImagePicked(bytes, file.name)
            }
        } catch (e: Exception) {
            onError(e.message ?: "Failed to load image")
        }
    }
}

@Composable
actual fun rememberImagePicker(): ImagePicker {
    return remember { ImagePicker() }
}

/**
 * Composable that provides image picking functionality for desktop
 */
@Composable
fun ImagePickerHost(
    onImagePicked: (ByteArray, String) -> Unit,
    onError: (String) -> Unit,
    content: @Composable (launchPicker: () -> Unit, selectedPath: String?) -> Unit
) {
    val picker = remember { ImagePicker() }
    var selectedPath by remember { mutableStateOf<String?>(null) }
    
    val launchPicker: () -> Unit = {
        picker.launchPicker(
            { bytes, name ->
                selectedPath = picker.getSelectedImagePath()
                onImagePicked(bytes, name)
            },
            onError
        )
    }
    
    content(launchPicker, selectedPath)
}

package ireader.presentation.ui.book.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import ireader.core.log.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * Desktop implementation of image picker using JFileChooser.
 * 
 * Opens a file chooser dialog when [show] is true.
 * Returns the file:// URI of the selected image.
 */
@Composable
actual fun ImagePickerDialog(
    show: Boolean,
    onImageSelected: (uri: String) -> Unit,
    onDismiss: () -> Unit
) {
    LaunchedEffect(show) {
        if (show) {
            withContext(Dispatchers.IO) {
                try {
                    val fileChooser = JFileChooser().apply {
                        dialogTitle = "Select Cover Image"
                        fileSelectionMode = JFileChooser.FILES_ONLY
                        isAcceptAllFileFilterUsed = false
                        fileFilter = FileNameExtensionFilter(
                            "Image Files (*.jpg, *.jpeg, *.png, *.gif, *.webp)",
                            "jpg", "jpeg", "png", "gif", "webp"
                        )
                        // Start in user's pictures directory if available
                        val picturesDir = File(System.getProperty("user.home"), "Pictures")
                        if (picturesDir.exists()) {
                            currentDirectory = picturesDir
                        }
                    }
                    
                    val result = fileChooser.showOpenDialog(null)
                    if (result == JFileChooser.APPROVE_OPTION) {
                        val selectedFile = fileChooser.selectedFile
                        if (selectedFile.exists() && selectedFile.isFile) {
                            val uri = "file://${selectedFile.absolutePath}"
                            Log.info { "Image selected: $uri" }
                            withContext(Dispatchers.Main) {
                                onImageSelected(uri)
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                onDismiss()
                            }
                        }
                    } else {
                        Log.info { "Image picker dismissed without selection" }
                        withContext(Dispatchers.Main) {
                            onDismiss()
                        }
                    }
                } catch (e: Exception) {
                    Log.error("Failed to open image picker", e)
                    withContext(Dispatchers.Main) {
                        onDismiss()
                    }
                }
            }
        }
    }
}

package ireader.presentation.ui.util

import java.awt.FileDialog
import java.awt.Frame
import java.io.File

actual object FilePicker {
    actual fun pickFileForSave(
        title: String,
        defaultFileName: String,
        onFileSelected: (String, ByteArray) -> Unit,
        onCancelled: () -> Unit
    ) {
        val fileDialog = FileDialog(null as Frame?, title, FileDialog.SAVE)
        fileDialog.file = defaultFileName
        fileDialog.isVisible = true
        
        val directory = fileDialog.directory
        val filename = fileDialog.file
        
        if (directory != null && filename != null) {
            val file = File(directory, filename)
            // Return empty byte array for save - the caller will write the content
            onFileSelected(file.absolutePath, ByteArray(0))
        } else {
            onCancelled()
        }
    }
    
    actual fun pickFileForLoad(
        title: String,
        onFileSelected: (String, ByteArray) -> Unit,
        onCancelled: () -> Unit
    ) {
        val fileDialog = FileDialog(null as Frame?, title, FileDialog.LOAD)
        fileDialog.file = "*.json"
        fileDialog.isVisible = true
        
        val directory = fileDialog.directory
        val filename = fileDialog.file
        
        if (directory != null && filename != null) {
            val file = File(directory, filename)
            if (file.exists()) {
                val content = file.readBytes()
                onFileSelected(file.absolutePath, content)
            } else {
                onCancelled()
            }
        } else {
            onCancelled()
        }
    }
}

package ireader.core.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * Desktop implementation of file picker using JFileChooser
 */
actual object PlatformFilePicker {
    actual suspend fun pickFiles(fileTypes: List<String>, multiSelect: Boolean): List<String>? {
        return withContext(Dispatchers.IO) {
            try {
                val fileChooser = JFileChooser().apply {
                    isMultiSelectionEnabled = multiSelect
                    
                    // Add file filter for font files
                    if (fileTypes.isNotEmpty()) {
                        val description = "Font Files (${fileTypes.joinToString(", ") { ".$it" }})"
                        val filter = FileNameExtensionFilter(description, *fileTypes.toTypedArray())
                        fileFilter = filter
                    }
                    
                    dialogTitle = "Select Font File${if (multiSelect) "s" else ""}"
                }
                
                val result = fileChooser.showOpenDialog(null)
                
                if (result == JFileChooser.APPROVE_OPTION) {
                    if (multiSelect) {
                        fileChooser.selectedFiles?.map { it.absolutePath }
                    } else {
                        fileChooser.selectedFile?.let { listOf(it.absolutePath) }
                    }
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}

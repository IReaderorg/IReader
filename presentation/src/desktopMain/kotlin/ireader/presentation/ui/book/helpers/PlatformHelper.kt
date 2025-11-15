package ireader.presentation.ui.book.helpers

import ireader.core.log.Log
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * Desktop implementation of platform-specific helper functions.
 */
actual class PlatformHelper {
    actual fun shareText(text: String, title: String) {
        try {
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            val stringSelection = StringSelection(text)
            clipboard.setContents(stringSelection, null)
            
            // Optionally show a notification that text was copied
            Log.info { "Text copied to clipboard: $title" }
        } catch (e: Exception) {
            Log.error("Failed to copy text to clipboard", e)
        }
    }
    
    actual fun createEpubExportUri(bookTitle: String, author: String): String? {
        return try {
            val sanitizedTitle = bookTitle.replace(Regex("[^a-zA-Z0-9\\s-]"), "")
            val sanitizedAuthor = author.replace(Regex("[^a-zA-Z0-9\\s-]"), "")
            val fileName = "$sanitizedTitle - $sanitizedAuthor.epub"
            
            val fileChooser = JFileChooser().apply {
                dialogTitle = "Save EPUB File"
                selectedFile = File(System.getProperty("user.home"), fileName)
                fileFilter = FileNameExtensionFilter("EPUB Files (*.epub)", "epub")
            }
            
            val result = fileChooser.showSaveDialog(null)
            if (result == JFileChooser.APPROVE_OPTION) {
                var file = fileChooser.selectedFile
                if (!file.name.endsWith(".epub")) {
                    file = File(file.parentFile, "${file.name}.epub")
                }
                file.toURI().toString()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.error("Failed to create EPUB export URI", e)
            null
        }
    }

    actual fun copyToClipboard(label: String, content: String) {
        try {
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            val stringSelection = StringSelection(content)
            clipboard.setContents(stringSelection, null)
        } catch (e: Exception) {
            Log.error("Failed to copy to clipboard", e)
        }
    }
}

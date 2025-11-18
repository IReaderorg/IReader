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
            // Sanitize filename for Windows - remove invalid characters
            // Windows invalid characters: < > : " / \ | ? *
            val sanitizedTitle = sanitizeFilename(bookTitle)
            val sanitizedAuthor = sanitizeFilename(author)
            
            // Build filename - handle empty author case
            val fileName = if (sanitizedAuthor.isNotBlank()) {
                "${sanitizedTitle} - ${sanitizedAuthor}.epub"
            } else {
                "${sanitizedTitle}.epub"
            }.take(200) // Limit filename length to avoid path too long errors
            
            val fileChooser = JFileChooser().apply {
                dialogTitle = "Save EPUB File"
                selectedFile = File(System.getProperty("user.home"), fileName)
                fileFilter = FileNameExtensionFilter("EPUB Files (*.epub)", "epub")
            }
            
            val result = fileChooser.showSaveDialog(null)
            if (result == JFileChooser.APPROVE_OPTION) {
                var file = fileChooser.selectedFile
                // Ensure .epub extension
                if (!file.name.endsWith(".epub", ignoreCase = true)) {
                    file = File(file.parentFile, "${file.name}.epub")
                }
                file.absolutePath
            } else {
                null
            }
        } catch (e: Exception) {
            Log.error("Failed to create EPUB export URI", e)
            null
        }
    }
    
    /**
     * Sanitize filename for Windows file system.
     * Removes invalid characters: < > : " / \ | ? *
     * Also removes control characters and trims whitespace.
     */
    private fun sanitizeFilename(name: String): String {
        return name
            // Remove Windows invalid characters
            .replace(Regex("[<>:\"/\\\\|?*]"), "")
            // Remove control characters (0x00-0x1F)
            .replace(Regex("[\\x00-\\x1F]"), "")
            // Replace multiple spaces with single space
            .replace(Regex("\\s+"), " ")
            // Trim whitespace and dots (Windows doesn't allow trailing dots)
            .trim()
            .trimEnd('.')
            // If empty after sanitization, use default
            .ifBlank { "Untitled" }
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

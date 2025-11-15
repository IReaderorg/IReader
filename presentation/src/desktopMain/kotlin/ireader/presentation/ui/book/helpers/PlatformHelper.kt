package ireader.presentation.ui.book.helpers

import ireader.core.log.Log

/**
 * Desktop implementation of platform-specific helper functions.
 * TODO: Implement proper Desktop sharing and file URI creation
 */
actual class PlatformHelper {
    actual fun shareText(text: String, title: String) {
        // TODO: Implement Desktop share functionality
        // Options: Copy to clipboard, open email client, or show share dialog
        // Example: Toolkit.getDefaultToolkit().systemClipboard.setContents(...)
        Log.warn { "PlatformHelper.shareText() not yet implemented on Desktop" }
    }
    
    actual fun createEpubExportUri(bookTitle: String, author: String): String? {
        // TODO: Implement Desktop EPUB export URI creation
        // Example: Use JFileChooser to let user select save location
        // Return the file:// URI as a string
        Log.warn { "PlatformHelper.createEpubExportUri() not yet implemented on Desktop" }
        return null
    }

    actual fun copyToClipboard(label: String, content: String) {
    }
}

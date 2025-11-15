package ireader.presentation.ui.book.helpers

import ireader.core.log.Log
import ireader.domain.utils.copyToClipboard

/**
 * Android implementation of platform-specific helper functions.
 * TODO: Implement proper Android sharing and file URI creation
 */
actual class PlatformHelper(private val context: android.content.Context) {
    actual fun shareText(text: String, title: String) {
        // TODO: Implement Android share functionality using Intent
        // Example: val intent = Intent(Intent.ACTION_SEND).apply {
        //     type = "text/plain"
        //     putExtra(Intent.EXTRA_TEXT, text)
        //     putExtra(Intent.EXTRA_TITLE, title)
        // }
        // context.startActivity(Intent.createChooser(intent, title))
        Log.warn { "PlatformHelper.shareText() not yet implemented on Android" }
    }
    
    actual fun createEpubExportUri(bookTitle: String, author: String): String? {
        // TODO: Implement Android EPUB export URI creation using Storage Access Framework
        // Example: Use DocumentsContract.createDocument() to create a file
        // Return the content:// URI as a string
        Log.warn { "PlatformHelper.createEpubExportUri() not yet implemented on Android" }
        return null
    }

    actual fun copyToClipboard(label: String, content: String) {
        context.copyToClipboard(label, content)
    }
}

package ireader.presentation.ui.book.helpers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import ireader.core.log.Log
import java.io.File

/**
 * Android implementation of platform-specific helper functions.
 */
actual class PlatformHelper(private val context: Context) {
    actual fun shareText(text: String, title: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                putExtra(Intent.EXTRA_TITLE, title)
            }
            val chooserIntent = Intent.createChooser(intent, title)
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)
        } catch (e: Exception) {
            Log.error("Failed to share text", e)
        }
    }
    
    actual fun createEpubExportUri(bookTitle: String, author: String): String? {
        return try {
            val sanitizedTitle = bookTitle.replace(Regex("[^a-zA-Z0-9\\s-]"), "")
            val sanitizedAuthor = author.replace(Regex("[^a-zA-Z0-9\\s-]"), "")
            val fileName = "$sanitizedTitle - $sanitizedAuthor.epub"
            
            // Use MediaStore for Android 10+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/epub+zip")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                
                val uri = context.contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    contentValues
                )
                uri?.toString()
            } else {
                // For older Android versions, use Downloads directory
                val downloadsDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                )
                val file = File(downloadsDir, fileName)
                file.toURI().toString()
            }
        } catch (e: Exception) {
            Log.error("Failed to create EPUB export URI", e)
            null
        }
    }

    actual fun copyToClipboard(label: String, content: String) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = ClipData.newPlainText(label, content)
            clipboard?.setPrimaryClip(clip)
        } catch (e: Exception) {
            Log.error("Failed to copy to clipboard", e)
        }
    }
}

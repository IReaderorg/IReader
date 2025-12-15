package ireader.presentation.ui.book.helpers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import ireader.core.log.Log
import ireader.domain.image.cache.DiskUtil
import java.io.File

/**
 * Android implementation of platform-specific helper functions.
 */
actual class PlatformHelper(private val context: Context) {
    
    companion object {
        private const val CUSTOM_COVERS_DIR = "IReader/cache/covers/custom"
    }
    
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
            
            // Use MediaStore for Android 10+ (API 29+)
            // Create in Downloads/IReader directory
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/epub+zip")
                    // Save to Downloads/IReader directory
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/IReader")
                }
                
                val uri = context.contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    contentValues
                )
                
                if (uri != null) {
                    Log.info { "EPUB will be saved to: Downloads/IReader/$fileName" }
                }
                
                uri?.toString()
            } else {
                // For older Android versions (API 28 and below), use Downloads/IReader directory
                val downloadsDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                )
                val ireaderDir = File(downloadsDir, "IReader")
                
                // Create IReader directory if it doesn't exist
                if (!ireaderDir.exists()) {
                    ireaderDir.mkdirs()
                }
                
                val file = File(ireaderDir, fileName)
                Log.info { "EPUB will be saved to: ${file.absolutePath}" }
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
    
    /**
     * Copy an image from a URI to the app's custom cover directory.
     * The image is stored in the cache directory with a hashed filename based on bookId.
     * 
     * @param sourceUri The URI of the source image (content:// or file://)
     * @param bookId The ID of the book to save the cover for
     * @return The file path of the saved cover (file:// URI), or null if failed
     */
    actual suspend fun copyImageToCustomCover(sourceUri: String, bookId: Long): String? {
        return try {
            val uri = Uri.parse(sourceUri)
            val customCoverDir = File(context.cacheDir, CUSTOM_COVERS_DIR)
            if (!customCoverDir.exists()) {
                customCoverDir.mkdirs()
            }
            
            // Use hashed filename for consistency with CoverCache
            val fileName = DiskUtil.hashKeyForDisk(bookId.toString())
            val destFile = File(customCoverDir, fileName)
            
            // Copy the image from URI to destination file
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                destFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: run {
                Log.error { "Failed to open input stream for URI: $sourceUri" }
                return null
            }
            
            Log.info { "Custom cover saved to: ${destFile.absolutePath}" }
            
            // Return file:// URI that can be loaded by image loader
            "file://${destFile.absolutePath}"
        } catch (e: Exception) {
            Log.error("Failed to copy image to custom cover", e)
            null
        }
    }
}

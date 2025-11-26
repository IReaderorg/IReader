package ireader.domain.usecases.epub

import android.content.Context
import ireader.domain.models.common.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

/**
 * Android implementation for EPUB export helpers
 */
actual fun createPlatformTempEpubPath(): String {
    val context = KoinHelper.context
    val tempFile = File.createTempFile("epub_export_", ".epub", context.cacheDir)
    return tempFile.absolutePath
}

actual suspend fun copyPlatformTempFileToContentUri(tempPath: String, contentUri: Uri) {
    withContext(Dispatchers.IO) {
        val context = KoinHelper.context
        val tempFile = File(tempPath)
        
        if (!tempFile.exists()) {
            throw Exception("Temp EPUB file not found: $tempPath")
        }
        
        context.contentResolver.openOutputStream(contentUri.androidUri)?.use { outputStream ->
            tempFile.inputStream().use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: throw Exception("Failed to open output stream for URI: ${contentUri.androidUri}")
        
        // Clean up temp file
        tempFile.delete()
    }
}

/**
 * Helper to get Android Context from Koin
 */
private object KoinHelper : KoinComponent {
    val context: Context by inject()
}

package ireader.domain.usecases.epub

import ireader.domain.models.common.Uri
import java.io.File
import java.util.UUID

/**
 * Desktop implementation for EPUB export helpers
 */
actual fun createPlatformTempEpubPath(): String {
    val tempDir = System.getProperty("java.io.tmpdir")
    val tempFile = File(tempDir, "epub_export_${UUID.randomUUID()}.epub")
    return tempFile.absolutePath
}

actual suspend fun copyPlatformTempFileToContentUri(tempPath: String, contentUri: Uri) {
    // Desktop doesn't use content URIs, this should never be called
    // If it is called, it means the file was already written to the correct location
}

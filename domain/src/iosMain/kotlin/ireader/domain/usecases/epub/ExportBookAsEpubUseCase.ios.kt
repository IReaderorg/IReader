package ireader.domain.usecases.epub

import ireader.domain.models.common.Uri
import platform.Foundation.*
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * Create a temporary path for EPUB file
 */
actual fun createPlatformTempEpubPath(): String {
    val tempDir = NSTemporaryDirectory()
    val uuid = NSUUID().UUIDString
    return "$tempDir$uuid.epub"
}

/**
 * Copy temporary EPUB file to the destination URI
 * 
 * On iOS, this handles:
 * - File URLs (file://)
 * - Document picker URLs (from UIDocumentPickerViewController)
 * - iCloud URLs
 */
@OptIn(ExperimentalForeignApi::class)
actual suspend fun copyPlatformTempFileToContentUri(tempPath: String, contentUri: Uri) {
    val fileManager = NSFileManager.defaultManager
    val uriString = contentUri.toString()
    
    // Determine destination path
    val destinationPath = when {
        uriString.startsWith("file://") -> {
            uriString.removePrefix("file://")
        }
        uriString.startsWith("/") -> {
            uriString
        }
        else -> {
            // For other URI schemes, try to resolve to a file path
            // This might be a document picker URL
            resolveDocumentPickerUrl(uriString) ?: throw IllegalArgumentException(
                "Cannot resolve URI to file path: $uriString"
            )
        }
    }
    
    // Ensure parent directory exists
    val parentDir = (destinationPath as NSString).stringByDeletingLastPathComponent
    if (!fileManager.fileExistsAtPath(parentDir)) {
        fileManager.createDirectoryAtPath(
            parentDir,
            withIntermediateDirectories = true,
            attributes = null,
            error = null
        )
    }
    
    // Remove existing file if present
    if (fileManager.fileExistsAtPath(destinationPath)) {
        fileManager.removeItemAtPath(destinationPath, error = null)
    }
    
    // Copy file
    val success = fileManager.copyItemAtPath(tempPath, toPath = destinationPath, error = null)
    
    if (!success) {
        throw Exception("Failed to copy EPUB file to destination: $destinationPath")
    }
    
    // Clean up temp file
    fileManager.removeItemAtPath(tempPath, error = null)
}

/**
 * Resolve a document picker URL to a file path
 * 
 * Document picker URLs need security-scoped access
 */
@OptIn(ExperimentalForeignApi::class)
private fun resolveDocumentPickerUrl(urlString: String): String? {
    val url = NSURL.URLWithString(urlString) ?: return null
    
    // Start accessing security-scoped resource
    val accessing = url.startAccessingSecurityScopedResource()
    
    return try {
        url.path
    } finally {
        if (accessing) {
            url.stopAccessingSecurityScopedResource()
        }
    }
}

/**
 * Get the Documents directory for saving EPUBs
 */
@OptIn(ExperimentalForeignApi::class)
fun getDocumentsDirectory(): String {
    val paths = NSSearchPathForDirectoriesInDomains(
        NSDocumentDirectory,
        NSUserDomainMask,
        true
    )
    return (paths.firstOrNull() as? String) ?: NSTemporaryDirectory()
}

/**
 * Get the default EPUB export directory
 */
@OptIn(ExperimentalForeignApi::class)
fun getEpubExportDirectory(): String {
    val documentsDir = getDocumentsDirectory()
    val epubDir = "$documentsDir/Exports"
    
    val fileManager = NSFileManager.defaultManager
    if (!fileManager.fileExistsAtPath(epubDir)) {
        fileManager.createDirectoryAtPath(
            epubDir,
            withIntermediateDirectories = true,
            attributes = null,
            error = null
        )
    }
    
    return epubDir
}

/**
 * Generate a safe filename for EPUB export
 */
fun generateEpubFilename(bookTitle: String): String {
    val sanitized = bookTitle
        .replace(Regex("[\\\\/:*?\"<>|]"), "_")
        .replace(Regex("\\s+"), "_")
        .take(100)
    
    return "$sanitized.epub"
}

/**
 * Check if a file exists at the given path
 */
@OptIn(ExperimentalForeignApi::class)
fun fileExists(path: String): Boolean {
    return NSFileManager.defaultManager.fileExistsAtPath(path)
}

/**
 * Delete a file at the given path
 */
@OptIn(ExperimentalForeignApi::class)
fun deleteFile(path: String): Boolean {
    return NSFileManager.defaultManager.removeItemAtPath(path, error = null)
}

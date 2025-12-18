package ireader.domain.storage

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import ireader.domain.preferences.prefs.UiPreferences
import java.io.InputStream
import java.io.OutputStream

/**
 * SAF (Storage Access Framework) based storage manager.
 * Uses DocumentFile API for all operations on user-selected folders.
 * 
 * This properly respects SAF permissions without needing MANAGE_EXTERNAL_STORAGE.
 */
class SafStorageManager(
    private val context: Context,
    private val uiPreferences: UiPreferences
) {
    companion object {
        private const val TAG = "SafStorageManager"
        
        // Directory names
        const val DIR_CACHE = "cache"
        const val DIR_EXTENSIONS = "Extensions"
        const val DIR_JS_PLUGINS = "js-plugins"
        const val DIR_BOOKS = "Books"
        const val DIR_BACKUPS = "Backups"
    }
    
    /**
     * Get the root DocumentFile for the user-selected SAF folder.
     * Returns null if no folder is selected or permission is lost.
     * 
     * If permission is lost or URI is invalid, clears the stored URI so user can select again.
     */
    fun getRootDocumentFile(): DocumentFile? {
        val uriString = uiPreferences.selectedStorageFolderUri().get()
        if (uriString.isNullOrEmpty()) {
            android.util.Log.d(TAG, "No SAF URI selected")
            return null
        }
        
        return try {
            val uri = Uri.parse(uriString)
            
            // Extract the tree URI from the stored URI (it might contain /document/ suffix)
            val treeUri = extractTreeUri(uri)
            android.util.Log.d(TAG, "Checking permission for tree URI: $treeUri (original: $uri)")
            
            // Check if we still have permission for this URI
            // Compare using string representation to handle URI encoding differences
            val persistedPermissions = context.contentResolver.persistedUriPermissions
            val hasPermission = persistedPermissions.any { perm ->
                val permTreeUri = extractTreeUri(perm.uri)
                val matches = permTreeUri.toString() == treeUri.toString() && 
                              perm.isReadPermission && perm.isWritePermission
                if (matches) {
                    android.util.Log.d(TAG, "Found matching permission: ${perm.uri}")
                }
                matches
            }
            
            if (!hasPermission) {
                android.util.Log.w(TAG, "SAF permission lost for URI: $uri")
                android.util.Log.d(TAG, "Available permissions: ${persistedPermissions.map { it.uri }}")
                clearStoredUri()
                return null
            }
            
            val docFile = DocumentFile.fromTreeUri(context, treeUri)
            if (docFile?.exists() == true && docFile.canWrite()) {
                android.util.Log.d(TAG, "Root DocumentFile: ${docFile.uri}, canWrite=${docFile.canWrite()}")
                docFile
            } else {
                android.util.Log.w(TAG, "SAF folder not accessible: exists=${docFile?.exists()}, canWrite=${docFile?.canWrite()} - clearing stored URI")
                clearStoredUri()
                null
            }
        } catch (e: SecurityException) {
            android.util.Log.e(TAG, "SecurityException accessing SAF folder - permission revoked, clearing stored URI", e)
            clearStoredUri()
            null
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to get root DocumentFile - clearing stored URI", e)
            clearStoredUri()
            null
        }
    }
    
    /**
     * Extract the tree URI from a document URI.
     * OpenDocumentTree can return URIs like:
     * - content://com.android.externalstorage.documents/tree/primary%3AIReader/document/primary%3AIReader
     * We need to extract just the tree part:
     * - content://com.android.externalstorage.documents/tree/primary%3AIReader
     */
    private fun extractTreeUri(uri: Uri): Uri {
        val uriString = uri.toString()
        val treeIndex = uriString.indexOf("/tree/")
        if (treeIndex == -1) return uri
        
        val documentIndex = uriString.indexOf("/document/", treeIndex)
        return if (documentIndex != -1) {
            Uri.parse(uriString.substring(0, documentIndex))
        } else {
            uri
        }
    }
    
    /**
     * Clear the stored SAF URI when permission is lost or invalid.
     * This allows the user to select a new folder.
     */
    fun clearStoredUri() {
        try {
            val uriString = uiPreferences.selectedStorageFolderUri().get()
            if (!uriString.isNullOrEmpty()) {
                // Try to release the persisted permission
                try {
                    val uri = Uri.parse(uriString)
                    val treeUri = extractTreeUri(uri)
                    context.contentResolver.releasePersistableUriPermission(
                        treeUri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    // Ignore - permission may already be released
                    android.util.Log.d(TAG, "Could not release permission (may already be released): ${e.message}")
                }
            }
            
            // Clear the stored URI
            uiPreferences.selectedStorageFolderUri().set("")
            android.util.Log.i(TAG, "Cleared stored SAF URI - user needs to select folder again")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to clear stored URI", e)
        }
    }
    
    /**
     * Check if SAF storage is available and writable.
     */
    fun isSafStorageAvailable(): Boolean {
        return getRootDocumentFile() != null
    }
    
    /**
     * Check if SAF permission needs to be requested (URI was cleared due to lost permission).
     */
    fun needsPermissionRequest(): Boolean {
        val uriString = uiPreferences.selectedStorageFolderUri().get()
        return uriString.isNullOrEmpty()
    }

    
    /**
     * Get or create a subdirectory in the SAF root.
     */
    fun getOrCreateDirectory(dirName: String): DocumentFile? {
        val root = getRootDocumentFile() ?: return null
        return getOrCreateSubDirectory(root, dirName)
    }
    
    /**
     * Get or create a subdirectory within a parent DocumentFile.
     */
    private fun getOrCreateSubDirectory(parent: DocumentFile, dirName: String): DocumentFile? {
        // Check if directory already exists
        val existing = parent.findFile(dirName)
        if (existing != null && existing.isDirectory) {
            return existing
        }
        
        // Create new directory
        return try {
            parent.createDirectory(dirName)?.also {
                android.util.Log.d(TAG, "Created directory: $dirName")
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to create directory: $dirName", e)
            null
        }
    }
    
    /**
     * Get the JS plugins directory.
     */
    fun getJsPluginsDirectory(): DocumentFile? {
        return getOrCreateDirectory(DIR_JS_PLUGINS)
    }
    
    /**
     * Get the cache directory.
     */
    fun getCacheDirectory(): DocumentFile? {
        return getOrCreateDirectory(DIR_CACHE)
    }
    
    /**
     * Get the extensions directory.
     */
    fun getExtensionsDirectory(): DocumentFile? {
        return getOrCreateDirectory(DIR_EXTENSIONS)
    }
    
    /**
     * Get the books directory.
     */
    fun getBooksDirectory(): DocumentFile? {
        return getOrCreateDirectory(DIR_BOOKS)
    }
    
    /**
     * Get the backups directory.
     */
    fun getBackupsDirectory(): DocumentFile? {
        return getOrCreateDirectory(DIR_BACKUPS)
    }
    
    /**
     * Find a file in a directory.
     */
    fun findFile(directory: DocumentFile, fileName: String): DocumentFile? {
        return directory.findFile(fileName)
    }
    
    /**
     * Create or overwrite a file in a directory.
     * @param directory Parent directory
     * @param fileName Name of the file
     * @param mimeType MIME type (e.g., "application/javascript", "application/json")
     * @return DocumentFile for the created file, or null on failure
     */
    fun createFile(directory: DocumentFile, fileName: String, mimeType: String): DocumentFile? {
        // Delete existing file if present
        directory.findFile(fileName)?.delete()
        
        return try {
            directory.createFile(mimeType, fileName)?.also {
                android.util.Log.d(TAG, "Created file: $fileName in ${directory.uri}")
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to create file: $fileName", e)
            null
        }
    }
    
    /**
     * Write content to a file.
     */
    fun writeToFile(file: DocumentFile, content: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(file.uri)?.use { outputStream ->
                outputStream.write(content.toByteArray(Charsets.UTF_8))
                outputStream.flush()
            }
            android.util.Log.d(TAG, "Wrote ${content.length} chars to ${file.name}")
            true
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to write to file: ${file.name}", e)
            false
        }
    }
    
    /**
     * Write bytes to a file.
     */
    fun writeToFile(file: DocumentFile, bytes: ByteArray): Boolean {
        return try {
            context.contentResolver.openOutputStream(file.uri)?.use { outputStream ->
                outputStream.write(bytes)
                outputStream.flush()
            }
            android.util.Log.d(TAG, "Wrote ${bytes.size} bytes to ${file.name}")
            true
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to write bytes to file: ${file.name}", e)
            false
        }
    }
    
    /**
     * Read content from a file as String.
     */
    fun readFromFile(file: DocumentFile): String? {
        return try {
            context.contentResolver.openInputStream(file.uri)?.use { inputStream ->
                inputStream.bufferedReader().readText()
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to read from file: ${file.name}", e)
            null
        }
    }
    
    /**
     * Read content from a file as ByteArray.
     */
    fun readBytesFromFile(file: DocumentFile): ByteArray? {
        return try {
            context.contentResolver.openInputStream(file.uri)?.use { inputStream ->
                inputStream.readBytes()
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to read bytes from file: ${file.name}", e)
            null
        }
    }
    
    /**
     * Get an InputStream for a file.
     */
    fun openInputStream(file: DocumentFile): InputStream? {
        return try {
            context.contentResolver.openInputStream(file.uri)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to open input stream: ${file.name}", e)
            null
        }
    }
    
    /**
     * Get an OutputStream for a file.
     */
    fun openOutputStream(file: DocumentFile): OutputStream? {
        return try {
            context.contentResolver.openOutputStream(file.uri)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to open output stream: ${file.name}", e)
            null
        }
    }
    
    /**
     * Delete a file.
     */
    fun deleteFile(file: DocumentFile): Boolean {
        return try {
            file.delete().also {
                android.util.Log.d(TAG, "Deleted file: ${file.name}, success=$it")
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to delete file: ${file.name}", e)
            false
        }
    }
    
    /**
     * List all files in a directory.
     */
    fun listFiles(directory: DocumentFile): List<DocumentFile> {
        return directory.listFiles().toList()
    }
    
    /**
     * List files with a specific extension.
     */
    fun listFilesWithExtension(directory: DocumentFile, extension: String): List<DocumentFile> {
        return directory.listFiles().filter { 
            it.isFile && it.name?.endsWith(extension, ignoreCase = true) == true 
        }
    }
    
    /**
     * Check if a file exists in a directory.
     */
    fun fileExists(directory: DocumentFile, fileName: String): Boolean {
        return directory.findFile(fileName)?.exists() == true
    }
    
    /**
     * Get file size.
     */
    fun getFileSize(file: DocumentFile): Long {
        return file.length()
    }
    
    /**
     * Copy content from InputStream to a DocumentFile.
     */
    fun copyToFile(inputStream: InputStream, file: DocumentFile): Boolean {
        return try {
            context.contentResolver.openOutputStream(file.uri)?.use { outputStream ->
                inputStream.copyTo(outputStream)
                outputStream.flush()
            }
            true
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to copy to file: ${file.name}", e)
            false
        }
    }
}

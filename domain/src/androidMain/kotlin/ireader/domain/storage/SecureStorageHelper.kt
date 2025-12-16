package ireader.domain.storage

import android.content.Context
import android.content.Intent
import android.net.Uri
import ireader.domain.preferences.prefs.UiPreferences
import java.io.File

/**
 * Helper object to get the secure storage directory based on user preferences.
 * 
 * For SAF (content://) URIs, we use the app's external files directory as a mirror
 * since SAF URIs can't be used directly as file paths for most operations.
 * 
 * The user-selected SAF folder is used for:
 * - Backups (via DocumentFile API in presentation layer)
 * - User-visible files
 * 
 * For internal app data (extensions, cache, etc.), we use:
 * - External files directory if available (persists on uninstall if user chose)
 * - App cache directory as fallback
 */
object SecureStorageHelper {
    
    private var uiPreferences: UiPreferences? = null
    private var cachedSecureDir: File? = null
    
    /**
     * Initialize with UiPreferences. Should be called early in app startup.
     */
    fun init(preferences: UiPreferences) {
        uiPreferences = preferences
        cachedSecureDir = null // Reset cache
        android.util.Log.d("SecureStorageHelper", "Initialized with UiPreferences, hasSecureStorage=${hasSecureStorage()}")
    }
    
    /**
     * Clear the cached directory. Call this when the storage preference changes.
     */
    fun clearCache() {
        cachedSecureDir = null
        android.util.Log.d("SecureStorageHelper", "Cache cleared")
    }
    
    /**
     * Check if user has selected a secure storage folder.
     */
    fun hasSecureStorage(): Boolean {
        val selectedUri = uiPreferences?.selectedStorageFolderUri()?.get()
        val result = !selectedUri.isNullOrEmpty()
        android.util.Log.d("SecureStorageHelper", "hasSecureStorage: $result, uri=$selectedUri, uiPreferences=${uiPreferences != null}")
        return result
    }
    
    /**
     * Get the SAF URI if user selected one, null otherwise.
     */
    fun getSafUri(): Uri? {
        val selectedUri = uiPreferences?.selectedStorageFolderUri()?.get()
        if (!selectedUri.isNullOrEmpty()) {
            return try {
                Uri.parse(selectedUri)
            } catch (e: Exception) {
                null
            }
        }
        return null
    }
    
    /**
     * Try to extract actual file path from SAF URI.
     * Works for primary storage URIs like:
     * content://com.android.externalstorage.documents/tree/primary%3AIReader
     */
    private fun getFilePathFromSafUri(uri: Uri): String? {
        try {
            val docId = uri.lastPathSegment ?: return null
            android.util.Log.d("SecureStorageHelper", "Parsing SAF URI docId: $docId")
            
            // Handle tree URIs: primary:IReader or primary%3AIReader
            val decodedDocId = java.net.URLDecoder.decode(docId, "UTF-8")
            
            // Extract the path part after "primary:" or from document path
            val pathPart = when {
                decodedDocId.startsWith("primary:") -> decodedDocId.substringAfter("primary:")
                decodedDocId.contains("/document/primary:") -> decodedDocId.substringAfter("/document/primary:")
                decodedDocId.contains(":") -> decodedDocId.substringAfter(":")
                else -> return null
            }
            
            if (pathPart.isNotEmpty()) {
                val fullPath = "/storage/emulated/0/$pathPart"
                android.util.Log.d("SecureStorageHelper", "Extracted path from SAF URI: $fullPath")
                return fullPath
            }
        } catch (e: Exception) {
            android.util.Log.e("SecureStorageHelper", "Failed to parse SAF URI: ${e.message}")
        }
        return null
    }
    
    /**
     * Take persistent permissions for a SAF URI.
     * Should be called when user selects a folder.
     */
    fun takePersistentPermissions(context: Context, uri: Uri) {
        try {
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)
        } catch (e: Exception) {
            // Permission might already be taken or not available
        }
    }
    
    /**
     * Get the base directory for app data.
     * If user selected a SAF folder on primary storage, try to use that path directly.
     * Otherwise falls back to app's external files directory.
     */
    private fun getSecureBaseDir(context: Context): File {
        android.util.Log.d("SecureStorageHelper", "getSecureBaseDir called, cachedSecureDir=${cachedSecureDir?.absolutePath}")
        
        cachedSecureDir?.let { 
            if (it.exists() || it.mkdirs()) {
                android.util.Log.d("SecureStorageHelper", "Using cached secure dir: ${it.absolutePath}")
                return it 
            }
        }
        
        // Check if user selected a folder
        val hasStorage = hasSecureStorage()
        android.util.Log.d("SecureStorageHelper", "hasSecureStorage=$hasStorage")
        
        // If user selected a folder, try to use the actual SAF path first
        if (hasStorage) {
            // Try to extract actual path from SAF URI
            val safUri = getSafUri()
            if (safUri != null) {
                val actualPath = getFilePathFromSafUri(safUri)
                android.util.Log.d("SecureStorageHelper", "SAF URI actual path: $actualPath")
                if (actualPath != null) {
                    val safDir = File(actualPath)
                    if (safDir.exists() && safDir.canWrite()) {
                        android.util.Log.d("SecureStorageHelper", "Using SAF directory directly: ${safDir.absolutePath}")
                        cachedSecureDir = safDir
                        return safDir
                    } else if (!safDir.exists() && safDir.mkdirs()) {
                        android.util.Log.d("SecureStorageHelper", "Created SAF directory: ${safDir.absolutePath}")
                        cachedSecureDir = safDir
                        return safDir
                    }
                    android.util.Log.d("SecureStorageHelper", "SAF dir not accessible: exists=${safDir.exists()}, canWrite=${safDir.canWrite()}")
                }
            }
            
            // Fallback to external files dir if SAF path not accessible
            val externalDir = context.getExternalFilesDir(null)
            android.util.Log.d("SecureStorageHelper", "Fallback to external files dir: $externalDir")
            if (externalDir != null) {
                val secureDir = File(externalDir, "IReader")
                val created = secureDir.exists() || secureDir.mkdirs()
                android.util.Log.d("SecureStorageHelper", "Secure dir: ${secureDir.absolutePath}, created=$created, exists=${secureDir.exists()}")
                if (created || secureDir.exists()) {
                    cachedSecureDir = secureDir
                    return secureDir
                }
            }
        }
        
        // Fallback to cache directory
        val cacheDir = File(context.cacheDir, "IReader")
        cacheDir.mkdirs()
        cachedSecureDir = cacheDir
        android.util.Log.d("SecureStorageHelper", "Fallback to cache dir: ${cacheDir.absolutePath}")
        return cacheDir
    }
    
    /**
     * Get the secure cache directory.
     * 
     * @param context Android context
     * @param subDir Optional subdirectory name
     * @return File pointing to the cache directory
     */
    fun getCacheDir(context: Context, subDir: String? = null): File {
        val baseDir = getBaseCacheDir(context)
        val dir = if (subDir != null) File(baseDir, subDir) else baseDir
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
    
    /**
     * Get the base cache directory.
     */
    fun getBaseCacheDir(context: Context): File {
        val baseDir = getSecureBaseDir(context)
        val cacheDir = File(baseDir, "cache")
        cacheDir.mkdirs()
        return cacheDir
    }
    
    /**
     * Get the extensions directory.
     */
    fun getExtensionsDir(context: Context): File {
        val baseDir = getSecureBaseDir(context)
        val extDir = File(baseDir, "Extensions")
        extDir.mkdirs()
        return extDir
    }
    
    /**
     * Get the JS plugins directory.
     */
    fun getJsPluginsDir(context: Context): File {
        val baseDir = getSecureBaseDir(context)
        val jsDir = File(baseDir, "js-plugins")
        val created = jsDir.mkdirs()
        android.util.Log.d("SecureStorageHelper", "getJsPluginsDir: ${jsDir.absolutePath}, exists=${jsDir.exists()}, created=$created, canWrite=${jsDir.canWrite()}")
        return jsDir
    }
    
    /**
     * Get the books/downloads directory.
     */
    fun getBooksDir(context: Context): File {
        val baseDir = getSecureBaseDir(context)
        val booksDir = File(baseDir, "Books")
        booksDir.mkdirs()
        return booksDir
    }
    
    /**
     * Get the backups directory.
     */
    fun getBackupsDir(context: Context): File {
        val baseDir = getSecureBaseDir(context)
        val backupsDir = File(baseDir, "Backups")
        backupsDir.mkdirs()
        return backupsDir
    }
    
    /**
     * Create a temp file in the secure cache directory.
     */
    fun createTempFile(context: Context, prefix: String, suffix: String): File {
        val cacheDir = getBaseCacheDir(context)
        return File.createTempFile(prefix, suffix, cacheDir)
    }
}

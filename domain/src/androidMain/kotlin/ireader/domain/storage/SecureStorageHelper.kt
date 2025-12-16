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
    }
    
    /**
     * Check if user has selected a secure storage folder.
     */
    fun hasSecureStorage(): Boolean {
        val selectedUri = uiPreferences?.selectedStorageFolderUri()?.get()
        return !selectedUri.isNullOrEmpty()
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
     * Uses external files directory which persists even if app is uninstalled
     * (unless user clears app data).
     */
    private fun getSecureBaseDir(context: Context): File {
        cachedSecureDir?.let { 
            if (it.exists() || it.mkdirs()) return it 
        }
        
        // If user selected a folder, use external files dir (more persistent)
        if (hasSecureStorage()) {
            val externalDir = context.getExternalFilesDir(null)
            if (externalDir != null) {
                val secureDir = File(externalDir, "IReader")
                if (secureDir.exists() || secureDir.mkdirs()) {
                    cachedSecureDir = secureDir
                    return secureDir
                }
            }
        }
        
        // Fallback to cache directory
        val cacheDir = File(context.cacheDir, "IReader")
        cacheDir.mkdirs()
        cachedSecureDir = cacheDir
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
        jsDir.mkdirs()
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

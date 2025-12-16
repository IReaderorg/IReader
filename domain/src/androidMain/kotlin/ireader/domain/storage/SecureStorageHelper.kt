package ireader.domain.storage

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import ireader.domain.preferences.prefs.UiPreferences
import java.io.File
import java.io.InputStream
import java.io.OutputStream

/**
 * Unified storage helper that supports both SAF (DocumentFile) and traditional File operations.
 * 
 * When user selects a SAF folder:
 * - Uses DocumentFile API for all operations (proper SAF support)
 * - No MANAGE_EXTERNAL_STORAGE permission needed
 * 
 * When no SAF folder is selected:
 * - Falls back to app's private external directory or cache
 */
object SecureStorageHelper {
    private const val TAG = "SecureStorageHelper"
    
    private var uiPreferences: UiPreferences? = null
    private var safStorageManager: SafStorageManager? = null
    private var cachedFallbackDir: File? = null
    
    // Directory names
    private const val DIR_CACHE = "cache"
    private const val DIR_EXTENSIONS = "Extensions"
    private const val DIR_JS_PLUGINS = "js-plugins"
    private const val DIR_BOOKS = "Books"
    private const val DIR_BACKUPS = "Backups"
    
    /**
     * Initialize with UiPreferences. Should be called early in app startup.
     */
    fun init(context: Context, preferences: UiPreferences) {
        uiPreferences = preferences
        safStorageManager = SafStorageManager(context, preferences)
        cachedFallbackDir = null
        android.util.Log.d(TAG, "Initialized with UiPreferences, hasSafStorage=${hasSafStorage()}")
    }
    
    /**
     * Legacy init for backward compatibility.
     */
    fun init(preferences: UiPreferences) {
        uiPreferences = preferences
        cachedFallbackDir = null
        android.util.Log.d(TAG, "Initialized with UiPreferences (legacy), hasSecureStorage=${hasSecureStorage()}")
    }
    
    /**
     * Clear the cached directory. Call this when the storage preference changes.
     */
    fun clearCache() {
        cachedFallbackDir = null
        android.util.Log.d(TAG, "Cache cleared")
    }
    
    /**
     * Check if user has selected a storage folder (legacy method name).
     */
    fun hasSecureStorage(): Boolean {
        val selectedUri = uiPreferences?.selectedStorageFolderUri()?.get()
        val result = !selectedUri.isNullOrEmpty()
        android.util.Log.d(TAG, "hasSecureStorage: $result, uri=$selectedUri")
        return result
    }
    
    /**
     * Check if SAF storage is available and writable.
     */
    fun hasSafStorage(): Boolean {
        return safStorageManager?.isSafStorageAvailable() == true
    }
    
    /**
     * Get the SafStorageManager instance.
     */
    fun getSafStorageManager(): SafStorageManager? = safStorageManager

    
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
            android.util.Log.d(TAG, "Took persistent permissions for: $uri")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to take persistent permissions", e)
        }
    }
    
    // ==================== SAF-based Directory Access ====================
    
    /**
     * Get the JS plugins directory as DocumentFile (SAF).
     * Returns null if SAF is not available.
     */
    fun getJsPluginsDocumentFile(): DocumentFile? {
        return safStorageManager?.getJsPluginsDirectory()
    }
    
    /**
     * Get the cache directory as DocumentFile (SAF).
     */
    fun getCacheDocumentFile(): DocumentFile? {
        return safStorageManager?.getCacheDirectory()
    }
    
    /**
     * Get the extensions directory as DocumentFile (SAF).
     */
    fun getExtensionsDocumentFile(): DocumentFile? {
        return safStorageManager?.getExtensionsDirectory()
    }
    
    /**
     * Get the books directory as DocumentFile (SAF).
     */
    fun getBooksDocumentFile(): DocumentFile? {
        return safStorageManager?.getBooksDirectory()
    }
    
    /**
     * Get the backups directory as DocumentFile (SAF).
     */
    fun getBackupsDocumentFile(): DocumentFile? {
        return safStorageManager?.getBackupsDirectory()
    }
    
    // ==================== Fallback File-based Access ====================
    
    /**
     * Get the fallback base directory (app's private storage).
     * Used when SAF is not available.
     */
    private fun getFallbackBaseDir(context: Context): File {
        cachedFallbackDir?.let { 
            if (it.exists() || it.mkdirs()) return it 
        }
        
        // Try external files dir first (persists on uninstall)
        val externalDir = context.getExternalFilesDir(null)
        if (externalDir != null) {
            val dir = File(externalDir, "IReader")
            if (dir.exists() || dir.mkdirs()) {
                cachedFallbackDir = dir
                return dir
            }
        }
        
        // Fallback to cache
        val cacheDir = File(context.cacheDir, "IReader")
        cacheDir.mkdirs()
        cachedFallbackDir = cacheDir
        return cacheDir
    }
    
    /**
     * Get the JS plugins directory as File (fallback).
     */
    fun getJsPluginsFallbackDir(context: Context): File {
        val baseDir = getFallbackBaseDir(context)
        val dir = File(baseDir, DIR_JS_PLUGINS)
        dir.mkdirs()
        return dir
    }
    
    /**
     * Get the cache directory as File (fallback).
     */
    fun getCacheFallbackDir(context: Context): File {
        val baseDir = getFallbackBaseDir(context)
        val dir = File(baseDir, DIR_CACHE)
        dir.mkdirs()
        return dir
    }
    
    /**
     * Get the extensions directory as File (fallback).
     */
    fun getExtensionsFallbackDir(context: Context): File {
        val baseDir = getFallbackBaseDir(context)
        val dir = File(baseDir, DIR_EXTENSIONS)
        dir.mkdirs()
        return dir
    }

    
    // ==================== Legacy File-based Methods (for backward compatibility) ====================
    // These methods try SAF first, then fall back to File-based access
    
    /**
     * Get the base cache directory.
     * @deprecated Use getCacheDocumentFile() for SAF support
     */
    fun getBaseCacheDir(context: Context): File {
        // For cache, always use fallback since cache operations need File paths
        return getCacheFallbackDir(context)
    }
    
    /**
     * Get the extensions directory.
     * @deprecated Use getExtensionsDocumentFile() for SAF support
     */
    fun getExtensionsDir(context: Context): File {
        return getExtensionsFallbackDir(context)
    }
    
    /**
     * Get the JS plugins directory.
     * @deprecated Use getJsPluginsDocumentFile() for SAF support
     */
    fun getJsPluginsDir(context: Context): File {
        return getJsPluginsFallbackDir(context)
    }
    
    /**
     * Get the books directory.
     */
    fun getBooksDir(context: Context): File {
        val baseDir = getFallbackBaseDir(context)
        val dir = File(baseDir, DIR_BOOKS)
        dir.mkdirs()
        return dir
    }
    
    /**
     * Get the backups directory.
     */
    fun getBackupsDir(context: Context): File {
        val baseDir = getFallbackBaseDir(context)
        val dir = File(baseDir, DIR_BACKUPS)
        dir.mkdirs()
        return dir
    }
    
    // ==================== Unified Storage Operations ====================
    
    /**
     * Write a JS plugin file. Uses SAF if available, otherwise fallback.
     * @return true if successful
     */
    fun writeJsPlugin(context: Context, fileName: String, content: String): Boolean {
        // Try SAF first
        val safDir = getJsPluginsDocumentFile()
        if (safDir != null) {
            val file = safStorageManager?.createFile(safDir, fileName, "application/javascript")
            if (file != null) {
                return safStorageManager?.writeToFile(file, content) == true
            }
        }
        
        // Fallback to File
        return try {
            val dir = getJsPluginsFallbackDir(context)
            val file = File(dir, fileName)
            file.writeText(content)
            android.util.Log.d(TAG, "Wrote JS plugin to fallback: ${file.absolutePath}")
            true
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to write JS plugin", e)
            false
        }
    }
    
    /**
     * Write a JS plugin metadata file.
     */
    fun writeJsPluginMetadata(context: Context, fileName: String, content: String): Boolean {
        val safDir = getJsPluginsDocumentFile()
        if (safDir != null) {
            val file = safStorageManager?.createFile(safDir, fileName, "application/json")
            if (file != null) {
                return safStorageManager?.writeToFile(file, content) == true
            }
        }
        
        return try {
            val dir = getJsPluginsFallbackDir(context)
            val file = File(dir, fileName)
            file.writeText(content)
            true
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to write JS plugin metadata", e)
            false
        }
    }
    
    /**
     * Read a JS plugin file.
     */
    fun readJsPlugin(context: Context, fileName: String): String? {
        // Try SAF first
        val safDir = getJsPluginsDocumentFile()
        if (safDir != null) {
            val file = safDir.findFile(fileName)
            if (file != null && file.exists()) {
                return safStorageManager?.readFromFile(file)
            }
        }
        
        // Fallback to File
        return try {
            val dir = getJsPluginsFallbackDir(context)
            val file = File(dir, fileName)
            if (file.exists()) file.readText() else null
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to read JS plugin", e)
            null
        }
    }
    
    /**
     * Delete a JS plugin file.
     */
    fun deleteJsPlugin(context: Context, fileName: String): Boolean {
        var deleted = false
        
        // Try SAF
        val safDir = getJsPluginsDocumentFile()
        if (safDir != null) {
            val file = safDir.findFile(fileName)
            if (file != null) {
                deleted = safStorageManager?.deleteFile(file) == true
            }
        }
        
        // Also try fallback
        try {
            val dir = getJsPluginsFallbackDir(context)
            val file = File(dir, fileName)
            if (file.exists()) {
                deleted = file.delete() || deleted
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to delete JS plugin from fallback", e)
        }
        
        return deleted
    }
    
    /**
     * Check if a JS plugin exists.
     */
    fun jsPluginExists(context: Context, fileName: String): Boolean {
        // Check SAF
        val safDir = getJsPluginsDocumentFile()
        if (safDir != null) {
            val file = safDir.findFile(fileName)
            if (file?.exists() == true) return true
        }
        
        // Check fallback
        val dir = getJsPluginsFallbackDir(context)
        return File(dir, fileName).exists()
    }
    
    /**
     * List all JS plugin files.
     */
    fun listJsPlugins(context: Context): List<JsPluginFile> {
        val plugins = mutableListOf<JsPluginFile>()
        
        // List from SAF
        val safDir = getJsPluginsDocumentFile()
        if (safDir != null) {
            safStorageManager?.listFilesWithExtension(safDir, ".js")?.forEach { docFile ->
                plugins.add(JsPluginFile.fromDocumentFile(docFile))
            }
        }
        
        // List from fallback (avoid duplicates)
        val existingNames = plugins.map { it.name }.toSet()
        val fallbackDir = getJsPluginsFallbackDir(context)
        fallbackDir.listFiles()?.filter { 
            it.isFile && it.name.endsWith(".js") && it.name !in existingNames 
        }?.forEach { file ->
            plugins.add(JsPluginFile.fromFile(file))
        }
        
        return plugins
    }
    
    /**
     * Get an InputStream for a JS plugin file.
     */
    fun getJsPluginInputStream(context: Context, fileName: String): InputStream? {
        // Try SAF first
        val safDir = getJsPluginsDocumentFile()
        if (safDir != null) {
            val file = safDir.findFile(fileName)
            if (file != null && file.exists()) {
                return safStorageManager?.openInputStream(file)
            }
        }
        
        // Fallback
        return try {
            val dir = getJsPluginsFallbackDir(context)
            val file = File(dir, fileName)
            if (file.exists()) file.inputStream() else null
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Write bytes to a JS plugin file (for downloading).
     */
    fun writeJsPluginBytes(context: Context, fileName: String, bytes: ByteArray): Boolean {
        val safDir = getJsPluginsDocumentFile()
        if (safDir != null) {
            val file = safStorageManager?.createFile(safDir, fileName, "application/javascript")
            if (file != null) {
                return safStorageManager?.writeToFile(file, bytes) == true
            }
        }
        
        return try {
            val dir = getJsPluginsFallbackDir(context)
            val file = File(dir, fileName)
            file.writeBytes(bytes)
            true
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to write JS plugin bytes", e)
            false
        }
    }
    
    /**
     * Create a temp file in the cache directory.
     */
    fun createTempFile(context: Context, prefix: String, suffix: String): File {
        val cacheDir = getBaseCacheDir(context)
        return File.createTempFile(prefix, suffix, cacheDir)
    }
    
    /**
     * Sync JS plugins from SAF storage to fallback storage.
     * This is needed because JSPluginLoader uses FileSystem.SYSTEM which requires file paths.
     * 
     * Call this when:
     * - App starts and SAF storage is available
     * - User changes storage folder
     * - User installs a plugin via file picker to SAF folder
     */
    fun syncJsPluginsFromSaf(context: Context): Int {
        val safDir = getJsPluginsDocumentFile() ?: return 0
        val fallbackDir = getJsPluginsFallbackDir(context)
        var syncedCount = 0
        
        try {
            val safFiles = safStorageManager?.listFilesWithExtension(safDir, ".js") ?: emptyList()
            
            for (safFile in safFiles) {
                val fileName = safFile.name ?: continue
                val fallbackFile = File(fallbackDir, fileName)
                
                // Check if fallback file is older or doesn't exist
                val safModified = safFile.lastModified()
                val fallbackModified = if (fallbackFile.exists()) fallbackFile.lastModified() else 0L
                
                if (!fallbackFile.exists() || safModified > fallbackModified) {
                    // Copy from SAF to fallback
                    val content = safStorageManager?.readBytesFromFile(safFile)
                    if (content != null) {
                        fallbackFile.writeBytes(content)
                        syncedCount++
                        android.util.Log.d(TAG, "Synced JS plugin from SAF: $fileName")
                        
                        // Also sync metadata file if exists
                        val metaFileName = fileName.replace(".js", ".meta.json")
                        val safMetaFile = safDir.findFile(metaFileName)
                        if (safMetaFile != null) {
                            val metaContent = safStorageManager?.readFromFile(safMetaFile)
                            if (metaContent != null) {
                                File(fallbackDir, metaFileName).writeText(metaContent)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to sync JS plugins from SAF", e)
        }
        
        return syncedCount
    }
    
    /**
     * Sync JS plugins from fallback storage to SAF storage.
     * This makes plugins visible in the user-selected folder.
     */
    fun syncJsPluginsToSaf(context: Context): Int {
        val safDir = getJsPluginsDocumentFile() ?: return 0
        val fallbackDir = getJsPluginsFallbackDir(context)
        var syncedCount = 0
        
        try {
            val fallbackFiles = fallbackDir.listFiles()?.filter { it.name.endsWith(".js") } ?: emptyList()
            
            for (fallbackFile in fallbackFiles) {
                val fileName = fallbackFile.name
                val safFile = safDir.findFile(fileName)
                
                // Check if SAF file is older or doesn't exist
                val fallbackModified = fallbackFile.lastModified()
                val safModified = safFile?.lastModified() ?: 0L
                
                if (safFile == null || fallbackModified > safModified) {
                    // Copy from fallback to SAF
                    val content = fallbackFile.readBytes()
                    val newSafFile = safStorageManager?.createFile(safDir, fileName, "application/javascript")
                    if (newSafFile != null && safStorageManager?.writeToFile(newSafFile, content) == true) {
                        syncedCount++
                        android.util.Log.d(TAG, "Synced JS plugin to SAF: $fileName")
                        
                        // Also sync metadata file if exists
                        val metaFileName = fileName.replace(".js", ".meta.json")
                        val metaFile = File(fallbackDir, metaFileName)
                        if (metaFile.exists()) {
                            val metaContent = metaFile.readText()
                            val newMetaFile = safStorageManager?.createFile(safDir, metaFileName, "application/json")
                            if (newMetaFile != null) {
                                safStorageManager?.writeToFile(newMetaFile, metaContent)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to sync JS plugins to SAF", e)
        }
        
        return syncedCount
    }
    
    /**
     * Perform bidirectional sync of JS plugins between SAF and fallback storage.
     * Newer files win in case of conflicts.
     */
    fun syncJsPluginsBidirectional(context: Context): Pair<Int, Int> {
        val fromSaf = syncJsPluginsFromSaf(context)
        val toSaf = syncJsPluginsToSaf(context)
        return Pair(fromSaf, toSaf)
    }
}

/**
 * Represents a JS plugin file from either SAF or File system.
 */
data class JsPluginFile(
    val name: String,
    val uri: Uri?,
    val file: File?,
    val size: Long,
    val lastModified: Long
) {
    val isFromSaf: Boolean get() = uri != null
    
    companion object {
        fun fromDocumentFile(docFile: DocumentFile): JsPluginFile {
            return JsPluginFile(
                name = docFile.name ?: "",
                uri = docFile.uri,
                file = null,
                size = docFile.length(),
                lastModified = docFile.lastModified()
            )
        }
        
        fun fromFile(file: File): JsPluginFile {
            return JsPluginFile(
                name = file.name,
                uri = null,
                file = file,
                size = file.length(),
                lastModified = file.lastModified()
            )
        }
    }
}
package ireader.domain.usecases.files

import android.content.Context
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.storage.AndroidCacheManager
import ireader.domain.storage.AndroidStorageManager
import ireader.domain.storage.CacheManager
import ireader.domain.storage.StorageManager
import okio.FileSystem
import okio.Path

/**
 * Android implementation of GetSimpleStorage.
 * Uses StorageManager and CacheManager for directory operations.
 * File picking is now handled by FileKit in the presentation layer.
 */
class AndroidGetSimpleStorage(
    private val context: Context,
    private val uiPreferences: UiPreferences,
) : GetSimpleStorage {
    
    private val storageManager: StorageManager = AndroidStorageManager(context, uiPreferences)
    private val cacheManager: CacheManager = AndroidCacheManager(context)
    private val fileSystem = FileSystem.SYSTEM

    override val mainIReaderDir: Path
        get() = storageManager.appDirectory

    override fun ireaderDirectory(dirName: String): Path =
        storageManager.getSubDirectory(dirName)

    override fun extensionDirectory(): Path =
        storageManager.extensionsDirectory

    override fun cacheExtensionDir(): Path = cacheManager.extensionCacheDirectory
    
    override fun ireaderCacheDir(): Path = cacheManager.cacheDirectory

    override val backupDirectory: Path
        get() = storageManager.backupDirectory
        
    override val booksDirectory: Path
        get() = storageManager.booksDirectory
        
    override val automaticBackupDirectory: Path
        get() = storageManager.automaticBackupDirectory

    override fun checkPermission(): Boolean {
        val hasPermission = storageManager.hasStoragePermission()
        if (hasPermission) {
            createIReaderDir()
            createNoMediaFile()
        }
        return hasPermission
    }

    override fun createIReaderDir() {
        storageManager.initializeDirectories()
    }

    override fun createNoMediaFile() {
        storageManager.preventMediaIndexing()
    }

    override fun clearImageCache() {
        cacheManager.clearImageCache()
    }

    override fun clearCache() {
        cacheManager.clearAllCache()
    }

    override fun getCacheSize(): String {
        return cacheManager.getCacheSize()
    }

    /**
     * Get Path for a directory using Okio.
     * Creates the directory if it doesn't exist.
     */
    fun get(dirName: String): Path {
        val dir = ireaderDirectory(dirName)
        if (!fileSystem.exists(dir)) {
            fileSystem.createDirectories(dir)
        }
        return dir
    }
}

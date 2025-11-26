package ireader.domain.usecases.files

import ireader.domain.storage.CacheManager
import ireader.domain.storage.DesktopCacheManager
import ireader.domain.storage.DesktopStorageManager
import ireader.domain.storage.StorageManager
import java.io.File


class DesktopGetSimpleStorage : GetSimpleStorage {
    
    private val storageManager: StorageManager = DesktopStorageManager()
    private val cacheManager: CacheManager = DesktopCacheManager()
    
    override val mainIReaderDir: File
        get() = storageManager.appDirectory

    override fun ireaderDirectory(dirName: String): File =
        storageManager.getSubDirectory(dirName)

    override fun extensionDirectory(): File =
        storageManager.extensionsDirectory

    override fun cacheExtensionDir() =
        cacheManager.extensionCacheDirectory

    override fun ireaderCacheDir() =
        cacheManager.cacheDirectory

    override val backupDirectory: File
        get() = storageManager.backupDirectory
        
    override val booksDirectory: File
        get() = storageManager.booksDirectory
        
    override val automaticBackupDirectory: File
        get() = storageManager.automaticBackupDirectory

    override fun checkPermission(): Boolean {
        return storageManager.hasStoragePermission()
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
}
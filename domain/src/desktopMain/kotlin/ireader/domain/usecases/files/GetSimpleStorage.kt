package ireader.domain.usecases.files

import ireader.domain.storage.CacheManager
import ireader.domain.storage.DesktopCacheManager
import ireader.domain.storage.DesktopStorageManager
import ireader.domain.storage.StorageManager
import okio.Path


class DesktopGetSimpleStorage : GetSimpleStorage {
    
    private val storageManager: StorageManager = DesktopStorageManager()
    private val cacheManager: CacheManager = DesktopCacheManager()
    
    override val mainIReaderDir: Path
        get() = storageManager.appDirectory

    override fun ireaderDirectory(dirName: String): Path =
        storageManager.getSubDirectory(dirName)

    override fun extensionDirectory(): Path =
        storageManager.extensionsDirectory

    override fun cacheExtensionDir(): Path =
        cacheManager.extensionCacheDirectory

    override fun ireaderCacheDir(): Path =
        cacheManager.cacheDirectory

    override val backupDirectory: Path
        get() = storageManager.backupDirectory
        
    override val booksDirectory: Path
        get() = storageManager.booksDirectory
        
    override val automaticBackupDirectory: Path
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
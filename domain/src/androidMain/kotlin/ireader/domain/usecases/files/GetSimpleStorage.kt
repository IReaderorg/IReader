package ireader.domain.usecases.files

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.anggrayudi.storage.SimpleStorage
import com.anggrayudi.storage.SimpleStorageHelper
import com.anggrayudi.storage.file.DocumentFileCompat
import androidx.documentfile.provider.DocumentFile
import ireader.domain.storage.AndroidCacheManager
import ireader.domain.storage.AndroidStorageManager
import ireader.domain.storage.CacheManager
import ireader.domain.storage.StorageManager
import okio.Path


class AndroidGetSimpleStorage(
    private val context: Context,
) : GetSimpleStorage {

    lateinit var storage: SimpleStorage
    lateinit var simpleStorageHelper: SimpleStorageHelper
    
    private val storageManager: StorageManager = AndroidStorageManager(context)
    private val cacheManager: CacheManager = AndroidCacheManager(context)

    fun provideActivity(activity: ComponentActivity, savedState: Bundle?) {
        storage = SimpleStorage(activity, savedState)
        simpleStorageHelper = SimpleStorageHelper(activity, savedState)
    }

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

    fun get(dirName: String): DocumentFile {
        val dir = ireaderDirectory(dirName).toFile()
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return DocumentFileCompat.fromFile(
            context,
            dir,
            requiresWriteAccess = true,
            considerRawFile = true
        )!!
    }
}
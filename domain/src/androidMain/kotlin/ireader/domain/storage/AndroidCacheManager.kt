package ireader.domain.storage

import android.content.Context
import coil3.imageLoader
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.utils.getCacheSize
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toOkioPath
import okio.Path.Companion.toPath
import java.io.File

class AndroidCacheManager(
    private val context: Context,
    private val uiPreferences: UiPreferences? = null
) : CacheManager {
    
    private val fileSystem = FileSystem.SYSTEM
    
    /**
     * Returns the base directory for cache operations.
     * Uses SecureStorageHelper which handles SAF URIs properly.
     */
    private val baseCacheDir: File
        get() = SecureStorageHelper.getBaseCacheDir(context)
    
    override val cacheDirectory: Path
        get() = SecureStorageHelper.getBaseCacheDir(context).toOkioPath()
    
    override val extensionCacheDirectory: Path
        get() = SecureStorageHelper.getExtensionsDir(context).toOkioPath()
    
    override fun getCacheSubDirectory(name: String): Path {
        val subDir = cacheDirectory / name
        fileSystem.createDirectories(subDir)
        return subDir
    }
    
    override fun clearImageCache() {
        context.imageLoader.memoryCache?.clear()
        // Clear disk cache for covers
        deleteRecursively(getCacheSubDirectory("covers"))
    }
    
    override fun clearAllCache() {
        baseCacheDir.deleteRecursively()
    }
    
    override fun getCacheSize(): String {
        return getCacheSize(context = context)
    }
    
    override fun getCacheSizeBytes(): Long {
        return baseCacheDir.walkTopDown()
            .filter { it.isFile }
            .map { it.length() }
            .sum()
    }
    
    override fun clearCacheDirectory(directory: Path) {
        val dirFile = directory.toFile()
        if (dirFile.exists() && dirFile.startsWith(baseCacheDir)) {
            dirFile.deleteRecursively()
        }
    }
    
    private fun deleteRecursively(path: Path) {
        if (!fileSystem.exists(path)) return
        val metadata = fileSystem.metadata(path)
        if (metadata.isDirectory) {
            fileSystem.list(path).forEach { deleteRecursively(it) }
        }
        fileSystem.delete(path)
    }
}

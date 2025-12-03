package ireader.domain.storage

import android.content.Context
import coil3.imageLoader
import ireader.domain.utils.getCacheSize
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toOkioPath
import java.io.File

class AndroidCacheManager(
    private val context: Context
) : CacheManager {
    
    private val fileSystem = FileSystem.SYSTEM
    
    override val cacheDirectory: Path
        get() = File(context.cacheDir, "IReader/cache/").toOkioPath()
    
    override val extensionCacheDirectory: Path
        get() = File(context.cacheDir, "IReader/Extensions/").toOkioPath()
    
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
        context.cacheDir.deleteRecursively()
    }
    
    override fun getCacheSize(): String {
        return getCacheSize(context = context)
    }
    
    override fun getCacheSizeBytes(): Long {
        return context.cacheDir.walkTopDown()
            .filter { it.isFile }
            .map { it.length() }
            .sum()
    }
    
    override fun clearCacheDirectory(directory: Path) {
        val dirFile = directory.toFile()
        if (dirFile.exists() && dirFile.startsWith(context.cacheDir)) {
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

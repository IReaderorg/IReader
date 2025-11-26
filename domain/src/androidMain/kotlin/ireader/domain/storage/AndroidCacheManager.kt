package ireader.domain.storage

import android.content.Context
import coil3.imageLoader
import ireader.domain.utils.getCacheSize
import java.io.File

class AndroidCacheManager(
    private val context: Context
) : CacheManager {
    
    override val cacheDirectory: File
        get() = File(context.cacheDir, "IReader/cache/")
    
    override val extensionCacheDirectory: File
        get() = File(context.cacheDir, "IReader/Extensions/")
    
    override fun getCacheSubDirectory(name: String): File {
        return File(cacheDirectory, name).also { it.mkdirs() }
    }
    
    override fun clearImageCache() {
        context.imageLoader.memoryCache?.clear()
        // Clear disk cache for covers
        getCacheSubDirectory("covers").deleteRecursively()
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
    
    override fun clearCacheDirectory(directory: File) {
        if (directory.exists() && directory.startsWith(context.cacheDir)) {
            directory.deleteRecursively()
        }
    }
}

package ireader.domain.storage

import ireader.core.storage.AppDir
import java.io.File
import java.text.DecimalFormat

class DesktopCacheManager : CacheManager {
    
    override val cacheDirectory: File
        get() = File(AppDir, "cache/")
    
    override val extensionCacheDirectory: File
        get() = File(AppDir, "cache/extensions/")
    
    override fun getCacheSubDirectory(name: String): File {
        return File(cacheDirectory, name).also { it.mkdirs() }
    }
    
    override fun clearImageCache() {
        getCacheSubDirectory("covers").deleteRecursively()
    }
    
    override fun clearAllCache() {
        cacheDirectory.deleteRecursively()
    }
    
    override fun getCacheSize(): String {
        val bytes = getCacheSizeBytes()
        if (bytes <= 0) return "0 B"
        
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        
        return DecimalFormat("#,##0.#").format(bytes / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
    }
    
    override fun getCacheSizeBytes(): Long {
        return if (cacheDirectory.exists()) {
            cacheDirectory.walkTopDown()
                .filter { it.isFile }
                .map { it.length() }
                .sum()
        } else {
            0L
        }
    }
    
    override fun clearCacheDirectory(directory: File) {
        if (directory.exists() && directory.startsWith(cacheDirectory)) {
            directory.deleteRecursively()
        }
    }
}

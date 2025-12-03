package ireader.domain.storage

import ireader.core.storage.AppDir
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

class DesktopCacheManager : CacheManager {
    
    private val fileSystem = FileSystem.SYSTEM
    private val appDirPath = AppDir.absolutePath.toPath()
    
    override val cacheDirectory: Path
        get() = appDirPath / "cache"
    
    override val extensionCacheDirectory: Path
        get() = cacheDirectory / "extensions"
    
    override fun getCacheSubDirectory(name: String): Path {
        val subDir = cacheDirectory / name
        fileSystem.createDirectories(subDir)
        return subDir
    }
    
    override fun clearImageCache() {
        val coversDir = getCacheSubDirectory("covers")
        deleteRecursively(coversDir)
    }
    
    override fun clearAllCache() {
        deleteRecursively(cacheDirectory)
    }
    
    override fun getCacheSize(): String {
        val bytes = getCacheSizeBytes()
        if (bytes <= 0) return "0 B"
        
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()
        
        return DecimalFormat("#,##0.#").format(bytes / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
    }
    
    override fun getCacheSizeBytes(): Long {
        return if (fileSystem.exists(cacheDirectory)) {
            calculateDirectorySize(cacheDirectory)
        } else {
            0L
        }
    }
    
    override fun clearCacheDirectory(directory: Path) {
        if (fileSystem.exists(directory) && directory.toString().startsWith(cacheDirectory.toString())) {
            deleteRecursively(directory)
        }
    }
    
    private fun calculateDirectorySize(path: Path): Long {
        if (!fileSystem.exists(path)) return 0L
        val metadata = fileSystem.metadata(path)
        return if (metadata.isDirectory) {
            fileSystem.list(path).sumOf { calculateDirectorySize(it) }
        } else {
            metadata.size ?: 0L
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

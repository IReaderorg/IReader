package ireader.domain.storage

import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSURL
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.round

class IosStorageManager : StorageManager {
    private val fileSystem = FileSystem.SYSTEM

    override val appDirectory: Path = appleDirectory(NSDocumentDirectory) / "ireader"
    override val booksDirectory: Path get() = appDirectory / "books"
    override val backupDirectory: Path get() = appDirectory / "backup"
    override val automaticBackupDirectory: Path get() = backupDirectory / "automatic"
    override val extensionsDirectory: Path get() = appDirectory / "extensions"

    override fun getSubDirectory(name: String): Path {
        val subDir = appDirectory / name
        fileSystem.createDirectories(subDir)
        return subDir
    }

    override fun hasStoragePermission(): Boolean = true

    override fun initializeDirectories() {
        listOf(
            appDirectory,
            booksDirectory,
            backupDirectory,
            automaticBackupDirectory,
            extensionsDirectory,
        ).forEach { fileSystem.createDirectories(it) }
    }

    override fun preventMediaIndexing() = Unit
}

class IosCacheManager : CacheManager {
    private val fileSystem = FileSystem.SYSTEM

    override val cacheDirectory: Path = appleDirectory(NSCachesDirectory) / "ireader"
    override val extensionCacheDirectory: Path get() = cacheDirectory / "extensions"

    override fun getCacheSubDirectory(name: String): Path {
        val subDir = cacheDirectory / name
        fileSystem.createDirectories(subDir)
        return subDir
    }

    override fun clearImageCache() {
        clearCacheDirectory(getCacheSubDirectory("covers"))
    }

    override fun clearAllCache() {
        deleteRecursively(cacheDirectory)
    }

    override fun getCacheSize(): String = formatBytes(getCacheSizeBytes())

    override fun getCacheSizeBytes(): Long {
        return if (fileSystem.exists(cacheDirectory)) calculateDirectorySize(cacheDirectory) else 0L
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
        if (fileSystem.metadata(path).isDirectory) {
            fileSystem.list(path).forEach { deleteRecursively(it) }
        }
        fileSystem.delete(path)
    }
}

private fun appleDirectory(type: ULong): Path {
    val urls = NSFileManager.defaultManager.URLsForDirectory(type, NSUserDomainMask)
    val path = (urls.firstOrNull() as? NSURL)?.path ?: "/tmp"
    return path.toPath()
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0L) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    val group = (ln(bytes.toDouble()) / ln(1024.0)).toInt().coerceIn(0, units.lastIndex)
    val value = bytes / 1024.0.pow(group)
    val rounded = round(value * 10.0) / 10.0
    return "$rounded ${units[group]}"
}

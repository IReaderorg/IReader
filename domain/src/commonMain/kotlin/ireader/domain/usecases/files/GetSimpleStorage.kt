package ireader.domain.usecases.files

import okio.Path

/**
 * Platform-agnostic storage interface.
 * Uses Okio Path for KMP compatibility instead of java.io.File.
 */
interface GetSimpleStorage {
    val mainIReaderDir: Path
    fun ireaderDirectory(dirName: String): Path
    fun extensionDirectory(): Path
    fun cacheExtensionDir(): Path
    fun ireaderCacheDir(): Path
    val backupDirectory: Path
    val booksDirectory: Path
    val automaticBackupDirectory: Path
    fun checkPermission(): Boolean
    fun createIReaderDir()
    fun createNoMediaFile()
    fun clearImageCache()
    fun clearCache()
    fun getCacheSize(): String
}
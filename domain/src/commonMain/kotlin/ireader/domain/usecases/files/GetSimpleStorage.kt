package ireader.domain.usecases.files

import java.io.File


interface GetSimpleStorage {
    val mainIReaderDir: File
    fun ireaderDirectory(dirName: String): File
    fun extensionDirectory(): File
    fun cacheExtensionDir()  : File
    fun ireaderCacheDir(): File
    val backupDirectory: File
    val booksDirectory: File
    val automaticBackupDirectory: File
    fun checkPermission(): Boolean
    fun createIReaderDir()
    fun createNoMediaFile()
}
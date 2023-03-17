package ireader.domain.usecases.files

import ireader.core.storage.AppDir
import ireader.core.storage.ExtensionDir
import java.io.File


class DesktopGetSimpleStorage : GetSimpleStorage {
    override val mainIReaderDir: File = AppDir

    override fun ireaderDirectory(dirName: String): File = AppDir

    override fun extensionDirectory(): File = ExtensionDir

    override fun cacheExtensionDir() = ExtensionDir

    override fun ireaderCacheDir() = AppDir

    override val backupDirectory: File = File(AppDir, "backup/")
    override val booksDirectory: File = File(AppDir, "books/")
    override val automaticBackupDirectory: File = File(AppDir, "backup/automatic/")

    override fun checkPermission(): Boolean {
        return true
    }

    override fun createIReaderDir() {

    }

    override fun createNoMediaFile() {

    }

    override fun clearImageCache() {

    }

    override fun clearCache() {

    }

    override fun getCacheSize(): String {
        return "Unknown"
    }


}
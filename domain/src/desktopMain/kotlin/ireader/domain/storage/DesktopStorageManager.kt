package ireader.domain.storage

import ireader.core.storage.AppDir
import ireader.core.storage.ExtensionDir
import java.io.File

class DesktopStorageManager : StorageManager {
    
    override val appDirectory: File = AppDir
    
    override val booksDirectory: File
        get() = File(appDirectory, "books/")
    
    override val backupDirectory: File
        get() = File(appDirectory, "backup/")
    
    override val automaticBackupDirectory: File
        get() = File(backupDirectory, "automatic/")
    
    override val extensionsDirectory: File = ExtensionDir
    
    override fun getSubDirectory(name: String): File {
        return File(appDirectory, name).also { it.mkdirs() }
    }
    
    override fun hasStoragePermission(): Boolean {
        return true
    }
    
    override fun initializeDirectories() {
        listOf(appDirectory, booksDirectory, backupDirectory, automaticBackupDirectory, extensionsDirectory).forEach {
            it.mkdirs()
        }
    }
    
    override fun preventMediaIndexing() {
        // Not needed on desktop
    }
}

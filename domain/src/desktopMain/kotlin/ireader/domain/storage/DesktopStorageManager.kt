package ireader.domain.storage

import ireader.core.storage.AppDir
import ireader.core.storage.ExtensionDir
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

class DesktopStorageManager : StorageManager {
    
    private val fileSystem = FileSystem.SYSTEM
    
    override val appDirectory: Path = AppDir.absolutePath.toPath()
    
    override val booksDirectory: Path
        get() = appDirectory / "books"
    
    override val backupDirectory: Path
        get() = appDirectory / "backup"
    
    override val automaticBackupDirectory: Path
        get() = backupDirectory / "automatic"
    
    override val extensionsDirectory: Path = ExtensionDir.absolutePath.toPath()
    
    override fun getSubDirectory(name: String): Path {
        val subDir = appDirectory / name
        fileSystem.createDirectories(subDir)
        return subDir
    }
    
    override fun hasStoragePermission(): Boolean {
        return true
    }
    
    override fun initializeDirectories() {
        listOf(appDirectory, booksDirectory, backupDirectory, automaticBackupDirectory, extensionsDirectory).forEach {
            fileSystem.createDirectories(it)
        }
    }
    
    override fun preventMediaIndexing() {
        // Not needed on desktop
    }
}

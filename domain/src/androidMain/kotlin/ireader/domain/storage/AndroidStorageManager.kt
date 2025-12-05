package ireader.domain.storage

import android.content.Context
import android.os.Environment
import androidx.documentfile.provider.DocumentFile
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toOkioPath
import java.io.File

class AndroidStorageManager(
    private val context: Context
) : StorageManager {
    
    private val fileSystem = FileSystem.SYSTEM
    private val appDirFile = File(Environment.getExternalStorageDirectory(), "IReader/")
    
    override val appDirectory: Path
        get() = appDirFile.toOkioPath()
    
    override val booksDirectory: Path
        get() = appDirectory / "Books"
    
    override val backupDirectory: Path
        get() = appDirectory / "Backups"
    
    override val automaticBackupDirectory: Path
        get() = backupDirectory / "Automatic"
    
    override val extensionsDirectory: Path
        get() = appDirectory / "Extensions"
    
    override fun getSubDirectory(name: String): Path {
        val subDir = appDirectory / name
        fileSystem.createDirectories(subDir)
        return subDir
    }
    
    override fun hasStoragePermission(): Boolean {
        // Clean up invalid directories
        listOf(appDirectory, backupDirectory, automaticBackupDirectory, booksDirectory).forEach { dir ->
            val file = dir.toFile()
            if (file.exists() && !file.isDirectory) {
                file.deleteRecursively()
            }
        }
        return true
    }
    
    override fun initializeDirectories() {
        kotlin.runCatching {
            if (!appDirFile.exists()) {
                DocumentFile.fromFile(Environment.getExternalStorageDirectory())
                    ?.createDirectory("IReader")
            }
        }
        
        // Ensure subdirectories exist - wrap in runCatching to handle permission issues gracefully
        kotlin.runCatching {
            listOf(booksDirectory, backupDirectory, automaticBackupDirectory, extensionsDirectory).forEach {
                fileSystem.createDirectories(it)
            }
        }
    }
    
    override fun preventMediaIndexing() {
        kotlin.runCatching {
            val noMediaFile = File(appDirFile, ".nomedia")
            if (!noMediaFile.exists()) {
                DocumentFile.fromFile(appDirFile)?.createFile("", ".nomedia")
            }
        }
    }
}

package ireader.domain.storage

import android.content.Context
import android.os.Environment
import androidx.documentfile.provider.DocumentFile
import java.io.File

class AndroidStorageManager(
    private val context: Context
) : StorageManager {
    
    override val appDirectory: File
        get() = File(Environment.getExternalStorageDirectory(), "IReader/")
    
    override val booksDirectory: File
        get() = File(appDirectory, "Books/")
    
    override val backupDirectory: File
        get() = File(appDirectory, "Backups/")
    
    override val automaticBackupDirectory: File
        get() = File(backupDirectory, "Automatic/")
    
    override val extensionsDirectory: File
        get() = File(appDirectory, "Extensions/")
    
    override fun getSubDirectory(name: String): File {
        return File(appDirectory, name).also { it.mkdirs() }
    }
    
    override fun hasStoragePermission(): Boolean {
        // Clean up invalid directories
        listOf(appDirectory, backupDirectory, automaticBackupDirectory, booksDirectory).forEach { dir ->
            if (dir.exists() && !dir.isDirectory) {
                dir.deleteRecursively()
            }
        }
        return true
    }
    
    override fun initializeDirectories() {
        kotlin.runCatching {
            if (!appDirectory.exists()) {
                DocumentFile.fromFile(Environment.getExternalStorageDirectory())
                    ?.createDirectory("IReader")
            }
        }
        
        // Ensure subdirectories exist
        listOf(booksDirectory, backupDirectory, automaticBackupDirectory, extensionsDirectory).forEach {
            it.mkdirs()
        }
    }
    
    override fun preventMediaIndexing() {
        kotlin.runCatching {
            val noMediaFile = File(appDirectory, ".nomedia")
            if (!noMediaFile.exists()) {
                DocumentFile.fromFile(appDirectory)?.createFile("", ".nomedia")
            }
        }
    }
}

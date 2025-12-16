package ireader.domain.storage

import android.content.Context
import android.os.Environment
import ireader.domain.preferences.prefs.UiPreferences
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toOkioPath
import okio.Path.Companion.toPath
import java.io.File

class AndroidStorageManager(
    private val context: Context,
    private val uiPreferences: UiPreferences? = null
) : StorageManager {
    
    private val fileSystem = FileSystem.SYSTEM
    
    // Default fallback directory
    private val defaultAppDirPath: Path = File(Environment.getExternalStorageDirectory(), "IReader/").toOkioPath()
    
    /**
     * Returns the app directory - uses user-selected folder if available, otherwise falls back to default.
     * The user-selected folder is set during onboarding and persists even if app is uninstalled.
     */
    override val appDirectory: Path
        get() {
            val selectedUri = uiPreferences?.selectedStorageFolderUri()?.get()
            if (!selectedUri.isNullOrEmpty()) {
                return try {
                    // Handle content:// URIs from SAF - use external files dir as base
                    if (selectedUri.startsWith("content://")) {
                        // For SAF URIs, we use the app's external files directory
                        // The actual SAF URI is stored for reference but we use a local path
                        val externalDir = context.getExternalFilesDir(null)
                        if (externalDir != null) {
                            return File(externalDir, "IReader").toOkioPath()
                        }
                        return defaultAppDirPath
                    }
                    // Handle regular file paths (e.g., /storage/emulated/0/IReader)
                    val path = selectedUri.toPath()
                    // Ensure it's a valid path
                    if (selectedUri.startsWith("/")) {
                        return path
                    }
                    defaultAppDirPath
                } catch (e: Exception) {
                    defaultAppDirPath
                }
            }
            return defaultAppDirPath
        }
    
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
        // Clean up invalid directories using Okio
        listOf(appDirectory, backupDirectory, automaticBackupDirectory, booksDirectory).forEach { dir ->
            val metadata = fileSystem.metadataOrNull(dir)
            if (metadata != null && !metadata.isDirectory) {
                fileSystem.deleteRecursively(dir)
            }
        }
        return true
    }
    
    override fun initializeDirectories() {
        kotlin.runCatching {
            // Create app directory using Okio
            fileSystem.createDirectories(appDirectory)
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
            val noMediaPath = appDirectory / ".nomedia"
            if (!fileSystem.exists(noMediaPath)) {
                // Create empty .nomedia file using Okio
                fileSystem.write(noMediaPath) {
                    // Empty file
                }
            }
        }
    }
}
